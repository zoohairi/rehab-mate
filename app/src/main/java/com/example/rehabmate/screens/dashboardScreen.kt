//change screen file name from 'exerciseScreen' to 'dashboard' screen
package com.example.rehabmate.screens

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import android.content.Context
import android.util.Log
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.rehabmate.firebase.fetchExercisesForUser
import com.example.rehabmate.firebase.fetchUserInfo
import com.example.rehabmate.firebase.getUidFromSharedPreferences
import com.example.rehabmate.generateSpeech
import com.example.rehabmate.screens.demoPurposes.DemoActionButton
import com.example.rehabmate.screens.demoPurposes.formatTime
import com.example.rehabmate.ui.theme.red_color
import com.example.rehabmate.ui.theme.white_color
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.nio.FloatBuffer
import java.util.Optional

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
//            2 -> ExerciseListTab(navController,s) // not sure about this page yet
//            3 -> ExerciseDemoTab(navController) // should like this page to ExerciseInfoTab (the exercise it self, should have the timer + TTS in this screen)
        }
    }
}

@Composable
fun HomeTab(navController: NavHostController) {
    val context = LocalContext.current

    // Retrieve UID from SharedPreferences
    val uid = getUidFromSharedPreferences(context)
    val auth = FirebaseAuth.getInstance()

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
                                        .clickable {
                                            auth.signOut()
                                            navController.navigate("welcome_screen") {
                                                popUpTo("welcome_screen") { inclusive = true }
                                            }
                                        }
                                )
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
                            title = "View Appointments",
                            onClick = { navController.navigate("appointment_screen") })

                        FeatureCard(
                            title = "Progress Tracking",
                            onClick = {
                                uid?.let { safeUid ->
                                    navController.navigate("progress_tracking_screen/$safeUid")
                                }
                            })

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

                    Column(
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        ProgressByAI(context)
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
                                Log.d("exercisedata", exercise.toString())

                                // debug: entire exercise data for inspection
                                Log.d("ExerciseDebug", exerciseList.toString())

                                // Extracting exercise data
                                val exerciseStatus = exercise["status"] as? String ?: "No Status"
                                val exerciseCode = exercise["id"] as? String ?: "No Code"
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
                                            navController.navigate("exercise_info_tab/$exerciseStatus/$exerciseCode")
                                        }
                                    })
                            }
                        }
                    }


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
//            2 -> ExerciseListTab(navController,s) // not sure about this page yet
//            3 -> ExerciseDemoTab(navController) // should like this page to ExerciseInfoTab (the exercise it self, should have the timer + TTS in this screen)
                            }
                        }
                    }

                    @Composable
                    fun HomeTab(navController: NavHostController) {
                        val context = LocalContext.current

                        // Retrieve UID from SharedPreferences
                        val uid = getUidFromSharedPreferences(context)
                        val auth = FirebaseAuth.getInstance()

                        // States to store the name of the user and exercise list
                        val userName = remember { mutableStateOf<String?>(null) }
                        val exerciseList =
                            remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
                        val isLoading = remember { mutableStateOf(true) }
                        val errorMessage =
                            remember { mutableStateOf<String?>(null) } // To show error message

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
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
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
                                                            .clickable { /* Handle add referral code click */ }
                                                    )

                                                    // Sign Out
                                                    Icon(
                                                        imageVector = Icons.Default.ExitToApp,
                                                        contentDescription = "Sign out",
                                                        tint = Color.White,
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clickable {
                                                                auth.signOut()
                                                                navController.navigate("welcome_screen") {
                                                                    popUpTo("welcome_screen") {
                                                                        inclusive = true
                                                                    }
                                                                }
                                                            }
                                                    )
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
                                                title = "View Appointments",
                                                onClick = { navController.navigate("appointment_screen") })

                                            FeatureCard(
                                                title = "Progress Tracking",
                                                onClick = {
                                                    uid?.let { safeUid ->
                                                        navController.navigate("progress_tracking_screen/$safeUid")
                                                    }
                                                })

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

                                        Column(
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        ) {
                                            ProgressByAI(context)
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
                                                    Text(
                                                        "No exercises available.",
                                                        color = Color.White
                                                    )
                                                }
                                            } else {
                                                items(exerciseList.value.size) { index ->
                                                    val exercise = exerciseList.value[index]
                                                    Log.d("exercisedata", exercise.toString())

                                                    // debug: entire exercise data for inspection
                                                    Log.d("ExerciseDebug", exerciseList.toString())

                                                    // Extracting exercise data
                                                    val exerciseStatus =
                                                        exercise["status"] as? String ?: "No Status"
                                                    val exerciseCode =
                                                        exercise["id"] as? String ?: "No Code"
                                                    val exerciseTitle =
                                                        exercise["exercise_title"] as? String
                                                            ?: "No Title"
                                                    val subexerciseList =
                                                        exercise["subexercise"] as? List<String>
                                                            ?: emptyList()
//                                val subexerciseIds =
//                                    subexerciseList.joinToString(", ")
//                                val description =
//                                    exercise["description"] as? String ?: "No Description"
//                                val remark = exercise["remark"] as? String ?: description


                                                    ExerciseItem(
                                                        title = exerciseTitle,
                                                        subtitle = exerciseStatus,
                                                        onClick = {
                                                            val exerciseId =
                                                                exercise["id"] as? String
                                                            Log.d(
                                                                "ExerciseDebug",
                                                                "Exercise clicked: $exerciseId"
                                                            )
//im here
                                                            // Navigate to exercise detail with the exercise ID
                                                            if (exerciseId != null) {
                                                                navController.navigate("exercise_info_tab/$exerciseStatus/$exerciseCode")
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
                                                    val status = exercise["status"] as? String
                                                        ?: "Not Started"

                                                    if (status == "progress") {
                                                        Log.d(
                                                            "exerciseData123",
                                                            exercise.toString()
                                                        )

                                                        // Extracting exercise data
                                                        val exerciseTitle =
                                                            exercise["exercise_title"] as? String
                                                                ?: "No Title"

                                                        ContinueButton(
                                                            title = exerciseTitle,
                                                            subtitle = "description here",
                                                            onClick = {
                                                                // Handle continue button click, e.g., navigate to exercise detail
                                                                val exerciseId =
                                                                    exercise["id"] as? String
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
                    fun ExerciseDemoTab(
                        navController: NavHostController,
                        exercise_des: String?,
                        exercise_Duration: String?,
                        exercise_Title: String?
                    ) {
                        var text by remember { mutableStateOf(TextFieldValue()) }
                        var isLoading by remember { mutableStateOf(false) }
                        var resultMessage by remember { mutableStateOf("") }
                        var voiceOverEnabled by remember { mutableStateOf(false) }

                        // Timer state and effect
                        var isPlaying by remember { mutableStateOf(false) }
                        var currentTime by remember { mutableStateOf(30) }
                        val context = LocalContext.current

                        val exerciseDurationInSeconds = convertDurationToSeconds(exercise_Duration)

                        // Set initial timer value based on exercise duration
                        LaunchedEffect(Unit) {
                            currentTime = exerciseDurationInSeconds ?: 30
                        }

                        // Timer logic to countdown automatically
                        LaunchedEffect(isPlaying) {
                            if (isPlaying) {
                                while (currentTime > 0) {
                                    delay(1000)
                                    currentTime-- // Decrease timer by 1 every second
                                }
                                isPlaying = false // Stop the timer when finished
                                currentTime = exerciseDurationInSeconds
                                    ?: 30 // Reset the timer to exercise duration
                            }
                        }

                        // Function to handle speech generation with consistent behavior
                        fun startSpeechAndTimer() {
                            if (text.text.isNotEmpty()) {
                                isLoading = true
                                generateSpeech(
                                    context = context,
                                    text = text.text,
                                    callback = { msg, mediaPlayer ->
                                        isLoading = false
                                        resultMessage = msg
                                        // Start the timer when the audio finishes playing
                                        mediaPlayer?.setOnCompletionListener {
                                            currentTime = exerciseDurationInSeconds ?: 30
                                            isPlaying = true  // Start the timer countdown
                                        }
                                    }
                                )
                            }
                        }

                        // Effect for voice over - only trigger speech when voiceOverEnabled changes to true
                        LaunchedEffect(voiceOverEnabled) {
                            if (voiceOverEnabled && text.text.isNotEmpty()) {
                                startSpeechAndTimer()
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
                                        .clickable { navController.popBackStack() }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = exercise_Title ?: "Exercise",
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
                                        color = Color.Black,
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Display exercise description
                                    Text(
                                        text = "Description: ${exercise_des ?: "No Description Provided"}",
                                        textAlign = TextAlign.Center,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Duration: ${exercise_Duration ?: "No Duration Provided"}",
                                        textAlign = TextAlign.Center,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Auto-fill text field with exercise description
                                    LaunchedEffect(exercise_des) {
                                        if (!exercise_des.isNullOrBlank()) {
                                            text = TextFieldValue(exercise_des)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Button to trigger speech generation
                                    Button(
                                        onClick = { startSpeechAndTimer() },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isLoading // Disable the button while loading
                                    ) {
                                        Text("Start Exercise")
                                    }

                                    if (isLoading) CircularProgressIndicator()
                                    if (resultMessage.isNotEmpty()) Text(resultMessage)

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Timer countdown
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .align(Alignment.CenterHorizontally)
                                            .clip(RoundedCornerShape(60.dp))
                                            .background(Color(0xFF303030))
                                            .border(
                                                width = 4.dp,
                                                color = Color(0xFFB7FF59),
                                                shape = RoundedCornerShape(60.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = formatTime(currentTime),
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Voice over toggle and "Auto Play" label
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        // Label for auto-play
                                        Text(
                                            text = "Auto Play",
                                            fontSize = 16.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )

                                        // Switch to enable/disable voice-over
                                        Switch(
                                            checked = voiceOverEnabled,
                                            onCheckedChange = { voiceOverEnabled = it },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFFB7FF59),
                                                uncheckedThumbColor = Color.White,
                                                uncheckedTrackColor = Color.Gray
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Action buttons simplified
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        DemoActionButton(
                                            icon = if (isPlaying) Icons.Default.Clear else Icons.Default.PlayArrow, // Conditionally set the icon
                                            label = if (isPlaying) "Stop Timer" else "Start Timer", // Conditionally set the label text
                                            onClick = {
                                                isPlaying = !isPlaying // Toggle the playing state
                                            },
                                            primary = true
                                        )
                                    }
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
                                text = title,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = Color.Yellow
                            )
                        }
                    }


                    @Composable
                    fun ContinueButton(
                        title: String,
                        subtitle: String,
                        onClick: () -> Unit,
                        modifier: Modifier = Modifier
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

                    fun ExerciseListTab(
                        navController: NavHostController,
                        status: String?,
                        exerciseCode: String?
                    ) {
                        val db = FirebaseFirestore.getInstance()
                        val exercises =
                            remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
                        val exerciseList =
                            remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
                        val errorMessage = remember { mutableStateOf<String?>(null) }
                        val isLoading = remember { mutableStateOf(true) }

                        Log.d("ExerciselistTab", exerciseCode.toString())

                        // Fetch exercises from Firebase
                        LaunchedEffect(exerciseCode) {
                            if (exerciseCode != null) {
                                Log.d("FirestoreDebug", "Fetching exercise for Code: $exerciseCode")

                                // Reference to the main exercise document using the exercise code
                                val exerciseRef = db.collection("Exercises").document(exerciseCode)

                                // Fetch the main exercise document
                                exerciseRef.get()
                                    .addOnSuccessListener { document ->
                                        if (document.exists()) {
                                            val exerciseData = document.data

                                            Log.d("FirestoreDebug", "Found Exercise: $exerciseData")

                                            // Fetch sub_exercises collection within the exercise document
                                            exerciseRef.collection("sub_exercises").get()
                                                .addOnSuccessListener { subExercisesSnapshot ->
                                                    val subExercises =
                                                        subExercisesSnapshot.documents.mapNotNull { it.data }

                                                    Log.d(
                                                        "FirestoreDebug",
                                                        "Fetched ${subExercises.size} Sub Exercises"
                                                    )

                                                    // Combine the main exercise and its sub-exercises into one data structure
                                                    val fullExerciseData = mapOf(
                                                        "exercise_id" to (exerciseData?.get("exercise_code")
                                                            ?: "Unknown ID"),
                                                        "exercise_title" to (exerciseData?.get("exercise_title")
                                                            ?: "No Title"),
                                                        "description" to (exerciseData?.get("description")
                                                            ?: "No Description"),
                                                        "remark" to (exerciseData?.get("remark")
                                                            ?: "No Remarks"),
                                                        "sub_exercises" to subExercises
                                                    )

                                                    // Set the data in your state or variable
                                                    exercises.value =
                                                        listOf(fullExerciseData) // Update exercises state
                                                    isLoading.value = false
                                                    Log.d(
                                                        "ExerciseListTabsss",
                                                        "Exercise data: ${exercises.value}"
                                                    )
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e(
                                                        "FirestoreError",
                                                        "Error fetching sub-exercises",
                                                        e
                                                    )
                                                    // Handle failure gracefully and still show the main exercise
                                                    exercises.value = listOf(
                                                        mapOf(
                                                            "exercise_id" to (exerciseData?.get("exercise_code")
                                                                ?: "Unknown ID"),
                                                            "exercise_title" to (exerciseData?.get("exercise_title")
                                                                ?: "No Title"),
                                                            "description" to (exerciseData?.get("description")
                                                                ?: "No Description"),
                                                            "remark" to (exerciseData?.get("remark")
                                                                ?: "No Remarks"),
                                                            "sub_exercises" to emptyList<Map<String, Any>>()
                                                        )
                                                    )
                                                    isLoading.value = false
                                                }
                                        } else {
                                            Log.w(
                                                "FirestoreWarning",
                                                "No exercise found for code: $exerciseCode"
                                            )
                                            errorMessage.value = "No exercise found for this code."
                                            exercises.value = emptyList()
                                            isLoading.value = false
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("FirestoreError", "Error fetching exercise info", e)
                                        errorMessage.value = "Failed to fetch exercise data."
                                        isLoading.value = false
                                    }
                            } else {
                                isLoading.value = false
                            }
                        }

                        // Assuming exercises.value is a list with at least one item (firstOrNull is safe in case the list is empty)
                        val mainExerciseTitle =
                            exercises.value.firstOrNull()?.get("exercise_title") as? String
                                ?: "Exercises"
                        val mainExerciseDescription =
                            exercises.value.firstOrNull()?.get("description") as? String
                                ?: "No Description"
                        val mainExerciseRemarks =
                            exercises.value.firstOrNull()?.get("remark") as? String
                                ?: "No Remarks Provided"

                        Log.d(
                            "ExerciseDetails",
                            "Title: $mainExerciseTitle, Description: $mainExerciseDescription, Remarks: $mainExerciseRemarks"
                        )

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
                                        .size(28.dp)
                                        .clickable { navController.navigate("home_tab") }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = mainExerciseTitle,  // Main exercise name
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Show loading or exercise list
                            if (isLoading.value) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)  // Ensure there's space between each item
                                ) {


                                    // List of Sub Exercises
                                    items(exercises.value.size) { index ->
                                        val exercise = exercises.value[index]
                                        Log.d("ExerciseListTab", "Sub Exercise: $exercise")

                                        // Pass the exercise and navController to the subExerciseItem composable
                                        subExerciseItem(exercise, navController, status)
                                    }
                                }
                            }
                        }
                    }


                    @Composable
                    fun subExerciseItem(
                        exercise: Map<String, Any>,
                        navController: NavController,
                        status: String?
                    ) {
                        // Extract the main exercise details
                        val exerciseTitle = exercise["exercise_title"] as? String ?: "Unknown Title"
                        val exerciseDescription =
                            exercise["description"] as? String ?: "No Description"
                        val exerciseRemark = exercise["remark"] as? String ?: "No Remarks"
                        val subExercises =
                            exercise["sub_exercises"] as? List<Map<String, Any>> ?: emptyList()

                        // Display the main exercise information
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(status.toString(), fontSize = 12.sp, color = red_color)
                                // Exercise Title
                                Text(
                                    text = "Exercise: $exerciseTitle",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(modifier = Modifier.height(8.dp)) // Adding some space after the title

                                // Description
                                Text(
                                    text = "Description:",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.height(4.dp)) // Adding space between description label and content
                                Text(
                                    text = exerciseDescription,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(12.dp)) // Add space before remarks

                                // Remark
                                Text(
                                    text = "Remark:",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = exerciseRemark,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            // Iterate over the sub_exercises and display them
                            subExercises.forEach { subExercise ->
                                val subExerciseTitle =
                                    subExercise["sub_exercise_title"] as? String
                                        ?: "Unknown Sub Exercise"
                                val subExerciseDescription =
                                    subExercise["description"] as? String ?: "No Description"
                                val duration =
                                    subExercise["duration"] as? String ?: "Unknown Duration"

                                // Display the sub-exercise information with a button for each
                                Column(modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)) {
                                    // Sub-exercise Title
                                    Text(
                                        text = "Sub Exercise: $subExerciseTitle",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    // Duration
                                    Text(
                                        text = "Duration: $duration",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )

                                    // Description
                                    Text(
                                        text = "Description: $subExerciseDescription",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )

                                    // Button to perform an action
                                    Button(
                                        onClick = {
                                            navController.navigate("exercise_demo_tab/${subExerciseDescription}/${duration}/${subExerciseTitle}")
                                            Log.d(
                                                "SubExercise",
                                                "Button clicked for $subExerciseTitle"
                                            )
                                        },

                                        enabled = status.toString()
                                            .uppercase() == "APPROVED" || status.toString()
                                            .uppercase() == "PROGRESS",  // Disable button if status is not valid,
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Text(text = "Start $subExerciseTitle")
                                    }
                                }
                            }
                        }
                    }

                    @Composable
                    fun ExerciseItem(exercise: Map<String, Any>) {
                        // Safely extract values from the exercise map
                        val exerciseTitle = exercise["exercise_title"] as? String ?: "No Title"
                        val description =
                            exercise["description"] as? String ?: "No description available."
                        val doctorInCharge =
                            exercise["doctor_incharge"] as? List<String> ?: emptyList()
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
                                        val doctorName =
                                            doctorDoc.getString("name") ?: "Unknown Doctor"
                                        names.add(doctorName)
                                        if (names.size == doctorInCharge.size) {
                                            doctorNames.value = names
                                        }
                                    }.addOnFailureListener { exception ->
                                        Log.e(
                                            "FirebaseError",
                                            "Error fetching doctor name: ${exception.message}"
                                        )
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
                                text = "Duration: $duration",
                                style = MaterialTheme.typography.bodySmall
                            )

                            // Recommended by (Doctor names)
                            if (doctorNames.value.isNotEmpty()) {
                                Text(
                                    text = "Recommended by: ${doctorNames.value.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Text(
                                    text = "Recommended by: Loading...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // Remark
                            Text(
                                text = "Remark: $remark",
                                style = MaterialTheme.typography.bodyMedium
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
                        val approvedActivities =
                            remember { mutableStateOf<List<String>>(emptyList()) }
                        val approvedCount = remember { mutableStateOf(0) }
                        val totalDays = remember { mutableStateOf(0) }
                        val totalProgression = remember { mutableStateOf(0) }
                        val predictedValue = remember { mutableStateOf<LongArray?>(null) }
                        val predictedString = remember { mutableStateOf<String>("") }
                        val adviceBasedOnPredication = remember { mutableStateOf<String>("") }

                        val uid = getUidFromSharedPreferences(context)

                        LaunchedEffect(uid) {
                            uid?.let { safeUid ->
                                db.collection("user_info").document(safeUid).get()
                                    .addOnSuccessListener { document ->
                                        if (document.exists()) {
                                            val userData = document.data
                                            Log.d("ProgressByAI", "User Data: $userData")

                                            // Extract age
                                            val profile =
                                                userData?.get("profile") as? Map<String, Any>
                                            val age =
                                                profile?.get("age") as? Long  // Firestore stores numbers as Long
                                            userAge.value =
                                                age?.toInt()  // Convert Long to Int and store in state

                                            // Extract number of active activities
                                            val activityList =
                                                userData?.get("Activity") as? List<Map<String, Any>>
                                            val approved =
                                                activityList?.filter { it["status"] == "Approved" }
                                                    ?: emptyList()
                                            val approvedCodes =
                                                approved.mapNotNull { it["code"] as? String }

                                            // Calculate total days for all active activities
                                            val totalDuration = approved.sumOf { activity ->
                                                val startTimestamp =
                                                    activity["start_date"] as? Timestamp
                                                val endTimestamp =
                                                    activity["end_date"] as? Timestamp

                                                if (startTimestamp != null && endTimestamp != null) {
                                                    val startDate = startTimestamp.toDate()
                                                    val endDate = endTimestamp.toDate()

                                                    // Ensure startDate is before endDate
                                                    val (correctStartDate, correctEndDate) = if (startDate.after(
                                                            endDate
                                                        )
                                                    ) {
                                                        endDate to startDate  // Swap if incorrectly ordered
                                                    } else {
                                                        startDate to endDate
                                                    }

                                                    // Calculate days between dates
                                                    val diffInMillis =
                                                        correctEndDate.time - correctStartDate.time
                                                    (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
                                                } else {
                                                    0
                                                }
                                            }

                                            // Progression
                                            val progression =
                                                userData?.get("progression") as? Map<String, Any>
                                                    ?: emptyMap()
                                            val totalProgressionValue =
                                                approvedCodes.sumOf { code ->
                                                    (progression[code] as? Number)?.toInt() ?: 0
                                                } / 100

                                            approvedActivities.value = approvedCodes
                                            approvedCount.value = approvedCodes.size
                                            totalDays.value = totalDuration
                                            totalProgression.value = totalProgressionValue

                                            val inputData = floatArrayOf(
                                                (userAge.value ?: 0).toFloat(),
                                                approvedCount.value.toFloat(),
                                                totalDays.value.toFloat(),
                                                totalProgression.value.toFloat()
                                            )

                                            predictedValue.value =
                                                loadONNXModel(context, inputData) ?: LongArray(1)

                                            val prediction = predictedValue.value?.firstOrNull()
                                            predictedString.value = when (prediction) {
                                                0L -> "Slow"
                                                1L -> "Moderate"
                                                2L -> "Fast"
                                                else -> "No activities are active"
                                            }

                                            adviceBasedOnPredication.value = when (prediction) {
                                                0L -> "Keep going! Progress might feel slow right now, but every small step you take is moving you closer to your goal."
                                                1L -> "It’s also important to maintain consistency. Regular progress will help you achieve your goals faster."
                                                2L -> "While fast progress is great, make sure to balance your intensity to avoid burnout. It's important to allow your body time to recover."
                                                else -> "You've completed all your exercises! Well done and we hope InHouse Rehab has guided you well through your rehab!"
                                            }

                                        } else {
                                            Log.e("ProgressByAI", "No user found with this UID")
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e(
                                            "ProgressByAI",
                                            "Error fetching user data: ${exception.message}"
                                        )
                                    }
                            } ?: Log.e("ProgressByAI", "UID is null, cannot fetch data")
                        }

                        Column {
                            Text(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                text = "Your Progress"
                            )

                            Text(
                                color = Color.White,
                                fontSize = 32.sp,
                                text = predictedString.value,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Text(
                                color = Color.White,
                                modifier = Modifier.padding(top = 4.dp),
                                text = adviceBasedOnPredication.value
                            )
                        }
                    }

                    fun loadONNXModel(context: Context, inputData: FloatArray): LongArray? {
                        try {
                            val ortEnv = OrtEnvironment.getEnvironment()
                            val session =
                                ortEnv.createSession(context.assets.open("model.onnx").readBytes())

                            val inputName = session.inputNames.first()
                            val shape = longArrayOf(1, inputData.size.toLong())

                            // Convert FloatArray to FloatBuffer
                            val floatBuffer = FloatBuffer.allocate(inputData.size)
                            floatBuffer.put(inputData)
                            floatBuffer.rewind()

                            val inputTensor = OnnxTensor.createTensor(ortEnv, floatBuffer, shape)
                            val outputs = session.run(mapOf(inputName to inputTensor))

                            // Debugging - Check output names
                            val outputNames = session.outputNames
                            Log.d("ONNX", "Model output names: $outputNames")

                            val outputName = session.outputNames.firstOrNull()
                            if (outputName == null) {
                                Log.e("ONNX", "No output name found in the model.")
                                return null
                            }

                            val outputOnnxValue = outputs[outputName]
                            if (outputOnnxValue == null) {
                                Log.e("ONNX", "No output found for key: $outputName")
                                return null
                            }

                            // Check if the output is an Optional and extract the value
                            if (outputOnnxValue is Optional<*>) {
                                // Log the content of the Optional
                                Log.d(
                                    "ONNX",
                                    "Output Optional contains: ${outputOnnxValue.orElse(null)}"
                                )

                                // Extract OnnxTensor from the Optional
                                val tensor = outputOnnxValue.orElse(null) as? OnnxTensor
                                if (tensor == null) {
                                    Log.e(
                                        "ONNX",
                                        "Output is not an OnnxTensor, it's null inside Optional."
                                    )
                                    return null
                                }

                                // Log the tensor information for debugging
                                Log.d("ONNX", "Tensor Info: ${tensor.info}")

                                // Handle extraction for INT64 tensor
                                try {
                                    val longBuffer = tensor.longBuffer
                                    val result = LongArray(longBuffer.remaining())
                                    var i = 0
                                    while (longBuffer.hasRemaining()) {
                                        result[i++] = longBuffer.get()
                                    }
                                    Log.d("ONNX", "Returned INT64 tensor as LongArray")
                                    return result
                                } catch (e: Exception) {
                                    Log.e(
                                        "ONNX",
                                        "Error extracting INT64 data: ${e.localizedMessage}"
                                    )
                                }
                            } else {
                                Log.e(
                                    "ONNX",
                                    "Expected Optional<OnnxTensor>, but found: ${outputOnnxValue::class.java}"
                                )
                                return null
                            }
                        } catch (e: Exception) {
                            Log.e("ONNX", "Model loading failed: ${e.localizedMessage}")
                            return null
                        }
                        return null
                    }

                    fun convertDurationToSeconds(duration: String?): Int {
                        return duration?.let {
                            val regex = """(\d+)\s*(minute|min|minutes|mins)?""".toRegex()
                            val matchResult = regex.find(it)
                            matchResult?.groupValues?.get(1)?.toInt()?.times(60) ?: 0
                        } ?: 0
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
fun ExerciseDemoTab(
    navController: NavHostController,
    exercise_des: String?,
    exercise_Duration: String?,
    exercise_Title: String?
) {
    var text by remember { mutableStateOf(TextFieldValue()) }
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var voiceOverEnabled by remember { mutableStateOf(false) }

    // Timer state and effect
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(30) }
    val context = LocalContext.current

    val exerciseDurationInSeconds = convertDurationToSeconds(exercise_Duration)

    // Set initial timer value based on exercise duration
    LaunchedEffect(Unit) {
        currentTime = exerciseDurationInSeconds ?: 30
    }

    // Timer logic to countdown automatically
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (currentTime > 0) {
                delay(1000)
                currentTime-- // Decrease timer by 1 every second
            }
            isPlaying = false // Stop the timer when finished
            currentTime = exerciseDurationInSeconds ?: 30 // Reset the timer to exercise duration
        }
    }

    // Function to handle speech generation with consistent behavior
    fun startSpeechAndTimer() {
        if (text.text.isNotEmpty()) {
            isLoading = true
            generateSpeech(
                context = context,
                text = text.text,
                callback = { msg, mediaPlayer ->
                    isLoading = false
                    resultMessage = msg
                    // Start the timer when the audio finishes playing
                    mediaPlayer?.setOnCompletionListener {
                        currentTime = exerciseDurationInSeconds ?: 30
                        isPlaying = true  // Start the timer countdown
                    }
                }
            )
        }
    }

    // Effect for voice over - only trigger speech when voiceOverEnabled changes to true
    LaunchedEffect(voiceOverEnabled) {
        if (voiceOverEnabled && text.text.isNotEmpty()) {
            startSpeechAndTimer()
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
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = exercise_Title ?: "Exercise",
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
                    color = Color.Black,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Display exercise description
                Text(
                    text = "Description: ${exercise_des ?: "No Description Provided"}",
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Duration: ${exercise_Duration ?: "No Duration Provided"}",
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Auto-fill text field with exercise description
                LaunchedEffect(exercise_des) {
                    if (!exercise_des.isNullOrBlank()) {
                        text = TextFieldValue(exercise_des)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Button to trigger speech generation
                Button(
                    onClick = { startSpeechAndTimer() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading // Disable the button while loading
                ) {
                    Text("Start Exercise")
                }

                if (isLoading) CircularProgressIndicator()
                if (resultMessage.isNotEmpty()) Text(resultMessage)

                Spacer(modifier = Modifier.height(24.dp))

                // Timer countdown
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(60.dp))
                        .background(Color(0xFF303030))
                        .border(
                            width = 4.dp,
                            color = Color(0xFFB7FF59),
                            shape = RoundedCornerShape(60.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatTime(currentTime),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Voice over toggle and "Auto Play" label
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Label for auto-play
                    Text(
                        text = "Auto Play",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    // Switch to enable/disable voice-over
                    Switch(
                        checked = voiceOverEnabled,
                        onCheckedChange = { voiceOverEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFB7FF59),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons simplified
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DemoActionButton(
                        icon = if (isPlaying) Icons.Default.Clear else Icons.Default.PlayArrow, // Conditionally set the icon
                        label = if (isPlaying) "Stop Timer" else "Start Timer", // Conditionally set the label text
                        onClick = {
                            isPlaying = !isPlaying // Toggle the playing state
                        },
                        primary = true
                    )
                }
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

fun ExerciseListTab(navController: NavHostController, status: String?, exerciseCode: String?) {
    val db = FirebaseFirestore.getInstance()
    val exercises = remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val exerciseList = remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val isLoading = remember { mutableStateOf(true) }

    Log.d("ExerciselistTab", exerciseCode.toString())

    // Fetch exercises from Firebase
    LaunchedEffect(exerciseCode) {
        if (exerciseCode != null) {
            Log.d("FirestoreDebug", "Fetching exercise for Code: $exerciseCode")

            // Reference to the main exercise document using the exercise code
            val exerciseRef = db.collection("Exercises").document(exerciseCode)

            // Fetch the main exercise document
            exerciseRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val exerciseData = document.data

                        Log.d("FirestoreDebug", "Found Exercise: $exerciseData")

                        // Fetch sub_exercises collection within the exercise document
                        exerciseRef.collection("sub_exercises").get()
                            .addOnSuccessListener { subExercisesSnapshot ->
                                val subExercises =
                                    subExercisesSnapshot.documents.mapNotNull { it.data }

                                Log.d(
                                    "FirestoreDebug",
                                    "Fetched ${subExercises.size} Sub Exercises"
                                )

                                // Combine the main exercise and its sub-exercises into one data structure
                                val fullExerciseData = mapOf(
                                    "exercise_id" to (exerciseData?.get("exercise_code")
                                        ?: "Unknown ID"),
                                    "exercise_title" to (exerciseData?.get("exercise_title")
                                        ?: "No Title"),
                                    "description" to (exerciseData?.get("description")
                                        ?: "No Description"),
                                    "remark" to (exerciseData?.get("remark") ?: "No Remarks"),
                                    "sub_exercises" to subExercises
                                )

                                // Set the data in your state or variable
                                exercises.value = listOf(fullExerciseData) // Update exercises state
                                isLoading.value = false
                                Log.d("ExerciseListTabsss", "Exercise data: ${exercises.value}")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirestoreError", "Error fetching sub-exercises", e)
                                // Handle failure gracefully and still show the main exercise
                                exercises.value = listOf(
                                    mapOf(
                                        "exercise_id" to (exerciseData?.get("exercise_code")
                                            ?: "Unknown ID"),
                                        "exercise_title" to (exerciseData?.get("exercise_title")
                                            ?: "No Title"),
                                        "description" to (exerciseData?.get("description")
                                            ?: "No Description"),
                                        "remark" to (exerciseData?.get("remark") ?: "No Remarks"),
                                        "sub_exercises" to emptyList<Map<String, Any>>()
                                    )
                                )
                                isLoading.value = false
                            }
                    } else {
                        Log.w("FirestoreWarning", "No exercise found for code: $exerciseCode")
                        errorMessage.value = "No exercise found for this code."
                        exercises.value = emptyList()
                        isLoading.value = false
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreError", "Error fetching exercise info", e)
                    errorMessage.value = "Failed to fetch exercise data."
                    isLoading.value = false
                }
        } else {
            isLoading.value = false
        }
    }

    // Assuming exercises.value is a list with at least one item (firstOrNull is safe in case the list is empty)
    val mainExerciseTitle =
        exercises.value.firstOrNull()?.get("exercise_title") as? String ?: "Exercises"
    val mainExerciseDescription =
        exercises.value.firstOrNull()?.get("description") as? String ?: "No Description"
    val mainExerciseRemarks =
        exercises.value.firstOrNull()?.get("remark") as? String ?: "No Remarks Provided"

    Log.d(
        "ExerciseDetails",
        "Title: $mainExerciseTitle, Description: $mainExerciseDescription, Remarks: $mainExerciseRemarks"
    )

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
                    .size(28.dp)
                    .clickable { navController.navigate("home_tab") }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = mainExerciseTitle,  // Main exercise name
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Show loading or exercise list
        if (isLoading.value) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)  // Ensure there's space between each item
            ) {


                // List of Sub Exercises
                items(exercises.value.size) { index ->
                    val exercise = exercises.value[index]
                    Log.d("ExerciseListTab", "Sub Exercise: $exercise")

                    // Pass the exercise and navController to the subExerciseItem composable
                    subExerciseItem(exercise, navController, status)
                }
            }
        }
    }
}


@Composable
fun subExerciseItem(exercise: Map<String, Any>, navController: NavController, status: String?) {
    // Extract the main exercise details
    val exerciseTitle = exercise["exercise_title"] as? String ?: "Unknown Title"
    val exerciseDescription = exercise["description"] as? String ?: "No Description"
    val exerciseRemark = exercise["remark"] as? String ?: "No Remarks"
    val subExercises = exercise["sub_exercises"] as? List<Map<String, Any>> ?: emptyList()

    // Display the main exercise information
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(status.toString(), fontSize = 12.sp, color = red_color)
            // Exercise Title
            Text(
                text = "Exercise: $exerciseTitle",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp)) // Adding some space after the title

            // Description
            Text(
                text = "Description:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp)) // Adding space between description label and content
            Text(
                text = exerciseDescription,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp)) // Add space before remarks

            // Remark
            Text(
                text = "Remark:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = exerciseRemark,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Iterate over the sub_exercises and display them
        subExercises.forEach { subExercise ->
            val subExerciseTitle =
                subExercise["sub_exercise_title"] as? String ?: "Unknown Sub Exercise"
            val subExerciseDescription = subExercise["description"] as? String ?: "No Description"
            val duration = subExercise["duration"] as? String ?: "Unknown Duration"

            // Display the sub-exercise information with a button for each
            Column(modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)) {
                // Sub-exercise Title
                Text(
                    text = "Sub Exercise: $subExerciseTitle",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                // Duration
                Text(
                    text = "Duration: $duration",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Description
                Text(
                    text = "Description: $subExerciseDescription",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Button to perform an action
                Button(
                    onClick = {
                        navController.navigate("exercise_demo_tab/${subExerciseDescription}/${duration}/${subExerciseTitle}")
                        Log.d("SubExercise", "Button clicked for $subExerciseTitle")
                    },

                    enabled = status.toString().uppercase() == "APPROVED" || status.toString()
                        .uppercase() == "PROGRESS",  // Disable button if status is not valid,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(text = "Start $subExerciseTitle")
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
    val predictedValue = remember { mutableStateOf<LongArray?>(null) }
    val predictedString = remember { mutableStateOf<String>("") }
    val adviceBasedOnPredication = remember { mutableStateOf<String>("") }

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
                            activityList?.filter { it["status"] == "approved" || it["status"] == "progress"} ?: emptyList()
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

                        val inputData = floatArrayOf(
                            (userAge.value ?: 0).toFloat(),
                            approvedCount.value.toFloat(),
                            totalDays.value.toFloat(),
                            totalProgression.value.toFloat()
                        )

                        predictedValue.value = loadONNXModel(context, inputData) ?: LongArray(1)

                        val prediction = predictedValue.value?.firstOrNull()
                        predictedString.value = when (prediction) {
                            0L -> "Slow"
                            1L -> "Moderate"
                            2L -> "Fast"
                            else -> "No activities are active"
                        }

                        adviceBasedOnPredication.value = when (prediction) {
                            0L -> "Keep going! Progress might feel slow right now, but every small step you take is moving you closer to your goal."
                            1L -> "It’s also important to maintain consistency. Regular progress will help you achieve your goals faster."
                            2L -> "While fast progress is great, make sure to balance your intensity to avoid burnout. It's important to allow your body time to recover."
                            else -> "You've completed all your exercises! Well done and we hope InHouse Rehab has guided you well through your rehab!"
                        }

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
        Text(
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            text = "Your Progress"
        )

        Text(
            color = Color.White,
            fontSize = 32.sp,
            text = predictedString.value,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            color = Color.White,
            modifier = Modifier.padding(top = 4.dp),
            text = adviceBasedOnPredication.value
        )
    }
}

fun loadONNXModel(context: Context, inputData: FloatArray): LongArray? {
    try {
        val ortEnv = OrtEnvironment.getEnvironment()
        val session = ortEnv.createSession(context.assets.open("model.onnx").readBytes())

        val inputName = session.inputNames.first()
        val shape = longArrayOf(1, inputData.size.toLong())

        // Convert FloatArray to FloatBuffer
        val floatBuffer = FloatBuffer.allocate(inputData.size)
        floatBuffer.put(inputData)
        floatBuffer.rewind()

        val inputTensor = OnnxTensor.createTensor(ortEnv, floatBuffer, shape)
        val outputs = session.run(mapOf(inputName to inputTensor))

        // Debugging - Check output names
        val outputNames = session.outputNames
        Log.d("ONNX", "Model output names: $outputNames")

        val outputName = session.outputNames.firstOrNull()
        if (outputName == null) {
            Log.e("ONNX", "No output name found in the model.")
            return null
        }

        val outputOnnxValue = outputs[outputName]
        if (outputOnnxValue == null) {
            Log.e("ONNX", "No output found for key: $outputName")
            return null
        }

        // Check if the output is an Optional and extract the value
        if (outputOnnxValue is Optional<*>) {
            // Log the content of the Optional
            Log.d("ONNX", "Output Optional contains: ${outputOnnxValue.orElse(null)}")

            // Extract OnnxTensor from the Optional
            val tensor = outputOnnxValue.orElse(null) as? OnnxTensor
            if (tensor == null) {
                Log.e("ONNX", "Output is not an OnnxTensor, it's null inside Optional.")
                return null
            }

            // Log the tensor information for debugging
            Log.d("ONNX", "Tensor Info: ${tensor.info}")

            // Handle extraction for INT64 tensor
            try {
                val longBuffer = tensor.longBuffer
                val result = LongArray(longBuffer.remaining())
                var i = 0
                while (longBuffer.hasRemaining()) {
                    result[i++] = longBuffer.get()
                }
                Log.d("ONNX", "Returned INT64 tensor as LongArray")
                return result
            } catch (e: Exception) {
                Log.e("ONNX", "Error extracting INT64 data: ${e.localizedMessage}")
            }
        } else {
            Log.e(
                "ONNX",
                "Expected Optional<OnnxTensor>, but found: ${outputOnnxValue::class.java}"
            )
            return null
        }
    } catch (e: Exception) {
        Log.e("ONNX", "Model loading failed: ${e.localizedMessage}")
        return null
    }
    return null
}

fun convertDurationToSeconds(duration: String?): Int {
    return duration?.let {
        val regex = """(\d+)\s*(minute|min|minutes|mins)?""".toRegex()
        val matchResult = regex.find(it)
        matchResult?.groupValues?.get(1)?.toInt()?.times(60) ?: 0
    } ?: 0
}