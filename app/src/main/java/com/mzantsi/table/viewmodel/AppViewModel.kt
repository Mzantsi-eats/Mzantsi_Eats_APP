package com.mzantsi.table.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.mzantsi.table.auth.GoogleAccount
import com.mzantsi.table.data.AppLanguage
import com.mzantsi.table.data.AuthProvider
import com.mzantsi.table.data.CommunityData
import com.mzantsi.table.data.CommunityPost
import com.mzantsi.table.data.CommunityStore
import com.mzantsi.table.data.Cook
import com.mzantsi.table.data.LocalAccountStore
import com.mzantsi.table.data.MockData
import com.mzantsi.table.data.NetworkMonitor
import com.mzantsi.table.data.OfflineStore
import com.mzantsi.table.data.PostComment
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.data.Review
import com.mzantsi.table.data.User
import com.mzantsi.table.data.firebase.FirestoreRepository
import com.mzantsi.table.data.firebase.UserFields
import com.mzantsi.table.data.firebase.UserProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random
class AppViewModel(
    private val repo: FirestoreRepository? = if (FirestoreRepository.isAvailable()) FirestoreRepository() else null,
    private val auth: FirebaseAuth? = if (FirestoreRepository.isAvailable()) FirebaseAuth.getInstance() else null
) : ViewModel() {

    private val TAG = "AppViewModel"

    private var userListener: ListenerRegistration? = null
    private var recipeListener: ListenerRegistration? = null
    private var reviewListener: ListenerRegistration? = null
    private var postListener: ListenerRegistration? = null
    private var reviewsRecipeId: Int? = null

    // ---------------------------------------------------------------- data the screens show

    private var _language by mutableStateOf(AppLanguage.EN)
    val language get() = _language

    var currentUser by mutableStateOf<User?>(null)
        private set

    // Before sign in: only the built-in recipes. After sign in: built-in recipes plus the ones users added.
    // Recipes the user downloaded are always included, so they can be opened without internet.
    private var _recipes by mutableStateOf(MockData.recipes)
    val recipes: List<Recipe>
        get() {
            val ids = _recipes.map { it.recipeId }.toSet()
            return _recipes + downloads.map { it.recipe }.filter { it.recipeId !in ids }
        }

    // True when the phone has internet. Used to show the "You're offline" banner.
    val isOnline get() = NetworkMonitor.isOnline

    // Set after a successful registration. The Register screen then switches to Login,
    // because a new user must sign in with their new email and password.
    var registeredEmail by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    // A message that shows once at the bottom of the screen (for example when a save failed).
    var lastError by mutableStateOf<String?>(null)
        private set

    // A message that shows on the Register / Login screen.
    var authError by mutableStateOf<String?>(null)
        private set

    var savedRecipeIds by mutableStateOf(setOf<Int>())
        private set
    var justCompletedRecipe by mutableStateOf<Recipe?>(null)
        private set
    var notificationsEnabled by mutableStateOf(true)
        private set
    var dietaryPreferences by mutableStateOf(setOf<String>())
        private set

    // Reviews of the recipe that is open on the Recipe Detail screen.
    var reviews by mutableStateOf(listOf<Review>())
        private set

    // Community Hub
    var joinedCommunities by mutableStateOf(setOf<String>())
        private set
    var followingCooks by mutableStateOf(setOf<String>())
        private set
    private var localPosts by mutableStateOf(listOf<CommunityPost>())
    private var remotePosts by mutableStateOf(listOf<CommunityPost>())
    val communityPosts: List<CommunityPost>
        get() = (remotePosts + localPosts).distinctBy { it.postId }.sortedByDescending { it.createdAtMillis }

    // Recipes downloaded for offline use
    private var downloads by mutableStateOf(listOf<OfflineStore.Download>())
    val downloadedRecipes: List<Recipe> get() = downloads.map { it.recipe }
    val downloadedRecipeIds: Set<Int> get() = downloads.map { it.recipe.recipeId }.toSet()

    // Who is signed in, as a key for the data saved on the phone (Firebase uid, or email offline).
    val currentUserKey: String? get() = userKey
    private val userKey: String?
        get() = currentUser?.let { u -> u.uid.ifBlank { u.email.trim().lowercase() } }?.ifBlank { null }

    init {
        // If the user was signed in last time, sign them in again straight away.
        if (repo == null || auth == null) {
            LocalAccountStore.sessionAccount()?.let {
                startOfflineSession(it)
                Log.i(TAG, "Offline session restored for ${it.email}")
            }
        } else {
            auth.currentUser?.let {
                startSession(it)
                Log.i(TAG, "Session restored for ${it.email}")
            }
        }
    }

    // ---------------------------------------------------------------- starting and stopping a session

    private fun startSession(fb: FirebaseUser, displayName: String? = null) {
        stopListeners()
        // Show the user right away (this also works offline). The Firestore listener fills in the rest.
        currentUser = User(
            uid = fb.uid,
            name = displayName ?: fb.displayName.orEmpty().ifBlank { fb.email.orEmpty().substringBefore('@') },
            email = fb.email.orEmpty(),
            languagePref = _language,
            authProvider = if (fb.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID })
                AuthProvider.GOOGLE else AuthProvider.EMAIL,
            photoUrl = fb.photoUrl?.toString().orEmpty()
        )

        val db = repo ?: return
        userListener = db.listenUser(
            uid = fb.uid,
            onChange = { applyProfile(fb.uid, it) },
            onError = { reportReadFailure("your profile", it) }
        )
        recipeListener = db.listenRecipes(
            onChange = { remote -> mergeRecipes(remote) },
            onError = { reportReadFailure("recipes", it) }
        )
        loadCommunityAndDownloads()
        remotePosts = CommunityStore.cachedRemotePosts() // shown straight away, even without internet
        postListener = db.listenPosts(
            onChange = { posts ->
                remotePosts = posts
                CommunityStore.cacheRemotePosts(posts)
            },
            onError = { reportReadFailure("the community feed", it) }
        )
    }

    // Loads what is saved on the phone for this user: joined communities, followed cooks,
    // the sample/offline community posts and the downloaded recipes.
    private fun loadCommunityAndDownloads() {
        val key = userKey ?: return
        joinedCommunities = CommunityStore.joined(key)
        followingCooks = CommunityStore.following(key)
        localPosts = CommunityStore.localPosts()
        downloads = OfflineStore.downloads(key)
    }

    private fun stopListeners() {
        userListener?.remove()
        recipeListener?.remove()
        reviewListener?.remove()
        postListener?.remove()
        userListener = null
        recipeListener = null
        reviewListener = null
        postListener = null
        reviewsRecipeId = null
    }

    private fun applyProfile(uid: String, p: UserProfile) {
        if (auth?.currentUser?.uid != uid) return // the user signed out while the data was loading
        val previous = currentUser
        currentUser = User(
            uid = uid,
            name = p.name.ifBlank { previous?.name.orEmpty() },
            email = p.email.ifBlank { previous?.email.orEmpty() },
            dietaryPrefs = p.dietaryPrefs.joinToString(","),
            languagePref = p.language,
            recipesAdded = p.recipesAdded,
            authProvider = p.provider,
            photoUrl = p.photoUrl
        )
        _language = p.language
        dietaryPreferences = p.dietaryPrefs
        notificationsEnabled = p.notificationsEnabled
        savedRecipeIds = p.savedRecipeIds
        // Joined communities and followed cooks also live in Firestore, so they follow the user to a new phone.
        p.joinedCommunities?.let { joinedCommunities = it; CommunityStore.setJoined(uid, it) }
        p.followingCooks?.let { followingCooks = it; CommunityStore.setFollowing(uid, it) }
    }

    // Built-in recipes first (unless Firestore has a recipe with the same id), then the Firestore recipes.
    private fun mergeRecipes(remote: List<Recipe>) {
        val remoteIds = remote.map { it.recipeId }.toSet()
        _recipes = MockData.recipes.filter { it.recipeId !in remoteIds } + remote
        Log.d(TAG, "Recipes: ${remote.size} from Firestore, ${_recipes.size} total")
    }

    // Offline mode: show the account that is saved on the phone.
    private fun startOfflineSession(account: LocalAccountStore.Account) {
        LocalAccountStore.setSession(account.email)
        applyOfflineAccount(account)
        _recipes = MockData.recipes + LocalAccountStore.recipes()
        remotePosts = emptyList()
        loadCommunityAndDownloads()
    }

    private fun applyOfflineAccount(account: LocalAccountStore.Account) {
        currentUser = account.toUser()
        _language = account.language
        dietaryPreferences = account.dietaryPrefs.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        notificationsEnabled = account.notificationsEnabled
        savedRecipeIds = account.savedRecipeIds
    }

    // Offline mode: change the signed-in account, save it on the phone and update the screens.
    private fun updateOfflineAccount(change: (LocalAccountStore.Account) -> LocalAccountStore.Account) {
        val current = LocalAccountStore.sessionAccount() ?: return
        val updated = change(current)
        LocalAccountStore.save(updated)
        applyOfflineAccount(updated)
    }

    // ---------------------------------------------------------------- register, login and Google

    fun clearAuthError() {
        authError = null
    }

    fun clearLastError() {
        lastError = null
    }

    // The Register screen calls this after it has switched to the Login tab.
    fun clearRegisteredEmail() {
        registeredEmail = null
    }

    // Creates an email + password account, then the user's document in Firestore.
    fun register(name: String, email: String, password: String) {
        val cleanEmail = email.trim().lowercase()
        val cleanName = name.trim()
        authError = null

        if (repo == null || auth == null) {
            if (LocalAccountStore.find(cleanEmail) != null) {
                authError = MSG_EMAIL_TAKEN
                return
            }
            val account = LocalAccountStore.createPasswordAccount(cleanName, cleanEmail, password, _language)
            // The new user is not signed in yet: they must log in with their new details.
            registeredEmail = account.email
            Log.i(TAG, "Registered offline ${account.email}")
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val fb = auth.createUserWithEmailAndPassword(cleanEmail, password).await().user
                    ?: throw IllegalStateException("Firebase did not return a user.")
                runCatching {
                    fb.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(cleanName).build()).await()
                }

                // Send verification email to the new user.
                try {
                    fb.sendEmailVerification().await()
                    Log.i(TAG, "Verification email sent successfully")
                } catch (e: Exception) {
                    Log.w(TAG, "Verification email failed", e)
                }

                runCatching {
                    withTimeoutOrNull(8_000) {
                        repo.createUserProfile(fb.uid, cleanName, cleanEmail, AuthProvider.EMAIL, "", _language).await()
                    }
                }.onFailure { Log.w(TAG, "Profile not saved during register", it) }
                // Firebase signs a new user in automatically. We sign them out so they log in themselves.
                auth.signOut()
                registeredEmail = cleanEmail
                Log.i(TAG, "Registered ${fb.email}, waiting for the user to sign in")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                authError = authMessage(e, registering = true)
                Log.w(TAG, "Register failed", e)
            } finally {
                isLoading = false
            }
        }
    }

    // Email + password login. Google accounts have no password, so we tell those users to use Google.
    fun login(email: String, password: String) {
        val cleanEmail = email.trim().lowercase()
        authError = null

        if (repo == null || auth == null) {
            val account = LocalAccountStore.find(cleanEmail)
            if (account == null) {
                authError = MSG_WRONG_LOGIN
                return
            }
            when {
                account.provider == AuthProvider.GOOGLE -> authError = MSG_GOOGLE_ACCOUNT
                !LocalAccountStore.verifyPassword(account, password) -> authError = MSG_WRONG_LOGIN
                else -> {
                    startOfflineSession(account)
                    Log.i(TAG, "Logged in offline ${account.email}")
                }
            }
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val fb = auth.signInWithEmailAndPassword(cleanEmail, password).await().user
                    ?: throw IllegalStateException("Firebase did not return a user.")
                startSession(fb)
                Log.i(TAG, "Logged in ${fb.email}")
                // If the profile could not be saved when the user registered (no internet), create it now.
                runCatching {
                    if (repo.getUserProfileOnce(fb.uid) == null) {
                        repo.createUserProfile(
                            fb.uid, fb.displayName.orEmpty().ifBlank { cleanEmail.substringBefore('@') },
                            cleanEmail, AuthProvider.EMAIL, "", _language
                        ).addOnFailureListener { reportWriteFailure("your profile", it) }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                authError = authMessage(e, registering = false)
                Log.w(TAG, "Login failed", e)
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Runs after the user picked a Google account.
     * Online mode: GoogleSignInManager has already signed into Firebase. Here we create users/{uid}
     * the first time. Offline mode: the account comes from the demo chooser and is saved on the phone.
     * Rule from our POE: an email that signed up with a password cannot log in with Google.
     */
    fun onGoogleSignedIn(google: GoogleAccount) {
        authError = null

        if (repo == null || auth == null) {
            val existing = LocalAccountStore.find(google.email)
            if (existing != null && existing.provider == AuthProvider.EMAIL) {
                authError = MSG_PASSWORD_ACCOUNT
                return
            }
            val account = existing ?: LocalAccountStore.createGoogleAccount(
                name = google.name.ifBlank { google.email.substringBefore('@') },
                email = google.email,
                photoUrl = google.photoUrl,
                language = _language
            )
            startOfflineSession(account)
            Log.i(TAG, "Google (demo) signed in offline: ${account.email} (new=${existing == null})")
            return
        }

        val fb = auth.currentUser
        if (fb == null || fb.uid != google.uid) {
            // This happens when Firebase is set up but Google sign-in is not (no web client in google-services.json).
            authError = "Google Sign-In isn't connected to Firebase yet. Check google-services.json and your SHA-1 (see FIREBASE_SETUP.md)."
            return
        }
        viewModelScope.launch {
            isLoading = true
            try {
                val existing = repo.getUserProfileOnce(fb.uid)
                if (existing != null && existing.provider == AuthProvider.EMAIL) {
                    stopListeners()
                    auth.signOut()
                    authError = MSG_PASSWORD_ACCOUNT
                    return@launch
                }
                if (existing == null) {
                    repo.createUserProfile(
                        uid = fb.uid,
                        name = google.name.ifBlank { google.email.substringBefore('@') },
                        email = google.email.trim().lowercase(),
                        provider = AuthProvider.GOOGLE,
                        photoUrl = google.photoUrl,
                        language = _language
                    ).addOnFailureListener { reportWriteFailure("your profile", it) }
                }
                startSession(fb)
                Log.i(TAG, "Google SSO signed in: ${google.email} (new=${existing == null})")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                authError = authMessage(e, registering = false)
                Log.w(TAG, "Google sign-in profile step failed", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun onGoogleError(message: String) {
        authError = message
        Log.w(TAG, "Google SSO error: $message")
    }

    fun signOut() {
        Log.i(TAG, "Signed out: ${currentUser?.email}")
        stopListeners() // stop listening first, so we don't get "permission denied" after signing out
        auth?.signOut()
        LocalAccountStore.setSession(null)
        currentUser = null
        _recipes = MockData.recipes
        savedRecipeIds = emptySet()
        dietaryPreferences = emptySet()
        notificationsEnabled = true
        justCompletedRecipe = null
        reviews = emptyList()
        authError = null
        lastError = null
        joinedCommunities = emptySet()
        followingCooks = emptySet()
        localPosts = emptyList()
        remotePosts = emptyList()
        downloads = emptyList()
    }

    // ---------------------------------------------------------------- change password

    /**
     * Changes the password of an email account. The current password is checked first.
     * onResult gets null when it worked, or a message explaining what went wrong.
     */
    fun changePassword(currentPassword: String, newPassword: String, onResult: (String?) -> Unit) {
        val user = currentUser ?: return onResult("Please sign in again.")
        if (user.authProvider == AuthProvider.GOOGLE) {
            return onResult("You sign in with Google, so your password is managed by your Google account.")
        }
        if (currentPassword == newPassword) return onResult("Choose a new password that is different from your current one.")

        if (repo == null || auth == null) {
            val account = LocalAccountStore.sessionAccount() ?: return onResult("Please sign in again.")
            if (!LocalAccountStore.verifyPassword(account, currentPassword)) {
                return onResult("Your current password is incorrect.")
            }
            LocalAccountStore.changePassword(account, newPassword)
            Log.i(TAG, "Password changed offline for ${account.email}")
            return onResult(null)
        }

        val fb = auth.currentUser ?: return onResult("Please sign in again.")
        viewModelScope.launch {
            isLoading = true
            try {
                // Firebase only lets you change a password if you signed in recently, so we check the current one again.
                fb.reauthenticate(EmailAuthProvider.getCredential(fb.email.orEmpty(), currentPassword)).await()
                fb.updatePassword(newPassword).await()
                Log.i(TAG, "Password changed for ${fb.email}")
                onResult(null)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Change password failed", e)
                onResult(
                    when (e) {
                        is FirebaseAuthInvalidCredentialsException -> "Your current password is incorrect."
                        is FirebaseNetworkException -> "No internet connection. Please connect and try again."
                        else -> authMessage(e, registering = false)
                    }
                )
            } finally {
                isLoading = false
            }
        }
    }

    // ---------------------------------------------------------------- language and settings

    fun setLanguage(lang: AppLanguage) {
        _language = lang
        if (repo == null || auth == null) {
            updateOfflineAccount { it.copy(language = lang) } // does nothing if nobody is signed in yet
            return
        }
        val uid = auth.currentUser?.uid ?: return // before login the choice is only kept in the app
        repo.patchUser(uid, mapOf(UserFields.LANGUAGE to lang.name.lowercase()))
            .addOnFailureListener { reportWriteFailure("your language", it) }
    }

    fun toggleDietaryPreference(pref: String) {
        val updated = if (pref in dietaryPreferences) dietaryPreferences - pref else dietaryPreferences + pref
        if (repo == null || auth == null) {
            updateOfflineAccount { it.copy(dietaryPrefs = updated.sorted().joinToString(",")) }
            return
        }
        val uid = auth.currentUser?.uid ?: return
        repo.setDietaryPrefs(uid, updated)
            .addOnFailureListener { reportWriteFailure("your dietary preferences", it) }
    }

    fun toggleNotifications(enabled: Boolean) {
        if (repo == null || auth == null) {
            updateOfflineAccount { it.copy(notificationsEnabled = enabled) }
            return
        }
        val uid = auth.currentUser?.uid ?: return
        repo.patchUser(uid, mapOf(UserFields.NOTIFICATIONS to enabled))
            .addOnFailureListener { reportWriteFailure("your notification setting", it) }
    }

    fun updateProfile(name: String, @Suppress("UNUSED_PARAMETER") bio: String) {
        if (repo == null || auth == null) {
            updateOfflineAccount { it.copy(name = name) }
            return
        }
        val uid = auth.currentUser?.uid ?: return
        repo.patchUser(uid, mapOf(UserFields.NAME to name))
            .addOnFailureListener { reportWriteFailure("your name", it) }
    }

    // ---------------------------------------------------------------- recipes

    fun addRecipe(recipe: Recipe) {
        if (currentUser == null) return
        val toSave = recipe.copy(
            recipeId = FirestoreRepository.newRecipeId(),
            authorName = currentUser?.name?.ifBlank { null } ?: recipe.authorName
        )
        if (repo == null || auth == null) {
            LocalAccountStore.addRecipe(toSave)
            updateOfflineAccount { it.copy(recipesAdded = it.recipesAdded + 1) }
            _recipes = MockData.recipes + LocalAccountStore.recipes()
            return
        }
        val fb = auth.currentUser ?: return
        // The recipes listener shows the new recipe straight away (even offline). Firestore uploads it later.
        repo.addRecipe(fb.uid, toSave)
            .addOnFailureListener { reportWriteFailure("your recipe", it) }
    }

    fun recipeById(id: Int): Recipe? = recipes.find { it.recipeId == id }

    fun toggleSaved(recipeId: Int) {
        val saveIt = recipeId !in savedRecipeIds
        if (repo == null || auth == null) {
            updateOfflineAccount {
                it.copy(savedRecipeIds = if (saveIt) it.savedRecipeIds + recipeId else it.savedRecipeIds - recipeId)
            }
            return
        }
        val uid = auth.currentUser?.uid ?: return
        repo.setSaved(uid, recipeId, saved = saveIt)
            .addOnFailureListener { reportWriteFailure("your saved recipes", it) }
    }

    fun markCompleted(recipe: Recipe) {
        justCompletedRecipe = recipe // this opens the celebration pop-up
        val db = repo ?: return      // offline mode only shows the pop-up
        val uid = auth?.currentUser?.uid ?: return
        db.markCompleted(uid, recipe)
            .addOnFailureListener { reportWriteFailure("that you completed this recipe", it) }
    }

    fun dismissCelebration() {
        justCompletedRecipe = null
    }

    // ---------------------------------------------------------------- reviews

    // Called when the Recipe Detail screen opens. Loads (and keeps listening to) that recipe's reviews.
    fun loadReviews(recipeId: Int) {
        if (reviewsRecipeId == recipeId) return // already showing this recipe's reviews
        reviewListener?.remove()
        reviewListener = null
        reviewsRecipeId = recipeId
        reviews = emptyList()

        val key = userKey
        if (repo == null || auth == null) {
            reviews = LocalAccountStore.reviews(recipeId).ifEmpty { key?.let { OfflineStore.reviews(it, recipeId) } ?: emptyList() }
            return
        }
        if (auth.currentUser == null) return
        // Without internet Firestore may have nothing saved yet, so show the reviews that were downloaded.
        reviews = key?.let { OfflineStore.reviews(it, recipeId) } ?: emptyList()
        reviewListener = repo.listenReviews(
            recipeId = recipeId,
            onChange = { list ->
                if (reviewsRecipeId == recipeId) {
                    reviews = if (list.isEmpty() && !NetworkMonitor.isOnline) reviews else list
                    // Keep the downloaded copy up to date.
                    if (list.isNotEmpty() && recipeId in downloadedRecipeIds) refreshDownload(recipeId, list)
                }
            },
            onError = { reportReadFailure("reviews", it) }
        )
    }

    fun addReview(recipeId: Int, rating: Int, comment: String) {
        val user = currentUser ?: return
        val review = Review(
            reviewId = Random.nextInt(1, Int.MAX_VALUE),
            recipeId = recipeId,
            userId = user.userId,
            rating = rating.coerceIn(1, 5),
            comment = comment.trim(),
            userName = user.name.ifBlank { "Anonymous" },
            createdAtMillis = System.currentTimeMillis()
        )
        if (repo == null || auth == null) {
            LocalAccountStore.addReview(review)
            if (reviewsRecipeId == recipeId) reviews = LocalAccountStore.reviews(recipeId)
            return
        }
        val uid = auth.currentUser?.uid ?: return
        // The reviews listener shows the new review straight away.
        repo.addReview(uid, review)
            .addOnFailureListener { reportWriteFailure("your review", it) }
    }

    // ---------------------------------------------------------------- downloads (offline use)

    // Saves recipes (with their reviews) on the phone so they can be opened without internet.
    fun downloadRecipes(list: List<Recipe>) {
        val key = userKey ?: return
        val now = System.currentTimeMillis()
        downloads = OfflineStore.save(key, list.map { r ->
            val r2 = if (r.recipeId == reviewsRecipeId) reviews else
                LocalAccountStore.reviews(r.recipeId).ifEmpty { OfflineStore.reviews(key, r.recipeId) }
            OfflineStore.Download(r, r2, now)
        })
        Log.i(TAG, "Downloaded ${list.size} recipe(s) for offline use")
    }

    fun downloadRecipe(recipe: Recipe) = downloadRecipes(listOf(recipe))

    fun removeDownload(recipeId: Int) {
        val key = userKey ?: return
        downloads = OfflineStore.remove(key, recipeId)
    }

    private fun refreshDownload(recipeId: Int, latestReviews: List<Review>) {
        val key = userKey ?: return
        val existing = downloads.firstOrNull { it.recipe.recipeId == recipeId } ?: return
        val recipe = _recipes.firstOrNull { it.recipeId == recipeId } ?: existing.recipe
        downloads = OfflineStore.save(key, listOf(OfflineStore.Download(recipe, latestReviews, existing.downloadedAtMillis)))
    }

    // ---------------------------------------------------------------- Community Hub

    // The cooks: the sample cooks plus anyone who added a recipe.
    val cooks: List<Cook>
        get() {
            val known = CommunityData.cooks
            val extra = recipes.map { it.authorName.trim() }
                .filter { it.isNotBlank() && it != "You" && known.none { c -> c.name.equals(it, ignoreCase = true) } }
                .distinct()
                .map { name ->
                    val culture = recipes.firstOrNull { it.authorName.trim() == name }?.culture ?: "Other"
                    Cook(name, culture, "Shares $culture recipes on The Mzantsi Table.", 0)
                }
            return known + extra
        }

    fun toggleJoin(communityId: String) {
        val key = userKey ?: return
        val updated = if (communityId in joinedCommunities) joinedCommunities - communityId else joinedCommunities + communityId
        joinedCommunities = updated
        CommunityStore.setJoined(key, updated)
        val uid = auth?.currentUser?.uid ?: return
        repo?.patchUser(uid, mapOf(UserFields.JOINED to updated.sorted()))
            ?.addOnFailureListener { reportWriteFailure("the communities you joined", it) }
    }

    fun toggleFollow(cookName: String) {
        val key = userKey ?: return
        val updated = if (cookName in followingCooks) followingCooks - cookName else followingCooks + cookName
        followingCooks = updated
        CommunityStore.setFollowing(key, updated)
        val uid = auth?.currentUser?.uid ?: return
        repo?.patchUser(uid, mapOf(UserFields.FOLLOWING to updated.sorted()))
            ?.addOnFailureListener { reportWriteFailure("the cooks you follow", it) }
    }

    // Shares "I cooked this!" with a community. The user also joins that community if they haven't yet.
    fun shareAttempt(recipe: Recipe, communityId: String, caption: String, photoPath: String) {
        val user = currentUser ?: return
        val key = userKey ?: return
        val post = CommunityPost(
            postId = "p" + System.currentTimeMillis().toString(36) + Random.nextInt(1000, 9999),
            authorKey = key,
            authorName = user.name.ifBlank { "Anonymous" },
            recipeId = recipe.recipeId,
            recipeTitle = recipe.title,
            communityId = communityId,
            caption = caption.trim(),
            photoPath = photoPath,
            createdAtMillis = System.currentTimeMillis()
        )
        if (communityId !in joinedCommunities) toggleJoin(communityId)

        val uid = auth?.currentUser?.uid
        if (repo == null || uid == null) {
            localPosts = CommunityStore.addLocalPost(post)
            return
        }
        // Show it straight away; the Firestore listener replaces it with the saved copy.
        remotePosts = listOf(post) + remotePosts
        repo.addPost(uid, post).addOnFailureListener { reportWriteFailure("your attempt", it) }
    }

    fun ratePost(post: CommunityPost, stars: Int) {
        val key = userKey ?: return
        val value = stars.coerceIn(1, 5)
        updatePost(post, change = { it.copy(ratings = it.ratings + (key to value)) }) { db, uid ->
            db.ratePost(post.postId, uid, value).addOnFailureListener { reportWriteFailure("your rating", it) }
        }
    }

    fun toggleCheer(post: CommunityPost) {
        val key = userKey ?: return
        val cheer = key !in post.cheers
        updatePost(post, change = { it.copy(cheers = if (cheer) it.cheers + key else it.cheers - key) }) { db, uid ->
            db.setCheer(post.postId, uid, cheer).addOnFailureListener { reportWriteFailure("your cheer", it) }
        }
    }

    fun addEncouragement(post: CommunityPost, text: String) {
        val user = currentUser ?: return
        val clean = text.trim()
        if (clean.isEmpty()) return
        val comment = PostComment(user.name.ifBlank { "Anonymous" }, clean.take(280), System.currentTimeMillis())
        updatePost(post, change = { it.copy(comments = it.comments + comment) }) { db, uid ->
            db.addComment(post.postId, uid, comment).addOnFailureListener { reportWriteFailure("your message", it) }
        }
    }

    // Sample posts and offline posts are changed on the phone. Firestore posts are changed in Firestore
    // (we also update the screen straight away so it feels instant).
    private fun updatePost(
        post: CommunityPost,
        change: (CommunityPost) -> CommunityPost,
        remote: (FirestoreRepository, String) -> Unit
    ) {
        val uid = auth?.currentUser?.uid
        val isLocal = post.isSample || repo == null || uid == null || localPosts.any { it.postId == post.postId }
        if (isLocal) {
            localPosts = CommunityStore.updateLocalPost(post.postId, change)
        } else {
            remotePosts = remotePosts.map { if (it.postId == post.postId) change(it) else it }
            remote(repo!!, uid!!)
        }
    }

    // ---------------------------------------------------------------- error messages

    private fun reportWriteFailure(what: String, e: Exception) {
        Log.w(TAG, "Firestore write failed ($what)", e)
        lastError = if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            "Couldn't save $what: Firestore security rules denied the write."
        } else {
            "Couldn't save $what."
        }
    }

    private fun reportReadFailure(what: String, e: FirebaseFirestoreException) {
        Log.w(TAG, "Firestore read failed ($what)", e)
        if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            lastError = "Couldn't load $what: Firestore security rules denied the read."
        }
    }

    // Turns a Firebase error into a message that a normal user can understand.
    private fun authMessage(e: Exception, registering: Boolean): String = when {
        e is FirebaseAuthException && e.errorCode == "ERROR_OPERATION_NOT_ALLOWED" ->
            "This sign-in method isn't enabled in Firebase yet (Authentication > Sign-in method)."
        e is FirebaseAuthUserCollisionException -> MSG_EMAIL_TAKEN
        e is FirebaseAuthWeakPasswordException ->
            "That password is too weak. Please choose a stronger one."
        e is FirebaseAuthInvalidCredentialsException && registering ->
            "That email address doesn't look valid."
        e is FirebaseAuthInvalidUserException || e is FirebaseAuthInvalidCredentialsException -> MSG_WRONG_LOGIN
        e is FirebaseNetworkException ->
            "No internet connection. Please connect and try again."
        e is FirebaseTooManyRequestsException ->
            "Too many attempts. Please wait a moment and try again."
        else -> e.localizedMessage ?: "Something went wrong. Please try again."
    }

    override fun onCleared() {
        stopListeners()
        super.onCleared()
    }

    private companion object {
        const val MSG_EMAIL_TAKEN =
            "An account with this email already exists. Please log in instead (if you signed up with Google, use “Continue with Google”)."
        const val MSG_WRONG_LOGIN =
            "Incorrect email or password. If you signed up with Google, use “Continue with Google”."
        const val MSG_GOOGLE_ACCOUNT =
            "This email signed up with Google. Please use “Continue with Google” to log in."
        const val MSG_PASSWORD_ACCOUNT =
            "This email is registered with a password. Please log in with your email and password."
    }
}
