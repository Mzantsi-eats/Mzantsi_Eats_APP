package com.mzantsi.table

import android.app.Application
import com.mzantsi.table.data.CommunityStore
import com.mzantsi.table.data.LocalAccountStore
import com.mzantsi.table.data.NetworkMonitor
import com.mzantsi.table.data.api.ApiCache
import com.mzantsi.table.data.OfflineStore
import com.mzantsi.table.data.firebase.FirestoreRepository

class MzantsiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        configureFirestoreCache()
        LocalAccountStore.init(this)
        CommunityStore.init(this)
        OfflineStore.init(this)
        ApiCache.init(this)
        NetworkMonitor.init(this)
    }

    private fun configureFirestoreCache() {
        if (!FirestoreRepository.isAvailable()) return
        runCatching {
            com.google.firebase.firestore.FirebaseFirestore.getInstance().firestoreSettings =
                com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(
                        com.google.firebase.firestore.PersistentCacheSettings.newBuilder()
                            .setSizeBytes(com.google.firebase.firestore.FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                            .build()
                    )
                    .build()
        }.onFailure { android.util.Log.w("MzantsiApp", "Firestore cache settings not applied", it) }
    }
}
