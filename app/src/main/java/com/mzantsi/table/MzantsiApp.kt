package com.mzantsi.table

import android.app.Application
import com.mzantsi.table.data.CommunityStore
import com.mzantsi.table.data.LocalAccountStore
import com.mzantsi.table.data.NetworkMonitor
import com.mzantsi.table.data.api.ApiCache
import com.mzantsi.table.data.OfflineStore
import com.mzantsi.table.data.firebase.FirestoreRepository

// This runs once when the app starts, before any screen opens.
class MzantsiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        configureFirestoreCache()   // must run before anything else touches Firestore
        // Get the offline storage ready first, because AppViewModel reads it to sign the user back in.
        LocalAccountStore.init(this)
        CommunityStore.init(this)   // Community Hub (joined communities, followed cooks, attempts)
        OfflineStore.init(this)     // recipes downloaded for offline use
        ApiCache.init(this)         // recipes + waiting-to-send items for the ASP.NET API
        NetworkMonitor.init(this)   // knows when the phone is offline
    }

    // Firestore already keeps a copy of the data on the phone, but its default cache is limited in size
    // and old data can be thrown away. We turn on the persistent cache with no size limit so that
    // everything the user has opened stays available offline.
    private fun configureFirestoreCache() {
        if (!FirestoreRepository.isAvailable()) return // offline-only build (no google-services.json)
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
