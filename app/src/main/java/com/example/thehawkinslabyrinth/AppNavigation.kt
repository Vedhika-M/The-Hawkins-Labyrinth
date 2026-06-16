package com.example.thehawkinslabyrinth

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "gamescreen"
    ) {

        composable("gamescreen") {
            GameScreen(navController)
        }

        composable(
            route = "gameoverscreen/{userHP}/{vecnaHP}"
        ) { backStackEntry ->

            val userHP =
                backStackEntry.arguments?.getString("userHP")?.toInt() ?: 0

            val vecnaHP =
                backStackEntry.arguments?.getString("vecnaHP")?.toInt() ?: 0

            GameoverScreen(
                navController = navController,
                userHP = userHP,
                vecnaHP = vecnaHP
            )
        }
    }
}