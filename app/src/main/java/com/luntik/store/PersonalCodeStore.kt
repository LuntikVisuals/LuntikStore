package com.luntik.store

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.security.SecureRandom

/** Персональный код для покупок в экосистеме Luntik (без доната). */
class PersonalCodeStore(context: Context) {
    private val prefs = context.getSharedPreferences("luntik_personal_code", Context.MODE_PRIVATE)

    var code by mutableStateOf(prefs.getString("code", null) ?: generateAndSave())
        private set

    private fun generateAndSave(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789"
        val rnd = SecureRandom()
        val body = buildString {
            append('#')
            repeat(28) { append(chars[rnd.nextInt(chars.length)]) }
        }
        prefs.edit().putString("code", body).apply()
        return body
    }

    fun regenerate(): String {
        code = generateAndSave()
        return code
    }
}

data class AntivirusReport(
    val appName: String,
    val permissions: List<String>,
    val risks: List<String>,
    val level: String // OK / ВНИМАНИЕ / РИСК
)

object AntivirusScanner {
    fun scan(app: CatalogApp): AntivirusReport {
        val risks = mutableListOf<String>()
        val perms = app.permissions.ifEmpty {
            when (app.installSource) {
                InstallSource.PLAY -> listOf("Зависит от Google Play")
                InstallSource.GITHUB -> listOf("INTERNET", "REQUEST_INSTALL_PACKAGES")
                else -> listOf("См. источник")
            }
        }
        if (perms.any { it.contains("INSTALL", true) }) {
            risks.add("Может устанавливать пакеты")
        }
        if (perms.any { it.contains("INTERNET", true) }) {
            risks.add("Доступ в сеть")
        }
        if (!app.isOfficial) {
            risks.add("Стороннее приложение — проверь автора: ${app.author}")
        }
        if (app.installSource == InstallSource.PLAY) {
            risks.add("Источник: Google Play (редирект)")
        }
        val level = when {
            risks.size >= 3 -> "ВНИМАНИЕ"
            risks.isNotEmpty() -> "ОК с замечаниями"
            else -> "ОК"
        }
        return AntivirusReport(app.name, perms, risks, level)
    }
}
