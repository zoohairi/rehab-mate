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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.rehabmate.ui.theme.RehabMateTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
                MainScreen(navController)
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
fun MainScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding) // Ensures padding for content
        )
    }
}

//Bottom Navigation Bar
@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Exercise") },
            label = { Text("Exercise") },
            selected = false,
            onClick = { navController.navigate("exercise_screen") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Search, contentDescription = "Appointment") },
            label = { Text("Appointment") },
            selected = false,
            onClick = { navController.navigate("appointment_screen") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = false,
            onClick = { navController.navigate("profile_screen") }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Face, contentDescription = "Exercise") },
            label = { Text("Exercise API") },
            selected = false,
            onClick = { navController.navigate("exercise_screen_api") }
        )
    }
}