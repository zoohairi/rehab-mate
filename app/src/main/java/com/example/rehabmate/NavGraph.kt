package com.example.rehabmate

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rehabmate.screens.*

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "welcome_screen",
        modifier = modifier
    ) {
        composable("welcome_screen") { WelcomeScreen(navController) }
        composable("login_screen") { LoginScreen(navController) }
        composable("register_screen") { RegisterScreen(navController) }
        composable("personalised_screen/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username")
            PersonalisedScreen(navController, username)
        }
        composable("appointment_screen") { AppointmentScreen(navController) }
        composable("exercise_screen") { ExerciseScreen(navController) }
        composable("editProfile_screen") { editprofileScreen(navController) }
        composable("profile_screen") { ProfileScreen(navController) }
    }
}
