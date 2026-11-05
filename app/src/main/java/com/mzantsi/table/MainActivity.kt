package com.mzantsi.table

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mzantsi.table.ui.nav.MzantsiNavGraph
import com.mzantsi.table.ui.theme.MzantsiTableTheme

// The only Activity in the app. It applies our theme and shows the navigation graph,
// which decides which screen is on display.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MzantsiTableTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MzantsiNavGraph()
                }
            }
        }
    }
}
