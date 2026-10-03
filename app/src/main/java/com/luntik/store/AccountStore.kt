package com.luntik.store

import android.content.Context
import java.security.MessageDigest
import java.util.UUID

data class Account(
    val id: String,
    val username: String,
    val displayName: String
)

class AccountStore(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = context.getSharedPreferences("luntik_account", Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean = prefs.getString("user_id", null) != null

    fun current(): Account? {
        val id = prefs.getString("user_id", null) ?: return null
        val username = prefs.getString("username", "") ?: ""
        val display = prefs.getString("display_name", username) ?: username
        return Account(id, username, display)
    }

    fun hasAcceptedTerms(): Boolean = prefs.getBoolean("terms_ok", false)

    fun acceptTerms() {
        prefs.edit().putBoolean("terms_ok", true).apply()
    }

    fun register(username: String, password: String, displayName: String): String? {
        val u = username.trim()
        if (u.length < 3) return "Имя пользователя минимум 3 символа"
        if (password.length < 4) return "Пароль минимум 4 символа"
        if (!hasAcceptedTerms()) return "Нужно принять пользовательское соглашение"
        val id = UUID.randomUUID().toString()
        val hash = sha256(password)
        prefs.edit()
            .putString("user_id", id)
            .putString("username", u)
            .putString("display_name", displayName.ifBlank { u })
            .putString("password_hash", hash)
            .apply()
        LocalFolders.ensureStructure(appContext)
        LocalFolders.writeAccountMeta(appContext, u, hash)
        LocalFolders.writeProfile(appContext, u, displayName.ifBlank { u })
        return null
    }

    fun login(username: String, password: String): String? {
        val saved = prefs.getString("username", null)
        val hash = prefs.getString("password_hash", null)
        if (saved == null || hash == null) return "Сначала зарегистрируйтесь"
        if (saved != username.trim()) return "Неверный логин"
        if (hash != sha256(password)) return "Неверный пароль"
        LocalFolders.ensureStructure(appContext)
        val display = prefs.getString("display_name", saved) ?: saved
        LocalFolders.writeProfile(appContext, saved, display)
        return null
    }

    /** Проверка пароля для облачного замка (без смены сессии). */
    fun verifyPassword(password: String): Boolean {
        val hash = prefs.getString("password_hash", null) ?: return false
        return hash == sha256(password)
    }

    fun updateDisplayName(name: String): String? {
        val n = name.trim()
        if (n.length < 2) return "Ник минимум 2 символа"
        if (prefs.getString("user_id", null) == null) return "Нет аккаунта"
        prefs.edit().putString("display_name", n).apply()
        val u = prefs.getString("username", n) ?: n
        LocalFolders.writeProfile(appContext, u, n)
        return null
    }

    fun changePassword(oldPassword: String, newPassword: String): String? {
        val hash = prefs.getString("password_hash", null) ?: return "Нет аккаунта"
        if (hash != sha256(oldPassword)) return "Неверный текущий пароль"
        if (newPassword.length < 4) return "Новый пароль минимум 4 символа"
        val newHash = sha256(newPassword)
        prefs.edit().putString("password_hash", newHash).apply()
        val u = prefs.getString("username", "") ?: ""
        LocalFolders.writeAccountMeta(appContext, u, newHash)
        return null
    }

    fun logout() {
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
