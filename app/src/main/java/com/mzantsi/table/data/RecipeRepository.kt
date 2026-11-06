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

class RecipeRepository(
    private val api: MzantsiApiService = RetrofitClient.api
) {
    private val tag = "RecipeRepository"

    private val flushLock = Mutex()

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

    fun cachedRecipes(): List<Recipe> = ApiCache.recipes()

    suspend fun syncFromServer(): List<Recipe>? {
        val startedAt = System.currentTimeMillis()
        val remote = safeCall("syncRecipes") { api.syncRecipes(ApiCache.lastSyncIso()) } ?: return null
        ApiCache.mergeRecipes(remote.mapNotNull { it.toRecipe() })
        ApiCache.setLastSync(startedAt - 2 * 60 * 1000L)
        return ApiCache.recipes()
    }

    fun queueRecipe(recipe: Recipe) = ApiCache.enqueueRecipe(recipe)

    fun queueReview(review: Review) = ApiCache.enqueueReview(review)

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
                        Log.w(tag, "API refused ${op.opId} ($code), dropping it from the outbox")
                        ApiCache.remove(op.opId)
                    } else {
                        if (op.attempts + 1 >= MAX_ATTEMPTS) ApiCache.remove(op.opId) else ApiCache.addAttempt(op.opId)
                        Log.w(tag, "API error $code for ${op.opId}, will retry later")
                        break
                    }
                } catch (e: Exception) {
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
                if (op.attempts > 0 && api.getRecipes().any { it.isSameAs(r) }) return
                api.createRecipe(
                    RecipeRequest(
                        title = r.title,
                        culture = r.culture,
                        province = r.province,
                        description = r.description,
                        imageUrl = if (r.imageRes.startsWith("file:")) "" else r.imageRes,
                        prepTimeMinutes = r.prepTimeMinutes,
                        servings = r.servings,
                        difficulty = r.difficulty.ordinal,
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

    suspend fun register(name: String, email: String, password: String): User? =
        safeCall("register") { api.register(RegisterRequest(name, email, password)) }?.toUser()

    suspend fun login(email: String, password: String): User? =
        safeCall("login") { api.login(LoginRequest(email, password)) }?.toUser()

    suspend fun updateSettings(userId: Int, dietaryPrefs: String, languagePref: String): User? =
        safeCall("updateSettings") {
            api.updateSettings(userId, SettingsRequest(dietaryPrefs, languagePref))
        }?.toUser()

    suspend fun createRecipe(recipe: Recipe): Recipe? {
        queueRecipe(recipe)
        flushOutbox()
        val opId = "${ApiCache.TYPE_RECIPE}-${recipe.recipeId}"
        val stillWaiting = ApiCache.outbox().any { op -> op.opId == opId }
        return if (stillWaiting) null else recipe
    }

    private fun com.mzantsi.table.data.api.RecipeDto.isSameAs(r: Recipe): Boolean =
        title.orEmpty().trim().equals(r.title.trim(), ignoreCase = true) &&
            authorName.orEmpty().trim().equals(r.authorName.trim(), ignoreCase = true) &&
            description.orEmpty().trim() == r.description.trim()

    private companion object {
        const val MAX_ATTEMPTS = 5
    }
}
