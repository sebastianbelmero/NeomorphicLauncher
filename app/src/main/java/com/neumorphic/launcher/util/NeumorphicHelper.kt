package com.neumorphic.launcher.util

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import com.neumorphic.launcher.model.ThemeConfig

/**
 * Helper untuk membuat elemen visual Neumorphic.
 * Helper for creating Neumorphic visual elements.
 *
 * Neumorphism = soft UI dengan bayangan ganda (light shadow + dark shadow) pada
 * background yang memiliki warna sama dengan elemen, sehingga terlihat seperti
 * muncul / tenggelam dari permukaan.
 *
 * Neumorphism = soft UI with dual shadows (light + dark) where element color
 * matches background, creating an embossed / extruded effect.
 */
object NeumorphicHelper {

    /**
     * Menghasilkan warna shadow terang (light shadow).
     * Generates light shadow color.
     */
    fun getLightShadowColor(bgColor: Int): Int {
        return blendColors(bgColor, Color.WHITE, 0.4f)
    }

    /**
     * Menghasilkan warna shadow gelap (dark shadow).
     * Generates dark shadow color.
     */
    fun getDarkShadowColor(bgColor: Int): Int {
        return blendColors(bgColor, Color.BLACK, 0.25f)
    }

    /**
     * Membuat background Neumorphic convex (timbul ke atas).
     * Creates a convex (raised) Neumorphic background drawable.
     *
     * Catatan: Untuk efek shadow yang akurat, gunakan [com.neumorphic.launcher.widget.NeumorphicCardView]
     * yang menggambar shadow secara programatik dengan shadow layer.
     *
     * Note: For accurate shadow effect, use [com.neumorphic.launcher.widget.NeumorphicCardView]
     * which draws shadows programmatically via shadow layer.
     *
     * @param config Konfigurasi tema
     * @param pressed Jika true, ubah ke gaya pressed/inset (tenggelam)
     */
    fun createConvexBackground(config: ThemeConfig, pressed: Boolean = false): Drawable {
        val bgColor = config.backgroundColor
        val cornerRadiusPx = config.cornerRadius

        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(bgColor)
            cornerRadius = cornerRadiusPx
        }

        val stateList = StateListDrawable()

        // State pressed — gaya inset (tenggelam)
        val pressedDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(bgColor)
            cornerRadius = cornerRadiusPx
        }
        stateList.addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)

        // State normal — gaya convex (timbul)
        stateList.addState(intArrayOf(), drawable)

        return stateList
    }

    /**
     * Membuat background inset (tenggelam ke bawah).
     * Creates an inset (sunken) Neumorphic background drawable.
     */
    fun createInsetBackground(config: ThemeConfig): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(config.backgroundColor)
            cornerRadius = config.cornerRadius
        }
    }

    /**
     * Menggabungkan dua warna — Blend two colors.
     *
     * @param color1 Warna pertama
     * @param color2 Warna kedua
     * @param ratio Rasio campuran (0 = color1, 1 = color2)
     */
    fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRatio = 1 - ratio
        val r = Color.red(color1) * inverseRatio + Color.red(color2) * ratio
        val g = Color.green(color1) * inverseRatio + Color.green(color2) * ratio
        val b = Color.blue(color1) * inverseRatio + Color.blue(color2) * ratio
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }

    /**
     * Mendapatkan warna teks yang kontras (hitam/putih) berdasarkan background.
     * Get contrast text color (black/white) based on background color.
     */
    fun getContrastTextColor(bgColor: Int): Int {
        val r = Color.red(bgColor) / 255.0
        val g = Color.green(bgColor) / 255.0
        val b = Color.blue(bgColor) / 255.0

        // Relative luminance
        val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
        return if (luminance > 0.6) Color.BLACK else Color.WHITE
    }

    /**
     * Membuat ColorStateList dengan warna accent untuk state aktif.
     * Create ColorStateList with accent color for active state.
     */
    fun createAccentColorStateList(accentColor: Int, defaultColor: Int): ColorStateList {
        return ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf(android.R.attr.state_activated),
                intArrayOf()
            ),
            intArrayOf(accentColor, accentColor, defaultColor)
        )
    }
}
