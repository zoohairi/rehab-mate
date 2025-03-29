package com.example.rehabmate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
fun FavoritesScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Top App Bar
        TopFavoritesBar(
            modifier = Modifier.fillMaxWidth(),
            navController = navController
        )

        // Main Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            FavoritesListContent(navController)
        }

        // Bottom Navigation
        FavoritesBottomNavigation(navController)
    }
}

@Composable
fun TopFavoritesBar(
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
                tint = Color(0xFFFF9800),
                modifier = Modifier
                    .clickable { /* Handle back navigation */ }
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Favorites",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
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
fun FavoritesListContent(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Sort by options
        SortByRow()

        Spacer(modifier = Modifier.height(16.dp))

        // Favorites categories list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                listOf(
                    FavoriteCategory(
                        title = "Upper Body",
                        exercises = "5 Exercises",
                        duration = "20 Min",
                        calories = "150 Cal"
                    ),
                    FavoriteCategory(
                        title = "Boost Energy And Vitality",
                        exercises = "7 Exercises",
                        duration = "30 Min",
                        calories = "220 Cal",
                        description = "Everyday physical and mental exercises to boost your energy"
                    ),
                    FavoriteCategory(
                        title = "Pull Out",
                        exercises = "10 Exercises",
                        duration = "45 Min",
                        calories = "280 Cal"
                    ),
                    FavoriteCategory(
                        title = "Lower Body Blast",
                        exercises = "8 Exercises",
                        duration = "35 Min",
                        calories = "250 Cal",
                        description = "A lower body blast is a great way to build your leg strength and improve overall fitness"
                    ),
                    FavoriteCategory(
                        title = "Avocado And Egg Toast",
                        exercises = "2 Exercises",
                        duration = "10 Min",
                        calories = "80 Cal"
                    )
                )
            ) { category ->
                FavoriteCategoryItem(category = category)
            }
        }
    }
}

@Composable
fun SortByRow() {
    var selectedSort by remember { mutableStateOf("Best match") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Sort by:",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(end = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortChip(
                text = "Best match",
                isSelected = selectedSort == "Best match",
                onClick = { selectedSort = "Best match" }
            )

            SortChip(
                text = "Recent",
                isSelected = selectedSort == "Recent",
                onClick = { selectedSort = "Recent" }
            )

            SortChip(
                text = "Duration",
                isSelected = selectedSort == "Duration",
                onClick = { selectedSort = "Duration" }
            )
        }
    }
}

@Composable
fun SortChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(
                if (isSelected) Color(0xFFFF9800) else Color(0xFF303030)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

data class FavoriteCategory(
    val title: String,
    val exercises: String,
    val duration: String,
    val calories: String,
    val description: String? = null
)

@Composable
fun FavoriteCategoryItem(category: FavoriteCategory) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle category click */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Exercise image placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF303030)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Exercise details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = category.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (category.description != null) {
                    Text(
                        text = category.description,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Exercise stats as simple text
                Text(
                    text = "${category.exercises} • ${category.duration} • ${category.calories}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Favorite icon
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Favorite",
                tint = Color.Yellow,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp)
                    .clickable { /* Handle favorite */ }
            )
        }
    }
}

@Composable
fun FavoritesBottomNavigation(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BottomNavItem(
            icon = Icons.Default.Home,
            label = "Home",
            isSelected = false,
            onClick = { /* Handle navigation */ }
        )

        BottomNavItem(
            icon = Icons.Default.Star,
            label = "Plan",
            isSelected = false,
            onClick = { /* Handle navigation */ }
        )

        BottomNavItem(
            icon = Icons.Default.Star,
            label = "Favorites",
            isSelected = true,
            onClick = { /* Handle navigation */ }
        )

        BottomNavItem(
            icon = Icons.Default.Person,
            label = "Profile",
            isSelected = false,
            onClick = { /* Handle navigation */ }
        )
    }
}

@Composable
fun BottomNavItem(
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