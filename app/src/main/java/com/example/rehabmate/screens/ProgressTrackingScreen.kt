package com.example.rehabmate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.getValue

data class ProgressData(
    val exerciseId: String,
    val exerciseTitle: String,
    val progressionValue: Int
)

/**
 * Colored progress bar composable
 * - Red if progress < 35
 * - Yellow if progress < 75
 * - Green if progress >= 75
 */
@Composable
fun ColoredProgressBar(
    progress: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0, 100) / 100f,
        label = ""
    )

    val fillColor = when {
        progress < 35 -> Color(0xFF7C2638)
        progress < 75 -> Color(0xFFFFC107)
        else -> Color(0xFF418D43)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF424242)) // Dark grey background
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(fillColor)
        )
        Text(
            text = "$progress%",
            modifier = Modifier.align(Alignment.Center),
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressTrackingScreen(navController: NavHostController, userId: String?) {
    // State for the raw progression map from user_info
    val progressionMapState = remember { mutableStateOf<Map<String, Any>?>(null) }
    val isLoadingUserDoc = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val db = FirebaseFirestore.getInstance()

    // Fetch user_info document to get "progression"
    LaunchedEffect(userId) {
        if (!userId.isNullOrEmpty()) {
            db.collection("user_info").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val progressionMap = document.get("progression") as? Map<String, Any>
                        progressionMapState.value = progressionMap
                    } else {
                        errorMessage.value = "No user found with this ID."
                    }
                    isLoadingUserDoc.value = false
                }
                .addOnFailureListener { exception ->
                    errorMessage.value = "Error fetching data: ${exception.message}"
                    isLoadingUserDoc.value = false
                }
        } else {
            errorMessage.value = "User ID not found or is empty."
            isLoadingUserDoc.value = false
        }
    }

    // Fetch exercise titles for each progression entry
    val progressDataList = remember { mutableStateOf<List<ProgressData>>(emptyList()) }
    val isLoadingExercises = remember { mutableStateOf(false) }

    LaunchedEffect(progressionMapState.value) {
        val progressionMap = progressionMapState.value ?: return@LaunchedEffect
        if (progressionMap.isNotEmpty()) {
            isLoadingExercises.value = true
            val tempList = mutableListOf<ProgressData>()
            progressionMap.forEach { (exerciseId, progressValue) ->
                val progressInt = when (progressValue) {
                    is Number -> progressValue.toInt()
                    is String -> progressValue.toIntOrNull() ?: 0
                    else -> 0
                }
                db.collection("Exercises").document(exerciseId).get()
                    .addOnSuccessListener { exerciseDoc ->
                        val exerciseTitle = if (exerciseDoc.exists()) {
                            exerciseDoc.getString("exercise_title") ?: "Untitled Exercise"
                        } else {
                            "Exercise $exerciseId (Not Found)"
                        }
                        tempList.add(
                            ProgressData(
                                exerciseId = exerciseId,
                                exerciseTitle = exerciseTitle,
                                progressionValue = progressInt
                            )
                        )
                        progressDataList.value = tempList.toList()
                    }
                    .addOnFailureListener { exception ->
                        errorMessage.value = "Error fetching exercise $exerciseId: ${exception.message}"
                    }
            }
            isLoadingExercises.value = false
        }
    }

    // Scaffold with CenterAlignedTopAppBar for centered title
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "PROGRESS TRACKING",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable { navController.popBackStack() }
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF2196F3)
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .background(Color.Black)
        ) {
            if (isLoadingUserDoc.value) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Color.White
                )
                return@Column
            }
            if (!errorMessage.value.isNullOrEmpty()) {
                Text(
                    text = errorMessage.value ?: "",
                    color = Color.Red,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                return@Column
            }
            val progressionMap = progressionMapState.value
            if (progressionMap == null || progressionMap.isEmpty()) {
                Text(
                    text = "No progression data found for User ID: $userId",
                    fontSize = 16.sp,
                    color = Color.White
                )
                return@Column
            }

            if (isLoadingExercises.value) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Color.White
                )
            } else {
                progressDataList.value.forEach { progressData ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 }) // very subtle
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            elevation = CardDefaults.cardElevation(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = progressData.exerciseTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ColoredProgressBar(
                                    progress = progressData.progressionValue,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
