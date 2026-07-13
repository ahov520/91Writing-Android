package com.writing91.app

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.webkit.WebViewAssetLoader
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import java.io.File

/**
 * Hosts the mobile Vue SPA via WebViewAssetLoader (https virtual host).
 * JS bridge: Writing91Bridge (export / keepScreenOn / haptic / version)
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var loadingPanel: LinearLayout
    private lateinit var errorPanel: LinearLayout
    private lateinit var errorText: TextView
    private lateinit var loadingText: TextView
    private lateinit var reloadButton: MaterialButton
    private lateinit var assetLoader: WebViewAssetLoader

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var lastBackPressMs = 0L
    private var pageReady = false

    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val callback = filePathCallback
            filePathCallback = null
            if (callback == null) return@registerForActivityResult
            if (result.resultCode != RESULT_OK || result.data == null) {
                callback.onReceiveValue(null)
                return@registerForActivityResult
            }
            val data = result.data
            val uris = mutableListOf<Uri>()
            data?.data?.let { uris.add(it) }
            data?.clipData?.let { clip ->
                for (i in 0 until clip.itemCount) {
                    clip.getItemAt(i).uri?.let { uris.add(it) }
                }
            }
            callback.onReceiveValue(if (uris.isEmpty()) null else uris.toTypedArray())
        }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_OK || result.data?.data == null) {
                safeEvalJs("try{window.__writing91OnImage&&window.__writing91OnImage(null)}catch(e){}")
                return@registerForActivityResult
            }
            try {
                val uri = result.data!!.data!!
                val dataUrl = compressUriToJpegDataUrl(uri)
                if (dataUrl == null) {
                    safeEvalJs("try{window.__writing91OnImage&&window.__writing91OnImage(null)}catch(e){}")
                    return@registerForActivityResult
                }
                val quoted = JSONObject.quote(dataUrl)
                safeEvalJs("try{window.__writing91OnImage&&window.__writing91OnImage($quoted)}catch(e){}")
            } catch (_: Exception) {
                safeEvalJs("try{window.__writing91OnImage&&window.__writing91OnImage(null)}catch(e){}")
            }
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        loadingPanel = findViewById(R.id.loadingPanel)
        errorPanel = findViewById(R.id.errorPanel)
        errorText = findViewById(R.id.errorText)
        loadingText = findViewById(R.id.loadingText)
        reloadButton = findViewById(R.id.reloadButton)

        assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        val root = findViewById<FrameLayout>(R.id.root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = bars.top, bottom = bars.bottom)
            insets
        }

        reloadButton.setOnClickListener { loadApp() }

        setupWebView()
        webView.addJavascriptInterface(Writing91Bridge(), "Writing91Bridge")
        setupBackHandler()
        loadApp()
        handleShareIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
    }


    private fun compressUriToJpegDataUrl(uri: android.net.Uri): String? {
        return try {
            val input = contentResolver.openInputStream(uri) ?: return null
            val original = android.graphics.BitmapFactory.decodeStream(input)
            input.close()
            if (original == null) return null
            val max = 320
            val w = original.width
            val h = original.height
            val scale = if (w > max || h > max) {
                minOf(max.toFloat() / w, max.toFloat() / h)
            } else 1f
            val nw = maxOf(1, (w * scale).toInt())
            val nh = maxOf(1, (h * scale).toInt())
            val scaled = if (scale < 1f) {
                android.graphics.Bitmap.createScaledBitmap(original, nw, nh, true)
            } else original
            if (scaled !== original) original.recycle()
            val baos = java.io.ByteArrayOutputStream()
            scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 72, baos)
            scaled.recycle()
            val b64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.NO_WRAP)
            "data:image/jpeg;base64,$b64"
        } catch (_: Exception) {
            null
        }
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action != Intent.ACTION_SEND) return
        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
        if (text.isBlank()) return
        // deliver after page ready; guard destroyed activity/webview
        if (!::webView.isInitialized) return
        webView.postDelayed({
            if (isFinishing || isDestroyed || !::webView.isInitialized) return@postDelayed
            val quoted = JSONObject.quote(text)
            safeEvalJs("try{window.__writing91OnShare&&window.__writing91OnShare($quoted)}catch(e){}")
        }, 800)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = false
            allowContentAccess = true
            // AssetLoader serves https://appassets… — no file:// universal access
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = false
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = false
            // User may point API to HTTP custom endpoints
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            cacheMode = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            textZoom = 100
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            // Prefer GPU compositing + offscreen pre-raster for smoother SPA scrolls
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                offscreenPreRaster = true
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                isAlgorithmicDarkeningAllowed = false
            }
            userAgentString = "$userAgentString Writing91Android/2.7.2"
        }

        // Layer type hardware improves long-text scroll in SPA WebViews
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.overScrollMode = View.OVER_SCROLL_NEVER

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url ?: return false
                val host = url.host ?: ""
                val scheme = url.scheme?.lowercase() ?: return false
                // Keep app assets + http(s) API inside WebView
                if (host == "appassets.androidplatform.net") return false
                return when (scheme) {
                    "http", "https", "about", "blob", "data" -> false
                    "mailto", "tel", "sms" -> {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, url))
                        } catch (_: Exception) {
                        }
                        true
                    }
                    else -> {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, url))
                            true
                        } catch (_: ActivityNotFoundException) {
                            false
                        }
                    }
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (!pageReady) {
                    loadingPanel.visibility = View.VISIBLE
                    errorPanel.visibility = View.GONE
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                pageReady = true
                loadingPanel.visibility = View.GONE
                errorPanel.visibility = View.GONE
                webView.visibility = View.VISIBLE
                injectAndroidBridge()
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame) {
                    showError("页面加载失败：${error.description}")
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                loadingText.text = getString(R.string.loading) + " $newProgress%"
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback?.onReceiveValue(null)
                this@MainActivity.filePathCallback = filePathCallback
                val intent = fileChooserParams?.createIntent()
                    ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    }
                return try {
                    fileChooserLauncher.launch(intent)
                    true
                } catch (e: Exception) {
                    this@MainActivity.filePathCallback = null
                    Toast.makeText(this@MainActivity, "无法打开文件选择器", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }

        webView.setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            handleDownload(url, userAgent, contentDisposition, mimeType)
        })
    }



    private fun safeEvalJs(script: String) {
        if (!::webView.isInitialized) return
        try {
            webView.evaluateJavascript(script, null)
        } catch (_: Exception) {
        }
    }

    private fun safeVersionName(): String {
        return try {
            if (Build.VERSION.SDK_INT >= 33) {
                packageManager.getPackageInfo(
                    packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                ).versionName ?: "2.7.2"
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0).versionName ?: "2.7.2"
            }
        } catch (_: Exception) {
            "2.7.2"
        }
    }

    private fun injectAndroidBridge() {
        if (!::webView.isInitialized) return
        val versionName = safeVersionName()
        try {
        webView.evaluateJavascript(
            """
            (function(){
              try {
                window.__WRITING91_ANDROID__ = true;
                window.__WRITING91_VERSION__ = ${JSONObject.quote(versionName)};
                document.documentElement.classList.add('writing91-android');
                var m = document.querySelector('meta[name=viewport]');
                if (m) {
                  m.setAttribute('content',
                    'width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes, viewport-fit=cover');
                }
              } catch(e) {}
            })();
            """.trimIndent(),
            null
        )
        } catch (_: Exception) {
        }
    }

    inner class Writing91Bridge {
        @JavascriptInterface
        fun getVersion(): String = safeVersionName()

        @JavascriptInterface
        fun keepScreenOn(on: Boolean) {
            runOnUiThread {
                if (on) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }

        @JavascriptInterface
        fun haptic() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = getSystemService(VibratorManager::class.java)
                    vm?.defaultVibrator?.vibrate(
                        VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    val v = getSystemService(VIBRATOR_SERVICE) as? Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v?.vibrate(20)
                    }
                }
            } catch (_: Exception) {
            }
        }

        @JavascriptInterface
        fun exportText(filename: String?, base64: String?, mime: String?) {
            if (base64.isNullOrBlank()) return
            runOnUiThread {
                try {
                    val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                    val safeName = (filename?.ifBlank { null } ?: "export.txt")
                        .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                    val file = File(cacheDir, safeName)
                    file.writeBytes(bytes)
                    val uri = FileProvider.getUriForFile(
                        this@MainActivity,
                        "$packageName.fileprovider",
                        file
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = mime?.ifBlank { null } ?: "text/plain"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, safeName)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(intent, "导出 $safeName"))
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "导出失败: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


        @JavascriptInterface
        fun pickImage() {
            runOnUiThread {
                try {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "image/*"
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                    imagePickerLauncher.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "无法打开相册", Toast.LENGTH_SHORT).show()
                    safeEvalJs("try{window.__writing91OnImage&&window.__writing91OnImage(null)}catch(e){}")
                }
            }
        }

        @JavascriptInterface
        fun setWidgetStats(json: String?) {
            try {
                val prefs = getSharedPreferences("writing91_widget", MODE_PRIVATE)
                prefs.edit().putString("stats", json ?: "{}").apply()
                val mgr = android.appwidget.AppWidgetManager.getInstance(this@MainActivity)
                val cn = android.content.ComponentName(this@MainActivity, StatsWidgetProvider::class.java)
                val ids = mgr.getAppWidgetIds(cn)
                if (ids != null && ids.isNotEmpty()) {
                    val intent = android.content.Intent(this@MainActivity, StatsWidgetProvider::class.java).apply {
                        action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                    sendBroadcast(intent)
                }
            } catch (_: Exception) {
            }
        }

        @JavascriptInterface
        fun setAppLockEnabled(on: Boolean) {
            try {
                getSharedPreferences("writing91_lock", MODE_PRIVATE)
                    .edit().putBoolean("enabled", on).apply()
            } catch (_: Exception) {
            }
        }

    private fun handleDownload(
        url: String,
        userAgent: String,
        contentDisposition: String?,
        mimeType: String?
    ) {
        try {
            if (url.startsWith("blob:")) {
                Toast.makeText(this, "请使用应用内导出功能保存文件", Toast.LENGTH_SHORT).show()
                return
            }
            if (url.startsWith("data:")) {
                shareDataUrl(url, mimeType)
                return
            }
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimeType)
            request.addRequestHeader("User-Agent", userAgent)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(this, getString(R.string.download_started, fileName), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (_: Exception) {
                Toast.makeText(this, "下载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareDataUrl(dataUrl: String, mimeType: String?) {
        try {
            val comma = dataUrl.indexOf(',')
            if (comma < 0) return
            val meta = dataUrl.substring(0, comma)
            val payload = dataUrl.substring(comma + 1)
            val isBase64 = meta.contains(";base64")
            val bytes = if (isBase64) {
                android.util.Base64.decode(payload, android.util.Base64.DEFAULT)
            } else {
                Uri.decode(payload).toByteArray(Charsets.UTF_8)
            }
            val ext = when {
                mimeType?.contains("json") == true -> "json"
                mimeType?.contains("text") == true -> "txt"
                mimeType?.contains("markdown") == true -> "md"
                else -> "bin"
            }
            val file = File(cacheDir, "export_${System.currentTimeMillis()}.$ext")
            file.writeBytes(bytes)
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType ?: "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "导出文件"))
        } catch (e: Exception) {
            Toast.makeText(this, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadApp() {
        pageReady = false
        loadingPanel.visibility = View.VISIBLE
        errorPanel.visibility = View.GONE
        webView.visibility = View.INVISIBLE

        val hasAssets = try {
            assets.open("www/index.html").close()
            true
        } catch (_: Exception) {
            false
        }

        if (!hasAssets) {
            showError(getString(R.string.no_web_assets))
            return
        }
        // Virtual HTTPS origin for assets (safer than file://)
        webView.loadUrl("https://appassets.androidplatform.net/assets/www/index.html")
    }

    private fun showError(message: String) {
        loadingPanel.visibility = View.GONE
        webView.visibility = View.INVISIBLE
        errorPanel.visibility = View.VISIBLE
        errorText.text = message
    }

    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!::webView.isInitialized) {
                    finish()
                    return
                }
                try {
                    webView.evaluateJavascript(
                        """
                        (function(){
                          try {
                            if (typeof window.__writing91ConsumeBack === 'function' && window.__writing91ConsumeBack()) {
                              return true;
                            }
                            if (location.hash && location.hash !== '#/' && location.hash !== '#') {
                              history.back();
                              return true;
                            }
                            return false;
                          } catch (e) { return false; }
                        })()
                        """.trimIndent()
                    ) { result ->
                        if (isFinishing || isDestroyed || !::webView.isInitialized) return@evaluateJavascript
                        val handled = result == "true"
                        if (handled) return@evaluateJavascript
                        Handler(Looper.getMainLooper()).post {
                            if (isFinishing || isDestroyed || !::webView.isInitialized) return@post
                            if (webView.canGoBack()) {
                                webView.goBack()
                            } else {
                                val now = System.currentTimeMillis()
                                if (now - lastBackPressMs < 2000) {
                                    finish()
                                } else {
                                    lastBackPressMs = now
                                    Toast.makeText(
                                        this@MainActivity,
                                        R.string.exit_confirm,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    finish()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (::webView.isInitialized) webView.onResume()
    }

    override fun onPause() {
        if (::webView.isInitialized) webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.loadUrl("about:blank")
            webView.stopLoading()
            webView.destroy()
        }
        super.onDestroy()
    }
}
