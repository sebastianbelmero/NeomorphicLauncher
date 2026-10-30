package com.neumorphic.launcher.pro.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi

/**
 * Helper untuk efek blur glassmorphism (iOS-style).
 * Helper for glassmorphism blur effect (iOS-style).
 *
 * Strategi:
 *  - Android 12+ (API 31): gunakan RenderEffect (hardware accelerated, real-time)
 *  - Android 8-11 (API 26-30): gunakan RenderScript (software fallback, di-cache)
 *  - Fallback: gunakan background semi-transparan tanpa blur
 *
 * Strategy:
 *  - Android 12+: use RenderEffect (hardware accelerated)
 *  - Android 8-11: use RenderScript (cached)
 *  - Fallback: semi-transparent background without blur
 */
object BlurHelper {

    /** Cache bitmap blur agar tidak re-render setiap frame */
    private var cachedBlurBitmap: Bitmap? = null
    private var cachedBlurKey: String? = null

    /**
     * Apply blur ke view (Android 12+).
     * Apply blur to view (Android 12+).
     *
     * @param view View yang akan di-blur
     * @param radius Radius blur (0-25, default 20f)
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun applyBlur(view: View, radius: Float = 20f) {
        val effect = RenderEffect.createBlurEffect(
            radius.coerceIn(0f, 25f),
            radius.coerceIn(0f, 25f),
            Shader.TileMode.CLAMP
        )
        view.setRenderEffect(effect)
    }

    /**
     * Hapus blur dari view.
     * Remove blur from view.
     */
    fun removeBlur(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            view.setRenderEffect(null)
        }
    }

    /**
     * Cek apakah device support real-time blur (Android 12+).
     * Check if device supports real-time blur (Android 12+).
     */
    fun supportsRealTimeBlur(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    /**
     * Blur bitmap menggunakan RenderScript (untuk Android 8-11).
     * Blur bitmap using RenderScript (for Android 8-11).
     *
     * @param context Context
     * @param bitmap Bitmap input
     * @param radius Radius blur (0-25)
     * @return Bitmap yang sudah di-blur
     */
    fun blurBitmap(context: Context, bitmap: Bitmap, radius: Float = 20f): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // API 31+: gunakan RenderEffect
            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            val paint = android.graphics.Paint().apply {
                isFilterBitmap = true
            }
            val effect = RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
            // Bitmap-based blur via RenderEffect not directly supported, use RenderScript
            return blurWithRenderScript(context, bitmap, radius)
        }
        return blurWithRenderScript(context, bitmap, radius)
    }

    private fun blurWithRenderScript(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
        val rs = RenderScript.create(context)
        val input = Allocation.createFromBitmap(rs, bitmap)
        val output = Allocation.createTyped(rs, input.type)
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        script.setRadius(radius.coerceIn(0f, 25f))
        script.setInput(input)
        script.forEach(output)
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        output.copyTo(result)
        rs.destroy()
        return result
    }

    /**
     * Capture view ke bitmap (untuk di-blur).
     * Capture view to bitmap (for blurring).
     */
    fun captureView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    /**
     * Convert drawable ke bitmap.
     * Convert drawable to bitmap.
     */
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        return when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            is ColorDrawable -> {
                val bmp = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
                bmp.eraseColor(drawable.color)
                bmp
            }
            else -> {
                val width = drawable.intrinsicWidth.coerceAtLeast(1)
                val height = drawable.intrinsicHeight.coerceAtLeast(1)
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp
            }
        }
    }
}
