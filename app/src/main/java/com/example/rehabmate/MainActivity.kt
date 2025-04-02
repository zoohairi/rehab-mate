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
import androidx.navigation.NavController
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
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitInstance.initialize(this)

        // Initialize Firebase only if not already initialized
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
            Log.d("Firebase", "Firebase initialized")
        } else {
            Log.d("Firebase", "Firebase is already initialized")
        }

        // Initialize Firebase services
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Check Firestore connection
        checkFirebaseConnection()

        // Check Authentication status
        checkAuthStatus()

        setContent {
            RehabMateTheme {
                val navController = rememberNavController()
                MainScreen(navController, auth)
            }
        }
    }

    private fun checkFirebaseConnection() {
        try {
            firestore.collection("test").get()
                .addOnSuccessListener {
                    Log.d("FirebaseConnection", "Success: Firestore is connected!")
                }
                .addOnFailureListener { exception ->
                    Log.e("FirebaseConnection", "Failed: ${exception.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseConnection", "Error occurred: ${e.message}")
        }
    }

    private fun checkAuthStatus() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("FirebaseAuth", "User is logged in: ${currentUser.uid}")
        } else {
            Log.d("FirebaseAuth", "No user is logged in")
        }
    }
}

// MainScreen encapsulates Scaffold with Bottom Navigation
@Composable
fun MainScreen(navController: NavHostController, auth: FirebaseAuth) {
    // Get the current route to determine if we're on an authentication screen
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Set up authentication state listener
    var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }

    // Update login state when auth changes
    DisposableEffect(Unit) {
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            isLoggedIn = firebaseAuth.currentUser != null
        }

        auth.addAuthStateListener(authListener)

        onDispose {
            auth.removeAuthStateListener(authListener)
        }
    }

    // Auto-login: If user is already logged in, navigate to dashboard
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && (currentRoute == "welcome_screen" || currentRoute == "login_screen" || currentRoute == null)) {
            // Small delay to ensure the nav controller is ready
            delay(100)
            navController.navigate("dashboard_screen") {
                popUpTo("welcome_screen") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // List of routes that don't need the bottom navigation (authentication screens)
    val authScreens = listOf(
        "welcome_screen",
        "login_screen",
        "register_screen",
        "forgot_password_screen",
        "personalised_screen/{username}"
    )

    // Check if current route is an auth screen or if user is not logged in
    val shouldShowBottomBar = isLoggedIn && currentRoute != null && !authScreens.any {
            route ->
        if (route.contains("{")) {
            // Handle route with parameter
            val routeBase = route.substringBefore("{")
            currentRoute.startsWith(routeBase)
        } else {
            route == currentRoute
        }
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(navController, currentRoute)
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding) // Ensures padding for content
        )
    }
}

// Bottom Navigation Bar
@Composable
fun BottomNavigationBar(navController: NavController, currentRoute: String?) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Exercise") },
            label = { Text("Exercise") },
            selected = currentRoute == "dashboard_screen",
            onClick = {
                if (currentRoute != "dashboard_screen") {
                    navController.navigate("dashboard_screen") {
                        // Pop up to the start destination to avoid building up a large stack
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when navigating back
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

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Face, contentDescription = "Exercise API") },
            label = { Text("Exercise API") },
            selected = currentRoute == "exercise_screen_api",
            onClick = {
                if (currentRoute != "exercise_screen_api") {
                    navController.navigate("exercise_screen_api") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )
    }
}