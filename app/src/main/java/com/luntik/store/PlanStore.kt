package com.luntik.store

import android.content.Context
import android.content.pm.PackageManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SnakePlan(val title: String, val price: Int, val days: Int, val updatePrice: Int, val battlePass: Boolean) {
    ARTIST("Художник", 1_000, 30, 300, false),
    WRITER("Писатель", 2_000, 30, 30, false),
    MAESTRO("Маэстро", 10_000, 37, 0, false),
    MAMMA("Мама Мия", 15_000, 90, 0, true)
}

enum class StorePlan(val title: String, val days: Int, val price: Int) {
    ORO_30("Oro", 30, 8_000),
    ORO_90("Oro", 90, 20_000),
    ORO_180("Oro", 180, 35_000),
    PLATINO_30("Platino", 30, 20_000),
    PLATINO_90("Platino", 90, 50_000),
    PLATINO_180("Platino", 180, 90_000),
    TITANO_30("Titano", 30, 45_000),
    TITANO_90("Titano", 90, 110_000),
    TITANO_180("Titano", 180, 200_000),
    ARGENTO_30("Argento", 30, 100_000),
    ARGENTO_90("Argento", 90, 200_000),
    ARGENTO_180("Argento", 180, 400_000),
    ARGENTO_360("Argento", 360, 10_000_000),
    ARGENTO_5Y("Argento", 365 * 5, 50_000_000)
}

class PlanStore(ctx: Context) {
    private val p = ctx.getSharedPreferences("luntik_plans", Context.MODE_PRIVATE)
    private val pm = ctx.packageManager

    init { grantLegacySnake() }

    var snakePlan: String
        get() = p.getString("snakePlan", "")!!
        set(v) = p.edit().putString("snakePlan", v).apply()
    var snakeUntil: Long
        get() = p.getLong("snakeUntil", 0)
        set(v) = p.edit().putLong("snakeUntil", v).apply()
    var storePlan: String
        get() = p.getString("storePlan", "")!!
        set(v) = p.edit().putString("storePlan", v).apply()
    var storeUntil: Long
        get() = p.getLong("storeUntil", 0)
        set(v) = p.edit().putLong("storeUntil", v).apply()
    var installedTag: String
        get() = p.getString("snakeTag", "")!!
        set(v) = p.edit().putString("snakeTag", v).apply()
    var remoteTag: String
        get() = p.getString("remoteTag", "")!!
        set(v) = p.edit().putString("remoteTag", v).apply()

    fun snakeActive() = snakeUntil > System.currentTimeMillis()
    fun storeActive() = storeUntil > System.currentTimeMillis()

    fun buySnake(plan: SnakePlan) {
        snakePlan = plan.name
        snakeUntil = System.currentTimeMillis() + plan.days * 86_400_000L
    }

    fun buyStore(plan: StorePlan) {
        storePlan = plan.name
        storeUntil = System.currentTimeMillis() + plan.days * 86_400_000L
    }

    fun updateCost(): Int {
        val plan = runCatching { SnakePlan.valueOf(snakePlan) }.getOrNull() ?: return 300
        if (!snakeActive()) return plan.updatePrice
        return plan.updatePrice
    }

    fun freeUpdates(): Boolean {
        val plan = runCatching { SnakePlan.valueOf(snakePlan) }.getOrNull() ?: return false
        return snakeActive() && plan.updatePrice == 0
    }

    fun statusLine(): String {
        if (!snakeActive() && snakePlan.isBlank()) return "Змейка · нет лицензии"
        val name = runCatching { SnakePlan.valueOf(snakePlan).title }.getOrDefault("—")
        val until = SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(snakeUntil))
        val upd = if (freeUpdates()) "обновление бесплатно" else "обновление ${updateCost()} LC"
        return "Змейка · $name · до $until · $upd"
    }

    fun installed(pkg: String) = runCatching { pm.getPackageInfo(pkg, 0); true }.getOrDefault(false)

    private fun grantLegacySnake() {
        if (p.getBoolean("legacy", false)) return
        if (installed("com.luntik.snake")) {
            snakePlan = SnakePlan.ARTIST.name
            snakeUntil = System.currentTimeMillis() + 10 * 86_400_000L
        }
        p.edit().putBoolean("legacy", true).apply()
    }
}
