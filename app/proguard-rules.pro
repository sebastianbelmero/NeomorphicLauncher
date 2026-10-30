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
