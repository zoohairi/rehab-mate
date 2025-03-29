package com.example.rehabmate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay

@Composable
fun CountdownScreen(navController: NavHostController) {
    // Use remember to hold the mutable state of the countdown timer
    val timeRemaining = remember { mutableStateOf(10) } // Starting countdown time

    // Countdown effect using LaunchedEffect
    LaunchedEffect(timeRemaining.value) {
        while (timeRemaining.value > 0) {
            delay(1000L)
            timeRemaining.value-- // Decrease time every second
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Time remaining: ${timeRemaining.value} seconds",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge
        )

        // Once countdown finishes, show the button to proceed
        if (timeRemaining.value == 0) {
            navController.navigate("login_screen") // Navigate to login_screen after countdown finishes
        }
    }
}
