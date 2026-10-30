package com.neumorphic.launcher.pro.feature

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import com.neumorphic.launcher.util.PreferenceManager
import org.xmlpull.v1.XmlPullParser

/**
 * Parser untuk Nova/Apex-style icon pack (.apk).
 * Parser for Nova/Apex-style icon pack (.apk).
 *
 * Cara kerja:
 *  - User install icon pack APK dari Play Store (misal: Whicons, Pixeled)
 *  - Launcher membaca resource APK tersebut
 *  - Parse appfilter.xml untuk mapping packageName → icon drawable
 *  - Saat render icon app, cek mapping dulu sebelum pakai icon default
 *
 * How it works:
 *  - User installs icon pack APK from Play Store (e.g., Whicons, Pixeled)
 *  - Launcher reads that APK's resources
 *  - Parse appfilter.xml for packageName → icon drawable mapping
 *  - When rendering app icon, check mapping first before using default icon
 */
class IconPackParser(private val context: Context) {

    private var iconPackPackageName: String? = null
    private var iconMapping: Map<String, String> = emptyMap() // component -> drawable name
    private var iconPackResources: android.content.res.Resources? = null
    private var iconPackResId: Int = 0

    /**
     * Set icon pack aktif (dari packageName).
     * Set active icon pack (from packageName).
     *
     * @param packageName Package dari icon pack APK (null = no icon pack)
     */
    fun setIconPack(packageName: String?) {
        iconPackPackageName = packageName
        iconMapping = emptyMap()
        iconPackResources = null
        iconPackResId = 0

        if (packageName.isNullOrBlank()) return

        try {
            val pm = context.packageManager
            iconPackResources = pm.getResourcesForApplication(packageName)
            iconPackResId = iconPackResources?.getIdentifier(
                "appfilter",
                "xml",
                packageName
            ) ?: 0

            if (iconPackResId != 0) {
                iconMapping = parseAppFilterXml(packageName, iconPackResId)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            // Icon pack tidak terinstall
            iconPackPackageName = null
        }
    }

    /**
     * Parse appfilter.xml dari icon pack.
     * Parse appfilter.xml from icon pack.
     *
     * Format XML:
     * <resources>
     *   <item component="ComponentInfo{com.example.app/com.example.app.MainActivity}"
     *         drawable="icon_name" />
     *   ...
     * </resources>
     */
    private fun parseAppFilterXml(packageName: String, resId: Int): Map<String, String> {
        val mapping = mutableMapOf<String, String>()
        val res = iconPackResources ?: return emptyMap()

        try {
            val parser: XmlResourceParser = res.getXml(resId)
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                    val component = parser.getAttributeValue(null, "component")
                    val drawable = parser.getAttributeValue(null, "drawable")
                    if (component != null && drawable != null) {
                        mapping[component] = drawable
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            // Parse failed, return empty
        }
        return mapping
    }

    /**
     * Dapatkan icon untuk packageName/component dari icon pack aktif.
     * Get icon for packageName/component from active icon pack.
     *
     * @param componentName ComponentInfo string (ComponentInfo{pkg/cls})
     * @return Drawable jika ada di icon pack, null jika tidak
     */
    fun getIconForComponent(componentName: String): Drawable? {
        val pkg = iconPackPackageName ?: return null
        val res = iconPackResources ?: return null
        if (iconPackResId == 0) return null

        val drawableName = iconMapping[componentName] ?: return null
        val drawableResId = res.getIdentifier(drawableName, "drawable", pkg)
        if (drawableResId == 0) return null

        return try {
            res.getDrawable(drawableResId, null)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Cek apakah icon pack aktif.
     * Check if icon pack is active.
     */
    fun hasIconPack(): Boolean = !iconPackPackageName.isNullOrBlank()

    /**
     * Dapatkan packageName icon pack aktif.
     * Get active icon pack packageName.
     */
    fun getActiveIconPack(): String? = iconPackPackageName

    /**
     * List semua icon pack terinstall di device.
     * List all icon packs installed on device.
     *
     * Icon pack dikenali via <intent-filter> dengan action
     * "org.adw.launcher.THEMES" atau "com.anddoes.launcher.THEME".
     */
    fun listInstalledIconPacks(): List<IconPackInfo> {
        val pm = context.packageManager
        val intents = listOf(
            Intent("org.adw.launcher.THEMES"),
            Intent("com.anddoes.launcher.THEME"),
            Intent("com.gau.go.launcherex.theme"),
            Intent("com.novalauncher.THEME")
        )

        val packages = mutableMapOf<String, CharSequence>()

        for (intent in intents) {
            val results = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)
            for (info in results) {
                val pkg = info.activityInfo.packageName
                if (!packages.containsKey(pkg)) {
                    packages[pkg] = info.loadLabel(pm)
                }
            }
        }

        // Manual import android.content.Intent
        return packages.map { (pkg, label) ->
            IconPackInfo(packageName = pkg, label = label.toString())
        }.sortedBy { it.label.lowercase() }
    }

    private fun Intent(action: String): android.content.Intent {
        return android.content.Intent(action)
    }
}

data class IconPackInfo(
    val packageName: String,
    val label: String
)
