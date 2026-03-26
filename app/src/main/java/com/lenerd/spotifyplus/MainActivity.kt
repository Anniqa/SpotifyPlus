package com.lenerd.spotifyplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lenerd.spotifyplus.manager.ui.SpotifyPlusApp
import com.lenerd.spotifyplus.manager.ui.theme.SpotifyPlusTheme
import com.lenerd.spotifyplus.manager.viewmodel.ManagerViewModel
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener {
            override fun onServiceBind(service: XposedService) {
                val prefs = service.getRemotePreferences("SpotifyPlus")
                SettingsSync.syncLocalToRemote(this@MainActivity, prefs)
                SettingsSync.syncRemoteToLocal(this@MainActivity, prefs)
            }

            override fun onServiceDied(p0: XposedService) { }
        })

        setContent {
            SpotifyPlusTheme {
                Surface(modifier = Modifier) {
                    val viewModel: ManagerViewModel = viewModel()
                    SpotifyPlusApp(viewModel = viewModel)
                }
            }
        }
    }
}