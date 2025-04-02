package com.example.rehabmate.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import com.example.rehabmate.ui.theme.blue_color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun splash_screen(navController: NavHostController) {
    // Animation states
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(targetValue = if (startAnimation) 1f else 0f, label = "")
    val scaleAnim = animateFloatAsState(targetValue = if (startAnimation) 1f else 0.8f, label = "")

    // Start animation and navigate after delay
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000) // display duration
        navController.navigate("welcome_screen") {
            popUpTo("splash_screen") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(blue_color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "RehabMate",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .graphicsLayer(
                    alpha = alphaAnim.value,
                    scaleX = scaleAnim.value,
                    scaleY = scaleAnim.value
                )
        )
    }
}
