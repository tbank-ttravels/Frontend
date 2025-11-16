package com.example.myapplication

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun TravelApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("auth") { AuthScreen(navController) }
        composable("registration") { Registration(navController) }
        composable("welcome") { Welcome(navController) }
        composable("main") { MainScreen(navController) }
        composable("trip_detail/{tripId}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            TripDetailScreen(tripId = tripId, navController = navController)
        }
        composable("trip_detail") {
            TripDetailScreen(tripId = null, navController = navController)
        }
        composable("create_trip"){ Create_trip(navController) }
    }
}
@Composable
fun TripDetailScreen(tripId: String?, navController: androidx.navigation.NavHostController) {

    Text("Детали поездки: $tripId")
}