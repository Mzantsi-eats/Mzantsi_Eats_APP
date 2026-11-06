package com.mzantsi.table.data.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import com.mzantsi.table.data.AppLanguage
import com.mzantsi.table.data.AppNotification
import com.mzantsi.table.data.NotificationType
import com.mzantsi.table.data.AuthProvider
import com.mzantsi.table.data.CommunityPost
import com.mzantsi.table.data.PostComment
import com.mzantsi.table.data.Difficulty
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.data.Review
import kotlin.random.Random

data class UserProfile(
    val name: String,
    val email: String,
    val provider: AuthProvider,
    val photoUrl: String,
    val language: AppLanguage,
    val dietaryPrefs: Set<String>,
    val notificationsEnabled: Boolean,
    val savedRecipeIds: Set<Int>,
    val recipesAdded: Int,
    val joinedCommunities: Set<String>? = null,
    val followingCooks: Set<String>? = null
)

/**
 * This is the only class that reads from and writes to Firestore.
 *
 * Firestore keeps a copy of the data on the phone. When there is no internet, reads come from
 * that copy and writes wait in a queue until the phone is online again.
 * Because of this we do not wait (await) for writes on the screen. We only add a failure
 * listener, so we can show a message if a write is rejected. The live listeners update the
 * screen straight away, even before the server has the change.
 */
class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun userDoc(uid: String) = db.collection(FirestorePaths.USERS).document(uid)

    private fun reviewsOf(recipeId: Int) =
        db.collection(FirestorePaths.RECIPES).document(recipeId.toString()).collection(FirestorePaths.REVIEWS)

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

    fun listenReviews(
        recipeId: Int,
        onChange: (List<Review>) -> Unit,
        onError: (FirebaseFirestoreException) -> Unit
    ): ListenerRegistration =
        reviewsOf(recipeId).addSnapshotListener { snap, error ->
            if (error != null) {
                onError(error)
            } else if (snap != null) {
                onChange(snap.documents.mapNotNull { it.toReviewOrNull(recipeId) }.sortedByDescending { it.createdAtMillis })
            }
        }

    suspend fun getUserProfileOnce(uid: String): UserProfile? {
        val snap = userDoc(uid).get().await()
        return if (snap.exists()) snap.toUserProfile() else null
    }

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

    fun patchUser(uid: String, fields: Map<String, Any>): Task<Void> =
        userDoc(uid).set(fields, SetOptions.merge())

    fun setSaved(uid: String, recipeId: Int, saved: Boolean): Task<Void> =
        patchUser(
            uid,
            mapOf(UserFields.SAVED to if (saved) FieldValue.arrayUnion(recipeId) else FieldValue.arrayRemove(recipeId))
        )

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

    fun addRecipe(uid: String, recipe: Recipe): Task<Void> {
        val batch = db.batch()
        batch.set(
            db.collection(FirestorePaths.RECIPES).document(recipe.recipeId.toString()),
            recipe.toMap(uid)
        )
        batch.set(userDoc(uid), mapOf(UserFields.RECIPES_ADDED to FieldValue.increment(1)), SetOptions.merge())
        return batch.commit()
    }

    fun addReview(uid: String, review: Review): Task<Void> =
        reviewsOf(review.recipeId).document().set(
            mapOf(
                "reviewId" to review.reviewId,
                "recipeId" to review.recipeId,
                "userUid" to uid,
                "userName" to review.userName,
                "rating" to review.rating,
                "comment" to review.comment,
                "createdAtMillis" to review.createdAtMillis
            )
        )

    private fun postDoc(postId: String) = db.collection(FirestorePaths.POSTS).document(postId)

    fun listenPosts(
        onChange: (List<CommunityPost>) -> Unit,
        onError: (FirebaseFirestoreException) -> Unit
    ): ListenerRegistration =
        db.collection(FirestorePaths.POSTS).addSnapshotListener { snap, error ->
            if (error != null) {
                onError(error)
            } else if (snap != null) {
                onChange(snap.documents.mapNotNull { it.toPostOrNull() }.sortedByDescending { it.createdAtMillis })
            }
        }

    fun addPost(uid: String, post: CommunityPost): Task<Void> =
        postDoc(post.postId).set(
            mapOf(
                "postId" to post.postId,
                "authorUid" to uid,
                "authorName" to post.authorName,
                "recipeId" to post.recipeId,
                "recipeTitle" to post.recipeTitle,
                "communityId" to post.communityId,
                "caption" to post.caption,
                "photoPath" to post.photoPath,
                "createdAtMillis" to post.createdAtMillis,
                "ratings" to emptyMap<String, Int>(),
                "cheeredBy" to emptyList<String>(),
                "comments" to emptyList<Map<String, Any>>()
            )
        )

    fun ratePost(postId: String, uid: String, stars: Int): Task<Void> =
        postDoc(postId).set(mapOf("ratings" to mapOf(uid to stars)), SetOptions.merge())

    fun setCheer(postId: String, uid: String, cheer: Boolean): Task<Void> =
        postDoc(postId).update("cheeredBy", if (cheer) FieldValue.arrayUnion(uid) else FieldValue.arrayRemove(uid))

    fun addComment(postId: String, uid: String, comment: PostComment): Task<Void> =
        postDoc(postId).update(
            "comments",
            FieldValue.arrayUnion(
                mapOf(
                    "authorUid" to uid,
                    "authorName" to comment.authorName,
                    "text" to comment.text,
                    "createdAtMillis" to comment.createdAtMillis
                )
            )
        )

    private fun notificationsOf(uid: String) = userDoc(uid).collection(FirestorePaths.NOTIFICATIONS)

    fun listenNotifications(
        uid: String,
        onChange: (List<AppNotification>) -> Unit,
        onError: (FirebaseFirestoreException) -> Unit
    ): ListenerRegistration =
        notificationsOf(uid)
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    onError(error)
                } else if (snap != null) {
                    onChange(snap.documents.mapNotNull { it.toNotificationOrNull() })
                }
            }

    fun sendNotification(toUid: String, fromUid: String, n: AppNotification): Task<Void> =
        notificationsOf(toUid).document(n.id).set(
            mapOf(
                "id" to n.id,
                "toUid" to toUid,
                "fromUid" to fromUid,
                "fromName" to n.fromName,
                "type" to n.type.name,
                "title" to n.title,
                "body" to n.body,
                "route" to n.route,
                "createdAtMillis" to n.createdAtMillis,
                "read" to false
            )
        )

    fun markNotificationsRead(uid: String, ids: Collection<String>): Task<Void> {
        val batch = db.batch()
        ids.forEach { batch.update(notificationsOf(uid).document(it), "read", true) }
        return batch.commit()
    }

    fun deleteNotifications(uid: String, ids: Collection<String>): Task<Void> {
        val batch = db.batch()
        ids.forEach { batch.delete(notificationsOf(uid).document(it)) }
        return batch.commit()
    }

    fun addFcmToken(uid: String, token: String): Task<Void> =
        patchUser(uid, mapOf(UserFields.FCM_TOKENS to FieldValue.arrayUnion(token)))

    fun removeFcmToken(uid: String, token: String): Task<Void> =
        patchUser(uid, mapOf(UserFields.FCM_TOKENS to FieldValue.arrayRemove(token)))

    companion object {
        fun newRecipeId(): Int = Random.nextInt(100_000, Int.MAX_VALUE)

        fun isAvailable(): Boolean = runCatching { FirebaseApp.getInstance() }.isSuccess
    }
}

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
    recipesAdded = (getLong(UserFields.RECIPES_ADDED) ?: 0L).toInt(),
    joinedCommunities = if (contains(UserFields.JOINED)) stringList(UserFields.JOINED).toSet() else null,
    followingCooks = if (contains(UserFields.FOLLOWING)) stringList(UserFields.FOLLOWING).toSet() else null
)

private fun DocumentSnapshot.toPostOrNull(): CommunityPost? {
    val title = getString("recipeTitle") ?: return null
    val ratings = (get("ratings") as? Map<*, *>)
        ?.mapNotNull { (k, v) -> (k as? String)?.let { key -> (v as? Number)?.toInt()?.let { key to it } } }
        ?.toMap() ?: emptyMap()
    val comments = (get("comments") as? List<*>)?.mapNotNull { item ->
        val m = item as? Map<*, *> ?: return@mapNotNull null
        PostComment(
            authorName = m["authorName"] as? String ?: "",
            text = m["text"] as? String ?: return@mapNotNull null,
            createdAtMillis = (m["createdAtMillis"] as? Number)?.toLong() ?: 0L
        )
    }?.sortedBy { it.createdAtMillis } ?: emptyList()
    return CommunityPost(
        postId = id,
        authorKey = getString("authorUid").orEmpty(),
        authorName = getString("authorName").orEmpty(),
        recipeId = (getLong("recipeId") ?: 0L).toInt(),
        recipeTitle = title,
        communityId = getString("communityId").orEmpty(),
        caption = getString("caption").orEmpty(),
        photoPath = getString("photoPath").orEmpty(),
        createdAtMillis = getLong("createdAtMillis") ?: 0L,
        ratings = ratings,
        cheers = stringList("cheeredBy").toSet(),
        comments = comments
    )
}

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
        authorName = getString("authorName").orEmpty(),
        authorUid = getString("authorUid").orEmpty()
    )
}

private fun DocumentSnapshot.toReviewOrNull(recipeId: Int): Review? {
    val stars = getLong("rating")?.toInt() ?: return null
    return Review(
        reviewId = (getLong("reviewId") ?: 0L).toInt(),
        recipeId = recipeId,
        userId = 0,
        rating = stars,
        comment = getString("comment").orEmpty(),
        userName = getString("userName").orEmpty(),
        createdAtMillis = getLong("createdAtMillis") ?: 0L
    )
}

private fun DocumentSnapshot.toNotificationOrNull(): AppNotification? {
    val title = getString("title") ?: return null
    return AppNotification(
        id = id,
        type = runCatching { NotificationType.valueOf(getString("type").orEmpty()) }.getOrDefault(NotificationType.POST_COMMENT),
        title = title,
        body = getString("body").orEmpty(),
        fromName = getString("fromName").orEmpty(),
        route = getString("route").orEmpty(),
        createdAtMillis = getLong("createdAtMillis") ?: 0L,
        read = getBoolean("read") ?: false
    )
}
