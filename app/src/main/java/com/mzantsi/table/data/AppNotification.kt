package com.mzantsi.table.data

import org.json.JSONObject

enum class NotificationType { POST_RATED, POST_CHEERED, POST_COMMENT, RECIPE_REVIEW, NEW_RECIPE, NEW_POST }

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val fromName: String = "",
    val route: String = "",
    val createdAtMillis: Long,
    val read: Boolean = false,
    val isLocal: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("type", type.name)
        put("title", title)
        put("body", body)
        put("fromName", fromName)
        put("route", route)
        put("createdAtMillis", createdAtMillis)
        put("read", read)
    }

    companion object {
        fun fromJson(o: JSONObject) = AppNotification(
            id = o.getString("id"),
            type = runCatching { NotificationType.valueOf(o.optString("type")) }.getOrDefault(NotificationType.NEW_POST),
            title = o.optString("title"),
            body = o.optString("body"),
            fromName = o.optString("fromName"),
            route = o.optString("route"),
            createdAtMillis = o.optLong("createdAtMillis", 0L),
            read = o.optBoolean("read", false),
            isLocal = true
        )

        fun newId(prefix: String = "n"): String =
            prefix + System.currentTimeMillis().toString(36) + (1000..9999).random()
    }
}
