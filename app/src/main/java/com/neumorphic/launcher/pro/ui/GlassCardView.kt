package com.neumorphic.launcher.pro.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.neumorphic.launcher.pro.util.BlurHelper

/**
 * GlassCardView — Custom view dengan efek glassmorphism iOS-style.
 * GlassCardView — Custom view with iOS-style glassmorphism effect.
 *
 * Efek:
 *  - Background semi-transparan (white 12% atau black 18% untuk dark mode)
 *  - Blur real-time pada Android 12+ (RenderEffect)
 *  - Border highlight tipis di tepi atas (efek cahaya)
 *  - Rounded corner dengan radius konsisten
 *  - Shadow halus untuk depth
 *
 * Effect:
 *  - Semi-transparent background (white 12% / black 18% dark mode)
 *  - Real-time blur on Android 12+ (RenderEffect)
 *  - Thin highlight border at top edge (light effect)
 *  - Rounded corner with consistent radius
 *  - Soft shadow for depth
 */
class GlassCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Konfigurasi visual
    private var cornerRadius: Float = 32f * resources.displayMetrics.density
    private var blurRadius: Float = 20f
    private var overlayColor: Int = Color.argb(30, 255, 255, 255) // 12% white
    private var borderColor: Int = Color.argb(50, 255, 255, 255) // 20% white border
    private var shadowColor: Int = Color.argb(40, 0, 0, 0)       // 16% black shadow
    private var enableBlur: Boolean = true
    private var enableBorder: Boolean = true

    // Paint objects
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = overlayColor
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = borderColor
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * resources.displayMetrics.density
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = shadowColor
        setShadowLayer(16f * resources.displayMetrics.density, 0f, 8f * resources.displayMetrics.density, shadowColor)
    }
    private val rect = RectF()
    private val clipPath = Path()

    init {
        // Hardware acceleration untuk RenderEffect
        setLayerType(LAYER_TYPE_HARDWARE, null)
        setupForApi()
    }

    private fun setupForApi() {
        // Auto-detect dark mode
        val isDark = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        if (isDark) {
            overlayColor = Color.argb(46, 255, 255, 255) // 18% white
            borderColor = Color.argb(60, 255, 255, 255)
            shadowColor = Color.argb(60, 0, 0, 0)
        } else {
            overlayColor = Color.argb(30, 255, 255, 255) // 12% white
            borderColor = Color.argb(50, 255, 255, 255)
            shadowColor = Color.argb(40, 0, 0, 0)
        }
        overlayPaint.color = overlayColor
        borderPaint.color = borderColor
        shadowPaint.color = shadowColor
    }

    /**
     * Set konfigurasi glass card.
     */
    fun setGlassConfig(
        cornerRadiusDp: Float = 24f,
        blurRadius: Float = 20f,
        enableBlur: Boolean = true,
        enableBorder: Boolean = true
    ) {
        this.cornerRadius = cornerRadiusDp * resources.displayMetrics.density
        this.blurRadius = blurRadius
        this.enableBlur = enableBlur && BlurHelper.supportsRealTimeBlur()
        this.enableBorder = enableBorder
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rect.set(0f, 0f, w.toFloat(), h.toFloat())
        clipPath.reset()
        clipPath.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)

        // Apply real-time blur jika didukung (API 31+)
        if (enableBlur && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0) {
            BlurHelper.applyBlur(this, blurRadius)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Background shadow (di luar rect untuk efek depth)
        // Tidak dipakai karena konflik dengan layer hardware - skip

        // 2. Glass overlay (semi-transparent)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, overlayPaint)

        // 3. Highlight border (efek cahaya di tepi)
        if (enableBorder) {
            // Border full rectangle
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

            // Highlight tambahan di tepi atas (light reflection)
            val topHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(80, 255, 255, 255)
                style = Paint.Style.STROKE
                strokeWidth = 1f * resources.displayMetrics.density
            }
            val topRect = RectF(
                cornerRadius / 2,
                1f * resources.displayMetrics.density,
                rect.width() - cornerRadius / 2,
                cornerRadius / 2 + 1f * resources.displayMetrics.density
            )
            canvas.drawArc(topRect, 200f, 140f, false, topHighlightPaint)
        }
    }
}
