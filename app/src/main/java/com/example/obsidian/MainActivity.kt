package com.example.obsidian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.obsidian.navigation.AppNavigation
import com.example.obsidian.ui.screen.MainMenu
import com.example.obsidian.ui.screen.SettingsScreen
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
                    AppRoot()
                }
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "main_menu",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("main_menu") {
            MainMenu(
                onNewInvestigation = { navController.navigate("game") },
                onContinueCase = { navController.navigate("game") },
                onSettings = { navController.navigate("settings") }
            )
        }

        // Entering the game - reuse existing AppNavigation which contains the inner NavHost and bottom bar
        composable("game") {
            AppNavigation()
        }

        // Settings screen opened from MainMenu (no bottom bar)
        composable("settings") {
            SettingsScreen(onBack = { navController.navigate("main_menu") })
        }
    }
}

