package com.example.rehabmate.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun medicalHistoryScreen(navController: NavHostController) {
    // Your UI content goes here
    Text("Medical History", fontSize = 20.sp, fontWeight = FontWeight.Bold)
}
