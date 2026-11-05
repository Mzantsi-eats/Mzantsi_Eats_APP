package com.mzantsi.table

import android.app.Application
import com.mzantsi.table.data.CommunityStore
import com.mzantsi.table.data.LocalAccountStore
import com.mzantsi.table.data.NetworkMonitor
import com.mzantsi.table.data.OfflineStore

// This runs once when the app starts, before any screen opens.
class MzantsiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Get the offline storage ready first, because AppViewModel reads it to sign the user back in.
        LocalAccountStore.init(this)
        CommunityStore.init(this)   // Community Hub (joined communities, followed cooks, attempts)
        OfflineStore.init(this)     // recipes downloaded for offline use
        NetworkMonitor.init(this)   // knows when the phone is offline
    }
}
