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
    val author: String = "LuntikVisuals",
    val permissions: List<String> = emptyList()
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
            version = "0.5.0",
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
            tagline = "2D-аркада · монеты и скины",
            description = "Змейка Luntik: режимы, магазин, кейсы, XP.",
            version = "1.1.0",
            packageName = "com.luntik.snake",
            githubRepo = "LuntikVisuals/Snake2D",
            apkAssetName = "Snake2D.apk",
            repoUrl = "https://github.com/LuntikVisuals/Snake2D",
            accent = Color(0xFF3DFF6E),
            category = AppCategory.GAMES,
            permissions = listOf()
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
            permissions = listOf()
        ),
        // ——— FOSS с GitHub (сторонние авторы) ———
        CatalogApp(
            id = "shatteredpd",
            name = "Shattered Pixel Dungeon",
            tagline = "Roguelike RPG",
            description = "Пошаговый рогалик в подземелье. Автор: Evan (00-Evan). Лицензия GPL-3.0. Не связано с Luntik.",
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
            description = "Строй заводы и турели, отбивай волны. Автор: Anuken. Лицензия GPL-3.0. Не связано с Luntik.",
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
            tagline = "Стратегия 4X · Civ-like",
            description = "Развивай цивилизацию: наука, армия, дипломатия. Автор: yairm210. Лицензия MPL-2.0. Не связано с Luntik.",
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
            id = "astroloop",
            name = "Astro Loop",
            tagline = "Космический roguelike-шутер",
            description = "Офлайн-шутер без рекламы и трекинга. Автор: PubDeer. Лицензия GPL-3.0. Не связано с Luntik.",
            version = "latest",
            packageName = "com.astroloop.game",
            githubRepo = "PubDeer/astro-loop",
            apkAssetName = "",
            repoUrl = "https://github.com/PubDeer/astro-loop",
            accent = Color(0xFF7E57C2),
            category = AppCategory.GAMES,
            isOfficial = false,
            author = "PubDeer"
        ),
        CatalogApp(
            id = "ricochlime",
            name = "Ricochlime",
            tagline = "Шутер с рикошетом",
            description = "Снаряды отскакивают и косят врагов. Автор: Adil Hanney (adil192). AGPL-3.0. Не связано с Luntik.",
            version = "latest",
            packageName = "com.adilhanney.ricochlime",
            githubRepo = "adil192/ricochlime",
            apkAssetName = "",
            repoUrl = "https://github.com/adil192/ricochlime",
            accent = Color(0xFFFF8A65),
            category = AppCategory.GAMES,
            isOfficial = false,
            author = "Adil Hanney"
        ),
        CatalogApp(
            id = "cavedroid",
            name = "CaveDroid",
            tagline = "2D sandbox",
            description = "Копай, строй, крафть — 2D песочница. Автор: fredboy. Не связано с Luntik.",
            version = "latest",
            packageName = "com.github.fredboy.cavedroid",
            githubRepo = "fredboy/cavedroid",
            apkAssetName = "",
            repoUrl = "https://github.com/fredboy/cavedroid",
            accent = Color(0xFF8D6E63),
            category = AppCategory.GAMES,
            isOfficial = false,
            author = "fredboy"
        ),
        CatalogApp(
            id = "retrowars",
            name = "Retrowars",
            tagline = "Ретро multiplayer",
            description = "Несколько ретро-игр друг против друга. Автор: retrowars. GPL-3.0. Не связано с Luntik.",
            version = "latest",
            packageName = "com.retrowars",
            githubRepo = "retrowars/retrowars",
            apkAssetName = "",
            repoUrl = "https://github.com/retrowars/retrowars",
            accent = Color(0xFFFFEB3B),
            category = AppCategory.GAMES,
            isOfficial = false,
            author = "retrowars"
        ),
        CatalogApp(
            id = "openttd",
            name = "OpenTTD",
            tagline = "Транспортный симулятор",
            description = "Строй сеть перевозок (порт OpenTTD). Автор порта: pelya. GPL. Не связано с Luntik.",
            version = "latest",
            packageName = "org.openttd.sdl",
            githubRepo = "pelya/openttd-android",
            apkAssetName = "",
            repoUrl = "https://github.com/pelya/openttd-android",
            accent = Color(0xFF26A69A),
            category = AppCategory.GAMES,
            isOfficial = false,
            author = "pelya / OpenTTD"
        ),
        CatalogApp(
            id = "rectball",
            name = "Rectball",
            tagline = "Цветной puzzle",
            description = "Головоломка на поле. Автор: danirod. GPL-3.0. Не связано с Luntik.",
            version = "latest",
            packageName = "es.danirod.rectball",
            githubRepo = "danirod/rectball",
            apkAssetName = "",
            repoUrl = "https://github.com/danirod/rectball",
            accent = Color(0xFFEC407A),
            category = AppCategory.GAMES,
            isOfficial = false,
            author = "danirod"
        )
    )

    fun byId(id: String): CatalogApp? = apps.find { it.id == id }

    fun byCategory(cat: AppCategory): List<CatalogApp> =
        if (cat == AppCategory.ALL) apps else apps.filter { it.category == cat }
}
