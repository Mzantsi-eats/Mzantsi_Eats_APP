package com.mzantsi.table.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

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

    fun joined(userKey: String): Set<String> = readSet(JOINED_PREFIX + userKey)
    fun setJoined(userKey: String, ids: Set<String>) = writeSet(JOINED_PREFIX + userKey, ids)

    fun following(userKey: String): Set<String> = readSet(FOLLOWING_PREFIX + userKey)
    fun setFollowing(userKey: String, names: Set<String>) = writeSet(FOLLOWING_PREFIX + userKey, names)

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

    fun cachedRemotePosts(): List<CommunityPost> =
        JsonCodec.parseList(prefs?.getString(KEY_REMOTE_CACHE, null), JsonCodec::jsonToPost)

    fun cacheRemotePosts(posts: List<CommunityPost>) {
        prefs?.edit()?.putString(KEY_REMOTE_CACHE, JsonCodec.writeList(posts, JsonCodec::postToJson))?.apply()
    }

    private fun readSet(key: String): Set<String> {
        val raw = prefs?.getString(key, null) ?: return emptySet()
        return runCatching { JSONArray(raw).let { a -> (0 until a.length()).map { a.optString(it) }.toSet() } }
            .getOrDefault(emptySet())
    }

    private fun writeSet(key: String, values: Set<String>) {
        prefs?.edit()?.putString(key, JSONArray(values.toList()).toString())?.apply()
    }
}
