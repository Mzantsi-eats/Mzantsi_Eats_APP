package com.mzantsi.table.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
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
import com.mzantsi.table.data.MockData
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.data.User
import com.mzantsi.table.data.firebase.FirestoreRepository
import com.mzantsi.table.data.firebase.UserFields
import com.mzantsi.table.data.firebase.UserProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Firebase is the source of truth:
 *  - Firebase Authentication  -> who is signed in (email/password and Google)
 *  - Cloud Firestore          -> users/{uid}, recipes/{id}, users/{uid}/completed/{id}
 *
 * While someone is signed in we keep two live Firestore listeners (their user document and the
 * recipes collection). Every action below writes to Firestore, and the listeners feed the result
 * back into the Compose state, so the screens always show what is in the database (including
 * changes made from another phone, and local changes made while offline).
 */
class AppViewModel(
    private val repo: FirestoreRepository = FirestoreRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val TAG = "AppViewModel"

    private var userListener: ListenerRegistration? = null
    private var recipeListener: ListenerRegistration? = null

    // ---------------------------------------------------------------- state

    private var _language by mutableStateOf(AppLanguage.EN)
    val language get() = _language

    var currentUser by mutableStateOf<User?>(null)
        private set

    /** Built-in sample recipes until signed in; afterwards built-ins + everything in Firestore. */
    private var _recipes by mutableStateOf(MockData.recipes)
    val recipes get() = _recipes

    var isLoading by mutableStateOf(false)
        private set

    /** One-shot message shown as a snackbar (e.g. a write that Firestore rejected). */
    var lastError by mutableStateOf<String?>(null)
        private set

    /** Message shown on the Register / Login screen. */
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

    init {
        // Firebase Auth remembers the signed-in user between launches.
        auth.currentUser?.let {
            startSession(it)
            Log.i(TAG, "Session restored for ${it.email}")
        }
    }

    // ---------------------------------------------------------------- session / listeners

    private fun startSession(fb: FirebaseUser, displayName: String? = null) {
        stopListeners()
        // Show something immediately (works offline); the Firestore listener refines it.
        currentUser = User(
            uid = fb.uid,
            name = displayName ?: fb.displayName.orEmpty().ifBlank { fb.email.orEmpty().substringBefore('@') },
            email = fb.email.orEmpty(),
            languagePref = _language,
            authProvider = if (fb.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID })
                AuthProvider.GOOGLE else AuthProvider.EMAIL,
            photoUrl = fb.photoUrl?.toString().orEmpty()
        )

        userListener = repo.listenUser(
            uid = fb.uid,
            onChange = { applyProfile(fb.uid, it) },
            onError = { reportReadFailure("your profile", it) }
        )
        recipeListener = repo.listenRecipes(
            onChange = { remote -> mergeRecipes(remote) },
            onError = { reportReadFailure("recipes", it) }
        )
    }

    private fun stopListeners() {
        userListener?.remove()
        recipeListener?.remove()
        userListener = null
        recipeListener = null
    }

    private fun applyProfile(uid: String, p: UserProfile) {
        if (auth.currentUser?.uid != uid) return // signed out meanwhile
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
    }

    /** Built-in samples that aren't overridden in Firestore, followed by the Firestore recipes. */
    private fun mergeRecipes(remote: List<Recipe>) {
        val remoteIds = remote.map { it.recipeId }.toSet()
        _recipes = MockData.recipes.filter { it.recipeId !in remoteIds } + remote
        Log.d(TAG, "Recipes: ${remote.size} from Firestore, ${_recipes.size} total")
    }

    // ---------------------------------------------------------------- auth

    fun clearAuthError() {
        authError = null
    }

    fun clearLastError() {
        lastError = null
    }

    /** Email + password registration through Firebase Authentication, then a users/{uid} document. */
    fun register(name: String, email: String, password: String) {
        val cleanEmail = email.trim().lowercase()
        val cleanName = name.trim()
        viewModelScope.launch {
            isLoading = true
            authError = null
            try {
                val fb = auth.createUserWithEmailAndPassword(cleanEmail, password).await().user
                    ?: throw IllegalStateException("Firebase did not return a user.")
                runCatching {
                    fb.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(cleanName).build()).await()
                }
                repo.createUserProfile(fb.uid, cleanName, cleanEmail, AuthProvider.EMAIL, "", _language)
                    .addOnFailureListener { reportWriteFailure("your profile", it) }
                startSession(fb, displayName = cleanName)
                Log.i(TAG, "Registered ${fb.email}")
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

    /** Email + password login. Google accounts have no password, so they are told to use Google. */
    fun login(email: String, password: String) {
        val cleanEmail = email.trim().lowercase()
        viewModelScope.launch {
            isLoading = true
            authError = null
            try {
                val fb = auth.signInWithEmailAndPassword(cleanEmail, password).await().user
                    ?: throw IllegalStateException("Firebase did not return a user.")
                startSession(fb)
                Log.i(TAG, "Logged in ${fb.email}")
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
     * Called after GoogleSignInManager has already signed into Firebase Auth. Creates users/{uid}
     * on first sign-in. Keeps the POE rule that an email registered with a password can't use Google.
     */
    fun onGoogleSignedIn(google: GoogleAccount) {
        val fb = auth.currentUser
        if (fb == null || fb.uid != google.uid) {
            // The demo account chooser (no google-services.json web client) never signs into Firebase.
            authError = "Google Sign-In isn't connected to Firebase yet. Check google-services.json and your SHA-1 (see FIREBASE_SETUP.md)."
            return
        }
        viewModelScope.launch {
            isLoading = true
            authError = null
            try {
                val existing = repo.getUserProfileOnce(fb.uid)
                if (existing != null && existing.provider == AuthProvider.EMAIL) {
                    stopListeners()
                    auth.signOut()
                    authError = "This email is registered with a password. Please log in with your email and password."
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
        stopListeners() // before signOut, so no "permission denied" callbacks fire
        auth.signOut()
        currentUser = null
        _recipes = MockData.recipes
        savedRecipeIds = emptySet()
        dietaryPreferences = emptySet()
        notificationsEnabled = true
        justCompletedRecipe = null
        authError = null
        lastError = null
    }

    // ---------------------------------------------------------------- language / settings

    fun setLanguage(lang: AppLanguage) {
        _language = lang
        val uid = auth.currentUser?.uid ?: return // before login it's just a local choice
        repo.patchUser(uid, mapOf(UserFields.LANGUAGE to lang.name.lowercase()))
            .addOnFailureListener { reportWriteFailure("your language", it) }
    }

    fun toggleDietaryPreference(pref: String) {
        val uid = auth.currentUser?.uid ?: return
        val updated = if (pref in dietaryPreferences) dietaryPreferences - pref else dietaryPreferences + pref
        repo.setDietaryPrefs(uid, updated)
            .addOnFailureListener { reportWriteFailure("your dietary preferences", it) }
    }

    fun toggleNotifications(enabled: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        repo.patchUser(uid, mapOf(UserFields.NOTIFICATIONS to enabled))
            .addOnFailureListener { reportWriteFailure("your notification setting", it) }
    }

    fun updateProfile(name: String, @Suppress("UNUSED_PARAMETER") bio: String) {
        val uid = auth.currentUser?.uid ?: return
        repo.patchUser(uid, mapOf(UserFields.NAME to name))
            .addOnFailureListener { reportWriteFailure("your name", it) }
    }

    // ---------------------------------------------------------------- recipes

    fun addRecipe(recipe: Recipe) {
        val fb = auth.currentUser ?: return
        val toSave = recipe.copy(
            recipeId = FirestoreRepository.newRecipeId(),
            authorName = currentUser?.name?.ifBlank { null } ?: recipe.authorName
        )
        // The recipes listener shows it straight away (even offline) and Firestore syncs it later.
        repo.addRecipe(fb.uid, toSave)
            .addOnFailureListener { reportWriteFailure("your recipe", it) }
    }

    fun recipeById(id: Int): Recipe? = recipes.find { it.recipeId == id }

    fun toggleSaved(recipeId: Int) {
        val uid = auth.currentUser?.uid ?: return
        repo.setSaved(uid, recipeId, saved = recipeId !in savedRecipeIds)
            .addOnFailureListener { reportWriteFailure("your saved recipes", it) }
    }

    fun markCompleted(recipe: Recipe) {
        justCompletedRecipe = recipe
        val uid = auth.currentUser?.uid ?: return
        repo.markCompleted(uid, recipe)
            .addOnFailureListener { reportWriteFailure("that you completed this recipe", it) }
    }

    fun dismissCelebration() {
        justCompletedRecipe = null
    }

    // ---------------------------------------------------------------- errors

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

    private fun authMessage(e: Exception, registering: Boolean): String = when {
        e is FirebaseAuthException && e.errorCode == "ERROR_OPERATION_NOT_ALLOWED" ->
            "This sign-in method isn't enabled in Firebase yet (Authentication > Sign-in method)."
        e is FirebaseAuthUserCollisionException ->
            "An account with this email already exists. Please log in instead (if you signed up with Google, use “Continue with Google”)."
        e is FirebaseAuthWeakPasswordException ->
            "That password is too weak. Please choose a stronger one."
        e is FirebaseAuthInvalidCredentialsException && registering ->
            "That email address doesn't look valid."
        e is FirebaseAuthInvalidUserException || e is FirebaseAuthInvalidCredentialsException ->
            "Incorrect email or password. If you signed up with Google, use “Continue with Google”."
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
}
