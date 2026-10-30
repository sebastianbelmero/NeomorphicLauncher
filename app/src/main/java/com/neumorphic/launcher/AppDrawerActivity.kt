package com.neumorphic.launcher

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.neumorphic.launcher.adapter.AppAdapter
import com.neumorphic.launcher.databinding.ActivityAppDrawerBinding
import com.neumorphic.launcher.model.AppInfo
import com.neumorphic.launcher.util.AppLoader
import com.neumorphic.launcher.util.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * AppDrawerActivity — Daftar semua aplikasi terinstal dengan pencarian.
 * AppDrawerActivity — List of all installed apps with search.
 *
 * Fitur:
 *  - Search bar di atas untuk mencari aplikasi berdasarkan nama
 *  - Grid 4 kolom aplikasi
 *  - Tap aplikasi untuk membuka
 *  - Long-press untuk tambahkan ke dock / favorit
 *  - Swipe down (atau tombol back) untuk menutup drawer
 *
 * Features:
 *  - Top search bar to search apps by name
 *  - 4-column app grid
 *  - Tap app to open
 *  - Long-press to add to dock / favorites
 *  - Swipe down (or back button) to close drawer
 */
class AppDrawerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppDrawerBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var appAdapter: AppAdapter

    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceManager = PreferenceManager(this)
        binding = ActivityAppDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchBar()
        setupCloseButton()

        loadApps()

        // Jika dibuka dengan flag search, langsung fokus ke search bar
        if (intent.getBooleanExtra(EXTRA_OPEN_SEARCH, false)) {
            binding.searchEditText.requestFocus()
        }
    }

    /**
     * Setup RecyclerView dengan grid 4 kolom.
     * Setup RecyclerView with 4-column grid.
     */
    private fun setupRecyclerView() {
        appAdapter = AppAdapter(
            onItemClick = { app ->
                try {
                    app.launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(app.launchIntent)
                    finish() // Tutup drawer setelah aplikasi dibuka
                } catch (e: Exception) {
                    Toast.makeText(this, "Tidak dapat membuka ${app.label}", Toast.LENGTH_SHORT).show()
                }
            },
            onItemLongClick = { app ->
                addToDock(app)
            }
        )

        binding.appRecyclerView.apply {
            layoutManager = GridLayoutManager(this@AppDrawerActivity, 4)
            adapter = appAdapter
            setHasFixedSize(false)
        }
    }

    /**
     * Setup search bar dengan text watcher untuk filter real-time.
     * Setup search bar with text watcher for real-time filtering.
     */
    private fun setupSearchBar() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterApps(s?.toString().orEmpty())
            }
        })
    }

    /**
     * Setup tombol close drawer.
     * Setup close drawer button.
     */
    private fun setupCloseButton() {
        binding.closeButton.setOnClickListener {
            finish()
        }
    }

    /**
     * Memuat daftar aplikasi secara asynchronous.
     * Load list of apps asynchronously.
     */
    private fun loadApps() {
        lifecycleScope.launch {
            binding.loadingProgressBar.visibility = View.VISIBLE
            allApps = withContext(Dispatchers.IO) {
                AppLoader.loadInstalledApps(this@AppDrawerActivity)
            }
            binding.loadingProgressBar.visibility = View.GONE
            appAdapter.submitList(allApps)
        }
    }

    /**
     * Memfilter aplikasi berdasarkan query pencarian (case-insensitive).
     * Filter apps based on search query (case-insensitive).
     */
    private fun filterApps(query: String) {
        if (query.isBlank()) {
            appAdapter.submitList(allApps)
            return
        }
        val filtered = allApps.filter {
            it.label.contains(query, ignoreCase = true)
        }
        appAdapter.submitList(filtered)

        // Tampilkan empty state jika tidak ada hasil
        binding.emptyStateText.visibility =
            if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    /**
     * Menambahkan aplikasi ke dock (menggantikan yang terakhir jika sudah penuh).
     * Add app to dock (replaces last one if dock is full).
     */
    private fun addToDock(app: AppInfo) {
        val dockApps = preferenceManager.getDockApps().toMutableList()

        // Cek apakah sudah ada — Check if already exists
        if (dockApps.contains(app.packageName)) {
            Toast.makeText(this, "${app.label} sudah ada di dock", Toast.LENGTH_SHORT).show()
            return
        }

        // Maksimal 5 aplikasi di dock — Max 5 apps in dock
        if (dockApps.size >= 5) {
            dockApps.removeAt(dockApps.lastIndex)
        }
        dockApps.add(app.packageName)
        preferenceManager.setDockApps(dockApps)

        Toast.makeText(
            this,
            "${app.label} ditambahkan ke dock",
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        const val EXTRA_OPEN_SEARCH = "extra_open_search"
    }
}
