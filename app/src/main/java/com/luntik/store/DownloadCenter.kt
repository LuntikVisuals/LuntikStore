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
    val appId: String, val name: String, val fromUrl: String, val toPath: String,
    val progress: Float, val status: String, val finished: Boolean = false, val error: String? = null
)

object DownloadCenter {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val items: SnapshotStateList<DownloadItem> = mutableStateListOf()
    private val activeIds = mutableSetOf<String>()
    private var appContext: Context? = null
    fun init(context: Context) { if (appContext != null) return; appContext = context.applicationContext; loadLog() }
    fun isDownloading(appId: String) = appId in activeIds
    fun upsert(item: DownloadItem) { val i = items.indexOfFirst { it.appId == item.appId }; if (i >= 0) items[i] = item else items.add(0, item); persistLog() }

    fun startGithubDownload(context: Context, app: CatalogApp, url: String, onInstalled: () -> Unit = {}) {
        init(context)
        val plans = PlanStore(context)
        if (app.price > 0 && !plans.isFree(app.id) && !(app.id == "snake" && plans.snakeActive())) {
            upsert(DownloadItem(app.id, app.name, url, "", 0f, "Сначала оплати в интеграциях", true, "need_pay"))
            return
        }
        if (app.id in activeIds) return
        activeIds.add(app.id)
        val destName = "${app.id}.apk"
        upsert(DownloadItem(app.id, app.name, url, File(context.cacheDir, destName).absolutePath, 0f, "Скачивание…"))
        scope.launch {
            val result = ApkDownloader.download(context, url, destName) { p ->
                upsert(DownloadItem(app.id, app.name, url, File(context.cacheDir, destName).absolutePath, p, "Скачивание ${(p * 100).toInt()}%"))
            }
            activeIds.remove(app.id)
            if (result.success && result.file != null) {
                upsert(DownloadItem(app.id, app.name, url, result.file.absolutePath, 1f, "Готово — установка", true))
                withContext(Dispatchers.Main) { ApkDownloader.install(context, result.file); onInstalled() }
            } else upsert(DownloadItem(app.id, app.name, url, "", 0f, "Ошибка", true, result.error))
        }
    }
    private fun persistLog() {
        val ctx = appContext ?: return
        try {
            val arr = JSONArray()
            items.take(20).forEach { d -> arr.put(JSONObject().put("appId", d.appId).put("name", d.name).put("fromUrl", d.fromUrl).put("toPath", d.toPath).put("progress", d.progress.toDouble()).put("status", d.status).put("finished", d.finished).put("error", d.error ?: "")) }
            ctx.getSharedPreferences("luntik_downloads", Context.MODE_PRIVATE).edit().putString("log", arr.toString()).apply()
        } catch (_: Exception) {}
    }
    private fun loadLog() {
        val ctx = appContext ?: return
        try {
            val arr = JSONArray(ctx.getSharedPreferences("luntik_downloads", Context.MODE_PRIVATE).getString("log", "[]") ?: "[]")
            items.clear()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                items.add(DownloadItem(o.getString("appId"), o.getString("name"), o.optString("fromUrl"), o.optString("toPath"), o.optDouble("progress").toFloat(), o.optString("status"), o.optBoolean("finished"), o.optString("error").ifBlank { null }))
            }
        } catch (_: Exception) {}
    }
}
