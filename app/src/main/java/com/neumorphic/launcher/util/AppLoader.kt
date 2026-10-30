package com.neumorphic.launcher.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import com.neumorphic.launcher.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility untuk memuat daftar aplikasi terinstal.
 * Utility for loading the list of installed applications.
 */
object AppLoader {

    /**
     * Memuat semua aplikasi yang bisa diluncurkan secara asynchronous.
     * Loads all launchable applications asynchronously.
     *
     * @param context Context aplikasi
     * @return List of [AppInfo] sorted alphabetically by label
     */
    suspend fun loadInstalledApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        val apps = mutableListOf<AppInfo>()

        for (info in resolveInfoList) {
            val activityInfo = info.activityInfo
            val packageName = activityInfo.packageName

            // Skip aplikasi launcher sendiri — Skip our own launcher
            if (packageName == context.packageName) continue

            val label = info.loadLabel(packageManager).toString()
            val icon = info.loadIcon(packageManager)
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: continue
            val isSystemApp = try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }

            apps.add(
                AppInfo(
                    packageName = packageName,
                    label = label,
                    icon = icon,
                    launchIntent = launchIntent,
                    isSystemApp = isSystemApp
                )
            )
        }

        // Urutkan berdasarkan label — Sort by label
        apps.sorted()
    }

    /**
     * Memuat ikon aplikasi tunggal (untuk dock / favorit).
     * Load a single application icon (for dock / favorites).
     */
    fun loadAppByPackageName(context: Context, packageName: String): AppInfo? {
        val packageManager = context.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return null

        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val label = packageManager.getApplicationLabel(appInfo).toString()
            val icon = packageManager.getApplicationIcon(appInfo)

            AppInfo(
                packageName = packageName,
                label = label,
                icon = icon,
                launchIntent = launchIntent,
                isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}
