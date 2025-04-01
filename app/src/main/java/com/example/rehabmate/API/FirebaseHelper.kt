package com.example.rehabmate.firebase

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

// Firebase authentication (for login)
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


//getting user user_Info and retrieve a;; exercise details
suspend fun fetchExercisesForUser(
    uid: String, onSuccess: (List<Map<String, Any>>) -> Unit, onFailure: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val exerciseList = mutableListOf<Map<String, Any>>()
    var completedCount = 0
    var hasError = false

    // Fetch user info to get the list of exercise codes and their status's
    db.collection("user_info").document(uid).get().addOnSuccessListener { userDoc ->
        if (!userDoc.exists()) {
            onFailure("User not found")
            return@addOnSuccessListener
        }

        val userData = userDoc.data ?: emptyMap()
        val userName = userData["name"] as? String ?: "Unknown"

        val activityList = userData["Activity"] as? List<Map<String, Any>> ?: emptyList()

        //  the exercise code and its corresponding status
        val exerciseStatusMap = activityList.associate {
            val code = it["code"] as? String
            val status = it["status"] as? String ?: "Not Started"
            code to status
        }.filterKeys { it != null } as Map<String, String>

        Log.d("exerciseStatusMap", exerciseStatusMap.toString())

        // Extract exercise codes
        val exerciseCodes = exerciseStatusMap.keys.toList()
        Log.d("ActivityDebug", "Extracted Exercise Codes: $exerciseCodes")

        if (exerciseCodes.isEmpty()) {
            onSuccess(emptyList())
            return@addOnSuccessListener
        }

        // Retrieve exercise details
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
                        exerciseWithDoctors["userName"] = userName
                        exerciseWithDoctors["status"] =
                            exerciseStatusMap[code] ?: "Not Started"

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
fun fetchUserAppointments(
    uid: String,
    onSuccess: (List<Map<String, Any>>) -> Unit,
    onFailure: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("user_info").document(uid)

    userRef.get().addOnSuccessListener { userDoc ->
        if (userDoc.exists()) {
            val profile = userDoc.data?.get("profile") as? Map<*, *> ?: emptyMap<Any, Any>()
            Log.d("ProfileData", profile.toString())

            val rawAppointments = profile["appointment"] as? List<*> ?: emptyList<Any>()

            // Convert timestamps to formatted date strings
            val dateFormatter = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault())

            val appointments = rawAppointments.mapNotNull {
                when (it) {
                    is Map<*, *> -> {
                        val code = it["code"] as? String ?: "Unknown Code"
                        val timestamp = it["date_time"] as? Timestamp
                        val formattedDate =
                            timestamp?.let { dateFormatter.format(it.toDate()) } ?: "Unknown Date"

                        // Step 1: Retrieve exercise based on code
                        val exerciseRef = db.collection("Exercises").whereEqualTo("code", code)
                        exerciseRef.get().addOnSuccessListener { exerciseSnapshot ->
                            if (exerciseSnapshot.documents.isNotEmpty()) {
                                val exerciseDoc = exerciseSnapshot.documents[0]
                                val doctorInCharge =
                                    exerciseDoc.get("doctor_incharge") as? List<String>
                                        ?: emptyList()

                                // Step 2: Check doctor details for each doctor_incharge UID
                                doctorInCharge.forEach { doctorUid ->
                                    val doctorRef = db.collection("Doctors").document(doctorUid)
                                    doctorRef.get().addOnSuccessListener { doctorDoc ->
                                        if (doctorDoc.exists()) {
                                            val doctorData = doctorDoc.data
                                            // You can process doctorData here
                                            Log.d("DoctorData", doctorData.toString())
                                        } else {
                                            Log.d("DoctorData", "Doctor doesn't exist")
                                        }
                                    }.addOnFailureListener { exception ->
                                        Log.d(
                                            "DoctorData",
                                            "Error retrieving doctor data: ${exception.message}"
                                        )
                                    }
                                }
                            } else {
                                Log.d("ExerciseData", "Exercise with code $code doesn't exist.")
                            }
                        }.addOnFailureListener { exception ->
                            Log.d(
                                "ExerciseData",
                                "Error fetching exercise data: ${exception.message}"
                            )
                        }

                        // Return appointment data after retrieving doctors
                        mapOf(
                            "code" to code,
                            "date_time" to formattedDate
                        )
                    }

                    else -> null
                }
            }

            Log.d("Appointments", appointments.toString())
            onSuccess(appointments)
        } else {
            onFailure("No user found with the given UID.")
        }
    }.addOnFailureListener { exception ->
        onFailure("Error fetching appointments: ${exception.message}")
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

//=====SHARED PREFERENCES=====
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
