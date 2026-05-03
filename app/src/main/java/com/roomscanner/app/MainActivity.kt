package com.roomscanner.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.roomscanner.app.ui.AdaptiveRoomScannerApp
import com.roomscanner.app.ui.theme.RoomScannerTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoomScannerTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                AdaptiveRoomScannerApp(windowSizeClass = windowSizeClass)
            }
        }
    }
}
