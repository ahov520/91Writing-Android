# 91Writing WebView shell
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepclassmembers class com.writing91.app.MainActivity$Writing91Bridge {
    <methods>;
}
-keep class com.writing91.app.MainActivity$Writing91Bridge { *; }

# WebView / AssetLoader
-dontwarn androidx.webkit.**
