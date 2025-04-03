package com.example.rehabmate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rehabmate.screens.*
import com.example.rehabmate.screens.WelcomeScreen

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
        composable("referral_screen") { ReferralScreen(navController) }

        // Exercise Screens
        composable("dashboard_screen") { HomeTab(navController) }
        composable("favorites_screen") { FavoritesScreen(navController) }
        composable("beginner_exercise_screen") { BeginnerExerciseScreen(navController) }

        composable("about_app_screen") { AboutAppScreen(navController) }
        composable("medical_records_screen") {
            medicalHistoryScreen(navController)
        }

        // ========== Dashboard tabs ==========
        //to display exercise details
        composable("home_tab") { HomeTab(navController) }
        composable("exercise_info_tab/{status}/{exerciseCode}") { backStackEntry ->
            val exerciseStatus = backStackEntry.arguments?.getString("status")
            val exerciseCode = backStackEntry.arguments?.getString("exerciseCode")
            ExerciseListTab(navController, exerciseStatus, exerciseCode)
        }

        //to exercise page
        composable("home_tab") { HomeTab(navController) }
        composable("exercise_demo_tab/{exercise_description}/{exercise_duration}/{exercise_title}") { backStackEntry ->
            val exercisedesc = backStackEntry.arguments?.getString("exercise_description")
            val exerciseDuration = backStackEntry.arguments?.getString("exercise_duration")
            val exerciseTitle = backStackEntry.arguments?.getString("exercise_title")
            ExerciseDemoTab(navController, exercisedesc, exerciseDuration, exerciseTitle)
        }

        //progress tracking
        composable(
            route = "progress_tracking_screen/{userId}"
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProgressTrackingScreen(navController, userId)
        }


    }
}