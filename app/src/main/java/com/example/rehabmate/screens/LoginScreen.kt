package com.example.rehabmate.screens

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.rehabmate.firebase.loginUser
import com.example.rehabmate.firebase.storeUidInSharedPreferences
import com.example.rehabmate.ui.theme.blue_color
import com.example.rehabmate.ui.theme.red_color
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    val auth = remember { FirebaseAuth.getInstance() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

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
                Text(
                    text = "LOG IN ACCOUNT",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Form Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF334460)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        TextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = "" },
                            placeholder = { Text("example@example.com") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = "" },
                            placeholder = { Text("********") },
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }

                Text(
                    text = "Forgot Password?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = blue_color,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp, end = 4.dp)
                        .clickable { navController.navigate("forgot_password_screen") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = red_color,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                val animatedColor by animateColorAsState(
                    if (isLoading) Color.Gray else Color(0xFFE53935),
                    label = ""
                )

                Button(
                    onClick = {
                        if (email.trim().isEmpty() || password.trim().isEmpty()) {
                            errorMessage = "Please fill in both fields"
                        } else {
                            isLoading = true
                            loginUser(email.trim(), password.trim(), onSuccess = { uid ->
                                storeUidInSharedPreferences(uid, context)
                                navController.navigate("dashboard_screen")
                            }, onFailure = { error ->
                                isLoading = false
                                errorMessage = error
                            })
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(animatedColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "LOG IN",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Don't have an account? Sign up here",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = blue_color,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable(
                        onClick = { navController.navigate("register_screen") },
                        indication = rememberRipple(bounded = true),
                        interactionSource = remember { MutableInteractionSource() }
                    )
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