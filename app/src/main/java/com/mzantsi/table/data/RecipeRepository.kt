package com.mzantsi.table.data

import android.util.Log
import com.mzantsi.table.data.api.ApiCache
import com.mzantsi.table.data.api.LoginRequest
import com.mzantsi.table.data.api.MzantsiApiService
import com.mzantsi.table.data.api.RecipeRequest
import com.mzantsi.table.data.api.RegisterRequest
import com.mzantsi.table.data.api.RetrofitClient
import com.mzantsi.table.data.api.ReviewRequest
import com.mzantsi.table.data.api.SettingsRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException

// Talks to the ASP.NET Core API, and keeps working when the API (or the internet) is not there:
//  - Reading: recipes from the API are saved on the phone (ApiCache). Offline, we use that copy.
//  - Writing: a new recipe or review is put in the outbox first, so it is never lost.
//    flushOutbox() sends the outbox when the API can be reached; otherwise it tries again later.
class RecipeRepository(
    private val api: MzantsiApiService = RetrofitClient.api
) {
    private val tag = "RecipeRepository"

    // Only one send at a time, so the same item can't be sent twice by two callers.
    private val flushLock = Mutex()

    // Runs an API call in the background. If it fails, we log the problem and return null instead of crashing.
    private suspend fun <T> safeCall(label: String, block: suspend () -> T): T? =
        withContext(Dispatchers.IO) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(tag, "$label failed: ${e.message}")
                null
            }
        }

    // ------------------------------------------------------------ recipes (offline aware)

    // Recipes from the API. If the API can't be reached: the saved copy, then the built-in recipes.
    suspend fun fetchRecipes(): List<Recipe> {
        val remote = safeCall("fetchRecipes") { api.getRecipes() }
            ?.mapNotNull { it.toRecipe() }
            .orEmpty()
        if (remote.isNotEmpty()) {
            ApiCache.mergeRecipes(remote)
            return remote
        }
        return ApiCache.recipes().ifEmpty { MockData.recipes }
    }

    // The recipes saved on the phone from earlier syncs (works with no internet).
    fun cachedRecipes(): List<Recipe> = ApiCache.recipes()

    // Asks the API only for recipes added since the last good sync, and adds them to the saved copy.
    // Returns the full saved list, or null if the API could not be reached (the old copy stays).
    suspend fun syncFromServer(): List<Recipe>? {
        val startedAt = System.currentTimeMillis()
        val remote = safeCall("syncRecipes") { api.syncRecipes(ApiCache.lastSyncIso()) } ?: return null
        ApiCache.mergeRecipes(remote.mapNotNull { it.toRecipe() })
        // Go back 2 minutes, so a recipe saved while we were asking is not missed (repeats are merged by id).
        ApiCache.setLastSync(startedAt - 2 * 60 * 1000L)
        return ApiCache.recipes()
    }

    // ------------------------------------------------------------ outbox (offline writes)

    // Saves the recipe in the outbox. It is sent by flushOutbox().
    fun queueRecipe(recipe: Recipe) = ApiCache.enqueueRecipe(recipe)

    fun queueReview(review: Review) = ApiCache.enqueueReview(review)

    // Sends everything in the outbox, oldest first (a recipe always goes before its reviews).
    // If the API can't be reached we stop and keep the rest for next time.
    // Returns how many items are still waiting.
    suspend fun flushOutbox(): Int = flushLock.withLock {
        withContext(Dispatchers.IO) {
            for (op in ApiCache.outbox()) {
                try {
                    send(op)
                    ApiCache.remove(op.opId)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: HttpException) {
                    val code = e.code()
                    if (code in 400..499 && code != 408 && code != 429) {
                        // The API understood the request and refused it. Sending it again won't help.
                        Log.w(tag, "API refused ${op.opId} ($code), dropping it from the outbox")
                        ApiCache.remove(op.opId)
                    } else {
                        // Server trouble: try again later, but not forever.
                        if (op.attempts + 1 >= MAX_ATTEMPTS) ApiCache.remove(op.opId) else ApiCache.addAttempt(op.opId)
                        Log.w(tag, "API error $code for ${op.opId}, will retry later")
                        break
                    }
                } catch (e: Exception) {
                    // No internet or the API is not running. Keep everything and try again later.
                    // We count the try: the API may have saved the recipe even though the answer never arrived,
                    // so next time send() first checks that the recipe is not already on the API.
                    ApiCache.addAttempt(op.opId)
                    Log.w(tag, "API not reachable (${e.message}), outbox kept")
                    break
                }
            }
            ApiCache.outboxSize()
        }
    }

    private suspend fun send(op: ApiCache.PendingOp) {
        when (op.type) {
            ApiCache.TYPE_RECIPE -> {
                val r = JsonCodec.jsonToRecipe(op.payload)
                // The API gives every new recipe a new id, so it can't tell that a retry is the same recipe.
                // If an earlier try may have got through, look first so we never create it twice.
                if (op.attempts > 0 && api.getRecipes().any { it.isSameAs(r) }) return
                api.createRecipe(
                    RecipeRequest(
                        title = r.title,
                        culture = r.culture,
                        province = r.province,
                        description = r.description,
                        // A photo picked on the phone ("file:...") only exists on this phone, so we don't send its path.
                        imageUrl = if (r.imageRes.startsWith("file:")) "" else r.imageRes,
                        prepTimeMinutes = r.prepTimeMinutes,
                        servings = r.servings,
                        difficulty = r.difficulty.ordinal, // Easy = 0, Medium = 1, Hard = 2 (same order as the API)
                        ingredients = r.ingredients,
                        steps = r.method,
                        authorName = r.authorName
                    )
                )
            }
            ApiCache.TYPE_REVIEW -> {
                val v = JsonCodec.jsonToReview(op.payload)
                api.submitReview(
                    v.recipeId,
                    ReviewRequest(
                        userId = v.userId,
                        rating = v.rating,
                        comment = v.comment
                    )
                )
            }
            else -> Log.w(tag, "Unknown outbox item ${op.opId}")
        }
    }

    // ------------------------------------------------------------ not offline-capable (need the server)

    suspend fun register(name: String, email: String, password: String): User? =
        safeCall("register") { api.register(RegisterRequest(name, email, password)) }?.toUser()

    suspend fun login(email: String, password: String): User? =
        safeCall("login") { api.login(LoginRequest(email, password)) }?.toUser()

    suspend fun updateSettings(userId: Int, dietaryPrefs: String, languagePref: String): User? =
        safeCall("updateSettings") {
            api.updateSettings(userId, SettingsRequest(dietaryPrefs, languagePref))
        }?.toUser()

    // Sends one recipe straight away (no outbox). Returns null if it could not be sent.
    suspend fun createRecipe(recipe: Recipe): Recipe? {
        queueRecipe(recipe)
        flushOutbox()
        val opId = "${ApiCache.TYPE_RECIPE}-${recipe.recipeId}"
        val stillWaiting = ApiCache.outbox().any { op -> op.opId == opId }
        return if (stillWaiting) null else recipe
    }

    // True when the API's copy is the recipe we are about to send (same title, author and description).
    private fun com.mzantsi.table.data.api.RecipeDto.isSameAs(r: Recipe): Boolean =
        title.orEmpty().trim().equals(r.title.trim(), ignoreCase = true) &&
            authorName.orEmpty().trim().equals(r.authorName.trim(), ignoreCase = true) &&
            description.orEmpty().trim() == r.description.trim()

    private companion object {
        const val MAX_ATTEMPTS = 5
    }
}
