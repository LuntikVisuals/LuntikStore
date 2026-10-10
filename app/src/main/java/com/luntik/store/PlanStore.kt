package com.luntik.store

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Base64
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class SnakePlan(val title: String, val price: Int, val days: Int, val updatePrice: Int, val battlePass: Boolean) {
    ARTIST("Художник", 1_000, 30, 300, false), WRITER("Писатель", 2_000, 30, 30, false), MAESTRO("Маэстро", 10_000, 37, 0, false), MAMMA("Мама Мия", 15_000, 90, 0, true)
}
enum class StorePlan(val title: String, val days: Int, val price: Int) {
    ORO_30("Oro", 30, 8_000), ORO_90("Oro", 90, 20_000), ORO_180("Oro", 180, 35_000),
    PLATINO_30("Platino", 30, 20_000), PLATINO_90("Platino", 90, 50_000), PLATINO_180("Platino", 180, 90_000),
    TITANO_30("Titano", 30, 45_000), TITANO_90("Titano", 90, 110_000), TITANO_180("Titano", 180, 200_000),
    ARGENTO_30("Argento", 30, 100_000), ARGENTO_90("Argento", 90, 200_000), ARGENTO_180("Argento", 180, 400_000),
    ARGENTO_360("Argento", 360, 10_000_000), ARGENTO_5Y("Argento", 365 * 5, 50_000_000)
}
class PlanStore(private val ctx: Context) {
    private val p = ctx.getSharedPreferences("luntik_plans", Context.MODE_PRIVATE)
    private val pm = ctx.packageManager
    init { grantLegacySnake(); resumeIfPauseEnded(); remindSoon() }
    var snakePlan: String get() = p.getString("snakePlan", "")!!; set(v) = p.edit().putString("snakePlan", v).apply()
    var snakeUntil: Long get() = p.getLong("snakeUntil", 0); set(v) = p.edit().putLong("snakeUntil", v).apply()
    var storePlan: String get() = p.getString("storePlan", "")!!; set(v) = p.edit().putString("storePlan", v).apply()
    var storeUntil: Long get() = p.getLong("storeUntil", 0); set(v) = p.edit().putLong("storeUntil", v).apply()
    var remoteTag: String get() = p.getString("remoteTag", "")!!; set(v) = p.edit().putString("remoteTag", v).apply()
    var freeGame: String get() = p.getString("freeGame", "")!!; set(v) = p.edit().putString("freeGame", v).apply()
    var pauseUsed: Boolean get() = p.getBoolean("pauseUsed", false); set(v) = p.edit().putBoolean("pauseUsed", v).apply()
    var pausedUntil: Long get() = p.getLong("pausedUntil", 0); set(v) = p.edit().putLong("pausedUntil", v).apply()
    var pauseLeft: Long get() = p.getLong("pauseLeft", 0); set(v) = p.edit().putLong("pauseLeft", v).apply()
    var familyPaid: Boolean get() = p.getBoolean("familyPaid", false); set(v) = p.edit().putBoolean("familyPaid", v).apply()
    var familySlots: Int get() = p.getInt("slots", 1); set(v) = p.edit().putInt("slots", v).apply()
    fun paused() = pausedUntil > System.currentTimeMillis()
    fun snakeActive() = !paused() && snakeUntil > System.currentTimeMillis()
    fun storeActive() = !paused() && storeUntil > System.currentTimeMillis()
    fun isFree(appId: String) = tier() >= 4 && freeGame == appId && storeActive()
    fun tier(): Int = if (!storeActive()) 0 else when { storePlan.startsWith("ARGENTO") -> 4; storePlan.startsWith("TITANO") -> 3; storePlan.startsWith("PLATINO") -> 2; storePlan.startsWith("ORO") -> 1; else -> 0 }
    fun perks() = buildList { if (tier() >= 1) add("блок рекламы"); if (tier() >= 2) add("Master в кошельке"); if (tier() >= 3) add("без комиссии"); if (tier() >= 4) add("1 бесплатная игра") }
    fun battlePass() = snakeActive() && runCatching { SnakePlan.valueOf(snakePlan).battlePass }.getOrDefault(false)
    fun receipts() = p.getString("receipts", "")!!.lines().filter { it.isNotBlank() }.take(8)
    fun addReceipt(line: String) { p.edit().putString("receipts", (listOf(line) + receipts()).take(8).joinToString("\n")).apply(); log(line) }
    fun log(line: String) { p.edit().putString("log", (listOf(line) + journal()).take(20).joinToString("\n")).apply() }
    fun journal() = p.getString("log", "")!!.lines().filter { it.isNotBlank() }
    fun auto(appId: String) = p.getBoolean("auto_$appId", false)
    fun setAuto(appId: String, v: Boolean) { p.edit().putBoolean("auto_$appId", v).apply() }
    fun mainStatus(): String {
        val s = if (storeActive()) "${storePlan.substringBefore('_')} · ещё ${((storeUntil - System.currentTimeMillis()) / 86_400_000L).coerceAtLeast(0)} д" else "нет подписки"
        val z = if (snakeActive()) "змейка ${((snakeUntil - System.currentTimeMillis()) / 86_400_000L).coerceAtLeast(0)} д" else "змейка нет"
        return "$s · $z"
    }
    fun buySnake(plan: SnakePlan) { snakePlan = plan.name; snakeUntil = System.currentTimeMillis() + plan.days * 86_400_000L; pauseUsed = false; addReceipt("${plan.title} · ${plan.price} LC") }
    fun buyStore(plan: StorePlan) { storePlan = plan.name; storeUntil = System.currentTimeMillis() + plan.days * 86_400_000L; pauseUsed = false; familyPaid = false; familySlots = 1; addReceipt("${plan.title} ${plan.days} д · ${plan.price} LC"); SettingsStore(ctx).updateHideAds(true) }
    fun familyPrice() = StorePlan.entries.find { it.name == storePlan }?.price?.div(2) ?: 0
    fun unlockFamily() { familyPaid = true; addReceipt("Семейный слот · ${familyPrice()} LC") }
    fun pause() { if (pauseUsed || paused() || (!snakeActive() && !storeActive())) return; pauseLeft = maxOf(snakeUntil, storeUntil) - System.currentTimeMillis(); pausedUntil = System.currentTimeMillis() + 7 * 86_400_000L; pauseUsed = true; log("пауза 7 д") }
    fun updateCost() = runCatching { SnakePlan.valueOf(snakePlan).updatePrice }.getOrDefault(300)
    fun freeUpdates() = snakeActive() && updateCost() == 0
    fun statusLine(): String {
        if (paused()) return "Пауза до " + SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(pausedUntil))
        if (!snakeActive() && snakePlan.isBlank()) return "Змейка · нет лицензии"
        val name = runCatching { SnakePlan.valueOf(snakePlan).title }.getOrDefault("—")
        return "Змейка · $name · до " + SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(snakeUntil))
    }
    fun installed(pkg: String) = runCatching { pm.getPackageInfo(pkg, 0); true }.getOrDefault(false)
    fun linked() = Catalog.apps.filter { it.isOfficial && installed(it.packageName) }.map { it.name }
    fun exportBackup(): String { val f = File(ctx.filesDir, "luntik_backup.txt"); f.writeText("snake=$snakePlan:$snakeUntil\nstore=$storePlan:$storeUntil\nfree=$freeGame"); return f.absolutePath }
    fun familyCode(): String { if (!storeActive() || !familyPaid || familySlots >= 2) return ""; return Base64.encodeToString("$storePlan|$storeUntil|1".toByteArray(), Base64.NO_WRAP) }
    fun takeFamily(code: String): Boolean {
        val raw = runCatching { String(Base64.decode(code.trim(), Base64.NO_WRAP)) }.getOrNull() ?: return false
        val parts = raw.split("|"); if (parts.size < 2) return false
        val until = parts[1].toLongOrNull() ?: return false
        if (until < System.currentTimeMillis()) return false
        if ((parts.getOrNull(2)?.toIntOrNull() ?: 1) >= 2) return false
        storePlan = parts[0]; storeUntil = until; familyPaid = false; familySlots = 2; log("семейный слот активирован"); return true
    }
    fun makeGift(kind: String): String { val code = Random.nextInt(100000, 999999).toString(); p.edit().putString("gift_$code", kind).apply(); log("подарок $kind"); return code }
    fun takeGift(code: String): Boolean {
        val kind = p.getString("gift_$code", null) ?: return false
        p.edit().remove("gift_$code").apply()
        val plan = StorePlan.entries.find { it.name == kind } ?: SnakePlan.entries.find { it.name == kind }
        when (plan) { is StorePlan -> buyStore(plan); is SnakePlan -> buySnake(plan); else -> return false }
        log("подарок принят $kind"); return true
    }
    private fun grantLegacySnake() { if (p.getBoolean("legacy", false)) return; if (installed("com.luntik.snake")) { snakePlan = SnakePlan.ARTIST.name; snakeUntil = System.currentTimeMillis() + 10 * 86_400_000L }; p.edit().putBoolean("legacy", true).apply() }
    private fun resumeIfPauseEnded() { if (pausedUntil == 0L || paused()) return; val add = pauseLeft; if (add > 0) { if (snakeUntil > 0) snakeUntil = System.currentTimeMillis() + add; if (storeUntil > 0) storeUntil = System.currentTimeMillis() + add }; pausedUntil = 0; pauseLeft = 0 }
    private fun remindSoon() {
        val left = snakeUntil - System.currentTimeMillis()
        if (left <= 0 || left > 3 * 86_400_000L || p.getBoolean("reminded", false)) return
        p.edit().putBoolean("reminded", true).apply()
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(NotificationChannel("plans", "Тарифы", NotificationManager.IMPORTANCE_DEFAULT))
        val n = if (Build.VERSION.SDK_INT >= 26) android.app.Notification.Builder(ctx, "plans") else @Suppress("DEPRECATION") android.app.Notification.Builder(ctx)
        nm.notify(41, n.setContentTitle("Змейка").setContentText("Лицензия закончится меньше чем через 3 дня").setSmallIcon(android.R.drawable.ic_dialog_info).build())
    }
}
