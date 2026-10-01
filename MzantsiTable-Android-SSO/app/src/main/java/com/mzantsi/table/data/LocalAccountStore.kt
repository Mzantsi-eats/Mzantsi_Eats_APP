package com.mzantsi.table.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object LocalAccountStore {

    private const val PREFS = "mzantsi_accounts"
    private const val KEY_SESSION = "session_email"
    private const val KEY_NEXT_ID = "next_user_id"
    private const val ACCOUNT_PREFIX = "acct_"
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
        val recipesAdded: Int = 0
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

    //lookup
    fun find(email: String): Account? {
        val raw = prefs?.getString(ACCOUNT_PREFIX + normalise(email), null) ?: return null
        return runCatching { JSONObject(raw).toAccount() }.getOrNull()
    }

    fun save(account: Account) {
        prefs?.edit()?.putString(ACCOUNT_PREFIX + normalise(account.email), account.toJson().toString())?.apply()
    }

    //create
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

    //session
    fun setSession(email: String?) {
        prefs?.edit()?.apply {
            if (email == null) remove(KEY_SESSION) else putString(KEY_SESSION, normalise(email))
        }?.apply()
    }

    fun sessionAccount(): Account? {
        val email = prefs?.getString(KEY_SESSION, null) ?: return null
        return find(email)
    }

    //helpers
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
        recipesAdded = optInt("recipesAdded", 0)
    )
}
