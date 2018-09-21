# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\opt\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html
#-injars      bin/classes
-injars      libs
-injars      ctlibs
#-outjars     bin/classes-processed.jar
#-libraryjars ctlibs/adobeMobileLibrary-4.8.3.jar

#Preverification is irrelevant for the dex compiler and the Dalvik VM, so we can switch it off with the -dontpreverify option.
-dontpreverify



# If you want to enable optimization, you should include the
# following:
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic
-optimizationpasses 5
-keepattributes *Annotation*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

-keep public class com.android.vending.licensing.ILicensingService

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
   public void *(android.view.MenuItem);
}

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class net.fortuna.ical4j.**
-dontwarn net.fortuna.ical4j.**

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version. We know about them, and they are safe.
#-dontwarn android.support.**
#-dontwarn com.google.ads.**

#-keep class com.google.common.**
#-dontwarn com.google.common.**

-keep class com.google.android.gms.**
-dontwarn com.google.android.gms.**

-keep class android.support.v4.app.**
-keep interface android.support.v4.app.**
-keep class android.support.v7.app.**
-keep interface android.support.v7.app.**

-keep class edu.emory.mathcs.backport.**
-dontwarn edu.emory.mathcs.backport.**

-keep class com.verizon.contenttransfer.activty.**
