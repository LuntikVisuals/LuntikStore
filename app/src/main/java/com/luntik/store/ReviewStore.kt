package com.luntik.store

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Review(
    val appId: String,
    val author: String,
    val rating: Int,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

/** Локальные отзывы на устройстве. Позже можно синхронизировать с сервером. */
class ReviewStore(context: Context) {

    private val prefs = context.getSharedPreferences("luntik_reviews", Context.MODE_PRIVATE)

    fun getReviews(appId: String): List<Review> {
        val raw = prefs.getString("reviews_$appId", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        Review(
                            appId = o.getString("appId"),
                            author = o.getString("author"),
                            rating = o.getInt("rating"),
                            text = o.getString("text"),
                            timestamp = o.optLong("timestamp", 0L)
                        )
                    )
                }
            }.sortedByDescending { it.timestamp }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addReview(review: Review) {
        val list = getReviews(review.appId).toMutableList()
        list.add(0, review)
        val arr = JSONArray()
        list.forEach { r ->
            arr.put(
                JSONObject()
                    .put("appId", r.appId)
                    .put("author", r.author)
                    .put("rating", r.rating.coerceIn(1, 5))
                    .put("text", r.text)
                    .put("timestamp", r.timestamp)
            )
        }
        prefs.edit().putString("reviews_${review.appId}", arr.toString()).apply()
    }

    fun averageRating(appId: String): Float {
        val list = getReviews(appId)
        if (list.isEmpty()) return 0f
        return list.map { it.rating }.average().toFloat()
    }
}
