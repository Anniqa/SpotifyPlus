package com.lenerd.spotifyplus.manager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lenerd.spotifyplus.manager.model.ManagerUiState
import com.lenerd.spotifyplus.manager.model.HookStatus

@Composable
fun AboutScreen(
    state: ManagerUiState,
    onRefreshStatus: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Status & Info",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Module Enabled: ${if (state.moduleEnabled) "Yes" else "No"}")
                    Text(
                        "Hook State: ${
                            when (state.hookStatus) {
                                HookStatus.HOOKED -> "Hooked"
                                HookStatus.PARTIAL -> "Partial"
                                HookStatus.NOT_HOOKED -> "Not Hooked"
                            }
                        }",
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        "Target Package: ${state.packageName}",
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        "Active Items: ${state.generalSettings.count { it.enabled }}",
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    Button(
                        onClick = onRefreshStatus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Text("Refresh")
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "About SpotifyPlus",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "This app manages hooks, scripts, status, and updates for the SpotifyPlus module.",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}