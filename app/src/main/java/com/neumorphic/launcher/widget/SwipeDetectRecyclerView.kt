package com.neumorphic.launcher.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView yang dapat mendeteksi swipe gesture global.
 * RecyclerView that can detect global swipe gestures.
 *
 * Mendeteksi swipe UP untuk membuka app drawer dan swipe DOWN untuk notifikasi.
 * Detects swipe UP to open app drawer and swipe DOWN for notifications.
 */
class SwipeDetectRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var onSwipeUp: (() -> Unit)? = null
    private var onSwipeDown: (() -> Unit)? = null

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var startY = 0f
    private var startX = 0f
    private var isSwiping = false

    fun setSwipeListeners(
        onSwipeUp: (() -> Unit)? = null,
        onSwipeDown: (() -> Unit)? = null
    ) {
        this.onSwipeUp = onSwipeUp
        this.onSwipeDown = onSwipeDown
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startY = e.rawY
                startX = e.rawX
                isSwiping = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isSwiping) {
                    val dy = startY - e.rawY
                    val dx = Math.abs(startX - e.rawX)
                    // Hanya trigger swipe jika vertikal dominan
                    if (Math.abs(dy) > touchSlop && Math.abs(dy) > dx * 2) {
                        isSwiping = true
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isSwiping) {
                    val dy = startY - e.rawY
                    if (dy > touchSlop * 2) {
                        // Swipe UP
                        onSwipeUp?.invoke()
                        return true
                    } else if (dy < -touchSlop * 2) {
                        // Swipe DOWN
                        onSwipeDown?.invoke()
                        return true
                    }
                }
                isSwiping = false
            }
        }
        return super.onInterceptTouchEvent(e)
    }
}
