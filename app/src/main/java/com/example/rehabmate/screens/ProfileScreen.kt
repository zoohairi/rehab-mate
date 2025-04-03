package com.example.rehabmate.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.rehabmate.firebase.fetchUserInfo
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid

    var name by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf("Loading...") }
    var phone by remember { mutableStateOf("-") }
    var birthDate by remember { mutableStateOf("-") }
    var completedActivities by remember { mutableStateOf(0) }
    var inProgressActivities by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid) {
        uid?.let {
            fetchUserInfo(it).onSuccess { data ->
                Log.d("ProfileScreen", "User data: $data")
                val profile = data["profile"] as? Map<String, Any> ?: emptyMap()
                name = profile["name"] as? String ?: "No Name" // <-- correct
                email = data["email"] as? String ?: "No Email"
                phone = profile["phone_number"]?.toString() ?: "-"
                birthDate = profile["date"] as? String ?: "-"

                val activities = data["Activity"] as? List<Map<String, Any>> ?: emptyList()
                completedActivities = activities.count { it["status"] == "closed" }
                inProgressActivities = activities.count { it["status"] == "Progress" }

            }.onFailure {
                error = it.message ?: "Unknown error"
            }
        } ?: run {
            error = "User not logged in"
        }

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .verticalScroll(rememberScrollState()) // Enables scrolling
    ) {

        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2196F3))
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clickable { navController.popBackStack() }
                    .size(24.dp)
            )

            Text(
                text = "MY PROFILE",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
            )
        }

        // Profile Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
        ) {
            // Initials Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                Text(
                    text = name.split(" ").filter { it.isNotEmpty() }
                        .map { it.first() }.joinToString("").take(2).uppercase(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = email, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            Text(text = "Phone: $phone", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            Text(text = "DOB: $birthDate", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
        }

        if (error != null) {
            Text("Error: $error", color = Color.Red, modifier = Modifier.padding(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Activity Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActivityStat(title = "Completed Exercises", value = completedActivities.toString())
            ActivityStat(title = "Ongoing Exercises", value = inProgressActivities.toString())
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Menu Options
        EnhancedMenuOption(
            Icons.Default.Person,
            "Edit Profile"
        ) { navController.navigate("editProfile_screen") }
        EnhancedMenuOption(
            Icons.Default.DateRange,
            "View Appointment"
        ) { navController.navigate("appointment_screen") }
        EnhancedMenuOption(
            Icons.Default.Info,
            "Your Medical Records"
        ) { navController.navigate("medical_records_screen") }
        EnhancedMenuOption(
            Icons.Default.Info,
            "About App"
        ) { navController.navigate("about_app_screen") }
        EnhancedMenuOption(Icons.AutoMirrored.Filled.ExitToApp, "Logout", tint = Color.Red) {
            auth.signOut()
            navController.navigate("welcome_screen") {
                popUpTo("welcome_screen") { inclusive = true }
            }
        }
    }
}


@Composable
fun ActivityStat(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color(0xFF1E88E5), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = title, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
fun StatItem(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color(0xFFE53935), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
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
fun EnhancedMenuOption(
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
            .background(
                color = Color(0xFF1E1E1E), // Dark background
                shape = RoundedCornerShape(12.dp) // Rounded corners
            )
            .padding(16.dp) // Inner padding for content
    ) {
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

        Spacer(modifier = Modifier.width(16.dp)) // Adding space between the icon and text

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = tint,
            modifier = Modifier.weight(1f) // Ensuring text takes the available space
        )

        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Go to $title",
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }

    // Divider with a lighter color
    Divider(color = Color(0xFF2A2A2A), thickness = 1.dp)
}