//change screen file name from 'exerciseScreen' to 'dashboard' screen
package com.example.rehabmate.screens

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import android.content.Context
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.rehabmate.firebase.fetchExercisesForUser
import com.example.rehabmate.firebase.fetchUserInfo
import com.example.rehabmate.firebase.getUidFromSharedPreferences
import com.example.rehabmate.generateSpeech
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.nio.FloatBuffer
import java.util.Optional

//Watch Items
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import androidx.compose.runtime.DisposableEffect

import org.json.JSONObject

@Composable
fun HomeTab(navController: NavHostController) {
    val context = LocalContext.current
    val uid = getUidFromSharedPreferences(context)
    val auth = FirebaseAuth.getInstance()

    val userName = remember { mutableStateOf<String?>(null) }
    val exerciseList = remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // --- Load User Info and Exercises ---
    LaunchedEffect(uid) {
        if (uid != null) {
            fetchUserInfo(uid).onSuccess { userData ->
                userName.value =
                    (userData["profile"] as? Map<String, Any>)?.get("name") as? String ?: "User"
                fetchExercisesForUser(uid, onSuccess = { exercises ->
                    exerciseList.value = exercises
                    isLoading.value = false
                }, onFailure = { error ->
                    errorMessage.value = error
                    isLoading.value = false
                })
            }.onFailure { error ->
                userName.value = "User"
                isLoading.value = false
            }
        } else {
            isLoading.value = false
        }
    }

    // --- UI ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        if (isLoading.value) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        // --- User Info + Actions ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hi ${userName.value ?: "User"},",
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

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.width(100.dp)
            ) {
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
                Icon(
                    imageVector = Icons.Default.Add,
                    tint = Color.White,
                    contentDescription = "Referral",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            navController.navigate("referral_screen")
                        }
                )
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    tint = Color.White,
                    contentDescription = "Logout",
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

        // --- Feature cards section ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FeatureCard(
                title = "View Appointments",
                onClick = { navController.navigate("appointment_screen") },

                )

            FeatureCard(
                title = "Progress Tracking",
                onClick = {
                    uid?.let { safeUid ->
                        navController.navigate("progress_tracking_screen/$safeUid")
                    }
                },
            )

            FeatureCard(
                title = "Medical Records",
                onClick = { navController.navigate("medical_records_screen") },
            )
        }


        // --- Progress By AI (Optional) ---
        ProgressByAI(context)

        Spacer(modifier = Modifier.height(16.dp))

        // --- Grid Section ---
        ExerciseGridSection(
            exerciseList = exerciseList.value,
            navController = navController
        )

        // --- Error Message ---
        errorMessage.value?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseGridSection(
    exerciseList: List<Map<String, Any>>,
    navController: NavHostController
) {
    val gridState = rememberLazyGridState()

    Text(
        text = "Your Rehab Exercises",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(bottom = 16.dp)
    ) {
        if (exerciseList.isEmpty()) {
            item {
                Text(
                    text = "No exercises available.",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        } else {
            items(exerciseList) { exercise ->
                ExerciseItem(
                    title = exercise["exercise_title"] as? String ?: "No Title",
                    subtitle = exercise["status"] as? String ?: "No Status",
                    onClick = {
                        val exerciseId = exercise["id"] as? String
                        if (exerciseId != null) {
                            navController.navigate("exercise_info_tab/${exercise["status"]}/${exercise["id"]}")
                        }
                    }
                )
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

    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(30) }
    val context = LocalContext.current
    val exerciseDurationInSeconds = convertDurationToSeconds(exercise_Duration)

    LaunchedEffect(Unit) { currentTime = exerciseDurationInSeconds }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (currentTime > 0) {
                delay(1000)
                currentTime--
            }
            isPlaying = false
            currentTime = exerciseDurationInSeconds
        }
    }

    DisposableEffect(Unit) {
        Log.d("Hello", "DisposableEffect called")
        val listener = object : MessageClient.OnMessageReceivedListener {
            override fun onMessageReceived(messageEvent: MessageEvent) {
                Log.d("DashboardScreen", "Received message: ${messageEvent.path} -> ${String(messageEvent.data)}")
            }
        }
        Wearable.getMessageClient(context).addListener(listener)
        onDispose {
            Wearable.getMessageClient(context).removeListener(listener)
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
                    mediaPlayer?.setOnCompletionListener {
                        currentTime = exerciseDurationInSeconds
                        isPlaying = true
                    }
                }
            )
        }
    }

    LaunchedEffect(exercise_des) {
        if (!exercise_des.isNullOrBlank()) {
            text = TextFieldValue(exercise_des)
        }
    }

    // -------- UI starts here --------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        // Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = exercise_Title ?: "Exercise",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(12.dp))

        // Description
        Text("Description:", color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(exercise_des ?: "No Description Provided", color = Color.LightGray)
        Spacer(Modifier.height(8.dp))

        // Duration
        Text("Duration:", color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(exercise_Duration ?: "No Duration Provided", color = Color.LightGray)

        Spacer(Modifier.height(16.dp))

        // Start Speech Button
        Button(
            onClick = { startSpeechAndTimer() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB7FF59)),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(
                color = Color.Black,
                modifier = Modifier.size(24.dp)
            )
            else Text("Start Exercise", fontWeight = FontWeight.Bold, color = Color.Black)
        }

        // Speech result
        if (resultMessage.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(resultMessage, color = Color.Green)
        }

        Spacer(Modifier.height(24.dp))

        // Timer Circle
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(70.dp))
                .background(Color(0xFF303030))
                .border(3.dp, Color(0xFFB7FF59), RoundedCornerShape(70.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatTime(currentTime),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(24.dp))

        // Auto Play toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Auto Play", color = Color.White, modifier = Modifier.padding(end = 8.dp))
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

        Spacer(Modifier.height(16.dp))

        // Timer Control Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DemoActionButton(
                icon = if (isPlaying) Icons.Default.Clear else Icons.Default.PlayArrow,
                label = if (isPlaying) "Stop Timer" else "Start Timer",
                onClick = { isPlaying = !isPlaying },
                primary = true
            )
        }
    }
}


@Composable
fun FeatureCard(
    title: String,
    onClick: () -> Unit,
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
                .background(Color.Gray)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
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
            color = Color.White
        )
    }
}

@Composable
fun ExerciseListTab(navController: NavHostController, status: String?, exerciseCode: String?) {
    val db = FirebaseFirestore.getInstance()
    val exercises = remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    Log.d("ExerciseListTab", "Exercise Code: $exerciseCode")

    // Fetch exercise and sub-exercises
    LaunchedEffect(exerciseCode) {
        if (!exerciseCode.isNullOrBlank()) {
            val exerciseRef = db.collection("Exercises").document(exerciseCode)
            exerciseRef.get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val exerciseData = doc.data
                        exerciseRef.collection("sub_exercises").get()
                            .addOnSuccessListener { subSnapshot ->
                                val subExercises = subSnapshot.documents.mapNotNull { it.data }
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
                                exercises.value = listOf(fullExerciseData)
                                isLoading.value = false
                            }
                            .addOnFailureListener {
                                exercises.value = emptyList()
                                isLoading.value = false
                                errorMessage.value = "Failed to load sub-exercises."
                            }
                    } else {
                        exercises.value = emptyList()
                        errorMessage.value = "Exercise not found."
                        isLoading.value = false
                    }
                }
                .addOnFailureListener {
                    errorMessage.value = "Failed to load exercise."
                    isLoading.value = false
                }
        } else {
            errorMessage.value = "Invalid exercise code."
            isLoading.value = false
        }
    }

    val exercise = exercises.value.firstOrNull()
    val exerciseTitle = exercise?.get("exercise_title") as? String ?: "Exercise"
    val exerciseDescription = exercise?.get("description") as? String ?: "No Description"
    val exerciseRemarks = exercise?.get("remark") as? String ?: "No Remarks"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        // --- Header ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = exerciseTitle,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // --- Loading ---
        if (isLoading.value) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        // --- Error ---
        errorMessage.value?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                modifier = Modifier.padding(8.dp)
            )
        }

        // --- Exercise Description ---
        Text("Description:", color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(exerciseDescription, color = Color.LightGray)
        Spacer(Modifier.height(8.dp))
        Text("Remarks:", color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(exerciseRemarks, color = Color.LightGray)
        Spacer(Modifier.height(16.dp))

        // --- Sub-exercises ---
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            val subExercises =
                exercise?.get("sub_exercises") as? List<Map<String, Any>> ?: emptyList()

            items(subExercises.size) { i ->
                val subExercise = subExercises[i]
                SubExerciseCard(
                    navController = navController,
                    status = status,
                    subExercise = subExercise
                )
            }
        }
    }
}

@Composable
fun SubExerciseCard(navController: NavController, status: String?, subExercise: Map<String, Any>) {
    val title = subExercise["sub_exercise_title"] as? String ?: "No Title"
    val description = subExercise["description"] as? String ?: "No Description"
    val duration = subExercise["duration"] as? String ?: "Unknown Duration"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Duration: $duration", color = Color.Gray)
            Text(description, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

            Button(
                onClick = {
                    navController.navigate("exercise_demo_tab/${description}/${duration}/${title}")
                },
                enabled = status?.uppercase() in listOf("APPROVED", "PROGRESS"),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB7FF59))
            ) {
                Text("Start $title", color = Color.Black, fontWeight = FontWeight.Bold)
            }
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
                            activityList?.filter { it["status"] == "approved" || it["status"] == "progress" }
                                ?: emptyList()
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        // Section Title
        Text(
            text = "Your Progress",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Predicted Speed
        Text(
            text = predictedString.value,
            color = Color(0xFFB7FF59),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Progress Message Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2E2E2E))
                .padding(12.dp)
        ) {
            Text(
                text = adviceBasedOnPredication.value,
                color = Color.LightGray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
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


// Helper function to format time
fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}


@Composable
fun DemoActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    primary: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(if (primary) Color(0xFFB7FF59) else Color(0xFF303030)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (primary) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}
