package com.mzantsi.table

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mzantsi.table.notifications.NotificationHelper
import com.mzantsi.table.ui.nav.MzantsiNavGraph
import com.mzantsi.table.ui.theme.MzantsiTableTheme

class MainActivity : ComponentActivity() {

    private var openRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) openRoute = routeFrom(intent)
        setContent {
            MzantsiTableTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MzantsiNavGraph(openRoute = openRoute, onRouteOpened = { openRoute = null })
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        routeFrom(intent)?.let { openRoute = it }
    }

    private fun routeFrom(intent: Intent?): String? =
        (intent?.getStringExtra(NotificationHelper.EXTRA_ROUTE) ?: intent?.getStringExtra("route"))?.ifBlank { null }
}
