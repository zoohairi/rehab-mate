package com.example.rehabmate.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.rehabmate.firebase.fetchUserAppointmentsAndUpdateState
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

//for storing of doctor info
data class DoctorInfo(
    val name: String,
    val specialty: String
)

//For storing of Appoint Data
data class AppointmentData(
    val id: String,
    val doctors: List<DoctorInfo>,  // Allow multiple doctors
    val date: String,
    val time: String,
    val location: String
)

@Composable
fun AppointmentScreen(navController: NavHostController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    var appointments by remember { mutableStateOf<List<AppointmentData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user) {
        user?.let {
            fetchUserAppointmentsAndUpdateState(it.uid, { appointmentList ->
                appointments = appointmentList
                Log.d("UpdatedAppointments", appointments.toString())
                isLoading = false
            }, { error ->
                Log.e("AppointmentScreen", error)
                isLoading = false
            })
        } ?: run {
            Log.e("AppointmentScreen", "User not logged in")
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
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
                    .align(Alignment.CenterStart)
                    .clickable { navController.popBackStack() }
                    .size(24.dp))

            Text(
                text = "APPOINTMENTS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            if (appointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No appointments scheduled from the doctor",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(appointments) { appointment ->
                        AppointmentCard(appointment = appointment)
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(appointment: AppointmentData) {
    val currentDate = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val appointmentDate = LocalDate.parse(appointment.date, dateFormatter)
    val daysDifference = ChronoUnit.DAYS.between(currentDate, appointmentDate)

    val appointmentStatus = when {
        daysDifference < 0 -> "Past appointment"
        daysDifference == 0L -> "Today"
        daysDifference == 1L -> "Tomorrow"
        else -> "$daysDifference days left"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Display of doctor info
            appointment.doctors.forEachIndexed { index, doctor ->
                // Display each doctor and their specialty
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    //point form
                    Text(
                        text = "${index + 1}.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    // Display the doctor's info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dr. " + doctor.name.split(" ")
                                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }, //sentence case
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = doctor.specialty.split(" ")
                                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }, //sentence case
                            fontSize = 14.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${appointment.date} at ${appointment.time}",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = appointment.location, fontSize = 14.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = appointmentStatus,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (daysDifference == 0L) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(text = "Your appointment is today!", color = Color.Gray)
                }
            } else if (daysDifference < 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = {},
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("This appointment has already passed.")
                    }
                }
            }



            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${appointment.date} at ${appointment.time}",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = appointment.location, fontSize = 14.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = appointmentStatus,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (daysDifference == 0L) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(text = "Your appointment is today!", color = Color.Gray)
                }
            } else if (daysDifference < 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = {},
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("This appointment has already passed.")
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentDialog(onDismiss: () -> Unit, onAddAppointment: () -> Unit) {
    var doctorName by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    AlertDialog(onDismissRequest = onDismiss, title = {
        Text(
            text = "Add New Appointment", fontSize = 18.sp, fontWeight = FontWeight.Bold
        )
    }, text = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            TextField(
                value = doctorName,
                onValueChange = { doctorName = it },
                label = { Text("Doctor Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            TextField(
                value = specialty,
                onValueChange = { specialty = it },
                label = { Text("Specialty") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            TextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (DD/MM/YYYY)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            TextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Time") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            TextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    }, confirmButton = {
        Button(
            onClick = onAddAppointment,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
        ) {
            Text("ADD")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("CANCEL")
        }
    })
}

// Helper function to get current month name
fun getCurrentMonth(): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(Date())
}
