package com.example.obsidian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.obsidian.navigation.AppNavigation
import com.example.obsidian.navigation.Screen
import com.example.obsidian.ui.components.CyberBottomBar
import com.example.obsidian.ui.theme.ObsidianTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Esconder las barras del sistema para modo inmersivo
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on Case screen (MainMenu)
    val showBottomBar = currentRoute != Screen.Case.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CyberBottomBar(navController)
            }
        }
    ) { innerPadding ->
        AppNavigation(modifier = Modifier.padding(innerPadding), navController = navController)
    }
}
