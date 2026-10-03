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

enum class InstallSource { GITHUB, PLAY, OFFICIAL_SITE, INTERNAL }

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
    val author: String = "LuntikVisuals",
    val permissions: List<String> = emptyList(),
    /** Цена в валюте Wallet; 0 = бесплатно */
    val price: Int = 0,
    val featured: Boolean = false,
    val installSource: InstallSource = InstallSource.GITHUB,
    val playPackage: String? = null,
    val officialDownloadUrl: String? = null
) {
    val downloadUrlFallback: String
        get() = if (apkAssetName.isNotBlank()) {
            "https://github.com/$githubRepo/releases/latest/download/$apkAssetName"
        } else {
            "https://github.com/$githubRepo/releases/latest"
        }
}

object Catalog {
    val apps: List<CatalogApp> = listOf(
        CatalogApp(
            id = "terminal",
            name = "LuntikTerminal",
            tagline = "Скрытый установщик",
            description = "Терминал в стиле Windows CMD. Через него устанавливается LuntikStore.",
            version = "0.4.0",
            packageName = "com.luntik.terminal",
            githubRepo = "LuntikVisuals/LuntikTerminal",
            apkAssetName = "LuntikTerminal.apk",
            repoUrl = "https://github.com/LuntikVisuals/LuntikTerminal",
            accent = Color(0xFF8B9CFF),
            category = AppCategory.TOOLS,
            permissions = listOf("INTERNET", "REQUEST_INSTALL_PACKAGES"),
            featured = true
        ),
        CatalogApp(
            id = "store",
            name = "LuntikStore",
            tagline = "Этот магазин",
            description = "Центральный магазин приложений Luntik.",
            version = "0.6.0",
            packageName = "com.luntik.store",
            githubRepo = "LuntikVisuals/LuntikStore",
            apkAssetName = "LuntikStore.apk",
            repoUrl = "https://github.com/LuntikVisuals/LuntikStore",
            accent = Color(0xFFB8C0FF),
            category = AppCategory.SYSTEM,
            permissions = listOf("INTERNET", "REQUEST_INSTALL_PACKAGES")
        ),
        CatalogApp(
            id = "wallet",
            name = "LuntikWallet",
            tagline = "Валюта и карта Luntik",
            description = "Кошелёк Luntik: накопление валюты, карта, подтверждение покупок в экосистеме. Без доната — валюту нельзя купить.",
            version = "0.1.0",
            packageName = "com.luntik.wallet",
            githubRepo = "LuntikVisuals/LuntikWallet",
            apkAssetName = "LuntikWallet.apk",
            repoUrl = "https://github.com/LuntikVisuals/LuntikWallet",
            accent = Color(0xFFFFC857),
            category = AppCategory.SYSTEM,
            price = 0,
            featured = true,
            installSource = InstallSource.INTERNAL
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
            permissions = listOf("INTERNET"),
            featured = true
        ),
        CatalogApp(
            id = "deepseek",
            name = "DeepSeek",
            tagline = "Официальный AI-ассистент",
            description = "Официальное приложение DeepSeek. Права: DeepSeek. Источник: Google Play или download.deepseek.com. Не связано с Luntik.",
            version = "latest",
            packageName = "com.deepseek.chat",
            githubRepo = "",
            apkAssetName = "",
            repoUrl = "https://www.deepseek.com",
            accent = Color(0xFF4D6BFE),
            category = AppCategory.AI,
            isOfficial = false,
            author = "DeepSeek",
            installSource = InstallSource.PLAY,
            playPackage = "com.deepseek.chat",
            officialDownloadUrl = "https://download.deepseek.com/app/",
            featured = true
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
            category = AppCategory.WIDGETS
        ),
        CatalogApp(
            id = "snake",
            name = "Змейка",
            tagline = "2D-аркада · монеты и скины",
            description = "Змейка Luntik: режимы, магазин, кейсы, XP.",
            version = "1.1.0",
            packageName = "com.luntik.snake",
            githubRepo = "LuntikVisuals/Snake2D",
            apkAssetName = "Snake2D.apk",
            repoUrl = "https://github.com/LuntikVisuals/Snake2D",
            accent = Color(0xFF3DFF6E),
            category = AppCategory.GAMES,
            price = 50
        ),
        CatalogApp(
            id = "dopros",
            name = "Допрос",
            tagline = "Хоррор-детектив · 2D",
            description = "Допрос подозреваемых: ток, фонарик, шкалы страха.",
            version = "0.1.0",
            packageName = "com.luntik.dopros",
            githubRepo = "LuntikVisuals/Dopros",
            apkAssetName = "Dopros.apk",
            repoUrl = "https://github.com/LuntikVisuals/Dopros",
            accent = Color(0xFFC45C5C),
            category = AppCategory.GAMES,
            price = 80
        ),
        CatalogApp(
            id = "shatteredpd",
            name = "Shattered Pixel Dungeon",
            tagline = "Roguelike RPG",
            description = "Пошаговый рогалик. Автор: Evan (00-Evan). GPL-3.0. Не связано с Luntik.",
            version = "latest",
            packageName = "com.shatteredpixel.shatteredpixeldungeon",
            githubRepo = "00-Evan/shattered-pixel-dungeon",
            apkAssetName = "",
            repoUrl = "https://github.com/00-Evan/shattered-pixel-dungeon",
            accent = Color(0xFFD4A574),
            category = AppCategory.GAMES,
            isOfficial = false,
            author = "00-Evan (Evan)"
        ),
        CatalogApp(
            id = "mindustry",
            name = "Mindustry",
            tagline = "Tower defense + factories",
            description = "Заводы и турели. Автор: Anuken. GPL-3.0. Не связано с Luntik.",
            version = "latest",
            packageName = "io.anuke.mindustry",
            githubRepo = "Anuken/Mindustry",
            apkAssetName = "",
            repoUrl = "https://github.com/Anuken/Mindustry",
            accent = Color(0xFF6BCB77),
            category = AppCategory.GAMES,
            isOfficial = false,
            author = "Anuken"
        ),
        CatalogApp(
            id = "unciv",
            name = "Unciv",
            tagline = "Стратегия 4X",
            description = "Цивилизация open-source. Автор: yairm210. MPL-2.0.",
            version = "latest",
            packageName = "com.unciv.app",
            githubRepo = "yairm210/Unciv",
            apkAssetName = "",
            repoUrl = "https://github.com/yairm210/Unciv",
            accent = Color(0xFF4FC3F7),
            category = AppCategory.GAMES,
            isOfficial = false,
            author = "yairm210"
        ),
        CatalogApp(
            id = "game2048",
            name = "2048",
            tagline = "Классика · MIT",
            description = "Порт 2048 для Android. Автор: uberspot. MIT. Не связано с Luntik.",
            version = "latest",
            packageName = "com.uberspot.a2048",
            githubRepo = "uberspot/2048-android",
            apkAssetName = "",
            repoUrl = "https://github.com/uberspot/2048-android",
            accent = Color(0xFFF2B179),
            category = AppCategory.GAMES,
            isOfficial = false,
            author = "uberspot"
        )
    )

    fun byId(id: String): CatalogApp? = apps.find { it.id == id }

    fun byCategory(cat: AppCategory): List<CatalogApp> =
        if (cat == AppCategory.ALL) apps else apps.filter { it.category == cat }

    fun featured(): List<CatalogApp> = apps.filter { it.featured }
}
