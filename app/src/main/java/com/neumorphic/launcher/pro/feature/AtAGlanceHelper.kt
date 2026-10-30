package com.neumorphic.launcher.pro.feature

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Helper untuk At-a-Glance widget (date + weather + next calendar event).
 * Helper for At-a-Glance widget (date + weather + next calendar event).
 *
 * Fitur:
 *  - Tanggal lengkap (Senin, 31 Oktober 2026)
 *  - Event kalender terdekat (butuh permission READ_CALENDAR)
 *  - Info cuaca sederhana (placeholder - weather API perlu key)
 *
 * Features:
 *  - Full date (Monday, 31 October 2026)
 *  - Next calendar event (needs READ_CALENDAR permission)
 *  - Simple weather info (placeholder - weather API needs key)
 */
class AtAGlanceHelper(private val context: Context) {

    private val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id"))

    /**
     * Dapatkan string tanggal hari ini.
     * Get today's date string.
     */
    fun getTodayDateString(): String {
        return dateFormat.format(Date()).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }
    }

    /**
     * Dapatkan event kalender terdekat.
     * Get next calendar event.
     *
     * @return null jika tidak ada event / tidak ada permission
     */
    fun getNextCalendarEvent(): CalendarEvent? {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return null

        val now = System.currentTimeMillis()
        val endTime = now + 7L * 24 * 60 * 60 * 1000 // 7 hari ke depan

        val projection = arrayOf(
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.EVENT_LOCATION
        )

        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf(now.toString(), endTime.toString())
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        return try {
            context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val title = cursor.getString(0) ?: return null
                    val dtStart = cursor.getLong(1)
                    val dtEnd = cursor.getLong(2)
                    val allDay = cursor.getInt(3) == 1
                    val location = cursor.getString(4)

                    CalendarEvent(
                        title = title,
                        startTime = dtStart,
                        endTime = dtEnd,
                        isAllDay = allDay,
                        location = location
                    )
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Format event ke string ringkas untuk ditampilkan.
     * Format event to concise string for display.
     */
    fun formatEventSummary(event: CalendarEvent): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale("id"))
        val now = Calendar.getInstance()
        val eventCal = Calendar.getInstance().apply { timeInMillis = event.startTime }
        val isToday = now.get(Calendar.YEAR) == eventCal.get(Calendar.YEAR) &&
                      now.get(Calendar.DAY_OF_YEAR) == eventCal.get(Calendar.DAY_OF_YEAR)

        val timeStr = if (event.isAllDay) {
            if (isToday) "Hari ini" else dayFormat.format(event.startTime)
        } else {
            val prefix = if (isToday) "" else "${dayFormat.format(event.startTime)} "
            "$prefix${timeFormat.format(Date(event.startTime))}"
        }

        val locationStr = if (event.location.isNullOrBlank()) "" else " · ${event.location}"
        return "$timeStr · ${event.title}$locationStr"
    }

    /**
     * Get cuaca saat ini (placeholder - butuh API key untuk implementasi real).
     * Get current weather (placeholder - needs API key for real implementation).
     *
     * Untuk implementasi real, gunakan OpenWeatherMap / Meteomatics / WeatherAPI.
     * For real implementation, use OpenWeatherMap / Meteomatics / WeatherAPI.
     */
    fun getWeatherPlaceholder(): String {
        return context.getString(com.neumorphic.launcher.R.string.ataglance_weather_unavailable)
    }
}

data class CalendarEvent(
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val isAllDay: Boolean,
    val location: String?
)
