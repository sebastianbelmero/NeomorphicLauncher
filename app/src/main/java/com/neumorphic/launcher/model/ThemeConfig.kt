package com.neumorphic.launcher.model

/**
 * Konfigurasi tema launcher — Launcher theme configuration.
 *
 * @property mode Mode tema: LIGHT, DARK, atau AUTO (mengikuti sistem)
 * @property accentColor Warna aksen untuk highlight dan tombol aktif
 * @property backgroundColor Warna latar belakang utama
 * @property cornerRadius Radius sudut (dalam dp) untuk elemen neumorphic
 * @property shadowIntensity Intensitas bayangan neumorphic (0.0 - 1.0)
 */
data class ThemeConfig(
    val mode: ThemeMode = ThemeMode.AUTO,
    val accentColor: Int = 0xFF6C63FF.toInt(),
    val backgroundColor: Int = 0xFFE0E5EC.toInt(),
    val cornerRadius: Float = 24f,
    val shadowIntensity: Float = 0.7f
)

/**
 * Enum mode tema — Theme mode enum.
 * LIGHT  : Mode terang (light theme)
 * DARK   : Mode gelap (dark theme)
 * AUTO   : Mengikuti pengaturan sistem (follow system)
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    AUTO
}
