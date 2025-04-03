package com.example.rehabmate.firebase


import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.example.rehabmate.AppointmentData
import com.example.rehabmate.DoctorInfo
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.TimeZone
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat


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


//getting user user_Info and retrieve all exercise details
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
                        exerciseWithDoctors["status"] = exerciseStatusMap[code] ?: "Not Started"

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

//retrieve sub exercsies base of the exercise Code
fun getExerciseInfo(
    exerciseCode: String,
    onResult: (Map<String, Any>?, List<Map<String, Any>>?) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    Log.d("FirestoreDebug", "Fetching data for Exercise Code: $exerciseCode")

    // Reference to the main exercise document
    val exerciseRef = db.collection("Exercises").document(exerciseCode)

    exerciseRef.get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val exerciseData = document.data
                Log.d("FirestoreDebug", "Main Exercise Data: $exerciseData")

                // Fetch sub-exercises from sub_exercises collection
                exerciseRef.collection("sub_exercises").get()
                    .addOnSuccessListener { subExercisesSnapshot ->
                        val subExercises =
                            subExercisesSnapshot.documents.mapNotNull { it.data }

                        Log.d("FirestoreDebug", "Fetched ${subExercises.size} Sub Exercises")
                        subExercises.forEachIndexed { index, subExercise ->
                            Log.d("FirestoreDebug", "Sub Exercise $index: $subExercise")
                        }

                        // Return both main exercise info and sub-exercises
                        onResult(exerciseData, subExercises)
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreError", "Error fetching sub-exercises", e)
                        onResult(exerciseData, null)
                    }
            } else {
                Log.w("FirestoreWarning", "No exercise found for code: $exerciseCode")
                onResult(null, null)
            }
        }
        .addOnFailureListener { e ->
            Log.e("FirestoreError", "Error fetching exercise info", e)
            onResult(null, null)
        }
}


// Update user info or create if it doesn't exist
fun updateUserProfile(
    uid: String,
    name: String,
    email: String,
    address: String,
    date: String,
    phoneNumber: String, // Change to String
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
            "phone_number" to phoneNumber,  // Ensure phone_number is a String
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


// get user's appointment details (format timestamp and ensure it is current time zone (as Firebase stores in UTC+8))
fun fetchUserAppointmentsAndUpdateState(
    uid: String,
    onUpdate: (List<AppointmentData>) -> Unit,
    onFailure: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("user_info").document(uid)

    userRef.get().addOnSuccessListener { userDoc ->
        if (!userDoc.exists()) {
            onFailure("No user found with the given UID.")
            return@addOnSuccessListener
        }

        val profile = userDoc.data?.get("profile") as? Map<*, *> ?: emptyMap<Any, Any>()
        val rawAppointments = profile["appointment"] as? List<*> ?: emptyList<Any>()

        if (rawAppointments.isEmpty()) {
            onUpdate(emptyList()) // No appointments found
            return@addOnSuccessListener
        }

        val localFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm a", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault() // Convert to device timezone
        }

        val appointmentsList = mutableListOf<AppointmentData>()
        var completedAppointments = 0
        val totalAppointments = rawAppointments.size

        rawAppointments.forEach { appointment ->
            (appointment as? Map<*, *>)?.let { appointmentMap ->
                val code = appointmentMap["code"] as? String ?: "Unknown Code"
                val timestamp = appointmentMap["date_time"] as? Timestamp
                val formattedDateTime =
                    timestamp?.let { localFormatter.format(it.toDate()) } ?: "Unknown Date/Time"

                val dateTimeParts = formattedDateTime.split(" ")
                val date = dateTimeParts.getOrNull(0) ?: "Unknown Date"
                val time =
                    if (dateTimeParts.size >= 3) "${dateTimeParts[1]} ${dateTimeParts[2]}" else "Unknown Time"

                // Fetch exercise details using the code
                db.collection("Exercises").document(code).get()
                    .addOnSuccessListener { exerciseDoc ->
                        Log.d("FirestoreDebug123", "Fetched Exercise Document: ${exerciseDoc.data}")

                        // Fetch the doctor_incharge field
                        val doctorInCharge =
                            exerciseDoc.get("doctor_incharge") as? List<String> ?: emptyList()

                        if (doctorInCharge.isEmpty()) {
                            // No doctors found, add the appointment immediately
                            appointmentsList.add(
                                AppointmentData(
                                    id = code,
                                    doctors = listOf(
                                        DoctorInfo(
                                            "Unknown Doctor",
                                            "Unknown Specialty",
                                            "Unknown Location", // Default to Unknown Location
                                            null // No GeoPoint
                                        )
                                    ),
                                    date = date,
                                    time = time,
                                    location = "Unknown Location"
                                )
                            )
                            completedAppointments++
                            if (completedAppointments == totalAppointments) onUpdate(
                                appointmentsList
                            )
                        } else {
                            // Fetch doctor details asynchronously
                            val doctorDetailsList = mutableListOf<DoctorInfo>()
                            var completedDoctors = 0

                            doctorInCharge.forEach { doctorId ->
                                db.collection("Doctors").document(doctorId).get()
                                    .addOnSuccessListener { doctorDoc ->
                                        Log.d(
                                            "FirestoreDebug123",
                                            "Fetched Doctor Document: ${doctorDoc.data}"
                                        )

                                        val doctorName =
                                            doctorDoc.getString("name") ?: "Unknown Name"
                                        val doctorSpecialty =
                                            doctorDoc.get("specialty") as? List<String>
                                                ?: emptyList()

                                        val geoPoint =
                                            doctorDoc.get("geopoint") as? com.google.firebase.firestore.GeoPoint
                                        val doctorLocation = if (geoPoint != null) {
                                            // Convert Firebase GeoPoint to OSMDroid GeoPoint
                                            val osmdroidGeoPoint = org.osmdroid.util.GeoPoint(
                                                geoPoint.latitude,
                                                geoPoint.longitude
                                            )
                                            "Lat: ${osmdroidGeoPoint.latitude}, Lng: ${osmdroidGeoPoint.longitude}"
                                        } else {
                                            "Unknown Location" // Fallback if 'geopoint' is not available or null
                                        }

                                        doctorDetailsList.add(
                                            DoctorInfo(
                                                doctorName,
                                                doctorSpecialty.joinToString(", "),
                                                doctorLocation,
                                                if (geoPoint != null) org.osmdroid.util.GeoPoint(
                                                    geoPoint.latitude,
                                                    geoPoint.longitude
                                                ) else null // Convert Firebase GeoPoint to OSMDroid GeoPoint
                                            )
                                        )

                                        completedDoctors++

                                        if (completedDoctors == doctorInCharge.size) {
                                            // Add the appointment with the doctor details once all doctors are fetched
                                            appointmentsList.add(
                                                AppointmentData(
                                                    id = code,
                                                    doctors = doctorDetailsList,
                                                    date = date,
                                                    time = time,
                                                    location = doctorDetailsList.joinToString(", ") { it.location }
                                                )
                                            )
                                            completedAppointments++
                                            if (completedAppointments == totalAppointments) onUpdate(
                                                appointmentsList
                                            )
                                        }
                                    }
                                    .addOnFailureListener {
                                        completedDoctors++
                                        if (completedDoctors == doctorInCharge.size) {
                                            completedAppointments++
                                            if (completedAppointments == totalAppointments) onUpdate(
                                                appointmentsList
                                            )
                                        }
                                    }
                            }
                        }
                    }
                    .addOnFailureListener {
                        completedAppointments++
                        if (completedAppointments == totalAppointments) onUpdate(appointmentsList)
                    }

            }
        }
    }.addOnFailureListener { exception ->
        onFailure("Error fetching appointments: ${exception.message}")
    }
}


//// Function to fetch doctor details
//fun fetchDoctorsDetails(
//    db: FirebaseFirestore,
//    doctorIds: List<String>,
//    onSuccess: (List<DoctorInfo>) -> Unit
//) {
//    val doctorDetails = mutableListOf<DoctorInfo>()
//    var completedDoctors = 0
//    val totalDoctors = doctorIds.size
//
//    doctorIds.forEach { doctorId ->
//        db.collection("Doctors").document(doctorId).get()
//            .addOnSuccessListener { doctorDoc ->
//                if (doctorDoc.exists()) {
//                    val doctorName = doctorDoc.getString("name") ?: "Unknown Doctor"
//                    val doctorLocation = doctorDoc.getString("location") ?: "Unknown Location"
//                    val doctoralLatitude =
//                        doctorDoc.getDouble("latitude")?.toString() ?: "Unknown Latitude"
//                    val doctorLongitude =
//                        doctorDoc.getDouble("longitude")?.toString() ?: "Unknown Longitude"
//
//                    val specialtyList = doctorDoc.get("specialty") as? List<*> ?: emptyList<Any>()
//                    val specialty = specialtyList.joinToString(", ")
//
//                    DoctorInfo(doctorName, specialty, doctorLocation)
//                }
//                completedDoctors++
//                if (completedDoctors == totalDoctors) {
//                    onSuccess(doctorDetails)
//                }
//            }
//            .addOnFailureListener {
//                completedDoctors++
//                if (completedDoctors == totalDoctors) {
//                    onSuccess(doctorDetails)
//                }
//            }
//    }
//}
//

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
