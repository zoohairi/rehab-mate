package com.example.rehabmate.firebase

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

// Firebase authentication
fun loginUser(
    email: String, password: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
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

// Function to fetch user's data
suspend fun fetchUserInfo(uid: String): Result<Map<String, Any>> {
    return try {
        val db = FirebaseFirestore.getInstance()
        val userSnapshot = db.collection("user_info").document(uid).get().await()

        if (userSnapshot.exists()) {
            val userData = userSnapshot.data ?: emptyMap()
            Result.success(userData)
        } else {
            Result.failure(Exception("No user found with the given UID."))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}


// Fetch exercises based on the codes (UIDs from 'Activity')
suspend fun fetchExercisesForUser(
    uid: String, onSuccess: (List<Map<String, Any>>) -> Unit, onFailure: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    // Now, fetch exercise details using these codes

    val exerciseList = mutableListOf<Map<String, Any>>()
    var completedCount = 0
    var hasError = false
    // First, fetch user info to get the list of exercise codes
    db.collection("user_info").document(uid).get().addOnSuccessListener { userDoc ->
        if (!userDoc.exists()) {
            onFailure("User not found")
            return@addOnSuccessListener
        }

        val userData = userDoc.data ?: emptyMap()
        val userName = userData["name"] as? String ?: "Unknown" // Extract user name
        val activityList = userData["Activity"] as? List<Map<String, Any>> ?: emptyList()
        val exerciseCodes = activityList.mapNotNull { it["code"] as? String }

        Log.d("ActivityDebug", "Extracted Exercise Codes: $exerciseCodes")

        if (exerciseCodes.isEmpty()) {
            onSuccess(emptyList())
            return@addOnSuccessListener
        }

        exerciseCodes.forEach { code ->
            db.collection("Exercises").document(code).get().addOnSuccessListener { exerciseDoc ->
                if (hasError) return@addOnSuccessListener

                if (exerciseDoc.exists()) {
                    val exerciseData = exerciseDoc.data ?: emptyMap()
                    val doctorInCharge =
                        exerciseData["doctor_incharge"] as? List<String> ?: emptyList()

                    fetchDoctorsInfo(doctorInCharge) { doctorData ->
                        val exerciseWithDoctors = exerciseData.toMutableMap()
                        exerciseWithDoctors["doctors"] = doctorData
                        exerciseWithDoctors["id"] = code
                        exerciseWithDoctors["userName"] = userName // Add user name

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
            }.addOnFailureListener { exception ->
                if (!hasError) {
                    hasError = true
                    onFailure("Error fetching exercise: ${exception.message}")
                }
            }
        }
    }.addOnFailureListener { exception ->
        onFailure("Error fetching user info: ${exception.message}")
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
        db.collection("Doctors").document(uid).get().addOnSuccessListener { doctorDoc ->
            if (doctorDoc.exists()) {
                val doctorData = doctorDoc.data?.toMutableMap() ?: mutableMapOf()
                doctorData["id"] = uid  // Add the doctor ID for reference
                doctorList.add(doctorData)
            }

            completedCount++
            if (completedCount == doctorUids.size) {
                onSuccess(doctorList)
            }
        }.addOnFailureListener { exception ->
            // Even on failure, we need to count it as completed
            completedCount++
            if (completedCount == doctorUids.size) {
                onSuccess(doctorList)
            }
        }
    }
}


//// Function to fetch user and exercises data (combined)
//fun fetchUserAndExercises(
//    uid: String,
//    onSuccess: (Map<String, Any>, List<Map<String, Any>>) -> Unit,
//    onFailure: (String) -> Unit
//) {
//    fetchUserInfo(uid, { userData, exerciseCodes ->
//        // After fetching user info and exercise codes, fetch exercises
//        fetchExercisesForUser(exerciseCodes, { exercises ->
//            // Return both the user data and exercises
//            onSuccess(userData, exercises)
//        }, { error ->
//            onFailure(error)
//        })
//    }, { error ->
//        onFailure(error)
//    })
//}

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
    userRef.set(updatedData, SetOptions.merge()).addOnSuccessListener {
        Log.d("Firestore Update", "User profile updated successfully")
        onSuccess()
    }.addOnFailureListener { exception ->
        val errorMsg = "Failed to update user profile: ${exception.message}"
        Log.e("Firestore Update", errorMsg)
        onFailure(errorMsg)
    }
}


// Function to register a new user
fun registerUser(
    email: String,
    password: String,
    name: String,
    birthDate: String,
    phoneNumber: String,
    address: String,
    context: Context,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Create a new user with email and password
    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            // Get the UID of the new user
            val uid = auth.currentUser?.uid

            if (uid != null) {
                // Create a map of user data to store in Firestore
                val userData = mapOf(
                    "name" to name, "email" to email, "profile" to mapOf(
                        "address" to address,
                        "date" to birthDate,
                        "phone_number" to phoneNumber,
                    )
                )


                // Store the user info in Firestore under "user_info"
                db.collection("user_info").document(uid).set(userData).addOnSuccessListener {
                    // Store UID in SharedPreferences
                    storeUidInSharedPreferences(uid, context)

                    // Return success callback
                    onSuccess(uid)
                }.addOnFailureListener { exception ->
                    val errorMessage = "Failed to store user info: ${exception.message}"
                    onFailure(errorMessage)
                }
            } else {
                onFailure("Authentication failed: No UID found")
            }
        } else {
            onFailure("Registration failed: ${task.exception?.message}")
        }
    }
}

//retrieve user appointments
fun fetchUserAppointments(
    uid: String, onSuccess: (List<Map<String, Any>>) -> Unit, onFailure: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("user_info").document(uid)

    userRef.get().addOnSuccessListener { userDoc ->
        if (userDoc.exists()) {
            val profile = userDoc.get("profile") as? Map<String, Any>
            val appointments = profile?.get("appointment") as? List<Map<String, Any>> ?: emptyList()
            onSuccess(appointments)
        } else {
            onFailure("No user found with the given UID.")
        }
    }.addOnFailureListener { exception ->
        onFailure("Error fetching appointments: ${exception.message}")
    }
}


// Function to store UID in SharedPreferences
fun storeUidInSharedPreferences(uid: String, context: Context) {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit {
        putString("user_uid", uid)
    }
}

