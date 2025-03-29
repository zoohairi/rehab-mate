package com.example.rehabmate.screens

import androidx.compose.foundation.background
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.rehabmate.ui.theme.blue_color
import com.example.rehabmate.ui.theme.white_color
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task

@Composable
fun ExerciseScreen(navController: NavHostController) {
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
                    text = { Text(text = title) }
                )
            }
        }

        // Content based on selected tab
        when (selectedTab) {
            0 -> HomeTab(navController)
            1 -> ExerciseInfoTab(navController)
            2 -> ExerciseListTab(navController)
            3 -> ExerciseDemoTab(navController)
        }
    }
}

@Composable
fun HomeTab(navController: NavHostController) {
    Box(modifier = Modifier.background(color = Color.Black)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {// Greeting section
                Row(
                    modifier = Modifier

                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hi There,",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold, color = white_color
                        )
                        Text(
                            text = "Ready to improve today",
                            fontSize = 14.sp,
                            color = white_color
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.width(100.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = white_color,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { /* Handle click */ }
                        )
//                    Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.Search,
                            tint = white_color,
                            contentDescription = "Search",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { /* Handle click */ }
                        )
//                    Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            tint = white_color,
                            contentDescription = "Search",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { /* Handle click */ }
                        )
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
                        title = "Exercise",
                        onClick = { /* Navigate to exercise section */ }
                    )

                    FeatureCard(
                        title = "Progress Tracking",
                        onClick = { /* Navigate to progress tracking */ }
                    )

                    FeatureCard(
                        title = "Assessment",
                        onClick = { /* Navigate to assessment */ }
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
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Graph Visualization",
                            fontSize = 18.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Your Rehab Exercises section
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
                        fontWeight = FontWeight.Bold, color = white_color
                    )

                    Row {
                        Text(
                            text = "See All",
                            fontSize = 14.sp,
                            color = white_color,
                            modifier = Modifier.clickable { /* Handle see all click */ }
                        )
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "View All",
                            tint = white_color,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { /* Handle back navigation */ }
                        )
                    }
                }

                // Exercise items
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(2) { index ->
                        ExerciseItem(
                            title = "15 Curls & 10 Minutes",
                            subtitle = "Back Exercises",
                            onClick = { /* Handle exercise click */ }
                        )
                    }
                }

                // Continue Exercise section
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
                        fontWeight = FontWeight.Bold, color = white_color
                    )

                    Row {
                        Text(
                            text = "See All",
                            fontSize = 14.sp,
                            color = white_color,
                            modifier = Modifier.clickable { /* Handle see all click */ }
                        )
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "View All",
                            tint = white_color,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { /* Handle back navigation */ }
                        )
                    }
                }

                // Continue buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ContinueButton(
                        title = "Squat Exercise",
                        subtitle = "Leg exercises",
                        onClick = { /* Handle continue click */ },
                        modifier = Modifier.weight(1f)
                    )

                    ContinueButton(
                        title = "Upper Exercise",
                        subtitle = "Shoulder rehab",
                        onClick = { /* Handle continue click */ },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
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
                    .clickable { /* Handle back navigation */ }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "[EXERCISE NAME]",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
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
        db.collection("Exercises")
            .get()
            .addOnSuccessListener { result ->
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
                            db.collection("Doctors").document(doctorUid)
                                .get()
                                .addOnSuccessListener { doctorDoc ->
                                    val doctorName = doctorDoc.getString("name") ?: "Unknown Doctor"
                                    doctorNameMap[doctorUid] = doctorName
                                    doctorNames.add(doctorName)
                                }
                                .addOnFailureListener { exception ->
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
            }
            .addOnFailureListener { exception ->
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
                    .clickable { /* Handle back navigation */ }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Exercises",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
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
            db.collection("Doctors").document(doctorUid)
                .get()
                .addOnSuccessListener { doctorDoc ->
                    val doctorName = doctorDoc.getString("name") ?: "Unknown Doctor"
                    names.add(doctorName)
                    if (names.size == doctorInCharge.size) {
                        doctorNames.value = names
                    }
                }
                .addOnFailureListener { exception ->
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
            text = description,
            style = MaterialTheme.typography.bodyMedium
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
                    .clickable { /* Handle back navigation */ }
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
    title: String,
    onClick: () -> Unit
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
                .padding(12.dp),
            contentAlignment = Alignment.Center
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
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
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
            .height(55.dp)
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
                    .background(Color.Red),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}