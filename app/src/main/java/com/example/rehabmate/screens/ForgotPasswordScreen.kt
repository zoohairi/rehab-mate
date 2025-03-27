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
fun ForgotPasswordScreen(navController: NavHostController) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
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
                    text = "FORGOTTEN PASSWORD",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Header Text
                Text(
                    text = "Forget Password?",
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emailTextField"),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // New Password Field
                    TextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; errorMessage = "" },
                        placeholder = { Text("************") },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("newPasswordTextField"),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirm Password Field
                    TextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = "" },
                        placeholder = { Text("************") },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirmPasswordTextField"),
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

                // Change Password Button
                Button(
                    onClick = {
                        if (email.trim().isEmpty() || newPassword.trim()
                                .isEmpty() || confirmPassword.trim().isEmpty()
                        ) {
                            errorMessage = "Please fill in all fields"
                        } else if (newPassword != confirmPassword) {
                            errorMessage = "Passwords do not match"
                        } else {
                            isLoading = true
                            auth.sendPasswordResetEmail(email.trim())
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        Log.d("ForgotPasswordScreen", "Password reset email sent")
                                        navController.navigate("login")
                                    } else {
                                        errorMessage = "Error: ${'$'}{task.exception?.message}"
                                        Log.e(
                                            "ForgotPasswordScreen",
                                            "Error: ${'$'}{task.exception?.message}"
                                        )
                                    }
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                        .testTag("changePasswordButton"),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(Color(0xFFE53935)) // Red color
                ) {
                    Text(
                        text = if (isLoading) "Processing..." else "CHANGE PASSWORD",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewForgotPasswordScreen() {
    ForgotPasswordScreen(navController = rememberNavController())
}