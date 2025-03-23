package com.example.rehabmate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.rehabmate.ui.theme.blue_color
import com.example.rehabmate.ui.theme.red_color

@Composable
fun WelcomeScreen(navController: NavHostController) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to", style = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp,
                    ), textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "RehabMate", style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp,
                        color = blue_color,
                        letterSpacing = 2.sp
                    ), textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = blue_color),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        // Navigate to Login screen
                        navController.navigate("login_screen")
                    }) {
                    Text(text = "Sign Up".uppercase(), modifier = Modifier.padding(10.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = red_color),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        // Navigate to Sign-up screen (if intended)
                        navController.navigate("register_screen")
                    }) {
                    Text(text = "register".uppercase(), modifier = Modifier.padding(10.dp))
                }
            }
        }
    }
}
