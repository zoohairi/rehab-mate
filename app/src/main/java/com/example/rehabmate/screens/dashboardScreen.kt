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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.rehabmate.firebase.fetchExercisesForUser
import com.example.rehabmate.firebase.fetchUserInfo
import com.example.rehabmate.firebase.getUidFromSharedPreferences
import com.example.rehabmate.ui.theme.blue_color
import com.example.rehabmate.ui.theme.white_color
import com.google.firebase.firestore.FirebaseFirestore

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
            1 -> ExerciseInfoTab(navController) // the exercise use selected from dashboard will lead to a page to show the exercise information
            2 -> ExerciseListTab(navController) // not sure about this page yet
            3 -> ExerciseDemoTab(navController) // should like this page to ExerciseInfoTab (the exercise it self, should have the timer + TTS in this screen)
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
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { /* Handle click */ })
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    tint = Color.White,
                                    contentDescription = "Search",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { /* Handle click */ })
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    tint = Color.White,
                                    contentDescription = "Account",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            navController.navigate("profile_screen")
                                        })
                            }
                        }
                    }

                    // Feature cards section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FeatureCard(
                            title = "Exercise", onClick = { /* Navigate to exercise section */ })

                        FeatureCard(
                            title = "Progress Tracking",
                            onClick = { /* Navigate to progress tracking */ })

                        FeatureCard(
                            title = "Assessment", onClick = { /* Navigate to assessment */ })
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
                                val subexerciseIds =
                                    subexerciseList.joinToString(", ") // Join the list into a string for logging
                                val description =
                                    exercise["description"] as? String ?: "No Description"
                                val remark = exercise["remark"] as? String ?: description

                                Log.d(
                                    "ExerciseDebug",
                                    "Title: $exerciseTitle, Subexercise IDs: $subexerciseIds, Description: $description, Remark: $remark"
                                )

                                ExerciseItem(
                                    title = exerciseTitle, subtitle = exerciseStatus, onClick = {
                                        val exerciseId = exercise["id"] as? String
                                        Log.d("ExerciseDebug", "Exercise clicked: $exerciseId")

                                        // Navigate to exercise detail with the exercise ID
                                        if (exerciseId != null) {
                                            // You can navigate to a detail screen with the ID
                                            // navController.navigate("exerciseDetail/$exerciseId")
                                        }
                                    })
                            }

                        }
                    }

                    // retrieve & display all the Exercise data also
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

                    // Continue buttons (Resume exercise)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Check if there are exercises to show for "Continue Exercise"
                        if (exerciseList.value.isNotEmpty()) {
                            // Display ContinueButton for each exercise
                            exerciseList.value.take(2).forEachIndexed { index, exercise ->
                                Log.d("exerciseData123", exercise.toString())
                                // Extracting exercise data
                                val exerciseTitle =
                                    exercise["exercise_title"] as? String ?: "No Title"
//                                val exerciseSubtitle =
//                                    exercise["remark"] as? String ?: "No Description"

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

                                        // You can navigate to a detail screen for this specific exercise
                                        if (exerciseId != null) {
                                            navController.navigate("exerciseDetail/$exerciseId")
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            // Show a message if there are no exercises to continue
                            Text(
                                text = "No exercises to continue",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = white_color
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ExerciseItem(title: String, subtitle: String, onClick: () -> Unit) {
    // make this clickable and click to =>  ExerciseInfoTab(navController)
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
                    text = subtitle, fontSize = 12.sp, color = Color.Gray
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
                    .clickable { /* Handle back navigation */ })
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
                imageVector = Icons.Default.Star, contentDescription = title, tint = Color.Yellow
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
fun AllExerciseItem(exercise: Map<String, Any>) {
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
            db.collection("Doctors").document(doctorUid).get().addOnSuccessListener { doctorDoc ->
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

@Composable
fun ExerciseInfoTab(navController: NavHostController) {
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
                text = "[EXERCISE NAME]", fontSize = 18.sp, fontWeight = FontWeight.Bold
            )
        }

        // Exercise information content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Information About The Exercise Selected From The Home Page",
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
            val doctorNameMap = mutableMapOf<String, String>() // Map to store doctor UID and name

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
            db.collection("Doctors").document(doctorUid).get().addOnSuccessListener { doctorDoc ->
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
