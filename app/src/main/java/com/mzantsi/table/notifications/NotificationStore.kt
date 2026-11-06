package com.mzantsi.table.notifications

import android.content.Context
import android.content.SharedPreferences
import com.mzantsi.table.data.AppNotification
import org.json.JSONArray

object NotificationStore {

    private const val PREFS = "mzantsi_notifications"
    private const val INBOX_PREFIX = "inbox_"
    private const val SHOWN = "shown_ids"
    private const val SEEN_RECIPES_PREFIX = "seen_recipes_"
    private const val SEEN_POSTS_PREFIX = "seen_posts_"
    private const val ENABLED = "enabled"
    private const val MAX_SAVED = 100

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        }
    }

    fun localInbox(userKey: String): List<AppNotification> {
        val raw = prefs?.getString(INBOX_PREFIX + userKey, null) ?: return emptyList()
        return runCatching {
            val a = JSONArray(raw)
            (0 until a.length()).mapNotNull { i -> runCatching { AppNotification.fromJson(a.getJSONObject(i)) }.getOrNull() }
        }.getOrDefault(emptyList())
    }

    private fun saveInbox(userKey: String, list: List<AppNotification>) {
        val trimmed = list.sortedByDescending { it.createdAtMillis }.take(MAX_SAVED)
        val a = JSONArray().apply { trimmed.forEach { put(it.toJson()) } }
        prefs?.edit()?.putString(INBOX_PREFIX + userKey, a.toString())?.apply()
    }

    fun addLocal(userKey: String, n: AppNotification): List<AppNotification> {
        val updated = listOf(n.copy(isLocal = true)) + localInbox(userKey).filter { it.id != n.id }
        saveInbox(userKey, updated)
        return localInbox(userKey)
    }

    fun markLocalRead(userKey: String, ids: Set<String>): List<AppNotification> {
        val updated = localInbox(userKey).map { if (it.id in ids) it.copy(read = true) else it }
        saveInbox(userKey, updated)
        return updated
    }

    fun deleteLocal(userKey: String, ids: Set<String>): List<AppNotification> {
        val updated = localInbox(userKey).filter { it.id !in ids }
        saveInbox(userKey, updated)
        return updated
    }

    @Synchronized
    fun markShown(id: String): Boolean {
        val shown = readSet(SHOWN)
        if (id in shown) return false
        writeSet(SHOWN, (shown.toList().takeLast(300) + id).toSet())
        return true
    }

    fun seenRecipes(userKey: String): Set<String>? = readSetOrNull(SEEN_RECIPES_PREFIX + userKey)
    fun setSeenRecipes(userKey: String, ids: Set<String>) = writeSet(SEEN_RECIPES_PREFIX + userKey, ids)

    fun seenPosts(userKey: String): Set<String>? = readSetOrNull(SEEN_POSTS_PREFIX + userKey)
    fun setSeenPosts(userKey: String, ids: Set<String>) = writeSet(SEEN_POSTS_PREFIX + userKey, ids)

    var enabled: Boolean
        get() = prefs?.getBoolean(ENABLED, true) ?: true
        set(value) { prefs?.edit()?.putBoolean(ENABLED, value)?.apply() }

    private fun readSet(key: String): Set<String> = readSetOrNull(key) ?: emptySet()

    private fun readSetOrNull(key: String): Set<String>? {
        val raw = prefs?.getString(key, null) ?: return null
        return runCatching { JSONArray(raw).let { a -> (0 until a.length()).map { a.optString(it) }.toSet() } }
            .getOrDefault(emptySet())
    }

    private fun writeSet(key: String, values: Set<String>) {
        prefs?.edit()?.putString(key, JSONArray(values.toList()).toString())?.apply()
    }
}
