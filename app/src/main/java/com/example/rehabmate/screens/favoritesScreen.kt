package com.example.rehabmate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun FavoritesScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF181818))
    ) {
        TopFavoritesBar(navController = navController)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            FavoritesListContent()
        }

        FavoritesBottomNavigation(navController)
    }
}

@Composable
fun TopFavoritesBar(navController: NavHostController) {
    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        color = Color(0xFF2196F3),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .clickable { navController.popBackStack() }
                        .size(26.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Favorites",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White, modifier = Modifier.size(22.dp))
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(22.dp))
                Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
fun FavoritesListContent() {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        SortByRow()

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            items(
                listOf(
                    FavoriteCategory("Upper Body", "5 Exercises", "20 Min", "150 Cal"),
                    FavoriteCategory("Boost Energy And Vitality", "7 Exercises", "30 Min", "220 Cal", "Everyday exercises to boost your energy"),
                    FavoriteCategory("Pull Out", "10 Exercises", "45 Min", "280 Cal"),
                    FavoriteCategory("Lower Body Blast", "8 Exercises", "35 Min", "250 Cal", "Strengthen your lower body effectively"),
                    FavoriteCategory("Avocado And Egg Toast", "2 Exercises", "10 Min", "80 Cal")
                )
            ) { category ->
                FavoriteCategoryItem(category)
            }
        }
    }
}

@Composable
fun SortByRow() {
    var selectedSort by remember { mutableStateOf("Best match") }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Sort by:", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Best match", "Recent", "Duration").forEach { sortOption ->
                SortChip(sortOption, selectedSort == sortOption) { selectedSort = sortOption }
            }
        }
    }
}

@Composable
fun SortChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (isSelected) Color(0xFF2196F3) else Color(0xFF303030))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

data class FavoriteCategory(val title: String, val exercises: String, val duration: String, val calories: String, val description: String? = null)

@Composable
fun FavoriteCategoryItem(category: FavoriteCategory) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF303030)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Column(
                modifier = Modifier.weight(1f).padding(start = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(category.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (!category.description.isNullOrEmpty()) {
                    Text(category.description, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                }
                Text("${category.exercises} • ${category.duration} • ${category.calories}", fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.Star, contentDescription = "Favorite", tint = Color.Yellow, modifier = Modifier.padding(start = 8.dp).size(24.dp).clickable { })
        }
    }
}

@Composable
fun FavoritesBottomNavigation(navController: NavHostController) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF1E1E1E)).padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BottomNavItem(Icons.Default.Home, "Home", false) {}
        BottomNavItem(Icons.Default.Star, "Plan", false) {}
        BottomNavItem(Icons.Default.Star, "Favorites", true) {}
        BottomNavItem(Icons.Default.Person, "Profile", false) {}
    }
}

@Composable
fun BottomNavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(4.dp)) {
        Icon(icon, contentDescription = label, tint = if (isSelected) Color.White else Color.Gray, modifier = Modifier.size(24.dp))
        Text(label, fontSize = 12.sp, color = if (isSelected) Color.White else Color.Gray)
    }
}
