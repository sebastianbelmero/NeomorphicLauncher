package com.neumorphic.launcher.pro

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.neumorphic.launcher.R
import com.neumorphic.launcher.pro.feature.GestureManager

/**
 * Activity untuk konfigurasi custom gestures.
 * Activity for configuring custom gestures.
 *
 * User bisa enable/disable gesture dan assign action ke gesture tertentu.
 * User can enable/disable gestures and assign actions to specific gestures.
 */
class GestureSettingsActivity : AppCompatActivity() {

    private lateinit var gestureManager: GestureManager

    private val prefs by lazy {
        getSharedPreferences("gesture_prefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gesture_settings)

        gestureManager = GestureManager(this)

        findViewById<android.widget.ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        setupGestureToggles()
    }

    private fun setupGestureToggles() {
        // Double tap → sleep
        val doubleTapSwitch = findViewById<Switch>(R.id.gestureDoubleTapSwitch)
        doubleTapSwitch.isChecked = prefs.getBoolean("double_tap_enabled", false)
        doubleTapSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("double_tap_enabled", isChecked).apply()
            if (isChecked) gestureManager.vibrate(30)
        }

        // Swipe up → app drawer
        val swipeUpSwitch = findViewById<Switch>(R.id.gestureSwipeUpSwitch)
        swipeUpSwitch.isChecked = prefs.getBoolean("swipe_up_enabled", true)
        swipeUpSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("swipe_up_enabled", isChecked).apply()
        }

        // Swipe down → notification
        val swipeDownSwitch = findViewById<Switch>(R.id.gestureSwipeDownSwitch)
        swipeDownSwitch.isChecked = prefs.getBoolean("swipe_down_enabled", true)
        swipeDownSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("swipe_down_enabled", isChecked).apply()
        }

        // Long press → settings
        val longPressSwitch = findViewById<Switch>(R.id.gestureLongPressSwitch)
        longPressSwitch.isChecked = prefs.getBoolean("long_press_enabled", true)
        longPressSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("long_press_enabled", isChecked).apply()
        }

        // Shake → torch
        val shakeSwitch = findViewById<Switch>(R.id.gestureShakeSwitch)
        shakeSwitch.isChecked = prefs.getBoolean("shake_enabled", false)
        shakeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("shake_enabled", isChecked).apply()
            if (isChecked) {
                // Test torch
                if (!gestureManager.toggleTorch()) {
                    Toast.makeText(this, "Senter tidak tersedia", Toast.LENGTH_SHORT).show()
                    shakeSwitch.isChecked = false
                    prefs.edit().putBoolean("shake_enabled", false).apply()
                } else {
                    gestureManager.toggleTorch() // turn off after test
                }
            }
        }
    }

    companion object {
        // Keys untuk gesture prefs (dipakai oleh MainActivity)
        const val PREFS_NAME = "gesture_prefs"
        const val KEY_DOUBLE_TAP = "double_tap_enabled"
        const val KEY_SWIPE_UP = "swipe_up_enabled"
        const val KEY_SWIPE_DOWN = "swipe_down_enabled"
        const val KEY_LONG_PRESS = "long_press_enabled"
        const val KEY_SHAKE = "shake_enabled"
    }
}
