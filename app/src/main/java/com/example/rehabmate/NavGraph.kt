package com.example.rehabmate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rehabmate.screens.*
import com.example.rehabmate.screens.demoPurposes.ExerciseApiScreen
import com.example.rehabmate.screens.demoPurposes.ExerciseDemoScreen
import com.example.rehabmate.screens.demoPurposes.SpeechScreen
import com.example.rehabmate.screens.screensToBeDeleted.WelcomeScreen

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
        composable("dashboard_screen") { DashboardScreen(navController) } //for exercise info's'
        composable("favorites_screen") { FavoritesScreen(navController) }
        composable("beginner_exercise_screen") { BeginnerExerciseScreen(navController) }

        // FOR TESTING API PURPOSE
        // Speech Screen - Using the proper Composable function
        composable("speechScreen/{instructions}") { backStackEntry ->
            val instructions = backStackEntry.arguments?.getString("instructions") ?: ""
            SpeechScreen(navController, instructions)
        }

        composable("exercise_screen_api") {
            ExerciseApiScreen(navController)
        }
        composable("exerciseInfo_screen/{exercise_name}") {
            ExerciseInfoTab(navController)
        }
        // Exercise Details Screens
        composable("exercise_demo_screen/{exerciseId}") { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: "0"
            ExerciseDemoScreen(navController)
        }

        composable("about_app_screen") { AboutAppScreen(navController) }
        composable("medical_records_screen") {
            // You can create a placeholder screen or redirect to profile for now
            ProfileScreen(navController)
        }

//        // Exercise History Screen (placeholder)
//        composable("exercise_history_screen") {
//            // You can create a placeholder screen or redirect to exercise screen for now
//            ExerciseScreen(navController)
//        }
    }
}
