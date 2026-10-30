package com.neumorphic.launcher.model

import android.graphics.drawable.Drawable

/**
 * Model data untuk informasi aplikasi terinstal.
 * Data model for installed application info.
 *
 * @property packageName Nama paket aplikasi (e.g. "com.example.app") — Application package name
 * @property label Nama tampilan aplikasi — Application display label
 * @property icon Ikon Drawable aplikasi — Application icon Drawable
 * @property launchIntent Intent untuk meluncurkan aplikasi — Launch intent for the app
 */
data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val launchIntent: android.content.Intent,
    val isSystemApp: Boolean = false
) : Comparable<AppInfo> {

    /** Urutkan berdasarkan label secara alfabetis (case-insensitive) */
    override fun compareTo(other: AppInfo): Int =
        label.lowercase().compareTo(other.label.lowercase())
}
