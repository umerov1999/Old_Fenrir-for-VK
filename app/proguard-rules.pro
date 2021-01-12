-dontobfuscate
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-repackageclasses ''
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*

-dontwarn com.squareup.okhttp.**

-keepattributes *Annotation*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

-assumenosideeffects class android.util.Log {
  public static *** d(...);
  public static *** v(...);
}

-keepclasseswithmembernames class * {
    native <methods>;
}

# Understand the @Keep support annotation.
-keep class androidx.annotation.Keep

-keep @androidx.annotation.Keep class * {*;}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

# OnGuiCreated annotation based on Java Reflection Api
-keepclassmembers class ** {
  @dev.ragnarok.fenrir.mvp.reflect.OnGuiCreated *;
}

-keepclassmembers class * {
public void onClickButton(android.view.View);
}

-keepclasseswithmembernames class * {
  native <methods>;
}

-keep public class custom.components.package.and.name.**

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

-keepclassmembers enum * {
  public static **[] values();
  public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep class **$$Parcelable { *; }

-keepclassmembers class * implements android.os.Parcelable {
  public static final ** CREATOR;
}

-keepclassmembers class **.R$* {
  public static <fields>;
}

-keep public class com.google.inject.** {
 public protected *;
}

-keep public class com.google.inject.util.** {
 public protected *;
}

-keep public class com.google.inject.spi.** {
 public protected *;
}

-keep public class com.google.inject.name.** {
 public protected *;
}

-keep public class com.google.inject.matcher.** {
 public protected *;
}

-keep public class com.google.inject.internal.** {
 public protected *;
}

-keep public class com.google.inject.binder.** {
 public protected *;
}

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class dev.ragnarok.fenrir.model.** { *; }
-keep class dev.ragnarok.fenrir.api.model.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

##---------------End: proguard configuration for Gson  ----------

-keep class org.springframework.**
-dontwarn org.springframework.**

-keep class ealvatag.tag.id3.framebody.** { *; }
-keep class ealvatag.tag.datatype.** { *; }

-keep public class com.umerov.parcel.ParcelNative {
  private void updateNative*(...);
}

-keep public interface com.umerov.rlottie.RLottie2Gif$Lottie2GifListener {
  void onStarted();
  void onProgress*(...);
  void onFinished();
}
