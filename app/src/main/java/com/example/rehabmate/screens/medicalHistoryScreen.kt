package com.example.rehabmate.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.example.rehabmate.firebase.fetchUserInfo
import com.example.rehabmate.firebase.getUidFromSharedPreferences

@Composable
fun medicalHistoryScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val uid = getUidFromSharedPreferences(context)
    val medicalHistory = remember { mutableStateListOf<String>() }

    LaunchedEffect(uid) {
        uid?.let { safeUid ->
            db.collection("user_info").document(safeUid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userData = document.data

                        // Retrive medical history list
                        val profile = userData?.get("profile") as? Map<*, *>
                        val historyList = profile?.get("medical_history") as? List<*> ?: emptyList<String>()

                        medicalHistory.clear()
                        medicalHistory.addAll(historyList.map { it.toString() })
                    } else {
                        Log.e("MEDICAL", "No user found with this UID")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("MEDICAL", "Error fetching user data: ${exception.message}")
                }
        } ?: Log.e("MEDICAL", "UID is null, cannot fetch data")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.Black)
            .padding(16.dp)
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = "<",
            fontSize = 24.sp,
            modifier = Modifier
                .clickable { navController.popBackStack() },
            color = Color.White
        )

        Text(
            "Medical History",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "An overview of your medical history",
            fontSize = 16.sp,
            color = Color.White
        )

        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
            items(medicalHistory) { historyItem ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = historyItem,
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}