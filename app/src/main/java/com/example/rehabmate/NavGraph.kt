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

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "splash_screen",
        modifier = modifier
    ) {
        // Splash & Auth Screens
        composable("splash_screen") { splash_screen(navController) }
        composable("welcome_screen") { WelcomeScreen(navController) }
        composable("login_screen") { LoginScreen(navController) }
        composable("register_screen") { RegisterScreen(navController) }
        composable("forgot_password_screen") { ForgotPasswordScreen(navController) }

        // Dashboard & Tabs
        composable("dashboard_screen") { DashboardScreen(navController) }
        composable("appointment_screen") { AppointmentScreen(navController) }
        composable("profile_screen") { ProfileScreen(navController) }
        composable("editProfile_screen") { editprofileScreen(navController) }
        composable("referral_screen") { ReferralScreen(navController) }

        // Exercise Pages
        composable("favorites_screen") { FavoritesScreen(navController) }
        composable("beginner_exercise_screen") { BeginnerExerciseScreen(navController) }

        composable("exercise_demo_screen/{exerciseId}") { backStackEntry ->
            ExerciseDemoScreen(navController)
        }

        composable("speechScreen/{instructions}") { backStackEntry ->
            val instructions = backStackEntry.arguments?.getString("instructions") ?: ""
            SpeechScreen(navController, instructions)
        }

        // Tabs
        composable("tab_home") { HomeTab(navController) }
        composable("tab_exercise_info/{status}/{exerciseCode}") { backStackEntry ->
            val status = backStackEntry.arguments?.getString("status")
            val code = backStackEntry.arguments?.getString("exerciseCode")
            ExerciseListTab(navController, status, code)
        }
        composable("tab_exercise_demo/{desc}/{duration}/{title}") { backStackEntry ->
            val desc = backStackEntry.arguments?.getString("desc")
            val duration = backStackEntry.arguments?.getString("duration")
            val title = backStackEntry.arguments?.getString("title")
            ExerciseDemoTab(navController, desc, duration, title)
        }

        // API & Progress
        composable("exercise_screen_api") { ExerciseApiScreen(navController) }
        composable("about_app_screen") { AboutAppScreen(navController) }
        composable("medical_records_screen") { medicalHistoryScreen(navController) }
        composable("progress_tracking_screen/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProgressTrackingScreen(navController, userId)
        }
    }
}