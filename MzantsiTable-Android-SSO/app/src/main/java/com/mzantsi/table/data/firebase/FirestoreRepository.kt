package com.mzantsi.table.data.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import com.mzantsi.table.data.AppLanguage
import com.mzantsi.table.data.AuthProvider
import com.mzantsi.table.data.Difficulty
import com.mzantsi.table.data.Recipe
import kotlin.random.Random

/** What we keep about a user in users/{uid}. */
data class UserProfile(
    val name: String,
    val email: String,
    val provider: AuthProvider,
    val photoUrl: String,
    val language: AppLanguage,
    val dietaryPrefs: Set<String>,
    val notificationsEnabled: Boolean,
    val savedRecipeIds: Set<Int>,
    val recipesAdded: Int
)

/**
 * The only class that talks to Firestore.
 *
 * Firestore's offline cache is on by default on Android: reads are served from the cache when
 * there is no network, and writes are queued and sent automatically when the device reconnects.
 * Because of that, write methods return a [Task] that the caller must NOT await on the UI path
 * (while offline it only completes once the server has acknowledged the write). Attach a failure
 * listener instead. Snapshot listeners already fire immediately for local writes, so the UI
 * updates straight away.
 */
class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun userDoc(uid: String) = db.collection(FirestorePaths.USERS).document(uid)

    // ---------------------------------------------------------------- listeners

    fun listenUser(
        uid: String,
        onChange: (UserProfile) -> Unit,
        onError: (FirebaseFirestoreException) -> Unit
    ): ListenerRegistration =
        userDoc(uid).addSnapshotListener { snap, error ->
            if (error != null) {
                onError(error)
            } else if (snap != null && snap.exists()) {
                onChange(snap.toUserProfile())
            }
        }

    /** Emits every recipe in the `recipes` collection, oldest first. Re-emits on any change. */
    fun listenRecipes(
        onChange: (List<Recipe>) -> Unit,
        onError: (FirebaseFirestoreException) -> Unit
    ): ListenerRegistration =
        db.collection(FirestorePaths.RECIPES).addSnapshotListener { snap, error ->
            if (error != null) {
                onError(error)
            } else if (snap != null) {
                val recipes = snap.documents
                    .mapNotNull { d -> d.toRecipeOrNull()?.let { r -> r to (d.getLong("createdAtMillis") ?: 0L) } }
                    .sortedBy { it.second }
                    .map { it.first }
                onChange(recipes)
            }
        }

    /** One-off read (needs a connection). Used once at Google sign-in to see if the user already exists. */
    suspend fun getUserProfileOnce(uid: String): UserProfile? {
        val snap = userDoc(uid).get().await()
        return if (snap.exists()) snap.toUserProfile() else null
    }

    // ---------------------------------------------------------------- user writes

    fun createUserProfile(
        uid: String,
        name: String,
        email: String,
        provider: AuthProvider,
        photoUrl: String,
        language: AppLanguage
    ): Task<Void> =
        userDoc(uid).set(
            mapOf(
                UserFields.USER_ID to uid,
                UserFields.NAME to name,
                UserFields.EMAIL to email,
                UserFields.AUTH_PROVIDER to provider.name,
                UserFields.PHOTO_URL to photoUrl,
                UserFields.LANGUAGE to language.name.lowercase(),
                UserFields.DIETARY to "",
                UserFields.NOTIFICATIONS to true,
                UserFields.SAVED to emptyList<Int>(),
                UserFields.RECIPES_ADDED to 0
            ),
            SetOptions.merge()
        )

    /** Merge-writes individual fields, so it also works if the document doesn't exist yet. */
    fun patchUser(uid: String, fields: Map<String, Any>): Task<Void> =
        userDoc(uid).set(fields, SetOptions.merge())

    fun setSaved(uid: String, recipeId: Int, saved: Boolean): Task<Void> =
        patchUser(
            uid,
            mapOf(UserFields.SAVED to if (saved) FieldValue.arrayUnion(recipeId) else FieldValue.arrayRemove(recipeId))
        )

    /** DietaryPrefs is a comma-separated string in the database, so the whole set is written. */
    fun setDietaryPrefs(uid: String, prefs: Set<String>): Task<Void> =
        patchUser(uid, mapOf(UserFields.DIETARY to prefs.sorted().joinToString(",")))

    fun markCompleted(uid: String, recipe: Recipe): Task<Void> =
        userDoc(uid).collection(FirestorePaths.COMPLETED).document(recipe.recipeId.toString()).set(
            mapOf(
                "recipeId" to recipe.recipeId,
                "title" to recipe.title,
                "completedAtMillis" to System.currentTimeMillis(),
                "timesCompleted" to FieldValue.increment(1)
            ),
            SetOptions.merge()
        )

    // ---------------------------------------------------------------- recipe writes

    /** Creates the recipe and bumps the author's recipesAdded counter in one atomic batch. */
    fun addRecipe(uid: String, recipe: Recipe): Task<Void> {
        val batch = db.batch()
        batch.set(
            db.collection(FirestorePaths.RECIPES).document(recipe.recipeId.toString()),
            recipe.toMap(uid)
        )
        batch.set(userDoc(uid), mapOf(UserFields.RECIPES_ADDED to FieldValue.increment(1)), SetOptions.merge())
        return batch.commit()
    }

    companion object {
        /**
         * Recipe ids are Ints in the app (navigation uses them). Random ids avoid two phones
         * creating the same "max + 1" id while offline. The built-in sample recipes use 1..N.
         */
        fun newRecipeId(): Int = Random.nextInt(100_000, Int.MAX_VALUE)
    }
}

// -------------------------------------------------------------------- mapping

private fun DocumentSnapshot.intList(field: String): List<Int> =
    (get(field) as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()

private fun DocumentSnapshot.stringList(field: String): List<String> =
    (get(field) as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

private fun DocumentSnapshot.toUserProfile() = UserProfile(
    name = getString(UserFields.NAME).orEmpty(),
    email = getString(UserFields.EMAIL).orEmpty(),
    provider = runCatching { AuthProvider.valueOf(getString(UserFields.AUTH_PROVIDER).orEmpty()) }
        .getOrDefault(AuthProvider.EMAIL),
    photoUrl = getString(UserFields.PHOTO_URL).orEmpty(),
    language = runCatching { AppLanguage.valueOf(getString(UserFields.LANGUAGE).orEmpty().uppercase()) }
        .getOrDefault(AppLanguage.EN),
    dietaryPrefs = getString(UserFields.DIETARY).orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
    notificationsEnabled = getBoolean(UserFields.NOTIFICATIONS) ?: true,
    savedRecipeIds = intList(UserFields.SAVED).toSet(),
    recipesAdded = (getLong(UserFields.RECIPES_ADDED) ?: 0L).toInt()
)

private fun Recipe.toMap(authorUid: String): Map<String, Any> = mapOf(
    "recipeId" to recipeId,
    "title" to title,
    "culture" to culture,
    "province" to province,
    "description" to description,
    "imageRes" to imageRes,
    "prepTimeMinutes" to prepTimeMinutes,
    "cookTimeMinutes" to cookTimeMinutes,
    "servings" to servings,
    "difficulty" to difficulty.name,
    "ratingAvg" to ratingAvg.toDouble(),
    "ingredients" to ingredients,
    "method" to method,
    "authorName" to authorName,
    "authorUid" to authorUid,
    "createdAtMillis" to System.currentTimeMillis()
)

/** Defensive: a half-filled document in the console must never crash the app. */
private fun DocumentSnapshot.toRecipeOrNull(): Recipe? {
    val rid = getLong("recipeId")?.toInt() ?: id.toIntOrNull() ?: return null
    val name = getString("title") ?: return null
    return Recipe(
        recipeId = rid,
        title = name,
        culture = getString("culture") ?: "Other",
        province = getString("province").orEmpty(),
        description = getString("description").orEmpty(),
        imageRes = getString("imageRes").orEmpty(),
        prepTimeMinutes = (getLong("prepTimeMinutes") ?: 0L).toInt(),
        cookTimeMinutes = (getLong("cookTimeMinutes") ?: 0L).toInt(),
        servings = (getLong("servings") ?: 1L).toInt(),
        difficulty = runCatching { Difficulty.valueOf(getString("difficulty").orEmpty().uppercase()) }
            .getOrDefault(Difficulty.MEDIUM),
        ratingAvg = (getDouble("ratingAvg") ?: 0.0).toFloat(),
        ingredients = stringList("ingredients"),
        method = stringList("method"),
        authorName = getString("authorName").orEmpty()
    )
}
