package com.example.rehabmate.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun LoginScreen(navController: NavHostController) {
    val usernameState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf("") }
    val showError = remember { mutableStateOf(false) }

    // Use Scaffold to ensure all the contents are uniform
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to RehabMate!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.testTag("titleText"),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Username input field
            OutlinedTextField(
                value = usernameState.value,
                onValueChange = {
                    usernameState.value = it
                    if (it.isNotEmpty()) {
                        showError.value = false
                        errorMessage.value = ""
                    }
                },
                label = { Text("Enter your username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("usernameTextField")
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Password input field
            OutlinedTextField(
                value = passwordState.value,
                onValueChange = {
                    passwordState.value = it
                    if (it.isNotEmpty()) {
                        showError.value = false
                        errorMessage.value = ""
                    }
                },
                label = { Text("Enter your password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("passwordTextField")
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Show error message if there's any invalid input
            if (showError.value) {
                Text(
                    text = errorMessage.value,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(8.dp)
                        .testTag("errorText")
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // Validate username and password
                    if (usernameState.value.trim().isEmpty() || passwordState.value.trim().isEmpty()) {
                        showError.value = true
                        errorMessage.value = "Please fill in both fields"
                    } else {
                        navController.navigate("personalised_screen/${usernameState.value.trim()}")
                    }
                },
                modifier = Modifier.testTag("loginButton")
            ) {
                Text("Log In")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Created by ZAPJOLLY for INF2007 at SIT",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("footnoteText")
            )
        }
    }
}


