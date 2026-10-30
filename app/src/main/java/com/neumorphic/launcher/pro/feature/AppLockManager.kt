package com.neumorphic.launcher.pro.feature

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manager untuk Hide/Lock apps (fingerprint/PIN protected).
 * Manager for Hide/Lock apps (fingerprint/PIN protected).
 *
 * Fitur:
 *  - HIDE: Sembunyikan app dari App Drawer (tidak muncul di list)
 *  - LOCK: App muncul di drawer, tapi saat dibuka minta fingerprint/PIN dulu
 *
 * Features:
 *  - HIDE: Hide app from App Drawer (not shown in list)
 *  - LOCK: App shows in drawer, but requires fingerprint/PIN to open
 */
class AppLockManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Dapatkan daftar packageName yang di-HIDE (sembunyikan dari drawer).
     * Get list of packageNames to HIDE (hidden from drawer).
     */
    fun getHiddenApps(): Set<String> {
        val raw = prefs.getString(KEY_HIDDEN_APPS, null) ?: return emptySet()
        return try {
            gson.fromJson(raw, object : TypeToken<Set<String>>() {}.type) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    fun isHidden(packageName: String): Boolean = packageName in getHiddenApps()

    fun hideApp(packageName: String) {
        val current = getHiddenApps().toMutableSet()
        current.add(packageName)
        prefs.edit().putString(KEY_HIDDEN_APPS, gson.toJson(current)).apply()
    }

    fun unhideApp(packageName: String) {
        val current = getHiddenApps().toMutableSet()
        current.remove(packageName)
        prefs.edit().putString(KEY_HIDDEN_APPS, gson.toJson(current)).apply()
    }

    /**
     * Dapatkan daftar packageName yang di-LOCK (butuh auth untuk buka).
     * Get list of packageNames that are LOCKED (need auth to open).
     */
    fun getLockedApps(): Set<String> {
        val raw = prefs.getString(KEY_LOCKED_APPS, null) ?: return emptySet()
        return try {
            gson.fromJson(raw, object : TypeToken<Set<String>>() {}.type) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    fun isLocked(packageName: String): Boolean = packageName in getLockedApps()

    fun lockApp(packageName: String) {
        val current = getLockedApps().toMutableSet()
        current.add(packageName)
        prefs.edit().putString(KEY_LOCKED_APPS, gson.toJson(current)).apply()
    }

    fun unlockApp(packageName: String) {
        val current = getLockedApps().toMutableSet()
        current.remove(packageName)
        prefs.edit().putString(KEY_LOCKED_APPS, gson.toJson(current)).apply()
    }

    /**
     * Set PIN untuk fallback authentication (jika biometric tidak tersedia).
     * Set PIN for fallback authentication (if biometric not available).
     *
     * PIN disimpan sebagai hash SHA-256 (bukan plain text).
     */
    fun setPin(pin: String) {
        val hash = hashPin(pin)
        prefs.edit().putString(KEY_PIN_HASH, hash).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return hashPin(pin) == storedHash
    }

    fun hasPinSet(): Boolean = prefs.getString(KEY_PIN_HASH, null) != null

    private fun hashPin(pin: String): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Cek apakah device support biometric authentication.
     * Check if device supports biometric authentication.
     */
    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Tampilkan biometric prompt untuk unlock app.
     * Show biometric prompt to unlock app.
     *
     * @param activity FragmentActivity (host)
     * @param appName Nama app yang akan dibuka (untuk subtitle)
     * @param onSuccess Callback ketika auth berhasil
     * @param onFailure Callback ketika auth gagal
     * @param onUsePin Callback ketika user pilih PIN fallback
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        appName: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        onUsePin: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(com.neumorphic.launcher.R.string.app_lock_title))
            .setSubtitle(activity.getString(com.neumorphic.launcher.R.string.app_lock_subtitle, appName))
            .setNegativeButtonText(activity.getString(com.neumorphic.launcher.R.string.app_lock_use_pin))
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onUsePin()
                } else {
                    onFailure()
                }
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        prompt.authenticate(promptInfo)
    }

    companion object {
        private const val PREFS_NAME = "app_lock_prefs"
        private const val KEY_HIDDEN_APPS = "hidden_apps"
        private const val KEY_LOCKED_APPS = "locked_apps"
        private const val KEY_PIN_HASH = "pin_hash"
    }
}
