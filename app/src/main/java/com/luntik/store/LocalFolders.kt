package com.luntik.store

import android.content.Context
import android.os.Build
import android.os.Environment
import org.json.JSONObject
import java.io.File

/**
 * Папка LuntikStore:
 * предпочтительно Documents/LuntikStore (видна в проводнике),
 * иначе Android/data/.../files/Documents/LuntikStore.
 *
 * 1_Аккаунт — логин (хеш) + WARNING
 * 2_Профиль — ник для друзей
 * 3_Друзья — чужие профили
 */
object LocalFolders {

    private const val ROOT = "LuntikStore"
    private const val ACC = "1_Аккаунт"
    private const val PROF = "2_Профиль"
    private const val FRIENDS = "3_Друзья"
    private const val WARN = "!!!СРОЧНО ПРОЧТИ МЕНЯ ЭТО ВАЖНО!!!!.txt"

    fun root(context: Context): File {
        // 1) Публичные Документы — проще найти в файловом менеджере
        try {
            val publicDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val publicRoot = File(publicDocs, ROOT)
            if (publicRoot.exists() || publicRoot.mkdirs()) {
                if (publicRoot.canWrite()) return publicRoot
            }
        } catch (_: Exception) { }

        // 2) App-specific external Documents
        try {
            val ext = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (ext != null) {
                val r = File(ext, ROOT)
                if (r.exists() || r.mkdirs()) return r
            }
        } catch (_: Exception) { }

        // 3) Internal
        return File(context.filesDir, ROOT).also { it.mkdirs() }
    }

    fun rootPath(context: Context): String = root(context).absolutePath

    fun ensureStructure(context: Context) {
        val r = root(context)
        File(r, ACC).mkdirs()
        File(r, PROF).mkdirs()
        File(r, FRIENDS).mkdirs()
        val warn = File(File(r, ACC), WARN)
        if (!warn.exists()) {
            warn.writeText(
                """
                ⚠ ВАЖНО — ПРОЧИТАЙ

                В этой папке (1_Аккаунт) хранятся данные входа.
                НЕ ОТПРАВЛЯЙ файлы из этой папки НИКОМУ —
                даже разработчикам LuntikVisuals.
                Это только твои личные данные.

                Для друзей отправляй файл только из папки 2_Профиль.

                Путь к корневой папке смотри в Настройки → Друзья.
                """.trimIndent()
            )
        }
        // README в корне чтобы папка точно создалась и была заметна
        val readme = File(r, "ЧИТАЙ_МЕНЯ.txt")
        if (!readme.exists()) {
            readme.writeText(
                "LuntikStore data folder\n" +
                    "1_Аккаунт — секретно\n" +
                    "2_Профиль — можно делиться с друзьями\n" +
                    "3_Друзья — чужие профили\n"
            )
        }
    }

    fun writeAccountMeta(context: Context, username: String, passwordHash: String) {
        ensureStructure(context)
        val f = File(File(root(context), ACC), "account.txt")
        f.writeText("username=$username\npassword_hash=$passwordHash\n")
    }

    fun writeProfile(
        context: Context,
        username: String,
        displayName: String,
        level: Int = 1,
        achievements: Int = 0
    ) {
        ensureStructure(context)
        val json = JSONObject()
            .put("username", username)
            .put("displayName", displayName)
            .put("level", level)
            .put("achievements", achievements)
            .put("exportedAt", System.currentTimeMillis())
        File(File(root(context), PROF), "profile_$username.json").writeText(json.toString(2))
    }

    fun listFriends(context: Context): List<FriendProfile> {
        ensureStructure(context)
        val dir = File(root(context), FRIENDS)
        return dir.listFiles()?.filter { it.extension == "json" }?.mapNotNull { f ->
            try {
                val o = JSONObject(f.readText())
                FriendProfile(
                    username = o.optString("username"),
                    displayName = o.optString("displayName"),
                    level = o.optInt("level", 1),
                    achievements = o.optInt("achievements", 0),
                    fileName = f.name
                )
            } catch (_: Exception) {
                null
            }
        } ?: emptyList()
    }

    fun importFriendProfile(context: Context, source: File): String? {
        ensureStructure(context)
        return try {
            val o = JSONObject(source.readText())
            val u = o.optString("username").ifBlank { return "Нет username в файле" }
            val dest = File(File(root(context), FRIENDS), "profile_$u.json")
            source.copyTo(dest, overwrite = true)
            null
        } catch (e: Exception) {
            e.message ?: "Ошибка импорта"
        }
    }

    fun profileDir(context: Context): File = File(root(context), PROF).also { it.mkdirs() }
    fun friendsDir(context: Context): File = File(root(context), FRIENDS).also { it.mkdirs() }
}

data class FriendProfile(
    val username: String,
    val displayName: String,
    val level: Int,
    val achievements: Int,
    val fileName: String
)
