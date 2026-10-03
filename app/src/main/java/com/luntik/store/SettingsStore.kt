package com.luntik.store

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class GridMode(val columns: Int, val title: String) {
    ONE(1, "1 в ряд"),
    TWO(2, "2 в ряд"),
    THREE(3, "3 в ряд")
}

enum class ThemeMode(val title: String) {
    DARK("Тёмная"),
    LIGHT("Светлая")
}

/** Персонализация + премиум-флаги (локально). */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("luntik_settings", Context.MODE_PRIVATE)

    var theme by mutableStateOf(
        ThemeMode.entries.getOrElse(prefs.getInt("theme", 0)) { ThemeMode.DARK }
    )
        private set

    var grid by mutableStateOf(
        GridMode.entries.getOrElse(prefs.getInt("grid", 0)) { GridMode.ONE }
    )
        private set

    var accentArgb by mutableIntStateOf(prefs.getInt("accent", 0xFF8B9CFF.toInt()))
        private set

    var hideAds by mutableStateOf(prefs.getBoolean("hide_ads", false))
        private set

    var wallpaperPath by mutableStateOf(prefs.getString("wallpaper", null))
        private set

    var cloudPasswordEnabled by mutableStateOf(prefs.getBoolean("cloud_pw", false))
        private set

    fun accentColor(): Color = Color(accentArgb)

    fun updateTheme(m: ThemeMode) {
        theme = m
        prefs.edit().putInt("theme", m.ordinal).apply()
    }

    fun updateGrid(m: GridMode) {
        grid = m
        prefs.edit().putInt("grid", m.ordinal).apply()
    }

    fun updateAccent(color: Color) {
        val argb = android.graphics.Color.argb(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
        accentArgb = argb
        prefs.edit().putInt("accent", argb).apply()
    }

    fun updateHideAds(v: Boolean) {
        hideAds = v
        prefs.edit().putBoolean("hide_ads", v).apply()
    }

    fun updateWallpaper(path: String?) {
        wallpaperPath = path
        prefs.edit().putString("wallpaper", path).apply()
    }

    fun updateCloudPasswordEnabled(v: Boolean) {
        cloudPasswordEnabled = v
        prefs.edit().putBoolean("cloud_pw", v).apply()
    }
}
