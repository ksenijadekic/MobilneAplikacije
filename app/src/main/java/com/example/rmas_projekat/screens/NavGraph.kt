package com.example.rmas_projekat

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rmas_projekat.screens.RegisterScreen
import com.example.rmas_projekat.screens.MapsScreen  // tvoj Mapbox screen

@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "register") {

        composable("register") {
            RegisterScreen(navController = nav)
        }

        composable("map") {
            MapsScreen()
        }
    }
}
