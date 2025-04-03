package com.example.rehabmate.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.rehabmate.R
import com.example.rehabmate.firebase.fetchUserInfo
import com.example.rehabmate.firebase.updateUserProfile
import com.example.rehabmate.ui.theme.blue_color
import com.example.rehabmate.ui.theme.green_color
import com.example.rehabmate.ui.theme.red_color
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.log

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


    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (!Places.isInitialized()) {
            val apiKey = context.getString(R.string.google_api_key)
            Places.initialize(context, apiKey)
        }
    }

    LaunchedEffect(uid) {
        uid?.let { safeUid ->
            fetchUserInfo(safeUid).onSuccess { userData ->
                Log.d("UserDataaaa", userData.toString())
                val profile = userData["profile"] as? Map<String, Any> ?: emptyMap()
                name = profile["name"] as? String ?: ""
                email = userData["email"] as? String ?: ""
                phone = (profile["phone_number"] as? Int)?.toString() ?: (profile["phone_number"] as? String ?: "")
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = blue_color,
                    titleContentColor = Color.White
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
                    color = green_color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(green_color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = red_color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(red_color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Full Name", fontWeight = FontWeight.Bold)
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5)
                        )
                    )

                    Text("Email", fontWeight = FontWeight.Bold)
                    TextField(
                        value = email,
                        onValueChange = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            disabledContainerColor = Color(0xFFF5F5F5)
                        )
                    )

                    Text("Phone Number", fontWeight = FontWeight.Bold)
                    TextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5)
                        )
                    )

                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        if (result.resultCode == Activity.RESULT_OK) {
                            val place = Autocomplete.getPlaceFromIntent(result.data!!)
                            address = place.address ?: ""
                        }
                    }

                    Text("Address", fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF5F5F5))
                            .clickable {
                                val intent = Autocomplete.IntentBuilder(
                                    AutocompleteActivityMode.OVERLAY,
                                    listOf(Place.Field.ID, Place.Field.ADDRESS)
                                ).build(context)
                                launcher.launch(intent)
                            }
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (address.isEmpty()) "Pick Address" else address,
                            color = if (address.isEmpty()) Color.Gray else Color.Black
                        )
                    }

                    Text("Date of Birth", fontWeight = FontWeight.Bold)
                    TextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5)
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
                colors = ButtonDefaults.buttonColors(containerColor = green_color),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("SAVE CHANGES", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
