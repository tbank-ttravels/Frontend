package com.example.myapplication

import TripDetailScreen
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun TravelApp() {
    val navController = rememberNavController()
    val tripViewModel: TripViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController = navController)
        }

        composable("auth") {
            AuthScreen(
                navController = navController,
                userViewModel = userViewModel
            )
        }

        composable("registration") {
            Registration(
                navController = navController,
                userViewModel = userViewModel
            )
        }

        composable("welcome") {
            Welcome(navController = navController)
        }

        composable("main") {
            MainScreen(
                navController = navController,
                tripViewModel = tripViewModel,
                userViewModel = userViewModel
            )
        }

        composable("profile") {
            Profile(
                navController = navController,
                userViewModel = userViewModel
            )
        }

        composable("create_trip") {
            Create_trip(
                navController = navController,
                tripViewModel = tripViewModel
            )
        }


        composable("trip_detail/{tripId}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            TripDetailScreen(
                tripId = tripId,
                navController = navController,
                tripViewModel = tripViewModel
            )
        }

        composable("edit_trip/{tripId}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            EditTripScreen(
                tripId = tripId,
                navController = navController,
                tripViewModel = tripViewModel
            )
        }

    }}