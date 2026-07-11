# 91Writing WebView shell — keep WebView bridges if minify is later enabled
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
