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
        // ——— Luntik ———
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
            version = "0.8.0",
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
            description = "Кошелёк Luntik: валюта, карта, покупки в экосистеме. Без доната.",
            version = "0.1.0",
            packageName = "com.luntik.wallet",
            githubRepo = "LuntikVisuals/LuntikWallet",
            apkAssetName = "LuntikWallet.apk",
            repoUrl = "https://github.com/LuntikVisuals/LuntikWallet",
            accent = Color(0xFFFFC857),
            category = AppCategory.SYSTEM,
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
            id = "twothrones",
            name = "Two Thrones",
            tagline = "Стратегия · Свет и Тьма",
            description = "Стратегия Luntik: правители, дипломатия, армии, регионы.",
            version = "0.2.0",
            packageName = "com.luntik.twothrones",
            githubRepo = "LuntikVisuals/TwoThrones",
            apkAssetName = "TwoThrones.apk",
            repoUrl = "https://github.com/LuntikVisuals/TwoThrones",
            accent = Color(0xFFE8C547),
            category = AppCategory.GAMES,
            price = 100,
            featured = true
        ),
        // ——— Внешние FOSS ———
        CatalogApp(
            id = "ricochlime",
            name = "Ricochlime",
            tagline = "Уютный шутер · ricochet",
            description = "Защищайся от волн монстров рикошетящими снарядами. Автор: Adil Hanney (adil192). AGPL-3.0. Не связано с Luntik.",
            version = "latest",
            packageName = "com.adilhanney.ricochlime",
            githubRepo = "adil192/ricochlime",
            apkAssetName = "",
            repoUrl = "https://github.com/adil192/ricochlime",
            accent = Color(0xFF7EC8E3),
            category = AppCategory.GAMES,
            isOfficial = false,
            author = "Adil Hanney (adil192)",
            featured = true
        ),
        CatalogApp(
            id = "deepseek",
            name = "DeepSeek",
            tagline = "Официальный AI-ассистент",
            description = "Официальное приложение DeepSeek. Права: DeepSeek. Источник: Google Play / download.deepseek.com.",
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
            id = "shatteredpd",
            name = "Shattered Pixel Dungeon",
            tagline = "Roguelike RPG",
            description = "Пошаговый рогалик. Автор: Evan (00-Evan). GPL-3.0.",
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
            description = "Заводы и турели. Автор: Anuken. GPL-3.0.",
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
            description = "Порт 2048 для Android. Автор: uberspot. MIT.",
            version = "latest",
            packageName = "com.uberspot.a2048",
            githubRepo = "uberspot/2048-android",
            apkAssetName = "",
            repoUrl = "https://github.com/uberspot/2048-android",
            accent = Color(0xFFF2B179),
            category = AppCategory.GAMES,
            isOfficial = false,
            author = "uberspot"
        ),
        CatalogApp(
            id = "newpipe",
            name = "NewPipe",
            tagline = "YouTube без Google",
            description = "Клиент YouTube с фоном и загрузками. TeamNewPipe. GPL-3.0.",
            version = "latest",
            packageName = "org.schabi.newpipe",
            githubRepo = "TeamNewPipe/NewPipe",
            apkAssetName = "",
            repoUrl = "https://github.com/TeamNewPipe/NewPipe",
            accent = Color(0xFFCD201F),
            category = AppCategory.TOOLS,
            isOfficial = false,
            author = "TeamNewPipe"
        ),
        CatalogApp(
            id = "antenna",
            name = "AntennaPod",
            tagline = "Подкасты",
            description = "Менеджер подкастов. AntennaPod. GPL-3.0.",
            version = "latest",
            packageName = "de.danoeh.antennapod",
            githubRepo = "AntennaPod/AntennaPod",
            apkAssetName = "",
            repoUrl = "https://github.com/AntennaPod/AntennaPod",
            accent = Color(0xFF1B7A55),
            category = AppCategory.TOOLS,
            isOfficial = false,
            author = "AntennaPod"
        ),
        CatalogApp(
            id = "organicmaps",
            name = "Organic Maps",
            tagline = "Карты офлайн",
            description = "Офлайн-карты без трекинга. Organic Maps. Apache-2.0.",
            version = "latest",
            packageName = "app.organicmaps",
            githubRepo = "organicmaps/organicmaps",
            apkAssetName = "",
            repoUrl = "https://github.com/organicmaps/organicmaps",
            accent = Color(0xFF6BBF59),
            category = AppCategory.TOOLS,
            isOfficial = false,
            author = "Organic Maps"
        ),
        CatalogApp(
            id = "krita",
            name = "Krita",
            tagline = "Рисование",
            description = "Профессиональный растровый редактор. KDE. GPL-3.0.",
            version = "latest",
            packageName = "org.krita",
            githubRepo = "KDE/krita",
            apkAssetName = "",
            repoUrl = "https://github.com/KDE/krita",
            accent = Color(0xFF3DAEE9),
            category = AppCategory.TOOLS,
            isOfficial = false,
            author = "KDE",
            installSource = InstallSource.PLAY,
            playPackage = "org.krita"
        ),
        CatalogApp(
            id = "termux",
            name = "Termux",
            tagline = "Терминал Linux",
            description = "Терминал и пакеты на Android. termux. GPL-3.0.",
            version = "latest",
            packageName = "com.termux",
            githubRepo = "termux/termux-app",
            apkAssetName = "",
            repoUrl = "https://github.com/termux/termux-app",
            accent = Color(0xFF000000),
            category = AppCategory.TOOLS,
            isOfficial = false,
            author = "Termux"
        ),
        CatalogApp(
            id = "osmand",
            name = "OsmAnd",
            tagline = "Навигация офлайн",
            description = "Навигация OpenStreetMap. OsmAnd. GPL-3.0.",
            version = "latest",
            packageName = "net.osmand",
            githubRepo = "osmandapp/OsmAnd",
            apkAssetName = "",
            repoUrl = "https://github.com/osmandapp/OsmAnd",
            accent = Color(0xFFFF8C00),
            category = AppCategory.TOOLS,
            isOfficial = false,
            author = "OsmAnd"
        ),
        CatalogApp(
            id = "libreoffice",
            name = "Collabora Office",
            tagline = "Документы",
            description = "Офисный пакет (LibreOffice / Collabora). Источник: Play / сайт.",
            version = "latest",
            packageName = "com.collabora.libreoffice",
            githubRepo = "",
            apkAssetName = "",
            repoUrl = "https://www.collaboraoffice.com",
            accent = Color(0xFF18A303),
            category = AppCategory.TOOLS,
            isOfficial = false,
            author = "Collabora",
            installSource = InstallSource.PLAY,
            playPackage = "com.collabora.libreoffice"
        ),
        CatalogApp(
            id = "vitruvia",
            name = "OpenSudoku",
            tagline = "Судоку",
            description = "Классическое судоку. open-sudoku. GPL-3.0.",
            version = "latest",
            packageName = "org.moire.opensudoku",
            githubRepo = "rosti-cz/open-sudoku",
            apkAssetName = "",
            repoUrl = "https://github.com/rosti-cz/open-sudoku",
            accent = Color(0xFF5B8DEF),
            category = AppCategory.GAMES,
            isOfficial = false,
            author = "OpenSudoku"
        )
    )

    fun byId(id: String): CatalogApp? = apps.find { it.id == id }

    fun byCategory(cat: AppCategory): List<CatalogApp> =
        if (cat == AppCategory.ALL) apps else apps.filter { it.category == cat }

    fun featured(): List<CatalogApp> = apps.filter { it.featured }
}
