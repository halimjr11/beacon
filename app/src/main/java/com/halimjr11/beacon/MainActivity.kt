package com.halimjr11.beacon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.halimjr11.beacon.navigation.BeaconNavHost
import com.halimjr11.beacon.ui.theme.BeaconTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { BeaconTheme { BeaconNavHost() } }
    }
}
