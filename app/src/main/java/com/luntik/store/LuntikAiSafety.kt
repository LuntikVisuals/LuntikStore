package com.luntik.store

object LuntikAiSafety {

    data class Report(
        val safe: Boolean,
        val score: Int,
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

    fun analyze(app: CatalogApp): Report {
        val points = mutableListOf<String>()
        var score = 50

        if (app.isOfficial && app.packageName in officialPackages) {
            score += 25
            points.add("Официальное приложение LuntikVisuals")
        } else if (app.isOfficial) {
            score += 15
            points.add("Помечено как официальное в каталоге Store")
        } else {
            score -= 20
            points.add("Не из официального каталога")
        }

        if (app.githubRepo.startsWith("LuntikVisuals/")) {
            score += 15
            points.add("Загрузка с GitHub LuntikVisuals")
        } else {
            score -= 15
            points.add("Источник не LuntikVisuals")
        }

        if (app.repoUrl.contains("github.com/LuntikVisuals")) {
            score += 10
            points.add("Публичный репозиторий: ${app.githubRepo}")
        }

        when {
            app.permissions.isEmpty() -> {
                score += 5
                points.add("Минимальные разрешения")
            }
            "REQUEST_INSTALL_PACKAGES" in app.permissions -> {
                points.add("Может устанавливать APK (нормально для Store/Terminal)")
            }
            else -> points.add("Разрешения: ${app.permissions.joinToString()}")
        }

        when (app.id) {
            "terminal" -> points.add("LuntikAi: точка входа, ставит Store")
            "store" -> points.add("LuntikAi: центральный магазин экосистемы")
            "ai" -> points.add("LuntikAi: это я, данные на устройстве")
            "lumina" -> points.add("LuntikAi: виджеты, открытый код")
        }

        points.add("Обновления проверяются через GitHub Releases API")

        score = score.coerceIn(0, 100)
        val safe = score >= 60
        return Report(
            safe = safe,
            score = score,
            title = if (safe) "Безопасное приложение" else "Требует осторожности",
            summary = if (safe) {
                "LuntikAi считает «${app.name}» безопасным."
            } else {
                "Проверь источник перед установкой «${app.name}»."
            },
            points = points
        )
    }
}
