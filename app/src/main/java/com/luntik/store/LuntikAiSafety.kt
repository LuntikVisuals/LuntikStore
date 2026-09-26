package com.luntik.store

/**
 * Проверка безопасности в стиле LuntikAi.
 * Знает каталог LuntikStore и официальные пакеты LuntikVisuals.
 */
object LuntikAiSafety {

    data class Report(
        val safe: Boolean,
        val score: Int, // 0..100
        val title: String,
        val summary: String,
        val points: List<String>
    )

    private val officialPackages = setOf(
        "com.luntik.store",
        "com.luntik.terminal",
        "com.luntik.ai",
        "com.lumina.widgets"
    )

    private val officialHosts = listOf(
        "github.com/LuntikVisuals/"
    )

    fun analyze(app: CatalogApp): Report {
        val points = mutableListOf<String>()
        var score = 50

        // Официальный разработчик
        if (app.isOfficial && app.packageName in officialPackages) {
            score += 25
            points.add("Официальное приложение LuntikVisuals")
        } else if (app.isOfficial) {
            score += 15
            points.add("Помечено как официальное в каталоге Store")
        } else {
            score -= 20
            points.add("Не из официального каталога — повышенное внимание")
        }

        // Источник загрузки
        if (officialHosts.any { app.downloadUrl.contains(it) }) {
            score += 15
            points.add("Загрузка с GitHub LuntikVisuals (открытый исходный код)")
        } else {
            score -= 15
            points.add("Источник загрузки не из LuntikVisuals GitHub")
        }

        // Репозиторий
        if (officialHosts.any { app.repoUrl.contains(it) }) {
            score += 10
            points.add("Публичный репозиторий: ${app.repoUrl.removePrefix("https://")}")
        }

        // Разрешения
        when {
            app.permissions.isEmpty() -> {
                score += 5
                points.add("Минимальные разрешения")
            }
            "REQUEST_INSTALL_PACKAGES" in app.permissions -> {
                points.add("Может устанавливать другие APK (нормально для установщика)")
            }
            else -> points.add("Разрешения: ${app.permissions.joinToString()}")
        }

        // Знание о Store
        if (app.id == "terminal") {
            points.add("LuntikAi: Terminal — точка входа в экосистему, ставит только Store")
        }
        if (app.id == "ai") {
            points.add("LuntikAi: это я. Локальный ИИ, данные на устройстве")
        }
        if (app.id == "lumina") {
            points.add("LuntikAi: виджеты без сбора аккаунтов, открытый код")
        }

        points.add("LuntikStore каталогизирует только проверенные сборки с Releases")

        score = score.coerceIn(0, 100)
        val safe = score >= 60

        return Report(
            safe = safe,
            score = score,
            title = if (safe) "Безопасное приложение" else "Требует осторожности",
            summary = if (safe) {
                "LuntikAi считает «${app.name}» безопасным для установки из LuntikStore."
            } else {
                "LuntikAi рекомендует проверить источник перед установкой «${app.name}»."
            },
            points = points
        )
    }

    fun aboutStore(): String = """
        LuntikStore — центральный магазин экосистемы Luntik.
        Приложения: LuntikTerminal, LuntikAi, Lumina.
        Установка APK только с GitHub Releases LuntikVisuals.
        Terminal скрывает ярлык после установки Store.
        Отзывы хранятся локально на устройстве.
        Проверка безопасности выполняется встроенным модулем LuntikAi.
    """.trimIndent()
}
