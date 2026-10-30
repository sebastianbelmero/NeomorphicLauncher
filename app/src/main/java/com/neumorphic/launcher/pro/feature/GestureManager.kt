package com.neumorphic.launcher.pro.feature

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.GestureDetector
import android.view.MotionEvent

/**
 * Manager untuk custom gestures di launcher.
 * Manager for custom gestures in launcher.
 *
 * Mendukung:
 *  - Double tap → sleep/lock screen
 *  - Swipe up → app drawer
 *  - Swipe down → notification shade
 *  - Long press → settings
 *  - Shake → torch on/off
 *  - Draw letter → open specific app (delegated to LetterGestureOverlayView)
 *
 * Supported:
 *  - Double tap → sleep/lock screen
 *  - Swipe up → app drawer
 *  - Swipe down → notification shade
 *  - Long press → settings
 *  - Shake → torch on/off
 *  - Draw letter → open specific app (delegated to LetterGestureOverlayView)
 */
class GestureManager(private val context: Context) {

    private val vibrator: Vibrator? = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    private var torchEnabled = false
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null

    init {
        // Init camera manager for torch
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        cameraId = try {
            cameraManager?.cameraIdList?.firstOrNull { id ->
                cameraManager?.getCameraCharacteristics(id)
                    ?.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) { null }
    }

    /**
     * Vibrate feedback (saat gesture terdeteksi).
     * Vibrate feedback (when gesture detected).
     */
    fun vibrate(durationMs: Long = 30) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(durationMs)
        }
    }

    /**
     * Toggle flashlight (untuk gesture shake).
     * Toggle flashlight (for shake gesture).
     *
     * @return true jika berhasil toggle, false jika gagal (no flash)
     */
    fun toggleTorch(): Boolean {
        val cm = cameraManager ?: return false
        val id = cameraId ?: return false

        return try {
            torchEnabled = !torchEnabled
            cm.setTorchMode(id, torchEnabled)
            vibrate(50)
            torchEnabled
        } catch (e: Exception) {
            false
        }
    }

    fun isTorchEnabled(): Boolean = torchEnabled

    /**
     * Build GestureDetector untuk home screen dengan custom callback.
     * Build GestureDetector for home screen with custom callbacks.
     */
    fun buildHomeGestureDetector(callbacks: GestureCallbacks): GestureDetector {
        return GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                callbacks.onSingleTap()
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                vibrate(40)
                callbacks.onDoubleTap()
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                vibrate(30)
                callbacks.onLongPress()
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val dy = e1.y - e2.y
                val dx = e1.x - e2.x

                // Swipe vertikal dominan
                if (Math.abs(dy) > Math.abs(dx) * 1.5 && Math.abs(dy) > 100) {
                    if (dy > 0) {
                        vibrate(20)
                        callbacks.onSwipeUp()
                        return true
                    } else {
                        vibrate(20)
                        callbacks.onSwipeDown()
                        return true
                    }
                }
                // Swipe horizontal
                if (Math.abs(dx) > Math.abs(dy) * 1.5 && Math.abs(dx) > 100) {
                    if (dx > 0) {
                        callbacks.onSwipeRight()
                    } else {
                        callbacks.onSwipeLeft()
                    }
                    return true
                }
                return false
            }
        })
    }

    /**
     * Callback interface untuk gestures.
     */
    interface GestureCallbacks {
        fun onSingleTap() {}
        fun onDoubleTap() {}
        fun onLongPress() {}
        fun onSwipeUp() {}
        fun onSwipeDown() {}
        fun onSwipeLeft() {}
        fun onSwipeRight() {}
    }
}
