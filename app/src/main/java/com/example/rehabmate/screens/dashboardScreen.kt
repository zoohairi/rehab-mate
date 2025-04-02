//change screen file name from 'exerciseScreen' to 'dashboard' screen
package com.example.rehabmate.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.rehabmate.firebase.fetchExercisesForUser
import com.example.rehabmate.firebase.fetchUserInfo
import com.example.rehabmate.firebase.getUidFromSharedPreferences
import com.example.rehabmate.ui.theme.blue_color
import com.example.rehabmate.ui.theme.red_color
import com.example.rehabmate.ui.theme.white_color
import com.google.firebase.firestore.FirebaseFirestore
import ai.onnxruntime.*
import android.content.Context
import com.google.firebase.Timestamp
import java.nio.FloatBuffer

@Composable
fun DashboardScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Exercise Info", "Exercise List", "Exercise Demo")

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar with navigation tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
            edgePadding = 8.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(text = title) })
            }
        }

        // Content based on selected tab
        when (selectedTab) {
            0 -> HomeTab(navController)
//            1 -> ExerciseInfoTab(navController) // the exercise use selected from dashboard will lead to a page to show the exercise information
            2 -> ExerciseListTab(navController) // not sure about this page yet
//            3 -> ExerciseDemoTab(navController) // should like this page to ExerciseInfoTab (the exercise it self, should have the timer + TTS in this screen)
        }
    }
}

@Composable
fun HomeTab(navController: NavHostController) {
    val context = LocalContext.current

    // Retrieve UID from SharedPreferences
    val uid = getUidFromSharedPreferences(context)

    // States to store the name of the user and exercise list
    val userName = remember { mutableStateOf<String?>(null) }
    val exerciseList = remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) } // To show error message

    // Fetch user info (name) and exercises from Firestore
    LaunchedEffect(uid) {
        if (uid != null) {
            // Fetch user info first
            val result = fetchUserInfo(uid)  // Fetch user info

            result.onSuccess { userData ->   // Handle success
                Log.d("UserInfo", userData.toString())

                // Extract user name from the userData map
                userName.value = when {
                    userData.containsKey("name") -> userData["name"] as? String
                    userData.containsKey("profile") -> {
                        val profile = userData["profile"] as? Map<String, Any>
                        profile?.get("name") as? String
                    }

                    else -> "User"
                }

                // Fetch user exercise from fb
                fetchExercisesForUser(uid, onSuccess = { exercises ->
                    Log.d("ExerciseList", exercises.toString())
                    exerciseList.value = exercises
                    isLoading.value = false
                }, onFailure = { error ->
                    errorMessage.value = error
                    isLoading.value = false
                })
            }.onFailure { error ->
                // Handle error
                Log.e("HomeTab", "Error fetching data: ${error.message}")
                userName.value = "User"
                exerciseList.value = emptyList() // Empty list in case of error
                isLoading.value = false
            }
        } else {
            isLoading.value = false
        }
    }

    Box(modifier = Modifier.background(color = Color.Black)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                // Greeting section
                if (isLoading.value) {
                    // Show a loading indicator while fetching data
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    userName.value?.let { name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Hi $name,",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Ready to improve today",
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }

                            // Icons for notifications, search, and account
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.width(100.dp)
                            ) {
                                //User's profile
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    tint = Color.White,
                                    contentDescription = "Account",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            navController.navigate("profile_screen")
                                        }
                                )

                                // Add Referral Code
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    tint = Color.White,
                                    contentDescription = "Add referral Code",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            navController.navigate("referral_screen")
                                        }
                                )

                                // Sign Out
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Sign out",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { /* Handle sign out click */ }
                                )
                            }
                        }
                    }

                    Column {
                        ProgressByAI(context)
                    }

                    // Feature cards section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FeatureCard(
                            title = "View Appointments",
                            onClick = { navController.navigate("appointment_screen") })

                        FeatureCard(
                            title = "Progress Tracking",
                            onClick = { /* Navigate to progress tracking */ })

                        FeatureCard(
                            title = "Medical Records",
                            onClick = { navController.navigate("medical_records_screen") })
                    }

                    // Error handling
                    errorMessage.value?.let { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Graph Visualization section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(color = blue_color)
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF1E1E1E))
                                .padding(16.dp), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Graph Visualization",
                                fontSize = 18.sp,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Exercises section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Rehab Exercises",
                            modifier = Modifier.padding(top = 10.dp),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = white_color
                        )
                        if (exerciseList.value.isNotEmpty()) {
                            Row {
                                Text(
                                    text = "See All",
                                    fontSize = 14.sp,
                                    color = white_color,
                                    modifier = Modifier.clickable { /* Handle see all click */ })
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "View All",
                                    tint = white_color,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { /* Handle back navigation */ })
                            }
                        }
                    }
                    //user's exercise list below
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        if (exerciseList.value.isEmpty()) {
                            item {
                                Text("No exercises available.", color = Color.White)
                            }
                        } else {
                            items(exerciseList.value.size) { index ->
                                val exercise = exerciseList.value[index]

                                // debug: entire exercise data for inspection
                                Log.d("ExerciseDebug", exerciseList.toString())

                                // Extracting exercise data
                                val exerciseStatus = exercise["status"] as? String ?: "No Status"
                                val exerciseTitle =
                                    exercise["exercise_title"] as? String ?: "No Title"
                                val subexerciseList =
                                    exercise["subexercise"] as? List<String> ?: emptyList()
//                                val subexerciseIds =
//                                    subexerciseList.joinToString(", ")
//                                val description =
//                                    exercise["description"] as? String ?: "No Description"
//                                val remark = exercise["remark"] as? String ?: description


                                ExerciseItem(
                                    title = exerciseTitle, subtitle = exerciseStatus, onClick = {
                                        val exerciseId = exercise["id"] as? String
                                        Log.d("ExerciseDebug", "Exercise clicked: $exerciseId")
//im here
                                        // Navigate to exercise detail with the exercise ID
                                        if (exerciseId != null) {
                                            navController.navigate("exercise_info_tab/$exerciseId")
                                        }
                                    })
                            }

                        }
                    }

                    // retrieve & display all the Exercise data also => (display view all if theres progression exercise)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Continue Exercise",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = white_color
                        )
                        if (exerciseList.value.isEmpty()) {
                            Row {
                                Text(
                                    text = "See All",
                                    fontSize = 14.sp,
                                    color = white_color,
                                    modifier = Modifier.clickable { /* Handle see all click */ })
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "View All",
                                    tint = white_color,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { /* Handle back navigation */ })
                            }
                        }
                    }

                    // Continue buttons (Resume exercise)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Check if there are exercises to show for "Continue Exercise"
                        if (exerciseList.value.isNotEmpty()) {
                            // Display ContinueButton for each exercise (limit to 2)
                            exerciseList.value.take(2).forEach { exercise ->
                                val status = exercise["status"] as? String ?: "Not Started"

                                if (status == "progress") {
                                    Log.d("exerciseData123", exercise.toString())

                                    // Extracting exercise data
                                    val exerciseTitle =
                                        exercise["exercise_title"] as? String ?: "No Title"

                                    ContinueButton(
                                        title = exerciseTitle,
                                        subtitle = "description here",
                                        onClick = {
                                            // Handle continue button click, e.g., navigate to exercise detail
                                            val exerciseId = exercise["id"] as? String
                                            Log.d(
                                                "ContinueButtonDebug",
                                                "Continue button clicked for $exerciseId"
                                            )

                                            if (exerciseId != null) {
                                                navController.navigate("exercise_demo_tab")
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Show message if no "progress" exercises exist
                            if (exerciseList.value.none { (it["status"] as? String) == "progress" }) {
                                Text(
                                    text = "No exercises to continue",
                                    fontSize = 16.sp,
                                    color = red_color
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Placeholder for exercise image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E))
                    .padding(8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = subtitle.uppercase(), // make text uppercase
                    fontSize = 12.sp,
                    style = getStatusTextStyle(subtitle) // Apply status style
                )
            }
        }
    }
}


@Composable
fun ExerciseDemoTab(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "[EXERCISE TOPIC NAME/NUMBER]",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Exercise demo content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Exercise Demo Screen",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "The Screen Must Have A Timer And Implement Of Voice Over",
                    textAlign = TextAlign.Center,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "(Let Me Know If You Want Do The Voice Over Thingy)",
                    textAlign = TextAlign.Center,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun FeatureCard(
    title: String, onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(110.dp)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF303030))
                .padding(12.dp), contentAlignment = Alignment.Center
        ) {
            // Use a simple icon placeholder instead of specific icons
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = title,
                tint = Color.Yellow
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title, fontSize = 12.sp, textAlign = TextAlign.Center, color = Color.Yellow
        )
    }
}


@Composable
fun ContinueButton(
    title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(65.dp)
            .width(60.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Red), contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.SpaceEvenly
                // Reduced padding here
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1, // Limit to one line
                    overflow = TextOverflow.Ellipsis // Hide overflow with ellipsis
                )


                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 2.dp),
                    color = Color.Gray,
                    maxLines = 1, // Limit to one line
                    overflow = TextOverflow.Ellipsis // Hide overflow with ellipsis
                )
            }
        }
    }
}

@Composable
fun ExerciseInfoTab(navController: NavHostController, exerciseId: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        navController.navigate("home_tab")
                    }
            ) // Go back instead of navigating
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Exercise Details: ${exerciseId ?: "Unknown"}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Details for Exercise ID: ${exerciseId ?: "N/A"}",
                textAlign = TextAlign.Center,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}


@Composable
fun ExerciseListTab(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val exercises = remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }

    // Fetch exercises from Firebase
    LaunchedEffect(true) {
        db.collection("Exercises").get().addOnSuccessListener { result ->
            val exerciseList = mutableListOf<Map<String, Any>>()
            val doctorNameMap =
                mutableMapOf<String, String>() // Map to store doctor UID and name

            for (document in result) {
                val exercise = document.data
                val doctorInCharge = exercise["doctor_incharge"] as? List<String> ?: emptyList()

                // Fetch doctor's name if UID exists in the 'Doctors' collection
                val doctorNames = mutableListOf<String>()
                doctorInCharge.forEach { doctorUid ->
                    // Check if doctor name is already fetched
                    if (doctorNameMap.containsKey(doctorUid)) {
                        doctorNames.add(doctorNameMap[doctorUid] ?: "Unknown Doctor")
                    } else {
                        db.collection("Doctors").document(doctorUid).get()
                            .addOnSuccessListener { doctorDoc ->
                                val doctorName = doctorDoc.getString("name") ?: "Unknown Doctor"
                                doctorNameMap[doctorUid] = doctorName
                                doctorNames.add(doctorName)
                            }.addOnFailureListener { exception ->
                                Log.e(
                                    "FirebaseError",
                                    "Error fetching doctor name: ${exception.message}"
                                )
                            }
                    }
                }

                // Add the exercise details along with doctor names
                val exerciseWithDoctors = exercise.toMutableMap()
                exerciseWithDoctors["doctor_names"] = doctorNames
                exerciseList.add(exerciseWithDoctors)
            }

            exercises.value = exerciseList
            isLoading.value = false
        }.addOnFailureListener { exception ->
            Log.e("FirebaseError", "Error fetching exercises: ${exception.message}")
            isLoading.value = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { /* Handle back navigation */ })
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Exercises", fontSize = 18.sp, fontWeight = FontWeight.Bold
            )
        }

        // Show loading or exercise list
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading.value) {
                CircularProgressIndicator() // Show loading indicator while fetching data
            } else {
                LazyColumn(
                    modifier = Modifier.padding(16.dp)
                ) {
                    items(exercises.value.size) { index ->
                        val exercise = exercises.value[index]
                        ExerciseItem(exercise)
                    }
                }
            }
        }
    }
}


@Composable
fun ExerciseItem(exercise: Map<String, Any>) {
    // Safely extract values from the exercise map
    val exerciseTitle = exercise["exercise_title"] as? String ?: "No Title"
    val description = exercise["description"] as? String ?: "No description available."
    val doctorInCharge = exercise["doctor_incharge"] as? List<String> ?: emptyList()
    val duration = exercise["duration"] as? String ?: "No duration"
    val remark = exercise["remark"] as? String ?: "No remarks"
    val keywords = exercise["keyword_api"] as? Map<String, Any> ?: emptyMap()
    val muscles = keywords["Muscle"] as? List<String> ?: emptyList()
    val types = keywords["Type"] as? List<String> ?: emptyList()

    // State to hold the doctor's names
    val doctorNames = remember { mutableStateOf<List<String>>(emptyList()) }

    // Fetch the doctor's names asynchronously
    LaunchedEffect(doctorInCharge) {
        val names = mutableListOf<String>()
        val db = FirebaseFirestore.getInstance()
        doctorInCharge.forEach { doctorUid ->
            db.collection("Doctors").document(doctorUid).get()
                .addOnSuccessListener { doctorDoc ->
                    val doctorName = doctorDoc.getString("name") ?: "Unknown Doctor"
                    names.add(doctorName)
                    if (names.size == doctorInCharge.size) {
                        doctorNames.value = names
                    }
                }.addOnFailureListener { exception ->
                    Log.e("FirebaseError", "Error fetching doctor name: ${exception.message}")
                }
        }
    }

    // Display exercise details
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Title
        Text(
            text = exerciseTitle,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        // Description
        Text(
            text = description, style = MaterialTheme.typography.bodyMedium
        )

        // Duration
        Text(
            text = "Duration: $duration", style = MaterialTheme.typography.bodySmall
        )

        // Recommended by (Doctor names)
        if (doctorNames.value.isNotEmpty()) {
            Text(
                text = "Recommended by: ${doctorNames.value.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = "Recommended by: Loading...", style = MaterialTheme.typography.bodyMedium
            )
        }

        // Remark
        Text(
            text = "Remark: $remark", style = MaterialTheme.typography.bodyMedium
        )

        // Muscles and exercise types if available
        if (muscles.isNotEmpty()) {
            Text(
                text = "Muscles: ${muscles.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (types.isNotEmpty()) {
            Text(
                text = "Exercise Types: ${types.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

//Get status Color
fun getStatusTextStyle(status: String): TextStyle {
    val color = when (status.uppercase()) {
        "APPROVED", "PROGRESS" -> Color.Green
        "PENDING", "CLOSED", "COMPLETED" -> Color.Red
        else -> Color.Gray
    }
    return if (status.lowercase() == "COMPLETED") {
        TextStyle(fontWeight = FontWeight.Bold, color = color)
    } else {
        TextStyle(color = color)
    }
}

// pass the inputdata into loadONNXmodel and then display the data in the composable
@Composable
fun ProgressByAI(context: Context) {
    val db = FirebaseFirestore.getInstance()

    val userAge = remember { mutableStateOf<Int?>(null) }
    val approvedActivities = remember { mutableStateOf<List<String>>(emptyList()) }
    val approvedCount = remember { mutableStateOf(0) }
    val totalDays = remember { mutableStateOf(0) }
    val totalProgression = remember { mutableStateOf(0) }

    val uid = getUidFromSharedPreferences(context)

    LaunchedEffect(uid) {
        uid?.let { safeUid ->
            db.collection("user_info").document(safeUid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userData = document.data
                        Log.d("ProgressByAI", "User Data: $userData")

                        // Extract age
                        val profile = userData?.get("profile") as? Map<String, Any>
                        val age = profile?.get("age") as? Long  // Firestore stores numbers as Long
                        userAge.value = age?.toInt()  // Convert Long to Int and store in state

                        // Extract number of active activities
                        val activityList = userData?.get("Activity") as? List<Map<String, Any>>
                        val approved =
                            activityList?.filter { it["status"] == "Approved" } ?: emptyList()
                        val approvedCodes = approved.mapNotNull { it["code"] as? String }

                        // Calculate total days for all active activities
                        val totalDuration = approved.sumOf { activity ->
                            val startTimestamp = activity["start_date"] as? Timestamp
                            val endTimestamp = activity["end_date"] as? Timestamp

                            if (startTimestamp != null && endTimestamp != null) {
                                val startDate = startTimestamp.toDate()
                                val endDate = endTimestamp.toDate()

                                // Ensure startDate is before endDate
                                val (correctStartDate, correctEndDate) = if (startDate.after(endDate)) {
                                    endDate to startDate  // Swap if incorrectly ordered
                                } else {
                                    startDate to endDate
                                }

                                // Calculate days between dates
                                val diffInMillis = correctEndDate.time - correctStartDate.time
                                (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
                            } else {
                                0
                            }
                        }

                        // Progression
                        val progression =
                            userData?.get("progression") as? Map<String, Any> ?: emptyMap()
                        val totalProgressionValue = approvedCodes.sumOf { code ->
                            (progression[code] as? Number)?.toInt() ?: 0
                        } / 100

                        approvedActivities.value = approvedCodes
                        approvedCount.value = approvedCodes.size
                        totalDays.value = totalDuration
                        totalProgression.value = totalProgressionValue
                    } else {
                        Log.e("ProgressByAI", "No user found with this UID")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ProgressByAI", "Error fetching user data: ${exception.message}")
                }
        } ?: Log.e("ProgressByAI", "UID is null, cannot fetch data")
    }

    Column {
        Text(text = "User Age: ${userAge.value ?: "Loading..."}")
        Text(text = "Approved Activity Codes: ${approvedActivities.value.joinToString(", ")}")
        Text(text = "Total Approved Activities: ${approvedCount.value}")
        Text(text = "Total Activity Duration: ${totalDays.value} days")
        Text(text = "Total Exercise Adherence: ${totalProgression.value}%")
    }
}

//fun loadONNXModel(context: Context, inputData: FloatArray): FloatArray? {
//    return try {
//        val ortEnv = OrtEnvironment.getEnvironment()
//        val session = ortEnv.createSession(context.assets.open("model.onnx").readBytes())
//
//        val inputName = session.inputNames.first()
//        val shape = longArrayOf(1, inputData.size.toLong())
//
//        // Convert FloatArray to FloatBuffer
//        val floatBuffer = FloatBuffer.allocate(inputData.size)
//        floatBuffer.put(inputData)
//        floatBuffer.rewind()
//
//        val inputTensor = OnnxTensor.createTensor(ortEnv, floatBuffer, shape)
//        val outputs = session.run(mapOf(inputName to inputTensor))
//
//        val outputArray = (outputs[0].value as Array<FloatArray>)[0]
//        outputs.close()
//        session.close()
//
//        outputArray
//    } catch (e: Exception) {
//        Log.e("ONNX", "Model loading failed: ${e.localizedMessage}")
//        null
//    }
//}