package com.neumorphic.launcher.util

import android.content.Context
import android.content.SharedPreferences
import com.neumorphic.launcher.model.ThemeConfig
import com.neumorphic.launcher.model.ThemeMode

/**
 * Manajer preferensi untuk menyimpan pengaturan launcher.
 * Preference manager for storing launcher settings.
 *
 * Menggunakan SharedPreferences untuk persistensi data — Uses SharedPreferences for data persistence.
 */
class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Memuat konfigurasi tema yang tersimpan — Load saved theme configuration.
     */
    fun loadThemeConfig(): ThemeConfig {
        val modeOrdinal = prefs.getInt(KEY_THEME_MODE, ThemeMode.AUTO.ordinal)
        val mode = ThemeMode.entries.getOrElse(modeOrdinal) { ThemeMode.AUTO }
        val accentColor = prefs.getInt(KEY_ACCENT_COLOR, 0xFF6C63FF.toInt())
        val bgColor = prefs.getInt(KEY_BG_COLOR, 0xFFE0E5EC.toInt())
        val cornerRadius = prefs.getFloat(KEY_CORNER_RADIUS, 24f)
        val shadowIntensity = prefs.getFloat(KEY_SHADOW_INTENSITY, 0.7f)

        return ThemeConfig(
            mode = mode,
            accentColor = accentColor,
            backgroundColor = bgColor,
            cornerRadius = cornerRadius,
            shadowIntensity = shadowIntensity
        )
    }

    /**
     * Menyimpan konfigurasi tema — Save theme configuration.
     */
    fun saveThemeConfig(config: ThemeConfig) {
        prefs.edit().apply {
            putInt(KEY_THEME_MODE, config.mode.ordinal)
            putInt(KEY_ACCENT_COLOR, config.accentColor)
            putInt(KEY_BG_COLOR, config.backgroundColor)
            putFloat(KEY_CORNER_RADIUS, config.cornerRadius)
            putFloat(KEY_SHADOW_INTENSITY, config.shadowIntensity)
            apply()
        }
    }

    /**
     * Mendapatkan daftar package aplikasi favorit (untuk dock).
     * Get list of favorite app packages (for dock).
     */
    fun getFavoriteApps(): List<String> {
        val raw = prefs.getString(KEY_FAVORITE_APPS, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(DELIMITER)
    }

    /**
     * Menyimpan daftar aplikasi favorit — Save favorite apps list.
     */
    fun setFavoriteApps(packages: List<String>) {
        prefs.edit().putString(KEY_FAVORITE_APPS, packages.joinToString(DELIMITER)).apply()
    }

    /**
     * Mendapatkan default dock apps (Phone, Chrome, Camera, Messages).
     * Get default dock apps if user hasn't customized.
     */
    fun getDockApps(): List<String> {
        val saved = getFavoriteApps()
        return if (saved.isEmpty()) DEFAULT_DOCK_APPS else saved
    }

    fun setDockApps(packages: List<String>) = setFavoriteApps(packages)

    companion object {
        private const val PREFS_NAME = "neumorphic_launcher_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_BG_COLOR = "bg_color"
        private const val KEY_CORNER_RADIUS = "corner_radius"
        private const val KEY_SHADOW_INTENSITY = "shadow_intensity"
        private const val KEY_FAVORITE_APPS = "favorite_apps"
        private const val DELIMITER = "|"

        /** Default dock apps — Aplikasi dock default */
        private val DEFAULT_DOCK_APPS = listOf(
            "com.android.dialer",
            "com.android.chrome",
            "com.android.camera",
            "com.google.android.apps.messaging"
        )
    }
}
