package com.example.rehabmate.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.rehabmate.firebase.fetchUserInfo
import com.example.rehabmate.firebase.updateUserProfile
import com.example.rehabmate.ui.theme.blue_color
import com.example.rehabmate.ui.theme.green_color
import com.example.rehabmate.ui.theme.red_color
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun editprofileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var uid: String? = null
    /// mutableState variables for user information
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }

    if (currentUser != null) {
        uid = currentUser.uid
        Log.d("User UID", "The UID of the current user is: $uid")
    } else {
        Log.d("User UID", "No user is currently logged in.")
    }

    // function to fetch user info
    LaunchedEffect(uid) {
        fetchUserInfo(uid.toString(), onSuccess = { userData, exerciseCodes ->
            Log.d("userinfo", userData.toString())

            // Update state variables with the fetched data
            val profile = userData["profile"] as? Map<String, Any> ?: emptyMap()

            //  extract values from the profile map
            name = userData["name"] as? String ?: "Not stated"
            email = userData["email"] as? String ?: "Not stated"
            phone = profile["phone_number"] as? String ?: "Not stated"
            address = profile["address"] as? String ?: "Not stated"
            birthDate = profile["date"] as? String ?: "Not stated"
        }, onFailure = { errorMessage ->
            Log.e("UserInfo", errorMessage)
        })
    }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(blue_color)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { navController.popBackStack() }
                    .size(24.dp))

            Text(
                text = "EDIT PROFILE",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Form Fields
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (successMessage.isNotEmpty()) {
                Text(
                    text = successMessage,
                    color = green_color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = red_color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name Field
            Text("Full Name", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedContainerColor = Color.DarkGray,
                    focusedContainerColor = Color.DarkGray,
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Email Field
            Text("Email", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            TextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedContainerColor = Color.DarkGray,
                    focusedContainerColor = Color.DarkGray,
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = false // Email can't be changed
            )

            // Phone Field
            Text(
                "Phone Number", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold
            )
            TextField(
                value = phone,
                onValueChange = { phone = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedContainerColor = Color.DarkGray,
                    focusedContainerColor = Color.DarkGray,
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Address Field
            Text("Address", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            TextField(
                value = address,
                onValueChange = { address = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedContainerColor = Color.DarkGray,
                    focusedContainerColor = Color.DarkGray,
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Birth Date Field
            Text(
                "Date of Birth", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold
            )
            TextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedContainerColor = Color.DarkGray,
                    focusedContainerColor = Color.DarkGray,
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = ""
                    successMessage = ""

                    val uid = currentUser?.uid
                    if (uid != null) {
                        updateUserProfile(
                            uid = uid,
                            name = name,
                            email = email,
                            address = address,
                            date = birthDate,
                            phoneNumber = phone.toIntOrNull() ?: 0,
                            onSuccess = {
                                isLoading = false
                                successMessage = "Profile updated successfully"
                            },
                            onFailure = { error ->
                                isLoading = false
                                errorMessage = error
                            })
                    } else {
                        isLoading = false
                        errorMessage = "User not logged in"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = red_color),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White, modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("SAVE CHANGES", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
