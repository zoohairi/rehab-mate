package com.example.rehabmate.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun PersonalisedScreen(navController: NavHostController, username: String?) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome message with username
            Text(
                text = "Welcome, $username!",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Dashboard buttons
            Button(
                onClick = {
                    // Navigate to recovery progress screen
                    navController.navigate("recovery_progress_screen")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Display Recovery Progress")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Navigate to personalised insights screen
                    navController.navigate("personalised_insights_screen")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Personalized Insights")
            }
        }
    }
}
