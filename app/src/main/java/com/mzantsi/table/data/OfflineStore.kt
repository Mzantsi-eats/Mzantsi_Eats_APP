package com.mzantsi.table.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

// Recipes the user downloaded for offline use.
// A download saves the whole recipe (ingredients, method, photo path) and its reviews on the phone,
// so the recipe can be opened and cooked from without internet - even recipes other users added.
// Downloads are kept per user, so two people sharing a phone each have their own list.
object OfflineStore {

    private const val PREFS = "mzantsi_offline"
    private const val PREFIX = "downloads_"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        }
    }

    data class Download(val recipe: Recipe, val reviews: List<Review>, val downloadedAtMillis: Long)

    fun downloads(userKey: String): List<Download> =
        JsonCodec.parseList(prefs?.getString(PREFIX + userKey, null)) { o ->
            Download(
                recipe = JsonCodec.jsonToRecipe(o.getJSONObject("recipe")),
                reviews = JsonCodec.parseList(o.optJSONArray("reviews")?.toString(), JsonCodec::jsonToReview),
                downloadedAtMillis = o.optLong("downloadedAtMillis")
            )
        }.sortedByDescending { it.downloadedAtMillis }

    // Saves (or refreshes) downloads. Returns the new list.
    fun save(userKey: String, items: List<Download>): List<Download> {
        val byId = downloads(userKey).associateBy { it.recipe.recipeId }.toMutableMap()
        items.forEach { byId[it.recipe.recipeId] = it }
        write(userKey, byId.values.toList())
        return downloads(userKey)
    }

    fun remove(userKey: String, recipeId: Int): List<Download> {
        write(userKey, downloads(userKey).filter { it.recipe.recipeId != recipeId })
        return downloads(userKey)
    }

    fun reviews(userKey: String, recipeId: Int): List<Review> =
        downloads(userKey).firstOrNull { it.recipe.recipeId == recipeId }?.reviews ?: emptyList()

    private fun write(userKey: String, items: List<Download>) {
        val text = JsonCodec.writeList(items) { d ->
            JSONObject().apply {
                put("recipe", JsonCodec.recipeToJson(d.recipe))
                put("reviews", org.json.JSONArray(JsonCodec.writeList(d.reviews, JsonCodec::reviewToJson)))
                put("downloadedAtMillis", d.downloadedAtMillis)
            }
        }
        prefs?.edit()?.putString(PREFIX + userKey, text)?.apply()
    }
}
