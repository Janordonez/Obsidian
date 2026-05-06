package com.example.obsidian.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.obsidian.ui.screen.*
import com.example.obsidian.ui.viewmodel.GameViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Case.route,
        modifier = modifier
    ) {
        composable(Screen.Case.route) {
            MainMenu(
                isGenerating = viewModel.isGenerating,
                onNewInvestigation = { 
                    viewModel.startNewInvestigation()
                    navController.navigate(Screen.Interrogation.route) 
                },
                onContinueCase = { navController.navigate(Screen.Evidence.route) },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Clue.route) { 
            ClueScreen(navController, Modifier.padding(innerPadding)) 
        }
        composable(Screen.Evidence.route) { 
            EvidenceScreen(navController, viewModel, Modifier.padding(innerPadding))
        }
        composable(Screen.Interrogation.route) { 
            InterrogationScreen(navController, viewModel, Modifier.padding(innerPadding)) 
        }
        composable(Screen.Map.route) { 
            MapScreen(navController, viewModel, Modifier.padding(innerPadding))
        }
        composable(Screen.Settings.route) { 
            SettingsScreen(navController, Modifier.padding(innerPadding)) 
        }
    }
}
