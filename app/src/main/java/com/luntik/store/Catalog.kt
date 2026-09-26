package com.luntik.store

import androidx.compose.ui.graphics.Color

data class CatalogApp(
    val id: String,
    val name: String,
    val tagline: String,
    val description: String,
    val version: String,
    val packageName: String,
    val downloadUrl: String,
    val repoUrl: String,
    val accent: Color,
    val isOfficial: Boolean = true,
    val permissions: List<String> = emptyList()
)

object Catalog {
    val apps: List<CatalogApp> = listOf(
        CatalogApp(
            id = "terminal",
            name = "LuntikTerminal",
            tagline = "Скрытый установщик",
            description = "Терминал в стиле Windows CMD. Через него впервые устанавливается LuntikStore. После установки ярлык скрывается — открыть Terminal можно только из магазина.",
            version = "0.3.0",
            packageName = "com.luntik.terminal",
            downloadUrl = "https://github.com/LuntikVisuals/LuntikTerminal/releases/latest/download/LuntikTerminal.apk",
            repoUrl = "https://github.com/LuntikVisuals/LuntikTerminal",
            accent = Color(0xFF8B9CFF),
            permissions = listOf("INTERNET", "REQUEST_INSTALL_PACKAGES")
        ),
        CatalogApp(
            id = "ai",
            name = "LuntikAi",
            tagline = "Локальный ИИ-агент",
            description = "Локальный самообучающийся ИИ для Android. Учится на твоих текстах, навыки, песочница кода, личности. Работает на устройстве.",
            version = "0.5.0",
            packageName = "com.luntik.ai",
            downloadUrl = "https://github.com/LuntikVisuals/LuntikAi/releases/latest/download/LuntikAi.apk",
            repoUrl = "https://github.com/LuntikVisuals/LuntikAi",
            accent = Color(0xFF5CFFB0),
            permissions = listOf("INTERNET")
        ),
        CatalogApp(
            id = "lumina",
            name = "Lumina",
            tagline = "Liquid glass виджеты",
            description = "Premium Android виджеты с эффектом жидкого стекла: музыка, часы, батарея, таймер фокуса, цитаты и ambient.",
            version = "0.2.0",
            packageName = "com.lumina.widgets",
            downloadUrl = "https://github.com/LuntikVisuals/Lumina/releases/latest/download/Lumina.apk",
            repoUrl = "https://github.com/LuntikVisuals/Lumina",
            accent = Color(0xFFFFB8E0),
            permissions = listOf()
        )
    )

    fun byId(id: String): CatalogApp? = apps.find { it.id == id }
}
