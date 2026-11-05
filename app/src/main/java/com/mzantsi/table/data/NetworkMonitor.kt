package com.mzantsi.table.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Tells the screens if the phone has internet right now, so we can show an
// "You're offline" banner and point the user to their downloaded recipes.
object NetworkMonitor {

    var isOnline by mutableStateOf(true)
        private set

    private var started = false
    private val main = Handler(Looper.getMainLooper())

    fun init(context: Context) {
        if (started) return
        started = true
        val cm = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return

        isOnline = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        runCatching {
            cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) = post(true)
                override fun onLost(network: Network) = post(false)
                override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) =
                    post(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            })
        }
    }

    private fun post(online: Boolean) {
        main.post { isOnline = online }
    }
}
