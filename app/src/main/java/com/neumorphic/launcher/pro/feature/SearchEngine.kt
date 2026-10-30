package com.neumorphic.launcher.pro.feature

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import com.neumorphic.launcher.model.AppInfo
import com.neumorphic.launcher.pro.data.SearchResult
import com.neumorphic.launcher.pro.data.SearchSection
import com.neumorphic.launcher.pro.data.ShortcutAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Engine untuk Universal Search (apps + contacts + settings + web + shortcuts).
 * Engine for Universal Search (apps + contacts + settings + web + shortcuts).
 *
 * Mencari secara paralel di:
 *  - Daftar aplikasi terinstal
 *  - Kontak (jika permission READ_CONTACTS diberikan)
 *  - Settings Android (predefined list)
 *  - Web (Google Search URL)
 *  - Shortcuts (call, message, camera, dll)
 *
 * Searches in parallel across:
 *  - Installed apps
 *  - Contacts (if READ_CONTACTS permission granted)
 *  - Android settings (predefined list)
 *  - Web (Google Search URL)
 *  - Shortcuts (call, message, camera, etc)
 */
class SearchEngine(private val context: Context) {

    /**
     * Lakukan pencarian universal.
     * Perform universal search.
     *
     * @param query Query pencarian
     * @param installedApps Daftar aplikasi terinstal (pre-loaded)
     * @return Map section -> list of results
     */
    suspend fun search(
        query: String,
        installedApps: List<AppInfo>
    ): Map<SearchSection, List<SearchResult>> = withContext(Dispatchers.Default) {
        if (query.isBlank()) return@withContext emptyMap()

        val q = query.trim().lowercase()
        val results = mutableMapOf<SearchSection, List<SearchResult>>()

        // 1. Cari di aplikasi
        val appResults = installedApps.filter {
            it.label.contains(query, ignoreCase = true) ||
            it.packageName.contains(q, ignoreCase = true)
        }.take(8).map {
            SearchResult.AppResult(
                title = it.label,
                subtitle = it.packageName,
                packageName = it.packageName,
                icon = it.icon
            )
        }
        if (appResults.isNotEmpty()) {
            results[SearchSection.APPS] = appResults
        }

        // 2. Cari di kontak (jika punya permission)
        val contactResults = searchContacts(query)
        if (contactResults.isNotEmpty()) {
            results[SearchSection.CONTACTS] = contactResults.take(5)
        }

        // 3. Cari di settings (predefined)
        val settingsResults = searchSettings(query)
        if (settingsResults.isNotEmpty()) {
            results[SearchSection.SETTINGS] = settingsResults.take(5)
        }

        // 4. Shortcut untuk query umum
        val shortcutResults = searchShortcuts(query)
        if (shortcutResults.isNotEmpty()) {
            results[SearchSection.SHORTCUTS] = shortcutResults
        }

        // 5. Web search (selalu ada 1 result untuk query apapun)
        results[SearchSection.WEB] = listOf(
            SearchResult.WebResult(
                title = "Cari \"$query\" di Google",
                subtitle = "www.google.com",
                query = query
            )
        )

        results
    }

    /**
     * Cari kontak berdasarkan nama / nomor telepon.
     * Search contacts by name / phone number.
     */
    private fun searchContacts(query: String): List<SearchResult.ContactResult> {
        // Cek permission
        val resolver = context.contentResolver
        val hasPermission = context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return emptyList()

        return try {
            val selection = "${ContactsContract.Contacts.DISPLAY_NAME} LIKE ?"
            val selectionArgs = arrayOf("%$query%")
            val cursor = resolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.PHOTO_URI,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER
                ),
                selection,
                selectionArgs,
                "${ContactsContract.Contacts.DISPLAY_NAME} ASC"
            )

            val results = mutableListOf<SearchResult.ContactResult>()
            cursor?.use { c ->
                while (c.moveToNext() && results.size < 10) {
                    val id = c.getLong(0)
                    val name = c.getString(1) ?: continue
                    val photoUri = c.getString(2)
                    val hasPhone = c.getInt(3) > 0
                    val phoneNumber = if (hasPhone) getPhoneNumber(id) else null

                    results.add(
                        SearchResult.ContactResult(
                            title = name,
                            subtitle = phoneNumber,
                            contactId = id,
                            phoneNumber = phoneNumber,
                            photoUri = photoUri
                        )
                    )
                }
            }
            results
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getPhoneNumber(contactId: Long): String? {
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )
        return cursor?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
    }

    /**
     * Daftar settings Android yang bisa di-search.
     * List of Android settings that can be searched.
     */
    private fun searchSettings(query: String): List<SearchResult.SettingsResult> {
        val q = query.lowercase()
        return SETTINGS_INDEX.filter { (title, _) ->
            title.contains(q, ignoreCase = true)
        }.map { (title, action) ->
            SearchResult.SettingsResult(
                title = title,
                subtitle = "Pengaturan Android",
                intentAction = action
            )
        }
    }

    private fun searchShortcuts(query: String): List<SearchResult.ShortcutResult> {
        val q = query.lowercase()
        val results = mutableListOf<SearchResult.ShortcutResult>()

        if ("telepon" in q || "panggil" in q || "call" in q) {
            results.add(SearchResult.ShortcutResult("Buka Telepon", "Aplikasi dialer", ShortcutAction.CALL))
        }
        if ("kamera" in q || "camera" in q || "foto" in q) {
            results.add(SearchResult.ShortcutResult("Buka Kamera", "Aplikasi kamera", ShortcutAction.CAMERA))
        }
        if ("alarm" in q || "jam" in q || "clock" in q) {
            results.add(SearchResult.ShortcutResult("Buka Jam/Alarm", "Aplikasi jam", ShortcutAction.ALARM))
        }
        if ("browser" in q || "internet" in q || "web" in q) {
            results.add(SearchResult.ShortcutResult("Buka Browser", "Aplikasi web", ShortcutAction.BROWSER))
        }
        if ("email" in q || "surat" in q || "mail" in q) {
            results.add(SearchResult.ShortcutResult("Buka Email", "Aplikasi email", ShortcutAction.EMAIL))
        }
        if ("kalender" in q || "agenda" in q || "calendar" in q) {
            results.add(SearchResult.ShortcutResult("Buka Kalender", "Aplikasi kalender", ShortcutAction.CALENDAR))
        }
        return results
    }

    /**
     * Index settings Android (intent action).
     * Index of Android settings (intent action).
     */
    private val SETTINGS_INDEX = listOf(
        "Wifi" to Settings.ACTION_WIFI_SETTINGS,
        "Bluetooth" to Settings.ACTION_BLUETOOTH_SETTINGS,
        "Mobile Data" to Settings.ACTION_DATA_USAGE_SETTINGS,
        "Airplane Mode" to Settings.ACTION_AIRPLANE_MODE_SETTINGS,
        "Display" to Settings.ACTION_DISPLAY_SETTINGS,
        "Brightness" to Settings.ACTION_DISPLAY_SETTINGS,
        "Sound" to Settings.ACTION_SOUND_SETTINGS,
        "Volume" to Settings.ACTION_SOUND_SETTINGS,
        "Battery" to Settings.ACTION_BATTERY_SAVER_SETTINGS,
        "Apps" to Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS,
        "Aplikasi" to Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS,
        "Storage" to Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
        "Penyimpanan" to Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
        "Location" to Settings.ACTION_LOCATION_SOURCE_SETTINGS,
        "Lokasi" to Settings.ACTION_LOCATION_SOURCE_SETTINGS,
        "Security" to Settings.ACTION_SECURITY_SETTINGS,
        "Keamanan" to Settings.ACTION_SECURITY_SETTINGS,
        "Privacy" to Settings.ACTION_PRIVACY_SETTINGS,
        "Privasi" to Settings.ACTION_PRIVACY_SETTINGS,
        "About Phone" to Settings.ACTION_DEVICE_INFO_SETTINGS,
        "Tentang Ponsel" to Settings.ACTION_DEVICE_INFO_SETTINGS,
        "Date" to Settings.ACTION_DATE_SETTINGS,
        "Tanggal" to Settings.ACTION_DATE_SETTINGS,
        "Language" to Settings.ACTION_LOCALE_SETTINGS,
        "Bahasa" to Settings.ACTION_LOCALE_SETTINGS,
        "Developer" to Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS,
        "Accessibility" to Settings.ACTION_ACCESSIBILITY_SETTINGS,
        "Aksesibilitas" to Settings.ACTION_ACCESSIBILITY_SETTINGS,
        "Notification" to Settings.ACTION_APP_NOTIFICATION_SETTINGS,
        "Notifikasi" to Settings.ACTION_APP_NOTIFICATION_SETTINGS
    )

    /**
     * Build intent untuk membuka hasil pencarian.
     * Build intent to open search result.
     */
    fun buildIntentForResult(result: SearchResult): Intent? {
        return when (result) {
            is SearchResult.AppResult -> {
                context.packageManager.getLaunchIntentForPackage(result.packageName)
            }
            is SearchResult.ContactResult -> {
                Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.withAppendedPath(
                        ContactsContract.Contacts.CONTENT_URI,
                        result.contactId.toString()
                    )
                }
            }
            is SearchResult.SettingsResult -> {
                Intent(result.intentAction)
            }
            is SearchResult.WebResult -> {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(result.query)}"))
            }
            is SearchResult.ShortcutResult -> {
                when (result.action) {
                    ShortcutAction.CALL -> Intent(Intent.ACTION_DIAL)
                    ShortcutAction.MESSAGE -> Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_APP_MESSAGING)
                    }
                    ShortcutAction.CAMERA -> Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                    ShortcutAction.ALARM -> Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)
                    ShortcutAction.BROWSER -> Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                    ShortcutAction.EMAIL -> Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_APP_EMAIL)
                    }
                    ShortcutAction.CALENDAR -> Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_APP_CALENDAR)
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "SearchEngine"
    }
}
