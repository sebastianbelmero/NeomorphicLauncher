package com.neumorphic.launcher.pro

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import com.neumorphic.launcher.R
import com.neumorphic.launcher.pro.feature.AppLockManager

/**
 * Activity untuk authentication saat membuka app yang di-LOCK.
 * Activity for authentication when opening LOCKED app.
 *
 * Flow:
 *  1. User tap app yang di-lock di drawer
 *  2. AppDrawerActivity start AppLockActivity dengan extra: target packageName
 *  3. AppLockActivity tampilkan biometric prompt (atau PIN fallback)
 *  4. Jika auth sukses → launch app target
 *  5. Jika gagal → tutup activity, kembali ke drawer
 *
 * Flow:
 *  1. User taps locked app in drawer
 *  2. AppDrawerActivity starts AppLockActivity with extra: target packageName
 *  3. AppLockActivity shows biometric prompt (or PIN fallback)
 *  4. If auth success → launch target app
 *  5. If fail → close activity, return to drawer
 */
class AppLockActivity : AppCompatActivity() {

    private lateinit var appLockManager: AppLockManager
    private var targetPackage: String? = null
    private var targetAppName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_lock)

        appLockManager = AppLockManager(this)
        targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE)
        targetAppName = intent.getStringExtra(EXTRA_TARGET_APP_NAME) ?: "Aplikasi"

        val subtitleView = findViewById<TextView>(R.id.lockSubtitle)
        subtitleView.text = getString(R.string.app_lock_subtitle, targetAppName)

        val pinSection = findViewById<View>(R.id.pinSection)
        val pinInput = findViewById<EditText>(R.id.pinInput)
        val pinConfirmButton = findViewById<Button>(R.id.pinConfirmButton)

        // Otomatis tampilkan biometric jika tersedia
        if (appLockManager.isBiometricAvailable(this) && !appLockManager.hasPinSet().not()) {
            // Jika biometric available, langsung tampilkan prompt
            showBiometricPrompt()
        } else if (appLockManager.hasPinSet()) {
            // Tampilkan PIN input
            pinSection.visibility = View.VISIBLE
        } else {
            // Tidak ada auth method, langsung buka app
            launchTargetApp()
            return
        }

        pinConfirmButton.setOnClickListener {
            val pin = pinInput.text.toString()
            if (appLockManager.verifyPin(pin)) {
                launchTargetApp()
            } else {
                Toast.makeText(this, R.string.app_lock_failed, Toast.LENGTH_SHORT).show()
                pinInput.text?.clear()
            }
        }
    }

    private fun showBiometricPrompt() {
        appLockManager.showBiometricPrompt(
            activity = this,
            appName = targetAppName,
            onSuccess = { launchTargetApp() },
            onFailure = {
                Toast.makeText(this, R.string.app_lock_failed, Toast.LENGTH_SHORT).show()
                finish()
            },
            onUsePin = {
                // Tampilkan PIN section
                findViewById<View>(R.id.pinSection).visibility = View.VISIBLE
                if (!appLockManager.hasPinSet()) {
                    // Jika belum ada PIN, kasih tau user untuk set PIN dulu
                    Toast.makeText(this, "Set PIN di Pengaturan dulu", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        )
    }

    private fun launchTargetApp() {
        targetPackage?.let { pkg ->
            packageManager.getLaunchIntentForPackage(pkg)?.let { intent ->
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        finish()
    }

    companion object {
        const val EXTRA_TARGET_PACKAGE = "extra_target_package"
        const val EXTRA_TARGET_APP_NAME = "extra_target_app_name"
    }
}
