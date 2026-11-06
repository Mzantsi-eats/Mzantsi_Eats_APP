package com.mzantsi.table.notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mzantsi.table.data.firebase.FirestoreRepository

class MzantsiMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val id = data["id"] ?: message.messageId ?: return
        val toUid = data["toUid"].orEmpty()
        val me = runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
        if (toUid.isNotEmpty() && toUid != me) {
            Log.i(TAG, "Push for another account ignored")
            return
        }
        NotificationHelper.show(
            id = id,
            title = data["title"] ?: message.notification?.title ?: "The Mzantsi Table",
            body = data["body"] ?: message.notification?.body.orEmpty(),
            route = data["route"].orEmpty()
        )
    }

    override fun onNewToken(token: String) {
        PushTokens.save(token)
    }

    private companion object {
        const val TAG = "MzantsiPush"
    }
}

object PushTokens {

    private const val TAG = "MzantsiPush"
    private var lastToken: String? = null

    private fun available(): Boolean = FirestoreRepository.isAvailable()

    fun register(onToken: (String) -> Unit) {
        if (!available()) return
        runCatching {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    lastToken = token
                    onToken(token)
                }
                .addOnFailureListener { Log.w(TAG, "Couldn't get the push token", it) }
        }.onFailure { Log.w(TAG, "Push messaging isn't set up", it) }
    }

    fun save(token: String) {
        lastToken = token
        if (!available()) return
        val uid = runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull() ?: return
        FirestoreRepository().addFcmToken(uid, token)
            .addOnFailureListener { Log.w(TAG, "Push token not saved", it) }
    }

    fun unregister(removeFromUser: (String) -> Unit) {
        if (!available()) return
        lastToken?.let { removeFromUser(it) }
        lastToken = null
        runCatching { FirebaseMessaging.getInstance().deleteToken() }
    }
}
