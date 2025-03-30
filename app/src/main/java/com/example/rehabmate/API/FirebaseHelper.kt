package com.example.rehabmate.firebase

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.content.edit
import com.google.firebase.firestore.QuerySnapshot

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
    val db = FirebaseFirestore.getInstance()

    val exerciseList = mutableListOf<Map<String, Any>>()

    // Fetch exercises using the list of 'code' (exercise uid)
    exerciseCodes.forEach { code ->
        db.collection("Exercises").document(code).get()
            .addOnSuccessListener { exerciseDoc ->
                if (exerciseDoc.exists()) {
                    val exerciseData = exerciseDoc.data ?: emptyMap()
                    val doctorInCharge =
                        exerciseData["doctor_incharge"] as? List<String> ?: emptyList()

                    // For each doctor associated with the exercise, fetch their information
                    fetchDoctorsInfo(doctorInCharge) { doctorData ->
                        val exerciseWithDoctors = exerciseData.toMutableMap()
                        exerciseWithDoctors["doctors"] = doctorData
                        exerciseList.add(exerciseWithDoctors)
                    }
                }
            }
            .addOnFailureListener { exception ->
                onFailure("Error fetching exercise: ${exception.message}")
            }
    }

    // If all exercises have been fetched, return the list
    onSuccess(exerciseList)
}

// Fetch doctor info based on the doctor UID
fun fetchDoctorsInfo(doctorUids: List<String>, onSuccess: (List<Map<String, Any>>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    val doctorList = mutableListOf<Map<String, Any>>()

    doctorUids.forEach { uid ->
        db.collection("Doctors").document(uid).get()
            .addOnSuccessListener { doctorDoc ->
                if (doctorDoc.exists()) {
                    doctorList.add(doctorDoc.data ?: emptyMap())
                }
            }
    }

    // After fetching all doctor data, pass it to the callback
    onSuccess(doctorList)
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
    sharedPreferences.edit() {
        putString("user_uid", uid)
    }
}

// Function to retrieve UID from SharedPreferences
fun getUidFromSharedPreferences(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("user_uid", null)
}
