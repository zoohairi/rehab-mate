package com.example.rehabmate

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rehabmate.screens.LoginScreen
import com.example.rehabmate.screens.PersonalisedScreen
import com.example.rehabmate.screens.appointmentScreen
import com.example.rehabmate.screens.editprofileScreen
import com.example.rehabmate.screens.exerciseScreen
import com.example.rehabmate.screens.welcomeScreen
import com.example.rehabmate.screens.registerScreen
//import com.example.rehabmate.screens.ProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = "login_screen") //uncomment this to see the linking (not completely integrated)
    //the path below is to be implemented
//    NavHost(navController = navController, startDestination = "welcome_screen")
    {

        // Welcome screen
        composable("welcome_screen") {
            welcomeScreen(navController)
        }

        // Login screen
        composable("login_screen") {
            LoginScreen(navController)
        }

        // Register screen (could be linked to login screen)
        composable("register_screen") {
            registerScreen(navController)
        }

        // User's home page
        composable("personalised_screen/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username")
            PersonalisedScreen(navController = navController, username = username)
        }

        // Appointment screen (needs amendment for linking)
        composable("appointment_screen") { backStackEntry ->
            appointmentScreen(navController)
        }

        // Exercise screen
        composable("exercise_screen") { backStackEntry ->
            exerciseScreen(navController)
        }

        // Edit profile screen
        composable("editProfile_screen") { backStackEntry ->
            editprofileScreen(navController)
        }

        // Profile page (the file code is all comment -Yet to update-)
        composable("profile_screen") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            //ProfileScreen(navController, username)
        }


        //================================= NOT IN USE =================================
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
            val agentImageResId =
                backStackEntry.arguments?.getString("agentImageResId")?.toIntOrNull() ?: 0
            //ChatScreen(navController, prompt, username, agentname, agentImageResId)
        }
    }
    //    The navigation follows this sequence:
    //
    //    1. Start Screen (route: start_screen) → The app begins here.
    //    2, Profile Screen (route: profile_screen/{name}) → User provides profile information.
    //    3. Choose Screen (route: choose_screen/{name}/{sex}/{age}/{occupation}) → User selects a virtual valentine.
    //    4. Chat Screen (route: chat_screen/{prompt}/{username}/{agentname}/{agentImageResId}) → User chats with their selected valentine.
}