# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see:
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep semua class model — Keep all model classes
-keep class com.neumorphic.launcher.model.** { *; }

# Keep custom view (diperlukan oleh inflater)
-keep class com.neumorphic.launcher.widget.** { *; }

# Keep pro feature data classes
-keep class com.neumorphic.launcher.pro.data.** { *; }

# Gson (untuk serialization AppLockManager dll)
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.neumorphic.launcher.pro.feature.AppLockManager$* { *; }

# Keep enum (Gson butuh)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
