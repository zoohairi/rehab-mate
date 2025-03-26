package com.example.rehabmate

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rehabmate.screens.ForgotPasswordScreen
import com.example.rehabmate.screens.LoginScreen
import com.example.rehabmate.screens.PersonalisedScreen
import com.example.rehabmate.screens.RegisterScreen
import com.example.rehabmate.screens.StartScreen

//import com.example.rehabmate.screens.ProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = "start_screen") {
        composable("start_screen") {
            StartScreen(navController)
        }
        composable("login_screen") {
            LoginScreen(navController)
        }
        composable("register_screen") {
            RegisterScreen(navController)
        }
        composable("forgot_password_screen") {
            ForgotPasswordScreen(navController)
        }
        composable("personalised_screen/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username")
            PersonalisedScreen(navController = navController, username = username)
        }
        composable("profile_screen/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            //ProfileScreen(navController, username)
        }
        composable("choose_screen/{name}/{sex}/{age}/{occupation}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val sex = backStackEntry.arguments?.getString("sex") ?: ""
            val age = backStackEntry.arguments?.getString("age") ?: ""
            val occupation = backStackEntry.arguments?.getString("occupation") ?: ""
            //ChooseScreen(navController, name, sex, age, occupation)
        }
        composable("chat_screen/{prompt}/{username}/{agentname}/{agentImageResId}") { backStackEntry ->
            val prompt = backStackEntry.arguments?.getString("prompt") ?: ""
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val agentname = backStackEntry.arguments?.getString("agentname") ?: ""
            val agentImageResId = backStackEntry.arguments?.getString("agentImageResId")?.toIntOrNull() ?: 0
            //ChatScreen(navController, prompt, username, agentname, agentImageResId)
        }
    }
    //    The navigation follows this sequence:
    //
    //    1. Start Screen (route: start_screen) → The app begins here.
    //    2, Profile Screen (route: profile_screen/{name}) → User provides profile information.
}