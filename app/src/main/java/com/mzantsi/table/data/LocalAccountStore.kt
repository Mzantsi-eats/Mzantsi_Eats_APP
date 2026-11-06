package com.mzantsi.table.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object LocalAccountStore {

    private const val PREFS = "mzantsi_accounts"
    private const val KEY_SESSION = "session_email"
    private const val KEY_NEXT_ID = "next_user_id"
    private const val KEY_RECIPES = "local_recipes"
    private const val ACCOUNT_PREFIX = "acct_"
    private const val REVIEWS_PREFIX = "reviews_"
    private const val ITERATIONS = 10_000

    private var prefs: SharedPreferences? = null

    data class Account(
        val userId: Int,
        val name: String,
        val email: String,
        val provider: AuthProvider,
        val salt: String = "",
        val hash: String = "",
        val photoUrl: String = "",
        val dietaryPrefs: String = "",
        val language: AppLanguage = AppLanguage.EN,
        val recipesAdded: Int = 0,
        val notificationsEnabled: Boolean = true,
        val savedRecipeIds: Set<Int> = emptySet()
    ) {
        fun toUser() = User(
            userId = userId,
            name = name,
            email = email,
            dietaryPrefs = dietaryPrefs,
            languagePref = language,
            recipesAdded = recipesAdded,
            authProvider = provider,
            photoUrl = photoUrl
        )
    }

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        }
    }

    private fun normalise(email: String) = email.trim().lowercase()

    fun find(email: String): Account? {
        val raw = prefs?.getString(ACCOUNT_PREFIX + normalise(email), null) ?: return null
        return runCatching { JSONObject(raw).toAccount() }.getOrNull()
    }

    fun save(account: Account) {
        prefs?.edit()?.putString(ACCOUNT_PREFIX + normalise(account.email), account.toJson().toString())?.apply()
    }

    fun createPasswordAccount(
        name: String,
        email: String,
        password: String,
        language: AppLanguage,
        serverUserId: Int? = null
    ): Account {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val account = Account(
            userId = serverUserId ?: nextId(),
            name = name,
            email = normalise(email),
            provider = AuthProvider.EMAIL,
            salt = Base64.encodeToString(salt, Base64.NO_WRAP),
            hash = hash(password, salt),
            language = language
        )
        save(account)
        return account
    }

    fun createGoogleAccount(name: String, email: String, photoUrl: String, language: AppLanguage): Account {
        val account = Account(
            userId = nextId(),
            name = name,
            email = normalise(email),
            provider = AuthProvider.GOOGLE,
            photoUrl = photoUrl,
            language = language
        )
        save(account)
        return account
    }

    fun verifyPassword(account: Account, password: String): Boolean {
        if (account.provider != AuthProvider.EMAIL || account.salt.isBlank()) return false
        val salt = Base64.decode(account.salt, Base64.NO_WRAP)
        return hash(password, salt) == account.hash
    }

    fun changePassword(account: Account, newPassword: String): Account {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val updated = account.copy(
            salt = Base64.encodeToString(salt, Base64.NO_WRAP),
            hash = hash(newPassword, salt)
        )
        save(updated)
        return updated
    }

    fun setSession(email: String?) {
        prefs?.edit()?.apply {
            if (email == null) remove(KEY_SESSION) else putString(KEY_SESSION, normalise(email))
        }?.apply()
    }

    fun sessionAccount(): Account? {
        val email = prefs?.getString(KEY_SESSION, null) ?: return null
        return find(email)
    }

    fun recipes(): List<Recipe> {
        val raw = prefs?.getString(KEY_RECIPES, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { i -> runCatching { array.getJSONObject(i).toRecipe() }.getOrNull() }
        }.getOrDefault(emptyList())
    }

    fun addRecipe(recipe: Recipe) {
        val array = JSONArray()
        (recipes() + recipe).forEach { array.put(it.toJson()) }
        prefs?.edit()?.putString(KEY_RECIPES, array.toString())?.apply()
    }

    fun reviews(recipeId: Int): List<Review> {
        val raw = prefs?.getString(REVIEWS_PREFIX + recipeId, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { i -> runCatching { array.getJSONObject(i).toReview() }.getOrNull() }
        }.getOrDefault(emptyList()).sortedByDescending { it.createdAtMillis }
    }

    fun addReview(review: Review) {
        val array = JSONArray()
        (reviews(review.recipeId) + review).forEach { array.put(it.toJson()) }
        prefs?.edit()?.putString(REVIEWS_PREFIX + review.recipeId, array.toString())?.apply()
    }

    private fun nextId(): Int {
        val p = prefs ?: return 1
        val id = p.getInt(KEY_NEXT_ID, 1)
        p.edit().putInt(KEY_NEXT_ID, id + 1).apply()
        return id
    }

    private fun hash(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun Account.toJson() = JSONObject().apply {
        put("userId", userId)
        put("name", name)
        put("email", email)
        put("provider", provider.name)
        put("salt", salt)
        put("hash", hash)
        put("photoUrl", photoUrl)
        put("dietaryPrefs", dietaryPrefs)
        put("language", language.name)
        put("recipesAdded", recipesAdded)
        put("notificationsEnabled", notificationsEnabled)
        put("savedRecipeIds", JSONArray(savedRecipeIds.toList()))
    }

    private fun JSONObject.toAccount() = Account(
        userId = optInt("userId", 1),
        name = optString("name"),
        email = optString("email"),
        provider = runCatching { AuthProvider.valueOf(optString("provider")) }.getOrDefault(AuthProvider.EMAIL),
        salt = optString("salt"),
        hash = optString("hash"),
        photoUrl = optString("photoUrl"),
        dietaryPrefs = optString("dietaryPrefs"),
        language = runCatching { AppLanguage.valueOf(optString("language")) }.getOrDefault(AppLanguage.EN),
        recipesAdded = optInt("recipesAdded", 0),
        notificationsEnabled = optBoolean("notificationsEnabled", true),
        savedRecipeIds = optJSONArray("savedRecipeIds")?.let { a -> (0 until a.length()).map { a.optInt(it) }.toSet() }
            ?: emptySet()
    )

    private fun Recipe.toJson() = JSONObject().apply {
        put("recipeId", recipeId)
        put("title", title)
        put("culture", culture)
        put("province", province)
        put("description", description)
        put("imageRes", imageRes)
        put("prepTimeMinutes", prepTimeMinutes)
        put("cookTimeMinutes", cookTimeMinutes)
        put("servings", servings)
        put("difficulty", difficulty.name)
        put("ratingAvg", ratingAvg.toDouble())
        put("ingredients", JSONArray(ingredients))
        put("method", JSONArray(method))
        put("authorName", authorName)
    }

    private fun JSONObject.toRecipe() = Recipe(
        recipeId = getInt("recipeId"),
        title = getString("title"),
        culture = optString("culture", "Other"),
        province = optString("province"),
        description = optString("description"),
        imageRes = optString("imageRes"),
        prepTimeMinutes = optInt("prepTimeMinutes", 0),
        cookTimeMinutes = optInt("cookTimeMinutes", 0),
        servings = optInt("servings", 1),
        difficulty = runCatching { Difficulty.valueOf(optString("difficulty")) }.getOrDefault(Difficulty.MEDIUM),
        ratingAvg = optDouble("ratingAvg", 0.0).toFloat(),
        ingredients = optJSONArray("ingredients")?.let { a -> (0 until a.length()).map { a.optString(it) } } ?: emptyList(),
        method = optJSONArray("method")?.let { a -> (0 until a.length()).map { a.optString(it) } } ?: emptyList(),
        authorName = optString("authorName")
    )

    private fun Review.toJson() = JSONObject().apply {
        put("reviewId", reviewId)
        put("recipeId", recipeId)
        put("userId", userId)
        put("rating", rating)
        put("comment", comment)
        put("userName", userName)
        put("createdAtMillis", createdAtMillis)
    }

    private fun JSONObject.toReview() = Review(
        reviewId = optInt("reviewId", 0),
        recipeId = getInt("recipeId"),
        userId = optInt("userId", 0),
        rating = optInt("rating", 0),
        comment = optString("comment"),
        userName = optString("userName"),
        createdAtMillis = optLong("createdAtMillis", 0L)
    )
}
