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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.obsidian.navigation.AppNavigation
import com.example.obsidian.navigation.Screen
import com.example.obsidian.ui.components.CyberBottomBar
import com.example.obsidian.ui.theme.ObsidianTheme
import com.example.obsidian.ui.viewmodel.GameViewModel

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Esconder las barras del sistema para modo inmersivo
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.let {
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            it.hide(WindowInsetsCompat.Type.systemBars())
        }

        setContent {
            ObsidianTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF09090B)
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
    val gameViewModel: GameViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on Case (MainMenu), Accusation, and Verdict screens
    val showBottomBar = currentRoute != null &&
            currentRoute != Screen.Case.route &&
            currentRoute != Screen.Accusation.route &&
            currentRoute != Screen.Verdict.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CyberBottomBar(navController)
            }
        }
    ) { innerPadding ->
        AppNavigation(
            modifier = Modifier.padding(innerPadding), 
            navController = navController,
            viewModel = gameViewModel
        )
    }

    // Alerta de Pista Desbloqueada Global
    val unlockedClue by gameViewModel.unlockedClueAlert.collectAsStateWithLifecycle()
    unlockedClue?.let { clue ->
        AlertDialog(
            onDismissRequest = { gameViewModel.clearUnlockedClueAlert() },
            title = {
                Text(
                    text = "🚨 NUEVA PISTA DESBLOQUEADA 🚨",
                    color = Color(0xFFFFD700), // Amarillo Neón/Oro
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column {
                    Text(
                        text = clue.title,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = clue.description,
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Ubicación: ${clue.locationName}",
                        color = Color(0xFF00FFFF), // Cian Neón
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { gameViewModel.clearUnlockedClueAlert() }
                ) {
                    Text("ENTENDIDO", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0D0E11),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        )
    }
}
