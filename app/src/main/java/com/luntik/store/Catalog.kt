package com.luntik.store

import androidx.compose.ui.graphics.Color

enum class AppCategory(val title: String) {
    ALL("Все"),
    TOOLS("Инструменты"),
    AI("ИИ"),
    WIDGETS("Виджеты"),
    GAMES("Игры"),
    SYSTEM("Системные")
}

data class CatalogApp(
    val id: String,
    val name: String,
    val tagline: String,
    val description: String,
    val version: String,
    val packageName: String,
    val githubRepo: String,
    val apkAssetName: String,
    val repoUrl: String,
    val accent: Color,
    val category: AppCategory,
    val isOfficial: Boolean = true,
    val permissions: List<String> = emptyList()
) {
    val downloadUrlFallback: String
        get() = "https://github.com/$githubRepo/releases/latest/download/$apkAssetName"
}

object Catalog {
    val apps: List<CatalogApp> = listOf(
        CatalogApp(
            id = "terminal",
            name = "LuntikTerminal",
            tagline = "Скрытый установщик",
            description = "Терминал в стиле Windows CMD. Через него впервые устанавливается LuntikStore.",
            version = "0.3.0",
            packageName = "com.luntik.terminal",
            githubRepo = "LuntikVisuals/LuntikTerminal",
            apkAssetName = "LuntikTerminal.apk",
            repoUrl = "https://github.com/LuntikVisuals/LuntikTerminal",
            accent = Color(0xFF8B9CFF),
            category = AppCategory.TOOLS,
            permissions = listOf("INTERNET", "REQUEST_INSTALL_PACKAGES")
        ),
        CatalogApp(
            id = "store",
            name = "LuntikStore",
            tagline = "Этот магазин",
            description = "Центральный магазин приложений Luntik.",
            version = "0.4.0",
            packageName = "com.luntik.store",
            githubRepo = "LuntikVisuals/LuntikStore",
            apkAssetName = "LuntikStore.apk",
            repoUrl = "https://github.com/LuntikVisuals/LuntikStore",
            accent = Color(0xFFB8C0FF),
            category = AppCategory.SYSTEM,
            permissions = listOf("INTERNET", "REQUEST_INSTALL_PACKAGES")
        ),
        CatalogApp(
            id = "ai",
            name = "LuntikAi",
            tagline = "Локальный ИИ-агент",
            description = "Локальный ИИ для Android. Учится на устройстве.",
            version = "0.5.0",
            packageName = "com.luntik.ai",
            githubRepo = "LuntikVisuals/LuntikAi",
            apkAssetName = "LuntikAi.apk",
            repoUrl = "https://github.com/LuntikVisuals/LuntikAi",
            accent = Color(0xFF5CFFB0),
            category = AppCategory.AI,
            permissions = listOf("INTERNET")
        ),
        CatalogApp(
            id = "lumina",
            name = "Lumina",
            tagline = "Liquid glass виджеты",
            description = "Виджеты с эффектом жидкого стекла.",
            version = "0.2.0",
            packageName = "com.lumina.widgets",
            githubRepo = "LuntikVisuals/Lumina",
            apkAssetName = "Lumina.apk",
            repoUrl = "https://github.com/LuntikVisuals/Lumina",
            accent = Color(0xFFFFB8E0),
            category = AppCategory.WIDGETS,
            permissions = listOf()
        ),
        CatalogApp(
            id = "snake",
            name = "Змейка",
            tagline = "Классическая 2D-аркада",
            description = "Змейка: свайп или кнопки, счёт и рекорд.",
            version = "1.0.0",
            packageName = "com.luntik.snake",
            githubRepo = "LuntikVisuals/Snake2D",
            apkAssetName = "Snake2D.apk",
            repoUrl = "https://github.com/LuntikVisuals/Snake2D",
            accent = Color(0xFF3DFF6E),
            category = AppCategory.GAMES,
            permissions = listOf()
        )
    )

    fun byId(id: String): CatalogApp? = apps.find { it.id == id }

    fun byCategory(cat: AppCategory): List<CatalogApp> =
        if (cat == AppCategory.ALL) apps else apps.filter { it.category == cat }
}
