package com.example.obsidian.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.obsidian.ui.screen.CaseScreen
import com.example.obsidian.ui.screen.ClueScreen
import com.example.obsidian.ui.screen.EvidenceScreen
import com.example.obsidian.ui.screen.InterrogationScreen
import com.example.obsidian.ui.screen.MapScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Case.route,
        modifier = modifier
    ) {
        composable(Screen.Case.route) {
            CaseScreen(
                onTabSelected = { screen ->
                    navController.navigate(screen.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.Clue.route) { ClueScreen(navController, modifier) }
        composable(Screen.Evidence.route) { EvidenceScreen(navController, modifier) }
        composable(Screen.Interrogation.route) { InterrogationScreen(navController, modifier) }
        composable(Screen.Map.route) { MapScreen(navController, modifier) }
    }
}