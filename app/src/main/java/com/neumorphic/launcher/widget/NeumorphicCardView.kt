package com.neumorphic.launcher.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.neumorphic.launcher.util.NeumorphicHelper

/**
 * Custom View dengan efek Neumorphic convex (timbul).
 * Custom View with convex (raised) Neumorphic effect.
 *
 * Menggambar dual shadow (light + dark) untuk menciptakan ilusi 3D halus,
 * sesuai prinsip Neumorphism / Soft UI.
 *
 * Draws dual shadow (light + dark) to create a soft 3D illusion,
 * following Neumorphism / Soft UI principles.
 */
class NeumorphicCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Konfigurasi visual — Visual configuration
    private var bgColor: Int = 0xFFE0E5EC.toInt()
    private var cornerRadius: Float = 48f // px
    private var shadowIntensity: Float = 0.7f
    private var inset: Boolean = false // true = tenggelam (inset), false = timbul (convex)

    // Paint objects
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val lightShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val darkShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    init {
        // Enable hardware acceleration
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        updatePaints()
    }

    /**
     * Update konfigurasi visual card.
     * Update card visual configuration.
     */
    fun setStyle(
        backgroundColor: Int,
        cornerRadiusDp: Float,
        shadowIntensity: Float,
        inset: Boolean = false
    ) {
        this.bgColor = backgroundColor
        this.cornerRadius = cornerRadiusDp * resources.displayMetrics.density
        this.shadowIntensity = shadowIntensity.coerceIn(0f, 1f)
        this.inset = inset
        updatePaints()
        invalidate()
    }

    private fun updatePaints() {
        val lightColor = NeumorphicHelper.getLightShadowColor(bgColor)
        val darkColor = NeumorphicHelper.getDarkShadowColor(bgColor)
        val shadowPx = (20f * shadowIntensity * resources.displayMetrics.density)

        bgPaint.color = bgColor

        // Light shadow (top-left)
        lightShadowPaint.color = Color.TRANSPARENT
        lightShadowPaint.setShadowLayer(shadowPx, -shadowPx / 2, -shadowPx / 2, lightColor)

        // Dark shadow (bottom-right)
        darkShadowPaint.color = Color.TRANSPARENT
        darkShadowPaint.setShadowLayer(shadowPx, shadowPx / 2, shadowPx / 2, darkColor)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rect.set(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (w - paddingRight).toFloat(),
            (h - paddingBottom).toFloat()
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (inset) {
            // Inset: hanya satu shadow (dark di top-left, light di bottom-right)
            val lightColor = NeumorphicHelper.getLightShadowColor(bgColor)
            val darkColor = NeumorphicHelper.getDarkShadowColor(bgColor)
            val shadowPx = 12f * resources.displayMetrics.density

            bgPaint.setShadowLayer(shadowPx, shadowPx / 2, shadowPx / 2, lightColor)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

            bgPaint.setShadowLayer(shadowPx, -shadowPx / 2, -shadowPx / 2, darkColor)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

            bgPaint.clearShadowLayer()
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)
        } else {
            // Convex: dual shadow untuk efek timbul
            // Pertama: light shadow
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, lightShadowPaint)
            // Kedua: dark shadow
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, darkShadowPaint)
            // Ketiga: background solid di atas shadow
            bgPaint.clearShadowLayer()
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)
        }
    }
}
