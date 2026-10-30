package com.neumorphic.launcher.pro.data

/**
 * Kategori aplikasi untuk Smart Folder.
 * App categories for Smart Folder.
 */
enum class AppCategory(val displayNameRes: Int) {
    GAMES(com.neumorphic.launcher.R.string.smart_folder_games),
    SOCIAL(com.neumorphic.launcher.R.string.smart_folder_social),
    TOOLS(com.neumorphic.launcher.R.string.smart_folder_tools),
    MEDIA(com.neumorphic.launcher.R.string.smart_folder_media),
    FINANCE(com.neumorphic.launcher.R.string.smart_folder_finance),
    SHOPPING(com.neumorphic.launcher.R.string.smart_folder_shopping),
    PRODUCTIVITY(com.neumorphic.launcher.R.string.smart_folder_productivity),
    OTHER(com.neumorphic.launcher.R.string.smart_folder_other);
}
