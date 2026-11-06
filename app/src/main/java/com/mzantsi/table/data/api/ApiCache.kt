package com.mzantsi.table.data.api

import android.content.Context
import android.content.SharedPreferences
import com.mzantsi.table.data.JsonCodec
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.data.Review
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object ApiCache {

    private const val PREFS = "mzantsi_api_cache"
    private const val KEY_RECIPES = "recipes"
    private const val KEY_OUTBOX = "outbox"
    private const val KEY_LAST_SYNC = "last_sync_millis"

    const val TYPE_RECIPE = "recipe"
    const val TYPE_REVIEW = "review"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        }
    }

    class PendingOp(val opId: String, val type: String, val payload: JSONObject, val attempts: Int)

    @Synchronized
    fun recipes(): List<Recipe> =
        JsonCodec.parseList(prefs?.getString(KEY_RECIPES, null)) { o -> JsonCodec.jsonToRecipe(o) }

    @Synchronized
    fun mergeRecipes(items: List<Recipe>) {
        if (items.isEmpty()) return
        val byId = LinkedHashMap<Int, Recipe>()
        recipes().forEach { byId[it.recipeId] = it }
        items.forEach { byId[it.recipeId] = it }
        val text = JsonCodec.writeList(byId.values.toList()) { r -> JsonCodec.recipeToJson(r) }
        prefs?.edit()?.putString(KEY_RECIPES, text)?.apply()
    }

    @Synchronized
    fun lastSyncIso(): String {
        val millis = prefs?.getLong(KEY_LAST_SYNC, 0L) ?: 0L
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(Date(millis))
    }

    @Synchronized
    fun setLastSync(millis: Long) {
        prefs?.edit()?.putLong(KEY_LAST_SYNC, millis)?.apply()
    }

    @Synchronized
    fun outbox(): List<PendingOp> {
        val raw = prefs?.getString(KEY_OUTBOX, null)
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { i ->
                runCatching {
                    val o = array.getJSONObject(i)
                    PendingOp(
                        opId = o.getString("opId"),
                        type = o.getString("type"),
                        payload = o.getJSONObject("payload"),
                        attempts = o.optInt("attempts", 0)
                    )
                }.getOrNull()
            }
        }.getOrDefault(emptyList())
    }

    @Synchronized
    fun outboxSize(): Int = outbox().size

    @Synchronized
    fun enqueueRecipe(recipe: Recipe) {
        enqueue("$TYPE_RECIPE-${recipe.recipeId}", TYPE_RECIPE, JsonCodec.recipeToJson(recipe))
    }

    @Synchronized
    fun enqueueReview(review: Review) {
        enqueue("$TYPE_REVIEW-${review.recipeId}-${review.reviewId}", TYPE_REVIEW, JsonCodec.reviewToJson(review))
    }

    private fun enqueue(opId: String, type: String, payload: JSONObject) {
        val list = outbox()
        if (list.any { it.opId == opId }) return
        write(list + PendingOp(opId, type, payload, 0))
    }

    @Synchronized
    fun remove(opId: String) {
        write(outbox().filter { it.opId != opId })
    }

    @Synchronized
    fun addAttempt(opId: String) {
        write(outbox().map { if (it.opId == opId) PendingOp(it.opId, it.type, it.payload, it.attempts + 1) else it })
    }

    private fun write(items: List<PendingOp>) {
        val array = JSONArray()
        items.forEach { op ->
            array.put(
                JSONObject().apply {
                    put("opId", op.opId)
                    put("type", op.type)
                    put("payload", op.payload)
                    put("attempts", op.attempts)
                }
            )
        }
        prefs?.edit()?.putString(KEY_OUTBOX, array.toString())?.apply()
    }
}
