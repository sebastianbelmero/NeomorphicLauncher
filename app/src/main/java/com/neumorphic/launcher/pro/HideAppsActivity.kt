package com.neumorphic.launcher.pro

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neumorphic.launcher.R
import com.neumorphic.launcher.model.AppInfo
import com.neumorphic.launcher.pro.feature.AppLockManager
import com.neumorphic.launcher.util.AppLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity untuk manage Hide/Lock apps.
 * Activity to manage Hide/Lock apps.
 *
 * Dua section:
 *  1. HIDE apps: checkbox untuk sembunyikan dari drawer
 *  2. LOCK apps: checkbox untuk require auth saat dibuka
 *
 * Two sections:
 *  1. HIDE apps: checkbox to hide from drawer
 *  2. LOCK apps: checkbox to require auth when opened
 */
class HideAppsActivity : AppCompatActivity() {

    private lateinit var appLockManager: AppLockManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hide_apps)

        appLockManager = AppLockManager(this)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        loadAppsList()
    }

    private fun loadAppsList() {
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                AppLoader.loadInstalledApps(this@HideAppsActivity)
            }
            renderHideAppsList(apps)
            renderLockAppsList(apps)
        }
    }

    private fun renderHideAppsList(apps: List<AppInfo>) {
        val recyclerView = findViewById<RecyclerView>(R.id.hideAppsRecyclerView)
        val hiddenApps = appLockManager.getHiddenApps()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = AppToggleAdapter(
            apps = apps,
            checkedPackages = hiddenApps,
            onToggle = { pkg, isChecked ->
                if (isChecked) appLockManager.hideApp(pkg)
                else appLockManager.unhideApp(pkg)
            }
        )
    }

    private fun renderLockAppsList(apps: List<AppInfo>) {
        val recyclerView = findViewById<RecyclerView>(R.id.lockAppsRecyclerView)
        val lockedApps = appLockManager.getLockedApps()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = AppToggleAdapter(
            apps = apps,
            checkedPackages = lockedApps,
            onToggle = { pkg, isChecked ->
                if (isChecked) appLockManager.lockApp(pkg)
                else appLockManager.unlockApp(pkg)
            }
        )
    }

    /**
     * Adapter untuk list app dengan toggle.
     * Adapter for app list with toggle.
     */
    private class AppToggleAdapter(
        val apps: List<AppInfo>,
        val checkedPackages: Set<String>,
        val onToggle: (packageName: String, isChecked: Boolean) -> Unit
    ) : RecyclerView.Adapter<AppToggleAdapter.VH>() {

        class VH(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_toggle, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val app = apps[position]
            holder.view.findViewById<android.widget.ImageView>(R.id.appIcon)
                .setImageDrawable(app.icon)
            holder.view.findViewById<TextView>(R.id.appName).text = app.label
            val cb = holder.view.findViewById<CheckBox>(R.id.appCheckbox)
            cb.setOnCheckedChangeListener(null) // clear listener
            cb.isChecked = app.packageName in checkedPackages
            cb.setOnCheckedChangeListener { _, isChecked ->
                onToggle(app.packageName, isChecked)
            }
            holder.view.setOnClickListener { cb.toggle() }
        }

        override fun getItemCount(): Int = apps.size
    }
}
