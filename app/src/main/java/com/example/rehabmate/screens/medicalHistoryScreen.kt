package com.example.rehabmate.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.google.firebase.firestore.FirebaseFirestore
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
            .fillMaxSize()
            .background(Color(0xFF181818))
            .padding(16.dp)
    ) {
        // Top Bar with proper back icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Medical History",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Text(
            text = "An overview of your medical history",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // History List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(medicalHistory) { historyItem ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = historyItem,
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
