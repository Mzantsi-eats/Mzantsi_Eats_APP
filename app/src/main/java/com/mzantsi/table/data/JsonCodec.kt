package com.mzantsi.table.data

import org.json.JSONArray
import org.json.JSONObject

// Turns recipes, reviews and community posts into JSON text (to save on the phone) and back again.
// Used by OfflineStore (downloads) and CommunityStore.
internal object JsonCodec {

    fun recipeToJson(r: Recipe) = JSONObject().apply {
        put("recipeId", r.recipeId)
        put("title", r.title)
        put("culture", r.culture)
        put("province", r.province)
        put("description", r.description)
        put("imageRes", r.imageRes)
        put("prepTimeMinutes", r.prepTimeMinutes)
        put("cookTimeMinutes", r.cookTimeMinutes)
        put("servings", r.servings)
        put("difficulty", r.difficulty.name)
        put("ratingAvg", r.ratingAvg.toDouble())
        put("ingredients", JSONArray(r.ingredients))
        put("method", JSONArray(r.method))
        put("authorName", r.authorName)
    }

    fun jsonToRecipe(o: JSONObject) = Recipe(
        recipeId = o.getInt("recipeId"),
        title = o.getString("title"),
        culture = o.optString("culture", "Other"),
        province = o.optString("province"),
        description = o.optString("description"),
        imageRes = o.optString("imageRes"),
        prepTimeMinutes = o.optInt("prepTimeMinutes", 0),
        cookTimeMinutes = o.optInt("cookTimeMinutes", 0),
        servings = o.optInt("servings", 1),
        difficulty = runCatching { Difficulty.valueOf(o.optString("difficulty")) }.getOrDefault(Difficulty.MEDIUM),
        ratingAvg = o.optDouble("ratingAvg", 0.0).toFloat(),
        ingredients = o.optJSONArray("ingredients").strings(),
        method = o.optJSONArray("method").strings(),
        authorName = o.optString("authorName")
    )

    fun reviewToJson(r: Review) = JSONObject().apply {
        put("reviewId", r.reviewId)
        put("recipeId", r.recipeId)
        put("userId", r.userId)
        put("rating", r.rating)
        put("comment", r.comment)
        put("userName", r.userName)
        put("createdAtMillis", r.createdAtMillis)
    }

    fun jsonToReview(o: JSONObject) = Review(
        reviewId = o.optInt("reviewId", 0),
        recipeId = o.getInt("recipeId"),
        userId = o.optInt("userId", 0),
        rating = o.optInt("rating", 0),
        comment = o.optString("comment"),
        userName = o.optString("userName"),
        createdAtMillis = o.optLong("createdAtMillis", 0L)
    )

    fun postToJson(p: CommunityPost) = JSONObject().apply {
        put("postId", p.postId)
        put("authorKey", p.authorKey)
        put("authorName", p.authorName)
        put("recipeId", p.recipeId)
        put("recipeTitle", p.recipeTitle)
        put("communityId", p.communityId)
        put("caption", p.caption)
        put("photoPath", p.photoPath)
        put("createdAtMillis", p.createdAtMillis)
        put("ratings", JSONObject().apply { p.ratings.forEach { (k, v) -> put(k, v) } })
        put("cheers", JSONArray(p.cheers.toList()))
        put("comments", JSONArray().apply {
            p.comments.forEach { c ->
                put(JSONObject().apply {
                    put("authorName", c.authorName)
                    put("text", c.text)
                    put("createdAtMillis", c.createdAtMillis)
                })
            }
        })
        put("isSample", p.isSample)
    }

    fun jsonToPost(o: JSONObject): CommunityPost {
        val ratingsJson = o.optJSONObject("ratings")
        val ratings = mutableMapOf<String, Int>()
        ratingsJson?.keys()?.forEach { k -> ratings[k] = ratingsJson.optInt(k) }
        val commentsJson = o.optJSONArray("comments")
        val comments = if (commentsJson == null) emptyList() else (0 until commentsJson.length()).mapNotNull { i ->
            commentsJson.optJSONObject(i)?.let {
                PostComment(it.optString("authorName"), it.optString("text"), it.optLong("createdAtMillis"))
            }
        }
        return CommunityPost(
            postId = o.getString("postId"),
            authorKey = o.optString("authorKey"),
            authorName = o.optString("authorName"),
            recipeId = o.optInt("recipeId"),
            recipeTitle = o.optString("recipeTitle"),
            communityId = o.optString("communityId"),
            caption = o.optString("caption"),
            photoPath = o.optString("photoPath"),
            createdAtMillis = o.optLong("createdAtMillis"),
            ratings = ratings,
            cheers = o.optJSONArray("cheers").strings().toSet(),
            comments = comments,
            isSample = o.optBoolean("isSample", false)
        )
    }

    fun JSONArray?.strings(): List<String> =
        if (this == null) emptyList() else (0 until length()).map { optString(it) }

    // Reads a JSON array of objects, skipping any item that can't be read instead of crashing.
    fun <T> parseList(raw: String?, read: (JSONObject) -> T): List<T> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { i -> runCatching { read(array.getJSONObject(i)) }.getOrNull() }
        }.getOrDefault(emptyList())
    }

    fun <T> writeList(items: List<T>, write: (T) -> JSONObject): String =
        JSONArray().apply { items.forEach { put(write(it)) } }.toString()
}
