package com.mzantsi.table.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

// Saves the Community Hub on the phone:
//  - which communities each user joined and which cooks they follow,
//  - the attempts (posts) and their ratings, cheers and comments.
// In offline mode (no Firebase) this is the only place the community is saved.
// In online mode posts are saved in Firestore, and a copy is kept here so the hub still opens without internet.
object CommunityStore {

    private const val PREFS = "mzantsi_community"
    private const val KEY_LOCAL_POSTS = "local_posts"
    private const val KEY_REMOTE_CACHE = "remote_posts_cache"
    private const val JOINED_PREFIX = "joined_"
    private const val FOLLOWING_PREFIX = "following_"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        }
    }

    // --- joined communities and followed cooks

    fun joined(userKey: String): Set<String> = readSet(JOINED_PREFIX + userKey)
    fun setJoined(userKey: String, ids: Set<String>) = writeSet(JOINED_PREFIX + userKey, ids)

    fun following(userKey: String): Set<String> = readSet(FOLLOWING_PREFIX + userKey)
    fun setFollowing(userKey: String, names: Set<String>) = writeSet(FOLLOWING_PREFIX + userKey, names)

    // posts

    // Posts saved on this phone: the sample posts (with the user's ratings and cheers on them)
    // plus, in offline mode, the attempts users shared on this phone.
    fun localPosts(): List<CommunityPost> {
        val raw = prefs?.getString(KEY_LOCAL_POSTS, null)
        if (raw == null) {
            val samples = CommunityData.samplePosts()
            saveLocalPosts(samples)
            return samples
        }
        return JsonCodec.parseList(raw, JsonCodec::jsonToPost)
    }

    fun saveLocalPosts(posts: List<CommunityPost>) {
        prefs?.edit()?.putString(KEY_LOCAL_POSTS, JsonCodec.writeList(posts, JsonCodec::postToJson))?.apply()
    }

    // Changes one local post (for example adds a rating) and saves the list again.
    fun updateLocalPost(postId: String, change: (CommunityPost) -> CommunityPost): List<CommunityPost> {
        val updated = localPosts().map { if (it.postId == postId) change(it) else it }
        saveLocalPosts(updated)
        return updated
    }

    fun addLocalPost(post: CommunityPost): List<CommunityPost> {
        val updated = listOf(post) + localPosts()
        saveLocalPosts(updated)
        return updated
    }

    // The last posts we got from Firestore, so the hub can open without internet.
    fun cachedRemotePosts(): List<CommunityPost> =
        JsonCodec.parseList(prefs?.getString(KEY_REMOTE_CACHE, null), JsonCodec::jsonToPost)

    fun cacheRemotePosts(posts: List<CommunityPost>) {
        prefs?.edit()?.putString(KEY_REMOTE_CACHE, JsonCodec.writeList(posts, JsonCodec::postToJson))?.apply()
    }

    // helpers

    private fun readSet(key: String): Set<String> {
        val raw = prefs?.getString(key, null) ?: return emptySet()
        return runCatching { JSONArray(raw).let { a -> (0 until a.length()).map { a.optString(it) }.toSet() } }
            .getOrDefault(emptySet())
    }

    private fun writeSet(key: String, values: Set<String>) {
        prefs?.edit()?.putString(key, JSONArray(values.toList()).toString())?.apply()
    }
}
