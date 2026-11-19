package com.halimjr11.beacon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.halimjr11.beacon.navigation.BeaconNavHost
import com.halimjr11.beacon.ui.theme.BeaconTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate: swaps the launch theme for Theme.Beacon after the splash.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { BeaconTheme { BeaconNavHost() } }
    }
}
