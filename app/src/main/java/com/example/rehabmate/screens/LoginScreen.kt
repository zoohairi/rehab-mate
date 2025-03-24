package com.example.rehabmate.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Back Button
            Text(
                text = "<",
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "LOG IN ACCOUNT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Welcome Text
                Text(
                    text = "Welcome",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean sed arcu eget quam sagittis vehicula in eu orci.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Input Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF4285F4), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    // Email Field
                    TextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = "" },
                        placeholder = { Text("example@example.com") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("emailTextField"),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    TextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = "" },
                        placeholder = { Text("********") },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("passwordTextField"),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Error Message
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Login Button
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
                                        navController.navigate("personalised_screen/${'$'}email")
                                    } else {
                                        errorMessage = "Authentication failed: ${'$'}{task.exception?.message}"
                                        Log.e("LoginScreen", "Login failed: ${'$'}{task.exception?.message}")
                                    }
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp).testTag("loginButton"),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(Color(0xFFE53935)) // Red color
                ) {
                    Text(
                        text = if (isLoading) "Logging in..." else "LOG IN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sign Up Link
                Text(
                    text = "Don't have an account? Sign up here",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable { /* Navigate to Sign Up */ }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen(navController = rememberNavController())
}