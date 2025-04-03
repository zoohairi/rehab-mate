package com.example.rehabmate

import android.os.Bundle
import androidx.compose.ui.platform.LocalContext
import org.osmdroid.config.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.rehabmate.firebase.fetchUserAppointmentsAndUpdateState
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Data classes
data class DoctorInfo(
    val name: String,
    val specialty: String,
    val location: String,
    val latLng: GeoPoint? // Latitude and Longitude (GeoPoint)
)

data class AppointmentData(
    val id: String,
    val doctors: List<DoctorInfo>,
    val date: String,
    val time: String,
    val location: String
)

@Composable
fun AppointmentScreen(navController: NavHostController) {
    var appointments by remember { mutableStateOf<List<AppointmentData>>(listOf()) }
    Configuration.getInstance().userAgentValue = "YourCustomUserAgent/1.0"

    var isLoading by remember { mutableStateOf(true) }
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(uid) {
        if (uid.isNullOrEmpty()) {
            Log.e("AppointmentScreen", "User not logged in")
            isLoading = false
            return@LaunchedEffect
        }

        fetchUserAppointmentsAndUpdateState(uid, { appointmentList ->
            appointments = appointmentList
            isLoading = false
        }, { error ->
            Log.e("AppointmentScreen", error)
            isLoading = false
        })
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFB7FF59))
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // --- Top Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFB7FF59),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { navController.popBackStack() }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Appointments",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // --- List ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(appointments.size) { index ->
                    AppointmentCard(appointment = appointments[index])
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(appointment: AppointmentData) {
    val context = LocalContext.current
    val today = Calendar.getInstance().time
    val appointmentDate =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(appointment.date)
    val daysDifference = (appointmentDate?.time ?: 0 - today.time) / (1000 * 60 * 60 * 24)

    val appointmentStatus = when {
        daysDifference == 0L -> "Scheduled for today"
        daysDifference > 0 -> "Upcoming appointment"
        else -> "Appointment has passed"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- Doctor Info ---
            appointment.doctors.forEachIndexed { index, doctor ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${index + 1}.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB7FF59),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = "Dr. ${doctor.name}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = doctor.specialty,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Map ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E2E))
            ) {
                Box(Modifier.fillMaxSize()) {
                    val mapView = remember { MapView(context) }
                    mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                    mapView.controller.setZoom(13)
                    mapView.controller.setCenter(GeoPoint(1.352083, 103.819839))

                    appointment.doctors.forEach { doctor ->
                        doctor.latLng?.let { geoPoint ->
                            val marker = Marker(mapView)
                            marker.position = GeoPoint(geoPoint.latitude, geoPoint.longitude)
                            marker.title = doctor.name
                            mapView.overlays.add(marker)
                        }
                    }

                    LaunchedEffect(mapView) { mapView.invalidate() }

                    AndroidView(
                        factory = { mapView },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Appointment Details ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Date",
                    tint = Color(0xFFB7FF59),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "${appointment.date} at ${appointment.time}",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            if (appointment.location.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFFB7FF59),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(appointment.location, fontSize = 14.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Status ---
            Text(
                appointmentStatus,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            if (daysDifference == 0L) {
                Text(
                    "Your appointment is today!",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            } else if (daysDifference < 0) {
                Text(
                    "This appointment has already passed.",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
