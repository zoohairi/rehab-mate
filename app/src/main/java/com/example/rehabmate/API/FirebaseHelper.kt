// FirebaseHelper.kt
package com.example.rehabmate.firebase

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.content.edit

// Firebase authentication
fun loginUser(
    email: String,
    password: String,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    onSuccess(uid)
                } else {
                    onFailure("Authentication failed: No UID found")
                }
            } else {
                onFailure("Authentication failed: ${task.exception?.message}")
            }
        }
}

// Fetch exercises from Firestore
fun fetchExercisesFromFirestore(
    onSuccess: (List<Map<String, Any>>) -> Unit,
    onFailure: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("Exercises")
        .get()
        .addOnSuccessListener { result ->
            val exerciseList = mutableListOf<Map<String, Any>>()
            for (document in result) {
                exerciseList.add(document.data)
            }
            onSuccess(exerciseList)
        }
        .addOnFailureListener { exception ->
            onFailure("Error fetching exercises: ${exception.message}")
        }
}

// Fetch user information from either 'Doctors' or 'user_info' collections based on UID
fun fetchUserInfoByUid(
    uid: String,
    onSuccess: (Map<String, Any>) -> Unit,
    onFailure: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    // First check 'Doctors' collection
    db.collection("Doctors").document(uid)
        .get()
        .addOnSuccessListener { doctorDoc ->
            if (doctorDoc.exists()) {
                // Found in 'Doctors' collection
                onSuccess(doctorDoc.data ?: emptyMap())
            } else {
                // If not found in 'Doctors', check 'user_info' collection
                db.collection("user_info").document(uid)
                    .get()
                    .addOnSuccessListener { userDoc ->
                        if (userDoc.exists()) {
                            // Found in 'user_info' collection
                            onSuccess(userDoc.data ?: emptyMap())
                        } else {
                            // If not found in either collection
                            onFailure("No user found with the given UID.")
                        }
                    }
                    .addOnFailureListener { exception ->
                        onFailure("Error fetching user from 'user_info': ${exception.message}")
                    }
            }
        }
        .addOnFailureListener { exception ->
            onFailure("Error fetching user from 'Doctors': ${exception.message}")
        }
}


//============ shared Preference ============

// Function to store UID in SharedPreferences
fun storeUidInSharedPreferences(uid: String, context: Context) {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit() {
        putString("user_uid", uid)
    }
}

// Function to retrieve UID from SharedPreferences
fun getUidFromSharedPreferences(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("user_uid", null)
}
