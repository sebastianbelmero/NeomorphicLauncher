package com.neumorphic.launcher.pro.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.neumorphic.launcher.pro.data.NotificationBadgeManager

/**
 * Notification Listener Service untuk menampilkan badge di ikon aplikasi.
 * Notification Listener Service for showing badges on app icons.
 *
 * Cara kerja:
 *  - Sistem mengirim notifikasi → service ini terima callback
 *  - Simpan count notifikasi per packageName di NotificationBadgeManager
 *  - Notify listener callback (UI adapter yang registered)
 *
 * How it works:
 *  - System sends notification → service receives callback
 *  - Store notification count per packageName in NotificationBadgeManager
 *  - Notify listener callback (UI adapter that registered)
 */
class NotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName ?: return
        val count = countNotificationsForPackage(packageName)
        NotificationBadgeManager.updateBadge(packageName, count)
        NotificationBadgeManager.notifyListeners(packageName, count)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val packageName = sbn.packageName ?: return
        val count = countNotificationsForPackage(packageName)
        NotificationBadgeManager.updateBadge(packageName, count)
        NotificationBadgeManager.notifyListeners(packageName, count)
    }

    private fun countNotificationsForPackage(targetPackage: String): Int {
        return try {
            activeNotifications.count {
                it.packageName == targetPackage &&
                it.notification.flags and Notification.FLAG_GROUP_SUMMARY == 0
            }
        } catch (e: Exception) {
            0
        }
    }
}
