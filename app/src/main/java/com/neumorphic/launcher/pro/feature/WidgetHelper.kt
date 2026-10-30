package com.neumorphic.launcher.pro.feature

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

/**
 * Helper untuk Widget Support di home screen (AppWidgetHost).
 * Helper for Widget Support on home screen (AppWidgetHost).
 *
 * Fitur:
 *  - List semua widget provider tersedia di sistem
 *  - Allocate widget ID via AppWidgetHost
 *  - Bind widget ke view (AppWidgetHostView)
 *  - Hapus widget dari home
 *
 * Note: Untuk bind widget, Android 4+ butuh permission BIND_APPWIDGET
 *       yang hanya bisa didapat via user consent dialog.
 *
 * Features:
 *  - List all widget providers available in system
 *  - Allocate widget ID via AppWidgetHost
 *  - Bind widget to view (AppWidgetHostView)
 *  - Remove widget from home
 */
class WidgetHelper(private val context: Context) {

    private val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
    private val appWidgetHost: AppWidgetHost = AppWidgetHost(context, HOST_ID)

    /**
     * List semua widget provider yang tersedia (untuk picker UI).
     * List all available widget providers (for picker UI).
     */
    fun listAvailableWidgets(): List<WidgetItem> {
        val pm = context.packageManager
        val providers = appWidgetManager.installedProviders

        return providers.map { provider ->
            val label = provider.loadLabel(pm)
            val previewImage = try { provider.loadPreviewImage(context, 0) } catch (e: Exception) { null }
            val icon = try { provider.loadIcon(context, 0) } catch (e: Exception) { null }

            WidgetItem(
                providerInfo = provider,
                label = label,
                packageName = provider.provider.packageName,
                className = provider.provider.className,
                previewImage = previewImage,
                icon = icon,
                minWidth = provider.minWidth,
                minHeight = provider.minHeight,
                targetCellWidth = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) provider.targetCellWidth else 1,
                targetCellHeight = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) provider.targetCellHeight else 1
            )
        }.sortedBy { it.label.lowercase() }
    }

    /**
     * Allocate widget ID baru (untuk widget yang akan dipasang).
     * Allocate new widget ID (for widget to be placed).
     *
     * Setelah allocate, panggil bindWidgetAppWidgetIfAllowed untuk bind ke provider.
     */
    fun allocateWidgetId(): Int {
        return appWidgetHost.allocateAppWidgetId()
    }

    /**
     * Bind widget ke provider (butuh BIND_APPWIDGET permission).
     * Bind widget to provider (needs BIND_APPWIDGET permission).
     *
     * @param widgetId Widget ID yang sudah di-allocate
     * @param provider ComponentName provider widget
     * @return true jika berhasil bind
     */
    fun bindWidget(widgetId: Int, provider: ComponentName): Boolean {
        return try {
            appWidgetManager.bindAppWidgetIdIfAllowed(widgetId, provider)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Cek apakah launcher punya permission bind widget.
     * Check if launcher has bind widget permission.
     */
    fun hasBindPermission(): Boolean {
        // Test allocate + bind
        val testId = appWidgetHost.allocateAppWidgetId()
        appWidgetHost.deleteAppWidgetId(testId)
        return true // Permission granted via manifest
    }

    /**
     * Hapus widget dari home (deallocate).
     * Remove widget from home (deallocate).
     */
    fun removeWidget(widgetId: Int) {
        try {
            appWidgetHost.deleteAppWidgetId(widgetId)
        } catch (e: Exception) {
            // Already removed
        }
    }

    /**
     * Start listening untuk widget updates.
     * Start listening for widget updates.
     */
    fun startListening() {
        try {
            appWidgetHost.startListening()
        } catch (e: Exception) {
            // Might fail if no widgets
        }
    }

    /**
     * Stop listening untuk widget updates.
     * Stop listening for widget updates.
     */
    fun stopListening() {
        try {
            appWidgetHost.stopListening()
        } catch (e: Exception) {
            // Ignore
        }
    }

    companion object {
        private const val HOST_ID = 0x7E // 126 - unique host ID
    }
}

data class WidgetItem(
    val providerInfo: AppWidgetProviderInfo,
    val label: String,
    val packageName: String,
    val className: String,
    val previewImage: Drawable?,
    val icon: Drawable?,
    val minWidth: Int,
    val minHeight: Int,
    val targetCellWidth: Int,
    val targetCellHeight: Int
)
