package com.example.rehabmate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun BeginnerExerciseScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Top App Bar
        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
            navController = navController
        )

        // Main Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Exercise List Screen displayed in the sketch
            ExerciseListScreen(navController)
        }

        // Bottom Navigation
        BottomNavigation(navController)
    }
}

@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF6200EE),
                modifier = Modifier
                    .clickable { /* Handle back navigation */ }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Beginner",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EE)
            )
        }

        Row {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { /* Handle search */ }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { /* Handle notifications */ }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { /* Handle profile */ }
            )
        }
    }
}

@Composable
fun ExerciseListScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Exercise Difficulty Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("Beginner", "Intermediate", "Advanced")) { difficulty ->
                DifficultyChip(
                    text = difficulty,
                    isSelected = difficulty == "Beginner",
                    onClick = { /* Handle difficulty selection */ }
                )
            }
        }

        // Featured Exercise Card
        FeaturedExerciseCard(
            title = "Exercise History",
            onClick = { /* Handle featured exercise click */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Round 1 Header
        Text(
            text = "Round 1",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Round 1 Exercises
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(0.5f)
        ) {
            items(
                listOf(
                    ExerciseData("Dumbbell Rows", 1),
                    ExerciseData("Russian Twists", 2),
                    ExerciseData("Squats", 3)
                )
            ) { exercise ->
                RoundExerciseItem(
                    exercise = exercise,
                    onClick = { /* Handle exercise click */ }
                )
            }
        }

        // Round 2 Header
        Text(
            text = "Round 2",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Round 2 Exercises
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(0.5f)
        ) {
            items(
                listOf(
                    ExerciseData("Tabata Intervals", 1),
                    ExerciseData("Bicycle Crunches", 2)
                )
            ) { exercise ->
                RoundExerciseItem(
                    exercise = exercise,
                    onClick = { /* Handle exercise click */ }
                )
            }
        }
    }
}

@Composable
fun DifficultyChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(
                if (isSelected) Color(0xFFB7FF59) else Color(0xFF303030)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun FeaturedExerciseCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF303030))
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Badge in the top right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFB7FF59))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Exercise History",
                    fontSize = 10.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            // Star icon in the bottom right
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Favorite",
                tint = Color.Yellow,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .clickable { /* Handle favorite */ }
            )

            // Exercise information in the bottom left
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Full-Body Training",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "30 Minutes",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Divider(
                        modifier = Modifier
                            .height(12.dp)
                            .width(1.dp),
                        color = Color.Gray
                    )

                    Text(
                        text = "5 Exercises",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

data class ExerciseData(
    val name: String,
    val repetition: Int
)

@Composable
fun RoundExerciseItem(
    exercise: ExerciseData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF303030))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise circle icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6200EE)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = exercise.repetition.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Exercise name
            Text(
                text = exercise.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            )

            // Repetition info
            Text(
                text = "Repetition ${exercise.repetition}",
                fontSize = 14.sp,
                color = Color(0xFF6200EE)
            )
        }
    }
}

@Composable
fun BottomNavigation(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BottomNavigationItem(
            icon = Icons.Default.Home,
            label = "Home",
            isSelected = false,
            onClick = { /* Handle navigation */ }
        )

        BottomNavigationItem(
            icon = Icons.Default.DateRange,
            label = "Plan",
            isSelected = false,
            onClick = { /* Handle navigation */ }
        )

        BottomNavigationItem(
            icon = Icons.Default.Star,
            label = "Favorites",
            isSelected = true,
            onClick = { /* Handle navigation */ }
        )

        BottomNavigationItem(
            icon = Icons.Default.Person,
            label = "Profile",
            isSelected = false,
            onClick = { /* Handle navigation */ }
        )
    }
}

@Composable
fun BottomNavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color.White else Color.Gray,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) Color.White else Color.Gray
        )
    }
}