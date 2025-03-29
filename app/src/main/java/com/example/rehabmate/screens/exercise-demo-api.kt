package com.example.rehabmate.screens

import Exercise
import RetrofitInstance
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseApiScreen(navController: NavController) {
    var exercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch exercises when screen is loaded
    LaunchedEffect(Unit) {
        fetchExercises("biceps") { fetchedExercises, error ->
            exercises = fetchedExercises
            loading = false
            errorMessage = error
        }
    }

    // UI for displaying exercises
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercise API Demo") }
            )
        },
        content = { paddingValues ->
            if (loading) {
                // Display loading indicator while fetching data
                CircularProgressIndicator(modifier = Modifier.padding(paddingValues))
            } else {
                if (errorMessage != null) {
                    // Display error message if available
                    Text(text = "Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                } else {
                    // Display exercises in a LazyColumn once data is loaded
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(exercises) { exercise ->
                            ExerciseItem(exercise) { instructions ->
                                // Pass the instructions to SpeechScreen
                                navController.navigate("speechScreen/$instructions")
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ExerciseItem(exercise: Exercise, onSelect: (String) -> Unit) {
    Card(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = exercise.name, style = MaterialTheme.typography.headlineLarge)
            Text(text = "Type: ${exercise.type}")
            Text(text = "Muscle: ${exercise.muscle}")
            Text(text = "Difficulty: ${exercise.difficulty}")
            Text(text = "Equipment: ${exercise.equipment}")
            Text(text = "Instructions: ${exercise.instructions}")

            // Button to trigger speech generation
            Button(
                onClick = {
                    // When button is clicked, pass the instructions to SpeechScreen
                    onSelect(exercise.instructions)
                }
            ) {
                Text("Read Instructions")
            }
        }
    }
}


// Retrofit API call setup
fun fetchExercises(muscle: String, callback: (List<Exercise>, String?) -> Unit) {
    RetrofitInstance.api.getExercises(muscle).enqueue(object : Callback<List<Exercise>> {
        override fun onResponse(call: Call<List<Exercise>>, response: Response<List<Exercise>>) {
            if (response.isSuccessful) {
                val exercises = response.body() ?: emptyList()
                Log.d("Exercise API Respond", exercises.toString())

                callback(exercises, null)
            } else {
                // Enhanced error logging
                Log.e("API Error", "Failed to fetch exercises. Response code: ${response.code()}")
                Log.e("API Error", "Response body: ${response.errorBody()?.string()}")
                callback(emptyList(), "Failed to fetch exercises. Error code: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<List<Exercise>>, t: Throwable) {
            // Log the exception message
            Log.e("API Error", "Error fetching exercises: ${t.message}")
            callback(emptyList(), "Error fetching exercises: ${t.message}")
        }
    })
}
