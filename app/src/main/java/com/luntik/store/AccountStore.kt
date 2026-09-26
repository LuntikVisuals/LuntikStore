package com.luntik.store

import android.content.Context
import java.security.MessageDigest
import java.util.UUID

data class Account(
    val id: String,
    val username: String,
    val displayName: String
)

/** Локальная регистрация. Пароль хранится только как SHA-256, без отправки на сервер. */
class AccountStore(context: Context) {

    private val prefs = context.getSharedPreferences("luntik_account", Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean = prefs.getString("user_id", null) != null

    fun current(): Account? {
        val id = prefs.getString("user_id", null) ?: return null
        val username = prefs.getString("username", "") ?: ""
        val display = prefs.getString("display_name", username) ?: username
        return Account(id, username, display)
    }

    fun register(username: String, password: String, displayName: String): String? {
        val u = username.trim()
        if (u.length < 3) return "Имя пользователя минимум 3 символа"
        if (password.length < 4) return "Пароль минимум 4 символа"
        if (prefs.getString("username", null) != null && prefs.getString("user_id", null) != null) {
            // already registered on this device — allow re-login with password
        }
        val id = UUID.randomUUID().toString()
        prefs.edit()
            .putString("user_id", id)
            .putString("username", u)
            .putString("display_name", displayName.ifBlank { u })
            .putString("password_hash", sha256(password))
            .apply()
        return null
    }

    fun login(username: String, password: String): String? {
        val saved = prefs.getString("username", null)
        val hash = prefs.getString("password_hash", null)
        if (saved == null || hash == null) return "Сначала зарегистрируйтесь"
        if (saved != username.trim()) return "Неверный логин"
        if (hash != sha256(password)) return "Неверный пароль"
        return null
    }

    fun logout() {
        // keep credentials for re-login, only clear session flag if we add one
        // For local-only: logout clears session display
        prefs.edit().putBoolean("session", false).apply()
    }

    fun setSession(active: Boolean) {
        prefs.edit().putBoolean("session", active).apply()
    }

    fun hasSession(): Boolean = prefs.getBoolean("session", isLoggedIn())

    private fun sha256(s: String): String {
        val d = MessageDigest.getInstance("SHA-256")
        return d.digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
