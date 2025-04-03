package com.example.rehabmate.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.rehabmate.firebase.fetchUserInfo
import com.example.rehabmate.firebase.updateUserProfile
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun editprofileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var uid by remember { mutableStateOf(currentUser?.uid) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val colors = MaterialTheme.colorScheme

    LaunchedEffect(uid) {
        uid?.let { safeUid ->
            fetchUserInfo(safeUid).onSuccess { userData ->
                val profile = userData["profile"] as? Map<String, Any> ?: emptyMap()
                name = profile["name"] as? String ?: ""
                email = userData["email"] as? String ?: ""
                phone = (profile["phone_number"] as? Int)?.toString()
                    ?: (profile["phone_number"] as? String ?: "")
                address = profile["address"] as? String ?: ""
                birthDate = profile["date"] as? String ?: ""
            }.onFailure {
                errorMessage = "Failed to load user data."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = colors.primary,
                    titleContentColor = colors.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (successMessage.isNotEmpty()) {
                Text(
                    text = successMessage,
                    color = colors.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = colors.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.error.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Full Name", fontWeight = FontWeight.Bold, color = colors.onSurface)
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colors.surface,
                            focusedContainerColor = colors.surface
                        )
                    )

                    Text("Email", fontWeight = FontWeight.Bold, color = colors.onSurface)
                    TextField(
                        value = email,
                        onValueChange = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            disabledContainerColor = colors.surface
                        )
                    )

                    Text("Phone Number", fontWeight = FontWeight.Bold, color = colors.onSurface)
                    TextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colors.surface,
                            focusedContainerColor = colors.surface
                        )
                    )

                    Text("Address", fontWeight = FontWeight.Bold, color = colors.onSurface)
                    TextField(
                        value = address,
                        onValueChange = { address = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colors.surface,
                            focusedContainerColor = colors.surface
                        )
                    )

                    Text("Date of Birth", fontWeight = FontWeight.Bold, color = colors.onSurface)
                    TextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colors.surface,
                            focusedContainerColor = colors.surface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isLoading = true
                    errorMessage = ""
                    successMessage = ""
                    uid?.let { safeUid ->
                        updateUserProfile(
                            uid = safeUid,
                            name = name,
                            email = email,
                            address = address,
                            date = birthDate,
                            phoneNumber = (phone.toIntOrNull() ?: 0).toString(),
                            onSuccess = {
                                isLoading = false
                                successMessage = "Profile updated successfully"
                            },
                            onFailure = { error ->
                                isLoading = false
                                errorMessage = error
                            }
                        )
                    } ?: run {
                        isLoading = false
                        errorMessage = "User not logged in"
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = colors.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("SAVE CHANGES", fontWeight = FontWeight.Bold, color = colors.onPrimary)
                }
            }
        }
    }
}
