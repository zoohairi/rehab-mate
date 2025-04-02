package com.example.rehabmate.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.rehabmate.firebase.loginUser
import com.google.firebase.auth.FirebaseAuth
import com.example.rehabmate.firebase.storeUidInSharedPreferences
import com.example.rehabmate.ui.theme.blue_color
import com.example.rehabmate.ui.theme.red_color

// Define these colors at the top of the file if not defined in your theme
val primaryColor = Color(0xFF4257B2)
val backgroundColor = Color(0xFF121212)
val surfaceColor = Color(0xFF1E1E1E)
val textPrimaryColor = Color.White
val textSecondaryColor = Color(0xFFB0B0B0)
val accentColor = Color(0xFFFFA726)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Get context for SharedPreferences
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(backgroundColor, Color(0xFF1A1A2E))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Back Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 32.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(surfaceColor.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = textPrimaryColor
                    )
                }
            }

            // Main content
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Logo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(primaryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RM",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = textPrimaryColor,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "Log in to your RehabMate account",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textSecondaryColor
                    ),
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Input Form
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = surfaceColor
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = "" },
                            placeholder = { Text("Email Address", color = textSecondaryColor) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = textSecondaryColor
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .testTag("emailTextField"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFF2A2A2A),
                                focusedContainerColor = Color(0xFF2A2A2A),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = primaryColor
                            ),
                            singleLine = true
                        )

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = "" },
                            placeholder = { Text("Password", color = textSecondaryColor) },
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = textSecondaryColor
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("passwordTextField"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFF2A2A2A),
                                focusedContainerColor = Color(0xFF2A2A2A),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = primaryColor
                            ),
                            singleLine = true
                        )

                        // Forgot Password Link
                        Text(
                            text = "Forgot Password?",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = accentColor,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable { navController.navigate("forgot_password_screen") }
                                .padding(top = 8.dp, bottom = 24.dp)
                        )

                        // Error Message
                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = red_color
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )
                        }

                        // Login Button
                        Button(
                            onClick = {
                                if (email.trim().isEmpty() || password.trim().isEmpty()) {
                                    errorMessage = "Please fill in both fields"
                                } else {
                                    isLoading = true
                                    loginUser(email.trim(), password.trim(), onSuccess = { uid ->
                                        storeUidInSharedPreferences(uid, context)
                                        Log.d("LoginScreen", "Login successful")
                                        navController.navigate("dashboard_screen")
                                    }, onFailure = { error ->
                                        isLoading = false
                                        errorMessage = error
                                        Log.e("LoginScreen", error)
                                    })
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("loginButton"),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                disabledContainerColor = primaryColor.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "LOG IN",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Sign Up Link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textSecondaryColor
                        )
                    )

                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = blue_color,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clickable { navController.navigate("register_screen") }
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen(navController = rememberNavController())
}