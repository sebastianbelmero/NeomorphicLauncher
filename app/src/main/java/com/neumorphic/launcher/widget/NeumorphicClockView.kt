package com.neumorphic.launcher.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Custom View widget Jam & Tanggal dengan gaya Neumorphic.
 * Custom Clock & Date widget with Neumorphic style.
 *
 * Menampilkan waktu besar di tengah dengan tanggal di bawahnya,
 * diperbarui setiap detik menggunakan handler.
 *
 * Displays large time in center with date below,
 * updated every second using a handler.
 */
class NeumorphicClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Warna & style
    private var timeColor: Int = Color.parseColor("#2D3047")
    private var dateColor: Int = Color.parseColor("#6C73A8")
    private var timeSize: Float = 88f * resources.displayMetrics.density
    private var dateSize: Float = 18f * resources.displayMetrics.density

    // Paint objects
    private val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = timeColor
        textSize = this@NeumorphicClockView.timeSize
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    private val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = dateColor
        textSize = this@NeumorphicClockView.dateSize
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }

    // Update handler
    private val updateTimeTask = object : Runnable {
        override fun run() {
            invalidate()
            postDelayed(this, 1000)
        }
    }

    private var timeFormat: SimpleDateFormat =
        SimpleDateFormat("HH:mm", Locale.getDefault())
    private var dateFormat: SimpleDateFormat =
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale(if (Locale.getDefault().language == "id") "id" else "en"))

    init {
        // Pakai format 24/12 jam sesuai sistem
        if (!DateFormat.is24HourFormat(context)) {
            timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(updateTimeTask)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(updateTimeTask)
    }

    /**
     * Set warna teks jam — Set clock text color.
     */
    fun setColors(timeColor: Int, dateColor: Int) {
        this.timeColor = timeColor
        this.dateColor = dateColor
        timePaint.color = timeColor
        datePaint.color = dateColor
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val now = Calendar.getInstance()
        val timeStr = timeFormat.format(now.time)
        val dateStr = dateFormat.format(now.time)

        val centerX = width / 2f
        val centerY = height / 2f

        // Gambar waktu di atas center, tanggal di bawah
        val timeY = centerY + dateSize
        val dateY = centerY + dateSize * 2.5f + timeSize / 4

        canvas.drawText(timeStr, centerX, timeY, timePaint)
        canvas.drawText(dateStr, centerX, dateY, datePaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val desiredHeight = (timeSize + dateSize * 3).toInt()
        setMeasuredDimension(measuredWidth, desiredHeight)
    }
}
