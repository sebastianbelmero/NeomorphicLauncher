package com.neumorphic.launcher.pro.util

import android.content.Context
import android.content.pm.ApplicationInfo
import com.neumorphic.launcher.model.AppInfo
import com.neumorphic.launcher.pro.data.AppCategory

/**
 * Engine untuk mengkategorikan aplikasi otomatis (Smart Folder).
 * Engine for automatic app categorization (Smart Folder).
 *
 * Strategi:
 *  1. Cek kategori dari Play Store intent metadata (jika tersedia)
 *  2. Cek nama paket terhadap daftar keyword kategori
 *  3. Cek nama aplikasi terhadap keyword kategori
 *  4. Fallback: AppCategory.OTHER
 *
 * Strategy:
 *  1. Check category from Play Store intent metadata (if available)
 *  2. Check package name against category keyword lists
 *  3. Check app label against category keyword lists
 *  4. Fallback: AppCategory.OTHER
 */
object AppCategorizer {

    /**
     * Klasifikasikan satu aplikasi ke kategori.
     * Classify one app into a category.
     */
    fun categorize(app: AppInfo): AppCategory {
        val pkg = app.packageName.lowercase()
        val label = app.label.lowercase()

        // Cek setiap kategori berdasarkan keyword
        for ((category, keywords) in CATEGORY_KEYWORDS) {
            for (keyword in keywords) {
                if (pkg.contains(keyword) || label.contains(keyword)) {
                    return category
                }
            }
        }
        return AppCategory.OTHER
    }

    /**
     * Kategorikan semua aplikasi sekaligus, return map per kategori.
     * Categorize all apps at once, return map per category.
     */
    fun categorizeAll(apps: List<AppInfo>): Map<AppCategory, List<AppInfo>> {
        return apps.groupBy { categorize(it) }
    }

    /**
     * Keyword map per kategori — Keyword map per category.
     * Disusun berdasarkan package name & label keyword umum.
     */
    private val CATEGORY_KEYWORDS: Map<AppCategory, List<String>> = mapOf(
        AppCategory.GAMES to listOf(
            "game", "play", "gaming", "pubg", "freefire", "mobilelegends",
            "genshin", "clash", "chess", "puzzle", "rpg", "mmo", "fps",
            "supercell", "tencent", "garena", "riot", "valve", "minecraft"
        ),

        AppCategory.SOCIAL to listOf(
            "whatsapp", "instagram", "facebook", "twitter", "tiktok",
            "telegram", "snapchat", "linkedin", "discord", "reddit",
            "pinterest", "twitter", "x.com", "threads", "line", "wechat",
            "messenger", "social", "chat", "message"
        ),

        AppCategory.MEDIA to listOf(
            "youtube", "netflix", "spotify", "vimeo", "twitch", "disney",
            "hbo", "primevideo", "vlc", "music", "video", "podcast",
            "radio", "stream", "player", "movie", "tv", "soundcloud",
            "joox", "rottentomato", "imdb"
        ),

        AppCategory.TOOLS to listOf(
            "calculator", "calendar", "clock", "alarm", "weather", "flash",
            "torch", "compass", "ruler", "scanner", "qr", "barcode", "file",
            "manager", "cleaner", "booster", "battery", "vpn", "proxy",
            "translator", "dictionary", "note", "memo", "recorder", "tool"
        ),

        AppCategory.FINANCE to listOf(
            "bank", "bca", "mandiri", "bni", "bri", "cimb", "ovo", "gopay",
            "dana", "shopeepay", "jenius", "linkaja", "sakuku", "paylater",
            "invest", "stock", "trading", "wallet", "finance", "paypal",
            "venmo", "cashapp", "octo", "mobile"
        ),

        AppCategory.SHOPPING to listOf(
            "shopee", "tokopedia", "lazada", "bukalapak", "blibli", "jd",
            "amazon", "ebay", "aliexpress", "shop", "store", "market",
            "cart", "deal", "OLX", "carousell"
        ),

        AppCategory.PRODUCTIVITY to listOf(
            "office", "word", "excel", "powerpoint", "docs", "sheet",
            "slides", "wps", "notion", "evernote", "todo", "task", "trello",
            "asana", "slack", "teams", "zoom", "meet", "drive", "dropbox",
            "onedrive", "email", "gmail", "outlook", "mail"
        )
    )
}
