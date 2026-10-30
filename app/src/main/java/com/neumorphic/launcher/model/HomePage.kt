package com.neumorphic.launcher.model

/**
 * Representasi satu halaman home screen — Single home screen page.
 *
 * @property id ID unik halaman
 * @property type Tipe halaman (default, widgets, blank)
 */
data class HomePage(
    val id: Int,
    val type: PageType = PageType.DEFAULT
)

enum class PageType {
    /** Halaman utama dengan jam & search — Main page with clock & search */
    DEFAULT,

    /** Halaman dengan widget — Page with widgets */
    WIDGETS,

    /** Halaman kosong untuk app shortcut — Blank page for app shortcuts */
    BLANK
}
