package com.writing91.app

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
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
import com.google.android.material.button.MaterialButton
import java.io.File

/**
 * Hosts the full 91Writing Vue SPA inside a WebView.
 * All features (AI writing, novel/chapter management, prompts, billing, backup…)
 * run from the bundled web assets under file:///android_asset/www/index.html
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var loadingPanel: LinearLayout
    private lateinit var errorPanel: LinearLayout
    private lateinit var errorText: TextView
    private lateinit var loadingText: TextView
    private lateinit var reloadButton: MaterialButton

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

        val root = findViewById<FrameLayout>(R.id.root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = bars.top, bottom = bars.bottom)
            insets
        }

        reloadButton.setOnClickListener { loadApp() }

        setupWebView()
        setupBackHandler()
        loadApp()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            // Needed for Vite relative asset loads inside file:// www
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            textZoom = 100
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(false)
            // Local storage / IndexedDB capacity for large novels
            cacheMode = WebSettings.LOAD_DEFAULT
            userAgentString = "$userAgentString Writing91Android/1.0"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url ?: return false
                val scheme = url.scheme?.lowercase() ?: return false
                // Keep all http(s)/file loads inside WebView so AI API + SPA routing work.
                // Only hand off non-web schemes to the system.
                return when (scheme) {
                    "http", "https", "file", "about", "blob", "data" -> false
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

    private fun injectAndroidBridge() {
        // Mark environment so web UI can adapt (safe no-op if unused)
        webView.evaluateJavascript(
            """
            (function(){
              try {
                window.__WRITING91_ANDROID__ = true;
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
    }

    private fun handleDownload(
        url: String,
        userAgent: String,
        contentDisposition: String?,
        mimeType: String?
    ) {
        try {
            // blob: URLs cannot use DownloadManager — ask WebView JS to trigger save
            if (url.startsWith("blob:")) {
                Toast.makeText(this, "请使用应用内导出功能保存文件", Toast.LENGTH_SHORT).show()
                return
            }
            if (url.startsWith("data:")) {
                // Save data URL to cache and share
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
            // Fallback: open in browser
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

        val indexPath = "file:///android_asset/www/index.html"
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
        webView.loadUrl(indexPath)
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
                if (::webView.isInitialized && webView.canGoBack()) {
                    webView.goBack()
                    return
                }
                val now = System.currentTimeMillis()
                if (now - lastBackPressMs < 2000) {
                    finish()
                } else {
                    lastBackPressMs = now
                    Toast.makeText(this@MainActivity, R.string.exit_confirm, Toast.LENGTH_SHORT).show()
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
