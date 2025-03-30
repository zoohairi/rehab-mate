package com.example.rehabmate.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.content.edit
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

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

// Function to fetch user info and activity from 'user_info'
fun fetchUserInfo(
    uid: String,
    onSuccess: (Map<String, Any>, List<String>) -> Unit,
    onFailure: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("user_info").document(uid).get()
        .addOnSuccessListener { userDoc ->
            if (userDoc.exists()) {
                val userData = userDoc.data ?: emptyMap()
                val activities = userData["Activity"] as? List<Map<String, Any>> ?: emptyList()

                // Extract 'code' from the activities
                val exerciseCodes = activities.mapNotNull { it["code"] as? String }

                onSuccess(userData, exerciseCodes)
            } else {
                onFailure("No user found with the given UID.")
            }
        }
        .addOnFailureListener { exception ->
            onFailure("Error fetching user info: ${exception.message}")
        }
}

// Fetch exercises based on the codes (UIDs from 'Activity')
fun fetchExercisesForUser(
    exerciseCodes: List<String>,
    onSuccess: (List<Map<String, Any>>) -> Unit,
    onFailure: (String) -> Unit
) {
    if (exerciseCodes.isEmpty()) {
        onSuccess(emptyList())
        return
    }

    val db = FirebaseFirestore.getInstance()
    val exerciseList = mutableListOf<Map<String, Any>>()
    var completedCount = 0
    var hasError = false

    // Fetch exercises using the list of 'code' (exercise uid)
    exerciseCodes.forEach { code ->
        db.collection("Exercises").document(code).get()
            .addOnSuccessListener { exerciseDoc ->
                if (hasError) return@addOnSuccessListener

                if (exerciseDoc.exists()) {
                    val exerciseData = exerciseDoc.data ?: emptyMap()
                    val doctorInCharge =
                        exerciseData["doctor_incharge"] as? List<String> ?: emptyList()

                    // For each doctor associated with the exercise, fetch their information
                    fetchDoctorsInfo(doctorInCharge) { doctorData ->
                        val exerciseWithDoctors = exerciseData.toMutableMap()
                        exerciseWithDoctors["doctors"] = doctorData
                        exerciseWithDoctors["id"] = code  // Add the exercise ID for reference
                        exerciseList.add(exerciseWithDoctors)

                        completedCount++
                        if (completedCount == exerciseCodes.size) {
                            onSuccess(exerciseList)
                        }
                    }
                } else {
                    completedCount++
                    if (completedCount == exerciseCodes.size) {
                        onSuccess(exerciseList)
                    }
                }
            }
            .addOnFailureListener { exception ->
                if (!hasError) {
                    hasError = true
                    onFailure("Error fetching exercise: ${exception.message}")
                }
            }
    }
}

// Fetch doctor info based on the doctor UID
fun fetchDoctorsInfo(doctorUids: List<String>, onSuccess: (List<Map<String, Any>>) -> Unit) {
    if (doctorUids.isEmpty()) {
        onSuccess(emptyList())
        return
    }

    val db = FirebaseFirestore.getInstance()
    val doctorList = mutableListOf<Map<String, Any>>()
    var completedCount = 0

    doctorUids.forEach { uid ->
        db.collection("Doctors").document(uid).get()
            .addOnSuccessListener { doctorDoc ->
                if (doctorDoc.exists()) {
                    val doctorData = doctorDoc.data?.toMutableMap() ?: mutableMapOf()
                    doctorData["id"] = uid  // Add the doctor ID for reference
                    doctorList.add(doctorData)
                }

                completedCount++
                if (completedCount == doctorUids.size) {
                    onSuccess(doctorList)
                }
            }
            .addOnFailureListener { exception ->
                // Even on failure, we need to count it as completed
                completedCount++
                if (completedCount == doctorUids.size) {
                    onSuccess(doctorList)
                }
            }
    }
}

// Function to fetch user and exercises data (combined)
fun fetchUserAndExercises(
    uid: String,
    onSuccess: (Map<String, Any>, List<Map<String, Any>>) -> Unit,
    onFailure: (String) -> Unit
) {
    fetchUserInfo(uid, { userData, exerciseCodes ->
        // After fetching user info and exercise codes, fetch exercises
        fetchExercisesForUser(exerciseCodes, { exercises ->
            // Return both the user data and exercises
            onSuccess(userData, exercises)
        }, { error ->
            onFailure(error)
        })
    }, { error ->
        onFailure(error)
    })
}

// Function to store UID in SharedPreferences
fun storeUidInSharedPreferences(uid: String, context: Context) {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit {
        putString("user_uid", uid)
    }
}

// Function to retrieve UID from SharedPreferences
fun getUidFromSharedPreferences(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("user_uid", null)
}

// New utility function to check if user is logged in
fun isUserLoggedIn(): Boolean {
    val currentUser = FirebaseAuth.getInstance().currentUser
    return currentUser != null
}

// New utility function to log out user
fun logoutUser(onComplete: () -> Unit) {
    FirebaseAuth.getInstance().signOut()
    onComplete()
}

// Update user info or create if it doesn't exist
fun updateUserProfile(
    uid: String,
    name: String,
    email: String,
    address: String,
    date: String,
    phoneNumber: Int,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("user_info").document(uid)

    // Create a nested map for 'profile'
    val updatedData = mapOf(
        "profile" to mapOf(
            "address" to address,
            "date" to date,
            "phone_number" to phoneNumber,
        )
    )

    // Using set with merge option to create or update data
    userRef.set(updatedData, SetOptions.merge())
        .addOnSuccessListener {
            Log.d("Firestore Update", "User profile updated successfully")
            onSuccess()
        }
        .addOnFailureListener { exception ->
            val errorMsg = "Failed to update user profile: ${exception.message}"
            Log.e("Firestore Update", errorMsg)
            onFailure(errorMsg)
        }
}
