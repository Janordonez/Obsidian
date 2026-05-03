package com.example.obsidian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.obsidian.navigation.AppNavigation
import com.example.obsidian.ui.theme.ObsidianTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ObsidianTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF09090B) // zinc-950
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
