package com.neumorphic.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.neumorphic.launcher.databinding.ActivitySettingsBinding
import com.neumorphic.launcher.model.ThemeConfig
import com.neumorphic.launcher.model.ThemeMode
import com.neumorphic.launcher.util.PreferenceManager

/**
 * SettingsActivity — Pengaturan tema & launcher.
 * SettingsActivity — Theme & launcher settings.
 *
 * Pengaturan tersedia:
 *  - Mode tema: Light / Dark / Auto (mengikuti sistem)
 *  - Warna aksen: pilihan preset (purple, blue, pink, green, orange)
 *  - Warna latar belakang: light gray / dark gray
 *  - Radius sudut kartu (slider 12dp - 32dp)
 *  - Intensitas bayangan neumorphic (slider 0% - 100%)
 *
 * Settings available:
 *  - Theme mode: Light / Dark / Auto (follow system)
 *  - Accent color: preset choices (purple, blue, pink, green, orange)
 *  - Background color: light gray / dark gray
 *  - Card corner radius (slider 12dp - 32dp)
 *  - Neumorphic shadow intensity (slider 0% - 100%)
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferenceManager: PreferenceManager
    private var currentConfig: ThemeConfig = ThemeConfig()

    // Preset warna aksen — Preset accent colors
    private val accentColorOptions = listOf(
        Triple("Ungu", 0xFF6C63FF.toInt(), 0xFF6C63FF.toInt()),
        Triple("Biru", 0xFF4A90E2.toInt(), 0xFF4A90E2.toInt()),
        Triple("Pink", 0xFFE91E63.toInt(), 0xFFE91E63.toInt()),
        Triple("Hijau", 0xFF4CAF50.toInt(), 0xFF4CAF50.toInt()),
        Triple("Oranye", 0xFFFF9800.toInt(), 0xFFFF9800.toInt())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceManager = PreferenceManager(this)
        currentConfig = preferenceManager.loadThemeConfig()

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupThemeModeOptions()
        setupAccentColorOptions()
        setupCornerRadiusSlider()
        setupShadowIntensitySlider()
        setupSaveButton()
        setupBackButton()
        setupProFeaturesSection()

        // Apply konfigurasi saat ini ke UI — Apply current config to UI
        applyCurrentConfigToUI()
    }

    /**
     * Setup section Pro Features (v2.0.0).
     * Setup Pro Features section (v2.0.0).
     */
    private fun setupProFeaturesSection() {
        val proPrefs = getSharedPreferences("pro_features", MODE_PRIVATE)

        // Smart Folder toggle
        binding.smartFolderSwitch.isChecked = proPrefs.getBoolean("smart_folder", true)
        binding.smartFolderSwitch.setOnCheckedChangeListener { _, isChecked ->
            proPrefs.edit().putBoolean("smart_folder", isChecked).apply()
        }

        // Notification Badge toggle
        binding.notifBadgeSwitch.isChecked = proPrefs.getBoolean("notif_badge", true)
        binding.notifBadgeSwitch.setOnCheckedChangeListener { _, isChecked ->
            proPrefs.edit().putBoolean("notif_badge", isChecked).apply()
            if (isChecked) {
                // Redirect ke notification access settings jika belum di-grant
                val enabledListeners = android.provider.Settings.Secure.getString(
                    contentResolver,
                    "enabled_notification_listeners"
                )
                if (enabledListeners == null || !enabledListeners.contains(packageName)) {
                    android.widget.Toast.makeText(
                        this,
                        getString(R.string.toast_need_permission, "Notification Access"),
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                }
            }
        }

        // Smart Suggestions toggle
        binding.smartSuggestionsSwitch.isChecked = proPrefs.getBoolean("smart_suggestions", true)
        binding.smartSuggestionsSwitch.setOnCheckedChangeListener { _, isChecked ->
            proPrefs.edit().putBoolean("smart_suggestions", isChecked).apply()
        }

        // At-a-Glance toggle
        binding.atAGlanceSwitch.isChecked = proPrefs.getBoolean("at_a_glance", true)
        binding.atAGlanceSwitch.setOnCheckedChangeListener { _, isChecked ->
            proPrefs.edit().putBoolean("at_a_glance", isChecked).apply()
        }

        // Gesture Settings row click
        binding.gestureSettingsRow.setOnClickListener {
            startActivity(Intent(this, com.neumorphic.launcher.pro.GestureSettingsActivity::class.java))
        }

        // Icon Pack picker row click
        binding.iconPackRow.setOnClickListener {
            startActivity(Intent(this, com.neumorphic.launcher.pro.IconPackPickerActivity::class.java))
        }

        // Hide/Lock Apps row click
        binding.hideAppsRow.setOnClickListener {
            startActivity(Intent(this, com.neumorphic.launcher.pro.HideAppsActivity::class.java))
        }
    }

    /**
     * Setup radio buttons untuk mode tema (Light/Dark/Auto).
     * Setup radio buttons for theme mode (Light/Dark/Auto).
     */
    private fun setupThemeModeOptions() {
        binding.themeModeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            currentConfig = when (checkedId) {
                R.id.radio_light -> currentConfig.copy(mode = ThemeMode.LIGHT)
                R.id.radio_dark -> currentConfig.copy(
                    mode = ThemeMode.DARK,
                    backgroundColor = 0xFF2A2D3A.toInt()
                )
                R.id.radio_auto -> currentConfig.copy(mode = ThemeMode.AUTO)
                else -> currentConfig
            }
            updateBackgroundColorButtons()
        }
    }

    /**
     * Setup tombol pilihan warna aksen.
     * Setup accent color choice buttons.
     */
    private fun setupAccentColorOptions() {
        binding.colorPurple.setOnClickListener { selectAccentColor(0) }
        binding.colorBlue.setOnClickListener { selectAccentColor(1) }
        binding.colorPink.setOnClickListener { selectAccentColor(2) }
        binding.colorGreen.setOnClickListener { selectAccentColor(3) }
        binding.colorOrange.setOnClickListener { selectAccentColor(4) }
    }

    private fun selectAccentColor(index: Int) {
        val (_, color, _) = accentColorOptions[index]
        currentConfig = currentConfig.copy(accentColor = color)

        // Update visual selection
        binding.colorPurple.alpha = if (index == 0) 1f else 0.5f
        binding.colorBlue.alpha = if (index == 1) 1f else 0.5f
        binding.colorPink.alpha = if (index == 2) 1f else 0.5f
        binding.colorGreen.alpha = if (index == 3) 1f else 0.5f
        binding.colorOrange.alpha = if (index == 4) 1f else 0.5f
    }

    /**
     * Setup slider untuk corner radius kartu.
     * Setup slider for card corner radius.
     */
    private fun setupCornerRadiusSlider() {
        binding.cornerRadiusSlider.apply {
            valueFrom = 12f
            valueTo = 32f
            stepSize = 2f
            value = currentConfig.cornerRadius
            addOnChangeListener { _, value, _ ->
                currentConfig = currentConfig.copy(cornerRadius = value)
                binding.cornerRadiusValueText.text = "${value.toInt()} dp"
            }
        }
    }

    /**
     * Setup slider untuk intensitas bayangan.
     * Setup slider for shadow intensity.
     */
    private fun setupShadowIntensitySlider() {
        binding.shadowIntensitySlider.apply {
            valueFrom = 0f
            valueTo = 1f
            value = currentConfig.shadowIntensity
            addOnChangeListener { _, value, _ ->
                currentConfig = currentConfig.copy(shadowIntensity = value)
                binding.shadowIntensityValueText.text = "${(value * 100).toInt()}%"
            }
        }
    }

    /**
     * Setup tombol Simpan — Setup Save button.
     */
    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            preferenceManager.saveThemeConfig(currentConfig)

            // Apply mode ke AppCompat delegate
            val nightMode = when (currentConfig.mode) {
                ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                ThemeMode.AUTO -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(nightMode)

            Toast.makeText(this, "Tema disimpan. Restart launcher untuk melihat perubahan.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    /**
     * Apply konfigurasi saat ini ke elemen UI.
     * Apply current configuration to UI elements.
     */
    private fun applyCurrentConfigToUI() {
        // Theme mode radio
        when (currentConfig.mode) {
            ThemeMode.LIGHT -> binding.themeModeRadioGroup.check(R.id.radio_light)
            ThemeMode.DARK -> binding.themeModeRadioGroup.check(R.id.radio_dark)
            ThemeMode.AUTO -> binding.themeModeRadioGroup.check(R.id.radio_auto)
        }

        // Accent color (cari yang paling dekat)
        val accentIndex = accentColorOptions.indexOfFirst { it.second == currentConfig.accentColor }
        if (accentIndex >= 0) selectAccentColor(accentIndex)

        // Slider values
        binding.cornerRadiusSlider.value = currentConfig.cornerRadius
        binding.cornerRadiusValueText.text = "${currentConfig.cornerRadius.toInt()} dp"
        binding.shadowIntensitySlider.value = currentConfig.shadowIntensity
        binding.shadowIntensityValueText.text = "${(currentConfig.shadowIntensity * 100).toInt()}%"
    }

    private fun updateBackgroundColorButtons() {
        // Toggle visibility based on theme mode
        val showBgColorPicker = currentConfig.mode != ThemeMode.DARK
        binding.backgroundColorSection.visibility =
            if (showBgColorPicker) View.VISIBLE else View.GONE
    }
}
