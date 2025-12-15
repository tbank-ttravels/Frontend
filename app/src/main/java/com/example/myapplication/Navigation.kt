package com.example.myapplication

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun TravelApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val backend = remember { BackendProvider.get(context) }

    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModel.provideFactory(backend)
    )

    val tripViewModel: TripViewModel = viewModel(
        factory = TripViewModel.provideFactoryTrip(backend)
    )

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController = navController, userViewModel = userViewModel)
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
            Welcome(
                navController = navController,
                userViewModel = userViewModel
            )
        }

        composable("main") {
            MainScreen(
                navController = navController,
                tripViewModel = tripViewModel
            )
        }

        composable("add_transfer/{tripId}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            AddTransferScreen(
                tripId = tripId,
                navController = navController,
                tripViewModel = tripViewModel
            )
        }

        composable("invitations") {
            InvitationsScreen(
                navController = navController,

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

        composable("add_finance/{tripId}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            val trip = tripViewModel.getTripById(tripId ?: "")
            if (trip != null) {
                Add_Finance(
                    trip = trip,
                    tripViewModel = tripViewModel,
                    navController = navController
                )
            } else {
                Text("Поездка не найдена")
            }
        }

        composable("edit_profile") {
            EditProfileScreen(
                navController = navController,
                userViewModel = userViewModel
            )
        }

        composable("add_participant/{tripId}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            AddParticipantScreen(
                tripId = tripId,
                navController = navController,
                userViewModel = userViewModel,
                tripViewModel = tripViewModel
            )
        }

        composable("trip_detail/{tripId}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            TripDetailScreen(
                tripId = tripId,
                navController = navController,
                tripViewModel = tripViewModel,
                userViewModel = userViewModel
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
    }
}