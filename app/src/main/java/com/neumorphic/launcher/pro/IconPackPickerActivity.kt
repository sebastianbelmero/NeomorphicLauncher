package com.neumorphic.launcher.pro

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neumorphic.launcher.R
import com.neumorphic.launcher.pro.feature.IconPackInfo
import com.neumorphic.launcher.pro.feature.IconPackParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity untuk pilih icon pack yang akan dipakai.
 * Activity to select icon pack to use.
 *
 * List semua icon pack terinstall di device, plus option "Default" (no icon pack).
 * Lists all icon packs installed on device, plus "Default" option (no icon pack).
 */
class IconPackPickerActivity : AppCompatActivity() {

    private lateinit var iconPackParser: IconPackParser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_icon_pack_picker)

        iconPackParser = IconPackParser(this)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        loadIconPacks()
    }

    private fun loadIconPacks() {
        lifecycleScope.launch {
            val packs = withContext(Dispatchers.IO) {
                iconPackParser.listInstalledIconPacks()
            }
            renderIconPacks(packs)
        }
    }

    private fun renderIconPacks(packs: List<IconPackInfo>) {
        val container = findViewById<ViewGroup>(R.id.iconPackListContainer)
        container.removeAllViews()

        // Option: Default (no icon pack)
        val defaultView = layoutInflater.inflate(R.layout.item_icon_pack, container, false)
        defaultView.findViewById<TextView>(R.id.packName).text = getString(R.string.icon_pack_default)
        defaultView.findViewById<TextView>(R.id.packDescription).text = "Gunakan ikon asli aplikasi"
        val defaultRadio = defaultView.findViewById<RadioButton>(R.id.packRadio)
        defaultRadio.isChecked = getActiveIconPack().isNullOrBlank()
        defaultView.setOnClickListener {
            setActiveIconPack(null)
            finish()
        }
        container.addView(defaultView)

        // List installed icon packs
        for (pack in packs) {
            val view = layoutInflater.inflate(R.layout.item_icon_pack, container, false)
            view.findViewById<TextView>(R.id.packName).text = pack.label
            view.findViewById<TextView>(R.id.packDescription).text = pack.packageName
            val radio = view.findViewById<RadioButton>(R.id.packRadio)
            radio.isChecked = pack.packageName == getActiveIconPack()
            view.setOnClickListener {
                setActiveIconPack(pack.packageName)
                finish()
            }
            container.addView(view)
        }

        if (packs.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "Belum ada icon pack terinstall.\nInstall dari Play Store (cari: icon pack)."
                setPadding(32, 32, 32, 32)
                textSize = 14f
            }
            container.addView(emptyView)
        }
    }

    private fun getActiveIconPack(): String? {
        return getSharedPreferences("icon_pack_prefs", MODE_PRIVATE)
            .getString("active_pack", null)
    }

    private fun setActiveIconPack(packageName: String?) {
        getSharedPreferences("icon_pack_prefs", MODE_PRIVATE)
            .edit()
            .putString("active_pack", packageName)
            .apply()
    }
}
