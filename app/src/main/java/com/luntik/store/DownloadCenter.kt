package com.luntik.store

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class DownloadItem(
    val appId: String,
    val name: String,
    val fromUrl: String,
    val toPath: String,
    val progress: Float,
    val status: String,
    val finished: Boolean = false,
    val error: String? = null
)

/**
 * Глобальный центр загрузок: не сбрасывается при уходе с экрана.
 * Живёт в процессе приложения + пишет лог в prefs.
 */
object DownloadCenter {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val items: SnapshotStateList<DownloadItem> = mutableStateListOf()
    private val activeIds = mutableSetOf<String>()
    private var appContext: Context? = null

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        loadLog()
    }

    fun isDownloading(appId: String): Boolean = appId in activeIds

    fun upsert(item: DownloadItem) {
        val i = items.indexOfFirst { it.appId == item.appId }
        if (i >= 0) items[i] = item else items.add(0, item)
        persistLog()
    }

    fun startGithubDownload(
        context: Context,
        app: CatalogApp,
        url: String,
        onInstalled: () -> Unit = {}
    ) {
        init(context)
        if (app.id in activeIds) return
        activeIds.add(app.id)
        val destName = "${app.id}.apk"
        upsert(
            DownloadItem(
                appId = app.id,
                name = app.name,
                fromUrl = url,
                toPath = File(context.cacheDir, destName).absolutePath,
                progress = 0f,
                status = "Скачивание…"
            )
        )
        scope.launch {
            val result = ApkDownloader.download(context, url, destName) { p ->
                upsert(
                    DownloadItem(
                        appId = app.id,
                        name = app.name,
                        fromUrl = url,
                        toPath = File(context.cacheDir, destName).absolutePath,
                        progress = p,
                        status = "Скачивание ${(p * 100).toInt()}%"
                    )
                )
            }
            activeIds.remove(app.id)
            if (result.success && result.file != null) {
                upsert(
                    DownloadItem(
                        appId = app.id,
                        name = app.name,
                        fromUrl = url,
                        toPath = result.file.absolutePath,
                        progress = 1f,
                        status = "Готово — установка",
                        finished = true
                    )
                )
                withContext(Dispatchers.Main) {
                    ApkDownloader.install(context, result.file)
                    onInstalled()
                }
            } else {
                upsert(
                    DownloadItem(
                        appId = app.id,
                        name = app.name,
                        fromUrl = url,
                        toPath = "",
                        progress = 0f,
                        status = "Ошибка",
                        finished = true,
                        error = result.error
                    )
                )
            }
        }
    }

    private fun persistLog() {
        val ctx = appContext ?: return
        try {
            val arr = JSONArray()
            items.take(20).forEach { d ->
                arr.put(
                    JSONObject()
                        .put("appId", d.appId)
                        .put("name", d.name)
                        .put("fromUrl", d.fromUrl)
                        .put("toPath", d.toPath)
                        .put("progress", d.progress.toDouble())
                        .put("status", d.status)
                        .put("finished", d.finished)
                        .put("error", d.error ?: "")
                )
            }
            ctx.getSharedPreferences("luntik_downloads", Context.MODE_PRIVATE)
                .edit().putString("log", arr.toString()).apply()
        } catch (_: Exception) { }
    }

    private fun loadLog() {
        val ctx = appContext ?: return
        try {
            val raw = ctx.getSharedPreferences("luntik_downloads", Context.MODE_PRIVATE)
                .getString("log", "[]") ?: "[]"
            val arr = JSONArray(raw)
            items.clear()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                items.add(
                    DownloadItem(
                        appId = o.getString("appId"),
                        name = o.getString("name"),
                        fromUrl = o.optString("fromUrl"),
                        toPath = o.optString("toPath"),
                        progress = o.optDouble("progress", 0.0).toFloat(),
                        status = o.optString("status"),
                        finished = o.optBoolean("finished"),
                        error = o.optString("error").ifBlank { null }
                    )
                )
            }
        } catch (_: Exception) { }
    }
}
