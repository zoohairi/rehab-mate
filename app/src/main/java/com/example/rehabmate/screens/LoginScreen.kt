package com.example.rehabmate.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavHostController) {
    //can combine with register screen
    val auth: FirebaseAuth = FirebaseAuth.getInstance() // Firebase Authentication instance

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Email input field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = ""
                },
                label = { Text("Enter your email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("emailTextField")
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Password input field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = ""
                },
                label = { Text("Enter your password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("passwordTextField")
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Show error message if authentication fails
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login button
            Button(
                onClick = {
                    if (email.trim().isEmpty() || password.trim().isEmpty()) {
                        errorMessage = "Please fill in both fields"
                    } else {
                        isLoading = true
                        auth.signInWithEmailAndPassword(email.trim(), password.trim())
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    Log.d("LoginScreen", "Login successful")
                                    navController.navigate("personalised_screen/${email.trim()}")
                                } else {
                                    errorMessage = "Authentication failed: ${task.exception?.message}"
                                    Log.e("LoginScreen", "Login failed: ${task.exception?.message}")
                                }
                            }
                    }
                },
                modifier = Modifier.testTag("loginButton"),
                enabled = !isLoading // Disable button while loading
            ) {
                Text(if (isLoading) "Logging in..." else "Log In")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Created by ZAPJOLLY for INF2007 at SIT",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}
