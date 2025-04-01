package com.example.rehabmate.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.rehabmate.MainActivity
import com.example.rehabmate.firebase.fetchExercisesForUser
import com.example.rehabmate.firebase.getUidFromSharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import Exercise
import RetrofitInstance
import kotlinx.coroutines.tasks.await

// Data class for API exercise with additional fields
data class ApiExercise(
    val name: String,
    val type: String,
    val muscle: String,
    val equipment: String,
    val difficulty: String,
    val instructions: String,
    val mainExercise: String = "",
    val exerciseTitle: String = ""
)

// Data class for grouping exercises by muscle
data class ExerciseGroup(
    val muscle: String,
    val exerciseTitle: String,
    val type: String,
    val remark: String,
    val apiExercises: List<ApiExercise> = emptyList()
)

@Composable
fun BeginnerExerciseScreen(navController: NavHostController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State to hold exercise groups
    var exerciseGroups by remember { mutableStateOf<List<ExerciseGroup>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Try to initialize RetrofitInstance if context is MainActivity
    LaunchedEffect(Unit) {
        if (context is MainActivity) {
            RetrofitInstance.initialize(context)
        }
    }

    // Fetch exercises when screen first loads
    LaunchedEffect(Unit) {
        val uid = getUidFromSharedPreferences(context)

        if (uid == null) {
            errorMessage = "User not logged in"
            isLoading = false
            return@LaunchedEffect
        }

        // Using the fetchExercisesForUser function from FirebaseHelper.kt
        fetchExercisesForUser(uid, { exercisesList ->
            // Process the exercises from Firebase
            coroutineScope.launch {
                try {
                    val groups = mutableListOf<ExerciseGroup>()

                    // For each exercise document
                    exercisesList.forEachIndexed { index, exerciseData ->
                        val id = exerciseData["id"] as? String ?: return@forEachIndexed
                        val title = exerciseData["exercise_title"] as? String ?: "Untitled Exercise"
                        val type = exerciseData["Type"] as? String ?: "Unknown"
                        val remark = exerciseData["remark"] as? String ?: ""

                        // Extract keyword_api data
                        val keywordApiMap = exerciseData["keyword_api"] as? Map<String, Any>
                        val muscles = (keywordApiMap?.get("Muscle") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                        if (muscles.isNotEmpty()) {
                            // For each muscle, create a group and fetch API exercises
                            muscles.forEach { muscle ->
                                fetchExercisesFromApi(muscle, type) { apiExercises ->
                                    val enhancedApiExercises = apiExercises.map { apiExercise ->
                                        ApiExercise(
                                            name = apiExercise.name,
                                            type = apiExercise.type,
                                            muscle = apiExercise.muscle,
                                            equipment = apiExercise.equipment,
                                            difficulty = apiExercise.difficulty,
                                            instructions = apiExercise.instructions,
                                            mainExercise = muscle,
                                            exerciseTitle = title
                                        )
                                    }

                                    val group = ExerciseGroup(
                                        muscle = muscle,
                                        exerciseTitle = title,
                                        type = type,
                                        remark = remark,
                                        apiExercises = enhancedApiExercises
                                    )

                                    groups.add(group)

                                    // If this is the last one, update the UI
                                    if (index == exercisesList.size - 1) {
                                        exerciseGroups = groups.sortedBy { it.muscle }
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    }

                    // If the list is empty, make sure to update loading state
                    if (exercisesList.isEmpty()) {
                        isLoading = false
                    }

                } catch (e: Exception) {
                    Log.e("BeginnerExerciseScreen", "Error processing exercises: ${e.message}")
                    errorMessage = "Error loading exercises: ${e.message}"
                    isLoading = false
                }
            }
        }, { error ->
            errorMessage = error
            isLoading = false
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Top App Bar
        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
            navController = navController
        )

        // Main Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (isLoading) {
                // Show loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF6200EE)
                )
            } else if (errorMessage != null) {
                // Show error message
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: $errorMessage",
                        color = Color.Red,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { isLoading = true /* Retry logic here */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                    ) {
                        Text("Retry")
                    }
                }
            } else if (exerciseGroups.isEmpty()) {
                // Show empty state
                Text(
                    text = "No exercises found",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                // Show exercise groups
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Difficulty filters
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(listOf("All", "Beginner", "Intermediate", "Advanced")) { difficulty ->
                                DifficultyChip(
                                    text = difficulty,
                                    isSelected = difficulty == "All",
                                    onClick = { /* Filter by difficulty */ }
                                )
                            }
                        }
                    }

                    // Featured exercise (first one)
                    item {
                        if (exerciseGroups.isNotEmpty() && exerciseGroups[0].apiExercises.isNotEmpty()) {
                            val featuredExercise = exerciseGroups[0].apiExercises[0]
                            FeaturedExerciseCard(
                                title = featuredExercise.exerciseTitle,
                                muscle = featuredExercise.muscle,
                                type = featuredExercise.type,
                                onClick = { /* Navigate to detail */ }
                            )
                        }
                    }

                    // Group sections
                    items(exerciseGroups) { group ->
                        ExerciseGroupSection(
                            group = group,
                            navController = navController
                        )
                    }
                }
            }
        }

        // Bottom Navigation
        BottomNavigation(navController)
    }
}

@Composable
fun ExerciseGroupSection(group: ExerciseGroup, navController: NavHostController) {
    Column {
        // Group header with muscle name
        Text(
            text = "${group.muscle} - ${group.exerciseTitle}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Type and remark info
        Text(
            text = "Type: ${group.type}",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Text(
            text = "Remark: ${group.remark}",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // API Exercises in this group
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            items(group.apiExercises) { exercise ->
                ApiExerciseItem(
                    exercise = exercise,
                    onClick = {
                        // Navigate to detailed view with instructions
                        navController.navigate("exerciseDetail/${exercise.name}")
                    }
                )
            }
        }
    }
}

@Composable
fun ApiExerciseItem(exercise: ApiExercise, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF303030))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Exercise name
            Text(
                text = exercise.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Exercise details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Difficulty: ${exercise.difficulty}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = "Equipment: ${exercise.equipment}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("View")
                }
            }
        }
    }
}

@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF6200EE),
                modifier = Modifier
                    .clickable { navController.navigateUp() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Exercise Library",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EE)
            )
        }

        Row {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { /* Handle search */ }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { /* Handle profile */ }
            )
        }
    }
}

@Composable
fun DifficultyChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(
                if (isSelected) Color(0xFFB7FF59) else Color(0xFF303030)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun FeaturedExerciseCard(
    title: String,
    muscle: String,
    type: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF303030))
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Badge in the top right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFB7FF59))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Featured",
                    fontSize = 10.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            // Star icon in the bottom right
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Favorite",
                tint = Color.Yellow,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .clickable { /* Handle favorite */ }
            )

            // Exercise information in the bottom left
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = muscle,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Divider(
                        modifier = Modifier
                            .height(12.dp)
                            .width(1.dp),
                        color = Color.Gray
                    )

                    Text(
                        text = type,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigation(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BottomNavigationItem(
            icon = Icons.Default.Home,
            label = "Home",
            isSelected = false,
            onClick = { navController.navigate("home") }
        )

        BottomNavigationItem(
            icon = Icons.Default.DateRange,
            label = "Plan",
            isSelected = false,
            onClick = { navController.navigate("plan") }
        )

        BottomNavigationItem(
            icon = Icons.Default.Star,
            label = "Exercises",
            isSelected = true,
            onClick = { /* Already on exercise screen */ }
        )

        BottomNavigationItem(
            icon = Icons.Default.Person,
            label = "Profile",
            isSelected = false,
            onClick = { navController.navigate("profile") }
        )
    }
}

@Composable
fun BottomNavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color.White else Color.Gray,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) Color.White else Color.Gray
        )
    }
}

// Function to fetch exercises from API using RetrofitInstance
private fun fetchExercisesFromApi(muscle: String, type: String, callback: (List<Exercise>) -> Unit) {
    RetrofitInstance.api.getExercises(muscle).enqueue(object : Callback<List<Exercise>> {
        override fun onResponse(call: Call<List<Exercise>>, response: Response<List<Exercise>>) {
            if (response.isSuccessful) {
                // Filter by type if needed
                val exercises = response.body() ?: emptyList()
                val filteredExercises = if (type.isEmpty() || type.equals("all", ignoreCase = true)) {
                    exercises
                } else {
                    exercises.filter { it.type.equals(type, ignoreCase = true) }
                }

                callback(filteredExercises)
            } else {
                Log.e("API Error", "Failed to fetch exercises. Response code: ${response.code()}")
                callback(emptyList())
            }
        }

        override fun onFailure(call: Call<List<Exercise>>, t: Throwable) {
            Log.e("API Error", "Error fetching exercises: ${t.message}")
            callback(emptyList())
        }
    })
}

// Helper function to fetch subexercises for a given exercise from Firestore
suspend fun fetchSubExercises(exerciseId: String): List<String> {
    val db = FirebaseFirestore.getInstance()
    val exerciseRef = db.collection("Exercises").document(exerciseId)

    return try {
        val exerciseDoc = exerciseRef.get().await()
        if (exerciseDoc.exists()) {
            val subexerciseList = exerciseDoc.get("subexercise") as? List<String> ?: emptyList()
            subexerciseList
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Error fetching subexercises: ${e.message}")
        emptyList()
    }
}