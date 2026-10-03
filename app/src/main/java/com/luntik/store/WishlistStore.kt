package com.luntik.store

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Желаемое: appId -> timestamp.
 * version++ на каждое изменение — UI сразу перерисовывается.
 */
class WishlistStore(context: Context) {
    private val prefs = context.getSharedPreferences("luntik_wishlist", Context.MODE_PRIVATE)
    private val key = "map"

    /** Меняется при toggle — читайте в @Composable для рекомпозиции */
    var version by mutableIntStateOf(0)
        private set

    private fun load(): MutableMap<String, Long> {
        val raw = prefs.getString(key, "{}") ?: "{}"
        val json = JSONObject(raw)
        val map = mutableMapOf<String, Long>()
        json.keys().forEach { k -> map[k] = json.getLong(k) }
        return map
    }

    private fun save(map: Map<String, Long>) {
        val json = JSONObject()
        map.forEach { (k, v) -> json.put(k, v) }
        prefs.edit().putString(key, json.toString()).apply()
        version++
    }

    fun isWished(appId: String): Boolean = load().containsKey(appId)

    fun toggle(appId: String): Boolean {
        val map = load()
        return if (map.containsKey(appId)) {
            map.remove(appId)
            save(map)
            false
        } else {
            map[appId] = System.currentTimeMillis()
            save(map)
            true
        }
    }

    fun all(): List<Pair<String, Long>> =
        load().entries.map { it.key to it.value }.sortedByDescending { it.second }

    fun daysInWishlist(appId: String): Long {
        val t = load()[appId] ?: return 0
        return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - t)
    }

    fun wishlistDiscountPercent(appId: String): Int {
        val d = daysInWishlist(appId)
        return when {
            d >= 30 -> 25
            d >= 10 -> 15
            d >= 2 -> 10
            else -> 0
        }
    }
}

object PromoCalendar {
    fun activePromoPercent(now: Long = System.currentTimeMillis()): Pair<Int, String>? {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = now }
        val month = cal.get(java.util.Calendar.MONTH)
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)

        if ((month == java.util.Calendar.OCTOBER && day >= 25) ||
            (month == java.util.Calendar.NOVEMBER && day == 1)
        ) {
            return 40 to "Хэллоуин"
        }

        val seasonStart = when (month) {
            java.util.Calendar.MARCH -> "Весенние скидки" to (day <= 10)
            java.util.Calendar.JUNE -> "Летние скидки" to (day <= 10)
            java.util.Calendar.SEPTEMBER -> "Осенние скидки" to (day <= 10)
            java.util.Calendar.DECEMBER -> "Зимние скидки" to (day <= 10)
            else -> null
        }
        if (seasonStart != null && seasonStart.second) {
            return 10 to seasonStart.first
        }

        val holidays = listOf(
            java.util.Calendar.JANUARY to 1,
            java.util.Calendar.FEBRUARY to 23,
            java.util.Calendar.MARCH to 8,
            java.util.Calendar.MAY to 1,
            java.util.Calendar.MAY to 9,
            java.util.Calendar.JUNE to 12,
            java.util.Calendar.NOVEMBER to 4,
            java.util.Calendar.DECEMBER to 31
        )
        if (holidays.any { it.first == month && it.second == day }) {
            return 20 to "Праздничная скидка"
        }
        return null
    }

    fun bestDiscount(wishlistPct: Int, now: Long = System.currentTimeMillis()): Pair<Int, String> {
        val promo = activePromoPercent(now)
        val p = promo?.first ?: 0
        return when {
            wishlistPct >= p && wishlistPct > 0 -> wishlistPct to "Желаемое"
            p > 0 -> p to (promo?.second ?: "Акция")
            else -> 0 to ""
        }
    }
}
