package com.example.rehabmate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val userName = currentUser?.displayName ?: "JOHN LIM"
    val userEmail = currentUser?.email ?: "johnlim@gmail.com"
    val userPhone = "0412 111 222" // This would come from Firestore in a real app

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Profile Header with background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2196F3))
                .padding(16.dp)
        ) {
            // Back button
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clickable { navController.popBackStack() }
                    .size(24.dp))

            // Header title
            Text(
                text = "MY PROFILE",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
            )

            // Profile content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            ) {
                // Profile picture
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    // Profile image placeholder
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        tint = Color.Red,
                        modifier = Modifier.size(60.dp)
                    )
                    Text(
                        text = "Profile\nPicture",
                        color = Color.Red,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User name
                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // User email
                Text(
                    text = userEmail, fontSize = 14.sp, color = Color.White
                )

                // User phone
                Text(
                    text = userPhone, fontSize = 14.sp, color = Color.White
                )
            }
        }

        // Stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(title = "Total Exercise", value = "157")
            StatItem(title = "Years Old", value = "27")
            StatItem(title = "XXXX", value = "XX")
        }

        // Menu options
        MenuOption(
            icon = Icons.Default.Person,
            title = "Edit Profile",
            onClick = { navController.navigate("editProfile_screen") })

        MenuOption(
            icon = Icons.Default.Person,
            title = "View Appointment",
            onClick = { navController.navigate("appointment_screen") })

        MenuOption(
            icon = Icons.Default.Person,
            title = "All Exercise History",
            onClick = { /* Navigate to exercise history */ })

        MenuOption(
            icon = Icons.Default.Person,
            title = "Your Medical Records",
            onClick = { /* Navigate to medical records */ })

        MenuOption(
            icon = Icons.Default.Person,
            title = "About App",
            onClick = { navController.navigate("about_app_screen") })

        // Logout option
        MenuOption(
            icon = Icons.Default.Person, title = "Logout", tint = Color.Red, onClick = {
                auth.signOut()
                navController.navigate("welcome_screen") {
                    popUpTo("welcome_screen") { inclusive = true }
                }
            })
    }
}

@Composable
fun StatItem(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color(0xFFE53935), shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White
        )
        Text(
            text = title, fontSize = 12.sp, color = Color.White
        )
    }
}

@Composable
fun MenuOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Icon in a circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }

        // Menu title
        Text(
            text = title,
            fontSize = 16.sp,
            color = tint,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )

        // Forward arrow
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Go to $title",
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }

    Divider(color = Color.DarkGray, thickness = 1.dp)
}