package com.example.rehabmate

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import java.lang.Exception

class MainActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        checkFirebaseConnection()

        setContent {
            val navController = rememberNavController()
            NavGraph(navController)
        }
    }

    private fun checkFirebaseConnection() {
        firestore.collection("test").get()
            .addOnSuccessListener {
                Log.d("FirebaseConnection", "Success: Firestore are connected!")
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseConnection", "Failed: ${exception.message}")
            }
    }

}
