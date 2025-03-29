package com.example.rehabmate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rehabmate.screens.AppointmentScreen
import com.example.rehabmate.screens.BeginnerExerciseScreen
import com.example.rehabmate.screens.ExerciseDemoScreen
import com.example.rehabmate.screens.ExerciseScreen
import com.example.rehabmate.screens.FavoritesScreen
import com.example.rehabmate.screens.ForgotPasswordScreen
import com.example.rehabmate.screens.LoginScreen
import com.example.rehabmate.screens.PersonalisedScreen
import com.example.rehabmate.screens.ProfileScreen
import com.example.rehabmate.screens.RegisterScreen
import com.example.rehabmate.screens.WelcomeScreen
import com.example.rehabmate.screens.editprofileScreen

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
        // Authentication Screens
        composable("welcome_screen") { WelcomeScreen(navController) }
        composable("login_screen") { LoginScreen(navController) }
        composable("register_screen") { RegisterScreen(navController) }
        composable("personalised_screen/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username")
            PersonalisedScreen(navController, username)
        }
        composable("forgot_password_screen") {
            ForgotPasswordScreen(navController)
        }

        // Main App Screens
        composable("appointment_screen") { AppointmentScreen(navController) }
        composable("profile_screen") { ProfileScreen(navController) }
        composable("editProfile_screen") { editprofileScreen(navController) }

        // Exercise Screens
        composable("exercise_screen") { ExerciseScreen(navController) }
        composable("favorites_screen") { FavoritesScreen(navController) }
        composable("beginner_exercise_screen") { BeginnerExerciseScreen(navController) }

        //ignore
        // Speech Screen - Using the proper Composable function
//        composable("speechScreen/{instructions}") { backStackEntry ->
//            val instructions = backStackEntry.arguments?.getString("instructions") ?: ""
//            SpeechScreen(navController, instructions)
//        }
//
//        composable("exercise_screen_api") {
//            ExerciseApiScreen(navController)
//        }

        // Exercise Details Screens
        composable("exercise_demo_screen/{exerciseId}") { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: "0"
            ExerciseDemoScreen(navController)
        }

    }
}