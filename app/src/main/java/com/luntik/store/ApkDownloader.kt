package com.luntik.store

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

object ApkDownloader {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    data class Result(
        val success: Boolean,
        val file: File? = null,
        val error: String? = null,
        val bytes: Long = 0
    )

    suspend fun download(
        context: Context,
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit = {}
    ): Result = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "LuntikStore/0.2")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result(
                        false,
                        error = "HTTP ${response.code}. Релиз ещё не опубликован?"
                    )
                }
                val body = response.body
                    ?: return@withContext Result(false, error = "Пустой ответ")

                val total = body.contentLength()
                val file = File(context.cacheDir, fileName)

                body.byteStream().use { input ->
                    file.outputStream().use { output ->
                        val buf = ByteArray(8192)
                        var done = 0L
                        var n: Int
                        while (input.read(buf).also { n = it } != -1) {
                            output.write(buf, 0, n)
                            done += n
                            if (total > 0) onProgress(done.toFloat() / total)
                        }
                    }
                }

                if (file.length() < 1000) {
                    file.delete()
                    return@withContext Result(false, error = "Файл слишком маленький")
                }
                Result(true, file = file, bytes = file.length())
            }
        } catch (e: Exception) {
            Result(false, error = e.message ?: "Ошибка сети")
        }
    }

    fun install(context: Context, apk: File): Boolean {
        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apk
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun canInstall(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else true
    }

    fun openInstallSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startActivity(
                Intent(
                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun isInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openApp(context: Context, packageName: String): Boolean {
        return try {
            val i = context.packageManager.getLaunchIntentForPackage(packageName)
            if (i != null) {
                context.startActivity(i)
                true
            } else false
        } catch (_: Exception) {
            false
        }
    }
}
