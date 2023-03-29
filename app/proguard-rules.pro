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

#It can't be empty package - in other case app crashes
-repackageclasses 'wxrvyn'

#Hide logs
-assumenosideeffects class android.util.Log {
     public static int v(...);
     public static int i(...);
     public static int w(...);
     public static int d(...);
     public static int e(...);
 }

#Ignore okhttp warnings (which can be safety according to https://github.com/square/okhttp/issues/392)ó
-dontwarn okhttp3.internal.platform.ConscryptPlatform

## Debug section start
## To output a full report of all the rules that R8 applies when building project.
#-printconfiguration mergedRules.txt
## To generate report of removed code:
#-printusage removedCode.txt
## To generate report of kept entry points:
#-printseeds seeds.txt
#
## To generate report which shows how original fields, methods and class
## names are mapped to obfuscated names.
##--------------------------------------------------------------------------
##WARNING.1: -printmapping should be commented/disabled to allow Crashlytics
##automatically upload mapping file. Uncomment it only for test purposes or to generate
##new mapping file for appcenter.
##WARNING.2: Only new crashes in app center will be deobfuscated when new mapping.txt file uploaded
##(remember to upload mapping asap after new build)
##WARNING.3: You don't have to use printmapping, file with mappings which is always available
##can be found in build/outputs/mapping/mapping.txt
#-printmapping mapping.txt
## Debug section end

-keep class com.shockwave.**
-keepclassmembers class **.R$* {
    public static <fields>;
}
-keepclasseswithmembernames class **.R$* {
    public static <fields>;
}
-keep class **.R$*
