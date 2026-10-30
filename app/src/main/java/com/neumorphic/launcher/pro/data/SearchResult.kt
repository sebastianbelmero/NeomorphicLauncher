package com.neumorphic.launcher.pro.data

/**
 * Model untuk hasil pencarian universal.
 * Model for universal search result.
 */
sealed class SearchResult {
    abstract val title: String
    abstract val subtitle: String?

    /** Hasil aplikasi */
    data class AppResult(
        override val title: String,
        override val subtitle: String? = null,
        val packageName: String,
        val icon: android.graphics.drawable.Drawable? = null
    ) : SearchResult()

    /** Hasil kontak */
    data class ContactResult(
        override val title: String,
        override val subtitle: String? = null,
        val contactId: Long,
        val phoneNumber: String? = null,
        val photoUri: String? = null
    ) : SearchResult()

    /** Hasil settings */
    data class SettingsResult(
        override val title: String,
        override val subtitle: String? = null,
        val intentAction: String,
        val iconRes: Int? = null
    ) : SearchResult()

    /** Hasil web search */
    data class WebResult(
        override val title: String,
        override val subtitle: String? = null,
        val query: String
    ) : SearchResult()

    /** Pintasan cepat */
    data class ShortcutResult(
        override val title: String,
        override val subtitle: String? = null,
        val action: ShortcutAction
    ) : SearchResult()
}

enum class ShortcutAction {
    CALL,
    MESSAGE,
    CAMERA,
    ALARM,
    BROWSER,
    EMAIL,
    CALENDAR
}

/**
 * Tipe section di hasil search.
 * Section type in search results.
 */
enum class SearchSection(val titleRes: Int) {
    APPS(com.neumorphic.launcher.R.string.search_section_apps),
    CONTACTS(com.neumorphic.launcher.R.string.search_section_contacts),
    SETTINGS(com.neumorphic.launcher.R.string.search_section_settings),
    WEB(com.neumorphic.launcher.R.string.search_section_web),
    SHORTCUTS(com.neumorphic.launcher.R.string.search_section_shortcuts)
}
