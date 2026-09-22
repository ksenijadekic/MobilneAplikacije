package com.example.rmas_projekat.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun AppNavHost(nav: NavHostController) {
    NavHost(navController = nav, startDestination = "login") {

        composable("login") {
            LoginScreen(navController = nav)
        }

        composable("register") {
            RegisterScreen(navController = nav)
        }

        // Glavni ekran: lista restorana (ostali ekrani su u donjoj navigaciji)
        composable("main") {
            MainScreen(navController = nav)
        }

        // Mapa: "maps" ili "maps?focusId=<id restorana>" (klik na restoran u listi)
        composable(
            route = "maps?focusId={focusId}",
            arguments = listOf(
                navArgument("focusId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            MapsScreen(
                navController = nav,
                focusId = entry.arguments?.getString("focusId")
            )
        }

        composable("addObject") {
            AddObjectScreen(navController = nav)
        }

        composable("leaderboard") {
            LeaderboardScreen(navController = nav)
        }

        composable("profile") {
            ProfileScreen(navController = nav)
        }
    }
}
