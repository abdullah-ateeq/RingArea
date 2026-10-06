package com.example.ringarea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.ringarea.data.model.UserSettings
import com.example.ringarea.data.repository.SettingsRepository
import com.example.ringarea.ui.navigation.RingAreaApp
import com.example.ringarea.ui.theme.RingAreaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsRepository = SettingsRepository(applicationContext)

        setContent {
            val settings by settingsRepository.settingsFlow.collectAsState(initial = UserSettings())
            RingAreaTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                RingAreaApp()
            }
        }
    }
}