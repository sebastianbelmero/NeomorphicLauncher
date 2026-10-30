package com.neumorphic.launcher.pro.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.OvershootInterpolator

/**
 * Helper untuk animasi premium spring physics (60fps smooth).
 * Helper for premium spring physics animations (60fps smooth).
 *
 * Menggunakan ObjectAnimator dengan OvershootInterpolator untuk simulasi
 * efek spring (overshoot + settle) yang smooth.
 *
 * Using ObjectAnimator with OvershootInterpolator to simulate
 * spring effect (overshoot + settle) that feels smooth.
 */
object SpringAnimationHelper {

    /**
     * Animasi bounce-in (untuk app drawer open, dialog appear).
     * Bounce-in animation (for app drawer open, dialog appear).
     */
    fun bounceIn(view: View, durationMs: Long = 400) {
        view.alpha = 0f
        view.scaleX = 0.7f
        view.scaleY = 0.7f
        view.translationY = 50f * view.resources.displayMetrics.density

        val alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.7f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.7f, 1f)
        val translationY = ObjectAnimator.ofFloat(view, "translationY",
            50f * view.resources.displayMetrics.density, 0f)

        AnimatorSet().apply {
            playTogether(alpha, scaleX, scaleY, translationY)
            duration = durationMs
            interpolator = OvershootInterpolator(0.8f)
            start()
        }
    }

    /**
     * Animasi fade-out (untuk close dialog).
     * Fade-out animation (for closing dialog).
     */
    fun fadeOut(view: View, durationMs: Long = 250, onEnd: () -> Unit = {}) {
        val alpha = ObjectAnimator.ofFloat(view, "alpha", view.alpha, 0f)
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", view.scaleX, 0.9f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", view.scaleY, 0.9f)

        AnimatorSet().apply {
            playTogether(alpha, scaleX, scaleY)
            duration = durationMs
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onEnd()
                }
            })
            start()
        }
    }

    /**
     * Spring scale (untuk button press feedback).
     * Spring scale (for button press feedback).
     */
    fun springScale(view: View, targetScale: Float = 0.92f) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", view.scaleX, targetScale)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", view.scaleY, targetScale)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 150
            interpolator = OvershootInterpolator(2f)
            start()
        }
    }

    /**
     * Press feedback: scale down on press, spring back on release.
     * Press feedback: scale down on press, spring back on release.
     */
    fun setupPressFeedback(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    springScale(v, 0.92f)
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    springScale(v, 1f)
                }
            }
            false
        }
    }

    /**
     * Animasi slide-up smooth (untuk app drawer reveal).
     * Smooth slide-up animation (for app drawer reveal).
     */
    fun slideUp(view: View, durationMs: Long = 350) {
        val startY = view.height.toFloat() * 0.3f
        view.translationY = startY
        view.alpha = 0f

        val translationY = ObjectAnimator.ofFloat(view, "translationY", startY, 0f)
        val alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)

        AnimatorSet().apply {
            playTogether(translationY, alpha)
            duration = durationMs
            interpolator = OvershootInterpolator(0.4f)
            start()
        }
    }

    /**
     * Pulse animation (untuk notification badge, attention).
     * Pulse animation (for notification badge, attention).
     */
    fun pulse(view: View, durationMs: Long = 1000, repeat: Boolean = true) {
        val scaleDown = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f).apply {
            duration = durationMs / 2
            repeatMode = ValueAnimator.REVERSE
            repeatCount = if (repeat) ValueAnimator.INFINITE else 0
        }
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f).apply {
            duration = durationMs / 2
            repeatMode = ValueAnimator.REVERSE
            repeatCount = if (repeat) ValueAnimator.INFINITE else 0
        }
        scaleDown.start()
        scaleDownY.start()
    }
}
