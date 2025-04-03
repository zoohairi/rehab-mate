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

    // Fetch appointments when the uid is available
    LaunchedEffect(uid) {
        if (uid.isNullOrEmpty()) {
            Log.e("AppointmentScreen", "User not logged in")
            isLoading = false
            return@LaunchedEffect
        }

        fetchUserAppointmentsAndUpdateState(uid, { appointmentList ->
            appointments = appointmentList  // Update the appointments list
            Log.d("UpdatedAppointments", appointments.toString())  // Log the appointments
            isLoading = false
        }, { error ->
            Log.e("AppointmentScreen", error)
            isLoading = false
        })
    }

    // Show loading indicator while fetching appointments
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
            CircularProgressIndicator()
        }
    } else {
        // Main UI content
        Column {
            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2196F3))
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .clickable { navController.popBackStack() }
                        .size(24.dp)
                )
                Text(
                    text = "APPOINTMENTS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            // Display appointments in a LazyColumn
            LazyColumn(modifier = Modifier.padding(8.dp)) {
                items(appointments.size) { index ->  // Use items(count) version
                    val appointment = appointments[index]
                    AppointmentCard(appointment = appointment)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(appointment: AppointmentData) {
    val context = LocalContext.current // Get the current context
    val today = Calendar.getInstance().time
    val appointmentDate =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(appointment.date)
    val daysDifference = (appointmentDate?.time ?: 0 - today.time) / (1000 * 60 * 60 * 24)

    val appointmentStatus = when {
        daysDifference == 0L -> "Scheduled for today"
        daysDifference > 0 -> "Upcoming appointment"
        else -> "Appointment has passed"
    }

    // Create the Appointment Card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Display doctor information for each appointment
            appointment.doctors.forEachIndexed { index, doctor ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        "${index + 1}.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Dr. ${doctor.name}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            doctor.specialty,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Add space before map

            // OSMDroid Map (Show location of appointment)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E2E))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Inside the map display logic (where you add the marker)
                    val mapView = remember { MapView(context) }
                    mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                    mapView.controller.setZoom(13) // Zoom in more for better visibility

                    // Default location (Singapore)
                    mapView.controller.setCenter(GeoPoint(1.352083, 103.819839))

                    // Loop through doctors and add markers for their locations
                    appointment.doctors.forEach { doctor ->
                        doctor.latLng?.let { geoPoint ->
                            Log.d("geoPoint", geoPoint.toString())
                            val marker = Marker(mapView)
                            marker.position = GeoPoint(geoPoint.latitude, geoPoint.longitude)
                            marker.title = doctor.name
                            mapView.overlays.add(marker)
                        }
                    }

                    // Ensure map is redrawn after adding markers
                    LaunchedEffect(mapView) {
                        mapView.invalidate()
                    }

                    Log.d("MapState", "MapView overlays: ${mapView.overlays}")
                    Log.d("MapDebug", "Markers added: ${mapView.overlays.size}")

                    AndroidView(
                        factory = { mapView },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Space between map and details

            // Display appointment details
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Date",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "${appointment.date} at ${appointment.time}",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (appointment.location.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(appointment.location, fontSize = 14.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                appointmentStatus,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                daysDifference == 0L -> Text(
                    "Your appointment is today!",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )

                daysDifference < 0 -> Text(
                    "This appointment has already passed.",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
