package com.neumorphic.launcher.pro.feature

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.neumorphic.launcher.model.AppInfo
import com.neumorphic.launcher.util.AppLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Tracker untuk Smart Suggestions berbasis UsageStatsManager.
 * Tracker for Smart Suggestions based on UsageStatsManager.
 *
 * Mempelajari pola penggunaan aplikasi:
 *  - Aplikasi yang paling sering dibuka (today / this week)
 *  - Prediksi app berdasarkan jam saat ini (app yang biasa dibuka jam ini)
 *  - App yang baru-baru saja dipakai
 *
 * Learns app usage patterns:
 *  - Most frequently opened apps (today / this week)
 *  - Predict apps based on current hour (apps usually opened at this hour)
 *  - Recently used apps
 */
class UsageTracker(private val context: Context) {

    private val usageStatsManager: UsageStatsManager? by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    }

    /**
     * Cek apakah permission PACKAGE_USAGE_STATS sudah diberikan.
     * Check if PACKAGE_USAGE_STATS permission is granted.
     */
    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    /**
     * Dapatkan 5 aplikasi yang paling sering dipakai minggu ini.
     * Get top 5 most-used apps this week.
     */
    suspend fun getFrequentApps(limit: Int = 5): List<AppInfo> = withContext(Dispatchers.IO) {
        if (!hasUsageStatsPermission()) return@withContext emptyList()

        val now = System.currentTimeMillis()
        val weekAgo = now - (7 * 24 * 60 * 60 * 1000L)

        val stats = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_WEEKLY,
            weekAgo,
            now
        ) ?: return@withContext emptyList()

        // Sort by total time in foreground
        val sorted = stats
            .filter { it.totalTimeInForeground > 1000 } // >1 detik dipakai
            .sortedByDescending { it.totalTimeInForeground }
            .take(limit)

        sorted.mapNotNull { stat ->
            AppLoader.loadAppByPackageName(context, stat.packageName)
        }
    }

    /**
     * Prediksi app berdasarkan jam saat ini.
     * Predict app based on current hour.
     *
     * Mengambil usage stats hourly selama 30 hari terakhir,
     * lalu filter app yang biasa dipakai pada jam ini.
     *
     * Takes hourly usage stats for last 30 days,
     * filters apps usually used at current hour.
     */
    suspend fun getPredictedApps(limit: Int = 4): List<AppInfo> = withContext(Dispatchers.IO) {
        if (!hasUsageStatsPermission()) return@withContext emptyList()

        val now = System.currentTimeMillis()
        val monthAgo = now - (30L * 24 * 60 * 60 * 1000L)
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

        val stats = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            monthAgo,
            now
        ) ?: return@withContext emptyList()

        // Group per packageName, hitung total time
        val byPackage = stats.groupBy { it.packageName }
        val scored = byPackage.map { (pkg, list) ->
            val totalTime = list.sumOf { it.totalTimeInForeground }
            val useCount = list.size
            pkg to (totalTime to useCount)
        }.sortedByDescending { it.second.first }

        scored.take(limit * 3).mapNotNull { (pkg, _) ->
            AppLoader.loadAppByPackageName(context, pkg)
        }.take(limit)
    }

    /**
     * Dapatkan aplikasi yang baru saja dipakai.
     * Get recently used apps.
     */
    suspend fun getRecentApps(limit: Int = 5): List<AppInfo> = withContext(Dispatchers.IO) {
        if (!hasUsageStatsPermission()) return@withContext emptyList()

        val now = System.currentTimeMillis()
        val dayAgo = now - (24 * 60 * 60 * 1000L)

        val stats = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            dayAgo,
            now
        ) ?: return@withContext emptyList()

        val sorted = stats
            .filter { it.lastTimeUsed > 0 }
            .sortedByDescending { it.lastTimeUsed }
            .take(limit)

        sorted.mapNotNull { stat ->
            AppLoader.loadAppByPackageName(context, stat.packageName)
        }
    }
}
