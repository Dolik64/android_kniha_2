package com.example.kniha_20.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kniha_20.ui.screens.EditorRoute
import com.example.kniha_20.ui.screens.HomeRoute
import com.example.kniha_20.ui.screens.PlayerRoute
import com.example.kniha_20.ui.screens.editor.EditorScreen
import com.example.kniha_20.ui.screens.home.HomeScreen
import com.example.kniha_20.ui.screens.player.PlayerScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToPlayer = { navController.navigate(PlayerRoute) },
                onNavigateToEditor = { navController.navigate(EditorRoute) }
            )
        }
        composable<PlayerRoute> {
            PlayerScreen(onBack = { navController.popBackStack() })
        }
        composable<EditorRoute> {
            EditorScreen(onBack = { navController.popBackStack() })
        }
    }
}