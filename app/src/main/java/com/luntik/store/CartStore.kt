package com.luntik.store

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class CartStore(context: Context) {
    private val prefs = context.getSharedPreferences("luntik_cart", Context.MODE_PRIVATE)
    private val key = "ids"

    var version by mutableIntStateOf(0)
        private set

    fun ids(): List<String> {
        val raw = prefs.getString(key, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            buildList { for (i in 0 until arr.length()) add(arr.getString(i)) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun contains(id: String): Boolean = ids().contains(id)
    fun add(id: String) {
        val list = ids().toMutableList()
        if (!list.contains(id)) list.add(id)
        save(list)
    }
    fun remove(id: String) = save(ids().filter { it != id })
    fun clear() = save(emptyList())

    private fun save(list: List<String>) {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        prefs.edit().putString(key, arr.toString()).apply()
        version++
    }
}

data class DownloadItem(
    val appId: String,
    val name: String,
    val fromUrl: String,
    val toPath: String,
    val progress: Float,
    val status: String,
    val readyToInstall: Boolean = false
)

/**
 * Центр загрузок: состояние в SharedPreferences + APK в filesDir/apks.
 * Можно уйти с экрана — файл и статус сохраняются, установка доступна позже.
 */
class DownloadCenter(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = context.getSharedPreferences("luntik_downloads", Context.MODE_PRIVATE)
    val items = mutableStateListOf<DownloadItem>()

    init {
        load()
    }

    fun apkDir(): File = File(appContext.filesDir, "apks").also { it.mkdirs() }

    fun apkFile(appId: String): File = File(apkDir(), "$appId.apk")

    fun upsert(item: DownloadItem) {
        val i = items.indexOfFirst { it.appId == item.appId }
        if (i >= 0) items[i] = item else items.add(0, item)
        persist()
    }

    fun remove(appId: String) {
        items.removeAll { it.appId == appId }
        apkFile(appId).delete()
        persist()
    }

    fun get(appId: String): DownloadItem? = items.find { it.appId == appId }

    private fun persist() {
        val arr = JSONArray()
        items.forEach { d ->
            arr.put(
                JSONObject()
                    .put("appId", d.appId)
                    .put("name", d.name)
                    .put("fromUrl", d.fromUrl)
                    .put("toPath", d.toPath)
                    .put("progress", d.progress.toDouble())
                    .put("status", d.status)
                    .put("ready", d.readyToInstall)
            )
        }
        prefs.edit().putString("items", arr.toString()).apply()
    }

    private fun load() {
        val raw = prefs.getString("items", "[]") ?: "[]"
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val path = o.optString("toPath")
                val ready = o.optBoolean("ready") && path.isNotBlank() && File(path).exists()
                items.add(
                    DownloadItem(
                        appId = o.getString("appId"),
                        name = o.optString("name"),
                        fromUrl = o.optString("fromUrl"),
                        toPath = path,
                        progress = o.optDouble("progress", 0.0).toFloat(),
                        status = if (ready) "Готово — можно установить" else o.optString("status"),
                        readyToInstall = ready
                    )
                )
            }
        } catch (_: Exception) { }
    }
}
