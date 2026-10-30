package com.neumorphic.launcher.pro.data

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Manager untuk menyimpan badge count per aplikasi (singleton) +
 * callback listener untuk update UI real-time.
 *
 * Manager for storing badge count per app (singleton) +
 * callback listener for real-time UI updates.
 */
object NotificationBadgeManager {

    private val badgeCounts = ConcurrentHashMap<String, Int>()
    private val listeners = CopyOnWriteArrayList<BadgeUpdateListener>()

    fun interface BadgeUpdateListener {
        fun onBadgeUpdated(packageName: String, count: Int)
    }

    /**
     * Register listener untuk update badge.
     * Register listener for badge updates.
     */
    fun addListener(listener: BadgeUpdateListener) {
        listeners.addIfAbsent(listener)
    }

    /**
     * Unregister listener.
     * Unregister listener.
     */
    fun removeListener(listener: BadgeUpdateListener) {
        listeners.remove(listener)
    }

    /**
     * Notify semua listener (dipanggil dari NotificationListenerService).
     * Notify all listeners (called from NotificationListenerService).
     */
    fun notifyListeners(packageName: String, count: Int) {
        listeners.forEach { it.onBadgeUpdated(packageName, count) }
    }

    /**
     * Update count notifikasi untuk packageName tertentu.
     * Update notification count for specific packageName.
     */
    fun updateBadge(packageName: String, count: Int) {
        if (count > 0) {
            badgeCounts[packageName] = count
        } else {
            badgeCounts.remove(packageName)
        }
    }

    /**
     * Ambil count notifikasi untuk packageName.
     * Get notification count for packageName.
     */
    fun getBadgeCount(packageName: String): Int = badgeCounts[packageName] ?: 0

    /**
     * Cek apakah app punya notifikasi.
     * Check if app has notifications.
     */
    fun hasBadge(packageName: String): Boolean = (badgeCounts[packageName] ?: 0) > 0

    /**
     * Clear semua badge.
     * Clear all badges.
     */
    fun clearAll() {
        badgeCounts.clear()
    }

    /**
     * Dapatkan semua packageName yang punya notifikasi.
     * Get all packageNames that have notifications.
     */
    fun getAllBadgedPackages(): Set<String> = badgeCounts.keys.toSet()
}
