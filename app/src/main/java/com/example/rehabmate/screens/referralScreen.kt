package com.example.rehabmate.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.Timestamp
import java.util.*

@Composable
fun ReferralScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()

    var referralCode by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Referral Code",
            fontSize = 24.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = referralCode,
            onValueChange = { referralCode = it },
            label = { Text("Enter Referral Code") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedContainerColor = Color.DarkGray,
                focusedContainerColor = Color.DarkGray,
                cursorColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (successMessage.isNotEmpty()) {
            Text(text = successMessage, color = Color.Green)
        }
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (referralCode.isEmpty()) {
                    errorMessage = "Please enter a referral code."
                    successMessage = ""
                    return@Button
                }
                if (currentUser == null) {
                    errorMessage = "User not logged in."
                    successMessage = ""
                    return@Button
                }

                isLoading = true
                errorMessage = ""
                successMessage = ""

                // Step 1: Check if referralCode exists in "Exercises"
                db.collection("Exercises").document(referralCode).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // Step 2: If code is valid, add it to user_info
                            val userId = currentUser.uid
                            val userRef = db.collection("user_info").document(userId)

                            val newActivity = hashMapOf(
                                "code" to referralCode,
                                "startDate" to Timestamp(Date()), // Current date
                                "endDate" to Timestamp(Calendar.getInstance().apply {
                                    add(Calendar.MONTH, 1) // +1 month
                                }.time),
                                "status" to "Progress"
                            )

                            userRef.update("Activity", FieldValue.arrayUnion(newActivity))
                                .addOnSuccessListener {
                                    isLoading = false
                                    successMessage = "Referral code added successfully!"
                                    errorMessage = ""
                                }
                                .addOnFailureListener { error ->
                                    isLoading = false
                                    errorMessage = "Error adding referral: ${error.message}"
                                    successMessage = ""
                                }
                        } else {
                            // If referral code doesn't exist
                            isLoading = false
                            errorMessage = "Invalid referral code! Please enter a valid code."
                            successMessage = ""
                        }
                    }
                    .addOnFailureListener { error ->
                        isLoading = false
                        errorMessage = "Error checking referral code: ${error.message}"
                        successMessage = ""
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Submit", color = Color.White)
            }
        }
    }
}