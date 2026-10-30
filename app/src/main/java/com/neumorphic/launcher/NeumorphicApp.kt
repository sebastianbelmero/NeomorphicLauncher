package com.neumorphic.launcher

import android.app.Application
import com.neumorphic.launcher.util.PreferenceManager

/**
 * Application class untuk launcher.
 * Application class for the launcher.
 *
 * Menginisialisasi global state & meng-apply tema saat startup.
 * Initializes global state & applies theme on startup.
 */
class NeumorphicApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Inisialisasi preference manager global — Initialize global preference manager
        preferenceManager = PreferenceManager(this)
    }

    companion object {
        @Volatile
        lateinit var instance: NeumorphicApp
            private set

        @Volatile
        lateinit var preferenceManager: PreferenceManager
            private set
    }
}
