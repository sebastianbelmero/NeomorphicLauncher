package com.neumorphic.launcher.util

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Helper untuk gesture Android — Helper for Android gestures.
 *
 * Catatan: Membuka notification shade secara programatik memerlukan
 * akses system atau reflective call. Untuk Android terbaru, kami menggunakan
 * StatusBarManager.
 *
 * Note: Programmatically opening the notification shade requires system
 * access or reflection. For newer Android, we use StatusBarManager.
 */
object GestureHelper {

    /**
     * Membuka notification shade (swipe down gesture simulation).
     * Opens the notification shade (swipe down gesture simulation).
     *
     * Membutuhkan permission EXPAND_STATUS_BAR.
     */
    fun expandNotificationShade(context: Context) {
        try {
            @Suppress("DEPRECATION")
            val service = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method = statusBarManager.getMethod("expandNotificationsPanel")
            method.invoke(service)
        } catch (e: Exception) {
            // Fallback: gunakan intent global settings
            try {
                val intent = android.content.Intent("android.intent.action.SHOW_NOTIFICATION_SHADE")
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } catch (_: Exception) {
                // Jika kedua metode gagal, abaikan — silently fail
            }
        }
    }

    /**
     * Mengecek apakah perangkat mendukung gesture navigation penuh (Android 10+).
     * Check if device supports full gesture navigation (Android 10+).
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun supportsGestureNavigation(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
}
