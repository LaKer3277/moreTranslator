# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontskipnonpubliclibraryclassmembers
-printmapping proguard.map
-renamesourcefileattribute ProGuard
-keepattributes SourceFile,LineNumberTable
-dontwarn oauth.signpost.signature.**
-ignorewarnings

-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# 保护代码中的Annotation不被混淆
-keepattributes *Annotation*,InnerClasses
# 排除所有注解类
-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }


-assumenosideeffects class android.util.Log{
    public static *** d(...);
    public static *** i(...);
    public static *** e(...);
    public static *** w(...);
    public static *** v(...);
}