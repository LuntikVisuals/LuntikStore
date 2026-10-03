package com.luntik.store

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import org.json.JSONArray

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

    fun remove(id: String) {
        save(ids().filter { it != id })
    }

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
    val status: String
)

/** Активные/недавние загрузки (в памяти + простой лог). */
class DownloadCenter {
    val items = mutableStateListOf<DownloadItem>()

    fun upsert(item: DownloadItem) {
        val i = items.indexOfFirst { it.appId == item.appId }
        if (i >= 0) items[i] = item else items.add(0, item)
    }

    fun remove(appId: String) {
        items.removeAll { it.appId == appId }
    }
}
