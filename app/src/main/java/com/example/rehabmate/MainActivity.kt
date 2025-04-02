// --- MainActivity.kt ---

package com.example.rehabmate

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rehabmate.ui.theme.RehabMateTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        setContent {
            RehabMateTheme {
                val navController = rememberNavController()
                MainScreen(navController)
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { isLoggedIn = it.currentUser != null }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && (currentRoute == null || currentRoute in listOf(
                "splash_screen",
                "login_screen",
                "register_screen",
                "welcome_screen",
                "forgot_password_screen"
            ))
        ) {
            delay(100)
            navController.navigate("dashboard_screen") {
                popUpTo("splash_screen") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (isLoggedIn && currentRoute !in listOf(
                    "login_screen",
                    "register_screen",
                    "welcome_screen",
                    "forgot_password_screen",
                    "splash_screen"
                )
            ) {
                BottomNavigationBar(navController, currentRoute)
            }
        }
    ) { padding ->
        NavGraph(navController, Modifier.padding(padding))
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Exercise") },
            label = { Text("Exercise") },
            selected = currentRoute == "dashboard_screen",
            onClick = {
                if (currentRoute != "dashboard_screen") {
                    navController.navigate("dashboard_screen") {
                        popUpTo("dashboard_screen") { inclusive = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Search, contentDescription = "Appointment") },
            label = { Text("Appointment") },
            selected = currentRoute == "appointment_screen",
            onClick = {
                if (currentRoute != "appointment_screen") {
                    navController.navigate("appointment_screen") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentRoute == "profile_screen",
            onClick = {
                if (currentRoute != "profile_screen") {
                    navController.navigate("profile_screen") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )

    }
}
