# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/hunt/AndroidEnv/android-sdk-linux/tools/proguard/proguard-android.txt
# You can edit the include path and rawDeviceOrder by changing the proguardFiles
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
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keepattributes Signature #保持泛型
-keepattributes SourceFile,LineNumberTable # keep住源文件以及行号

-keep public class * extends com.google.android.exoplayer.**{*;}

-keep public class * extends android.os.SystemProperties
-keep public class * extends android.app.ActivityThread
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public class org.msgpack.** { *; }
-keep public class org.webrtc.** { *; }
-keep public class com.cylan.** { *; }
-keep public class com.cylan.panorama.** { *; }
#内部类混淆
-keep class org.webrtc.videoengine.MediaCodecVideoEncoder$*{
    *;
}
-keep class org.webrtc.videoengine.MediaCodecVideoDecoder$*{
    *;
}
-keep class org.webrtc.videoengine.MediaCodecVideoDecoder{
    *;
}

-keep public class com.cylan.jiafeigou.support.Security{
    public <methods>;
}
-dontnote java.util.AbstractMap.**
-dontwarn org.msgpack.**
-dontnote org.msgpack.**

-keep  class net.sqlcipher.**{*;}
-dontnote net.sqlcipher.**

-dontwarn okio.**
-dontwarn javax.annotation.**

-dontnote android.os.SystemProperties
-dontnote android.app.ActivityThread
-dontnote com.google.android.**
-dontnote com.cylan.**

-keep public class sun.security.**{*;}
-dontwarn sun.security.**
-dontnote sun.security.**
-dontnote android.net.http.*
-dontnote org.apache.**
-dontnote com.android.org.conscrypt.**

-keep  class java.nio.** { *; }
-dontwarn java.nio.**
-dontnote java.nio.**

#######################bugly#########################################
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class com.tencent.bugly.**{*;}
-keep class com.tencent.android.tpush.**  {* ;}
-keep class com.tencent.mid.**  {* ;}
-dontnote  com.tencent.**
###mta##
-keep class com.tencent.**  {* ;}
################################################################

-keepattributes InnerClasses
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}


-dontnote libcore.icu.ICU
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in activity_cloud_live_mesg_video_talk_item class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# Gson specific classes
-keep class sun.misc.** { *; }
-dontwarn sun.misc.**
-dontnote sun.misc.**
-dontnote com.google.gson.**
-dontnote com.sina.weibo.sdk.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
# -keep class mypersonalclass.data.model.** { *; }

##  okhttp3   ####start
# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.** { *; }
-dontwarn com.squareup.okhttp3.**
-dontwarn okhttp3.**
##  okhttp3  ####end


########################Rxjava###############################################
-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
########################Rxjava###############################################


# retrofit specific
-dontwarn com.squareup.okhttp.**
-dontwarn com.google.appengine.api.urlfetch.**
-dontwarn rx.**
-dontwarn retrofit.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

-keep class com.facebook.** {
   *;
}
-keep class com.twitter.** {
   *;
}
-dontnote io.fabric.sdk.**
-dontnote com.twitter.**
-dontnote com.facebook.**
-keepattributes Signature

-dontnote tv.danmaku.ijk.**
-keep class tv.danmaku.ijk.media.player.* {*; }
-keep class tv.danmaku.ijk.media.player.IjkMediaPlayer{*;}
-keep class tv.danmaku.ijk.media.player.ffmpeg.FFmpegApi{*;}

-dontwarn com.squareup.picasso.**

-ignorewarning
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
# hmscore-support: remote transport
-keep class * extends com.huawei.hms.core.aidl.IMessageEntity { *; }
# hmscore-support: remote transport
-keepclasseswithmembers class * implements com.huawei.hms.support.api.transport.DatagramTransport {
 <init>(...);
}
# manifest: provider for updates
-keep public class com.huawei.hms.update.provider.UpdateProvider { public *; protected *; }
