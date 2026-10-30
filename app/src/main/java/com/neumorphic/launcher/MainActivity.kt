package com.neumorphic.launcher

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.neumorphic.launcher.adapter.DockAdapter
import com.neumorphic.launcher.adapter.HomePagerAdapter
import com.neumorphic.launcher.databinding.ActivityMainBinding
import com.neumorphic.launcher.model.AppInfo
import com.neumorphic.launcher.model.ThemeConfig
import com.neumorphic.launcher.util.AppLoader
import com.neumorphic.launcher.util.GestureHelper
import com.neumorphic.launcher.util.PreferenceManager
import com.neumorphic.launcher.util.NeumorphicHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

/**
 * MainActivity = Home Screen utama launcher.
 * MainActivity = Main home screen of the launcher.
 *
 * Berisi:
 *  - ViewPager2 multi-page untuk home pages (swipe kiri/kanan)
 *  - NeumorphicClockView (jam & tanggal)
 *  - Search bar (tap untuk membuka app drawer dengan search aktif)
 *  - Dock bawah dengan 4 aplikasi favorit
 *  - Swipe UP → App Drawer, Swipe DOWN → Notification shade
 *
 * Contains:
 *  - ViewPager2 multi-page for home pages (swipe left/right)
 *  - NeumorphicClockView (clock & date)
 *  - Search bar (tap to open app drawer with active search)
 *  - Bottom dock with 4 favorite apps
 *  - Swipe UP → App Drawer, Swipe DOWN → Notification shade
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var themeConfig: ThemeConfig
    private lateinit var dockAdapter: DockAdapter
    private lateinit var homePagerAdapter: HomePagerAdapter

    private var dockApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceManager = PreferenceManager(this)
        themeConfig = preferenceManager.loadThemeConfig()

        // Apply tema — Apply theme
        applyTheme()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClock()
        setupViewPager()
        setupSearchBar()
        setupDock()
        setupSwipeGesture()

        loadDockApps()
    }

    /**
     * Apply tema berdasarkan konfigurasi.
     * Apply theme based on configuration.
     */
    private fun applyTheme() {
        // Untuk mode AUTO, gunakan sistem — For AUTO mode, use system
        window.decorView.setBackgroundColor(themeConfig.backgroundColor)
    }

    /**
     * Setup clock widget dengan warna sesuai tema.
     * Setup clock widget with theme-appropriate colors.
     */
    private fun setupClock() {
        val textColor = NeumorphicHelper.getContrastTextColor(themeConfig.backgroundColor)
        val dateColor = NeumorphicHelper.blendColors(
            themeConfig.backgroundColor,
            textColor,
            0.5f
        )
        binding.clockView.setColors(textColor, dateColor)

        // Tap jam untuk membuka app default clock
        binding.clockContainer.setOnClickListener {
            val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
            clockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                startActivity(clockIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "Aplikasi jam tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Setup ViewPager untuk multi-page home screen.
     * Setup ViewPager for multi-page home screen.
     */
    private fun setupViewPager() {
        homePagerAdapter = HomePagerAdapter()
        // Inisialisasi dengan 3 halaman kosong — Initialize with 3 empty pages
        // Halaman dapat diisi dengan app shortcut (drag-and-drop) di versi mendatang
        homePagerAdapter.submitList(listOf(emptyList(), emptyList(), emptyList()))

        binding.viewPager.apply {
            adapter = homePagerAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updatePageIndicator(position)
                }
            })
        }
        updatePageIndicator(0)
    }

    private fun updatePageIndicator(position: Int) {
        // Sederhana: highlight dot aktif
        val totalPages = (binding.viewPager.adapter?.itemCount ?: 1)
        binding.pageIndicator.text = "${position + 1} / $totalPages"
    }

    /**
     * Setup search bar — tap untuk buka app drawer dengan search aktif.
     * Setup search bar — tap to open app drawer with active search.
     */
    private fun setupSearchBar() {
        binding.searchBar.setOnClickListener {
            val intent = Intent(this, AppDrawerActivity::class.java)
            intent.putExtra(AppDrawerActivity.EXTRA_OPEN_SEARCH, true)
            startActivity(intent)
        }
    }

    /**
     * Setup dock bawah dengan 4 aplikasi favorit.
     * Setup bottom dock with 4 favorite apps.
     */
    private fun setupDock() {
        dockAdapter = DockAdapter { app ->
            try {
                app.launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(app.launchIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "Tidak dapat membuka ${app.label}", Toast.LENGTH_SHORT).show()
            }
        }
        binding.dockRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = dockAdapter
            setHasFixedSize(true)
        }
    }

    /**
     * Muat aplikasi untuk dock dari preference.
     * Load dock apps from preferences.
     */
    private fun loadDockApps() {
        val packageNames = preferenceManager.getDockApps()
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                packageNames.mapNotNull { pkg ->
                    AppLoader.loadAppByPackageName(this@MainActivity, pkg)
                }
            }
            dockApps = apps
            dockAdapter.submitList(apps)
        }
    }

    /**
     * Setup swipe gesture: UP → app drawer, DOWN → notification.
     * Setup swipe gesture: UP → app drawer, DOWN → notification.
     */
    private fun setupSwipeGesture() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val dy = e1.y - e2.y
                val dx = e1.x - e2.x

                // Swipe harus dominan vertikal
                if (Math.abs(dy) > Math.abs(dx) * 1.5 && Math.abs(dy) > 100) {
                    if (dy > 0) {
                        // Swipe UP → App Drawer
                        startActivity(Intent(this@MainActivity, AppDrawerActivity::class.java))
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        return true
                    } else {
                        // Swipe DOWN → Notification shade
                        GestureHelper.expandNotificationShade(this@MainActivity)
                        return true
                    }
                }
                return false
            }

            override fun onLongPress(e: MotionEvent) {
                // Long press untuk buka settings
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        })

        // Apply ke root view
        binding.root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    /**
     * Refresh dock saat kembali ke home (jika user mengubah favorit).
     * Refresh dock when returning home (if user changed favorites).
     */
    override fun onResume() {
        super.onResume()
        loadDockApps()
    }

    /**
     * Disable back button di home screen (karena ini launcher utama).
     * Disable back button on home screen (since this is the main launcher).
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Kembali ke halaman pertama ViewPager daripada keluar
        if (binding.viewPager.currentItem > 0) {
            binding.viewPager.currentItem = 0
        }
        // Jangan panggil super agar tidak keluar dari launcher
    }
}
