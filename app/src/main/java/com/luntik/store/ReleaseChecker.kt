package com.luntik.store

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class RemoteRelease(
    val available: Boolean,
    val downloadUrl: String?,
    val publishedAt: String?,
    val tagName: String?,
    val assetName: String?,
    val error: String? = null
)

object ReleaseChecker {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val PREFS = "luntik_release_meta"

    suspend fun fetchLatest(app: CatalogApp): RemoteRelease = withContext(Dispatchers.IO) {
        if (app.githubRepo.isBlank() ||
            app.installSource == InstallSource.PLAY ||
            app.installSource == InstallSource.OFFICIAL_SITE
        ) {
            return@withContext RemoteRelease(
                available = false,
                downloadUrl = null,
                publishedAt = null,
                tagName = null,
                assetName = null,
                error = "Внешний источник (не GitHub)"
            )
        }
        try {
            val url = "https://api.github.com/repos/${app.githubRepo}/releases/latest"
            val req = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "LuntikStore/0.6")
                .build()

            client.newCall(req).execute().use { resp ->
                if (resp.code == 404) {
                    return@withContext RemoteRelease(
                        available = false,
                        downloadUrl = null,
                        publishedAt = null,
                        tagName = null,
                        assetName = null,
                        error = "Релиз ещё не опубликован на GitHub"
                    )
                }
                if (!resp.isSuccessful) {
                    return@withContext RemoteRelease(
                        available = false,
                        downloadUrl = null,
                        publishedAt = null,
                        tagName = null,
                        assetName = null,
                        error = "GitHub API: HTTP ${resp.code}"
                    )
                }
                val body = resp.body?.string() ?: return@withContext RemoteRelease(
                    false, null, null, null, null, "Пустой ответ GitHub"
                )
                val json = JSONObject(body)
                val tag = json.optString("tag_name", "")
                val published = json.optString("published_at", "")
                val assets = json.optJSONArray("assets")

                var apkUrl: String? = null
                var assetName: String? = null
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val a = assets.getJSONObject(i)
                        val name = a.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            if (name.equals(app.apkAssetName, ignoreCase = true) || apkUrl == null) {
                                apkUrl = a.optString("browser_download_url", null)
                                assetName = name
                                if (name.equals(app.apkAssetName, ignoreCase = true)) break
                            }
                        }
                    }
                }

                if (apkUrl.isNullOrBlank()) {
                    apkUrl = app.downloadUrlFallback
                    assetName = app.apkAssetName
                }

                RemoteRelease(
                    available = true,
                    downloadUrl = apkUrl,
                    publishedAt = published,
                    tagName = tag,
                    assetName = assetName
                )
            }
        } catch (e: Exception) {
            RemoteRelease(
                available = false,
                downloadUrl = app.downloadUrlFallback,
                publishedAt = null,
                tagName = null,
                assetName = null,
                error = e.message
            )
        }
    }

    fun markInstalledRelease(context: Context, appId: String, publishedAt: String?) {
        if (publishedAt.isNullOrBlank()) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("published_$appId", publishedAt)
            .apply()
    }

    fun lastKnownPublished(context: Context, appId: String): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("published_$appId", null)
    }

    fun hasUpdate(context: Context, appId: String, remotePublishedAt: String?): Boolean {
        if (remotePublishedAt.isNullOrBlank()) return false
        val local = lastKnownPublished(context, appId) ?: return true
        return remotePublishedAt > local
    }
}
