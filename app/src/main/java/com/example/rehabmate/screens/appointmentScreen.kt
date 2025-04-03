package com.example.rehabmate.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import com.example.rehabmate.firebase.fetchExercisesForUser
import com.example.rehabmate.firebase.fetchUserAppointmentsAndUpdateState
import com.example.rehabmate.firebase.getUidFromSharedPreferences
import com.example.rehabmate.ui.theme.red_color
import com.example.rehabmate.ui.theme.white_color
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale


//for storing of doctor info
data class DoctorInfo(
    val name: String, val specialty: String
)

//For storing of Appoint Data
data class AppointmentData(
    val id: String, val doctors: List<DoctorInfo>,  // Allow multiple doctors
    val date: String, val time: String, val location: String
)

@Composable
fun AppointmentScreen(navController: NavHostController) {
    val context = LocalContext.current
    val uid = getUidFromSharedPreferences(context).toString()

    var appointments by remember { mutableStateOf<List<AppointmentData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddAppointmentDialog by remember { mutableStateOf(false) }
    var exerciseList by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    // Function to get the latest appointment
    val latestAppointment: AppointmentData? = appointments.sortedByDescending {
        LocalDate.parse(
            it.date,
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )
    }.firstOrNull()

    // Fetch appointments inside LaunchedEffect
    LaunchedEffect(uid) {
        uid?.let {
            fetchUserAppointmentsAndUpdateState(uid, { appointmentList ->
                appointments = appointmentList
                Log.d("UpdatedAppointments", appointments.toString())
                isLoading = false
            }, { error ->
                Log.e("AppointmentScreen", error)
                isLoading = false
            })

            // Fetch exercises
            fetchExercisesForUser(uid, onSuccess = { exercises ->
                // Log the entire exercises data to see the structure
                Log.d("ExerciseList", exercises.toString())

                // Check if the status is either "approved" or "progress"
                if (exercises.any { exercise ->
                        val status = (exercise["status"] as? String)?.uppercase()
                        status == "APPROVED" || status == "PROGRESS"
                    }) {
                    // Retrieve only exercise_title and location from exercises
                    val exerciseDetails = exercises.mapNotNull { exercise ->
                        val exerciseTitle = exercise["exercise_title"] as? String
                        val doctors = exercise["doctors"] as? List<Map<String, Any>>

                        // If doctors exist, extract the location from the first one
                        val location = doctors?.firstOrNull()?.get("location") as? String

                        if (exerciseTitle != null && location != null) {
                            exerciseTitle to location
                        } else {
                            null
                        }
                    }

                    // Assign the filtered details to exerciseList
                    exerciseList = exerciseDetails
                    Log.d("exerciseList", exerciseList.toString())
                }
            }, onFailure = { error ->
                Log.d("Error", "Error fetching exercises: $error")
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
            // Back button
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { navController.popBackStack() }
                    .size(24.dp))

            // Header title
            Text(
                text = "APPOINTMENTS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )

            // Add appointment button
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Appointment",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { showAddAppointmentDialog = true }
                    .size(24.dp))
        }

        // Calendar header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF303030))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Upcoming Appointments",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Show the latest appointment date

                if (latestAppointment != null) {
                    // Parse the date from latestAppointment
                    val latestDate = LocalDate.parse(
                        latestAppointment.date, DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    )

                    // Compare the parsed date with the current date
                    if (latestDate.isBefore(LocalDate.now())) {
                        // Latest appointment is in the past
                        Text(
                            text = "Latest appointment: ${
                                latestDate.format(
                                    DateTimeFormatter.ofPattern("dd MMM yyyy")
                                )
                            }", fontSize = 14.sp, color = Color.LightGray
                        )
                        Text(
                            text = "(This appointment has passed)",
                            fontSize = 14.sp,
                            color = red_color
                        )
                    } else {
                        // Latest appointment is in the future
                        Text(
                            text = "Latest appointment: ${
                                latestDate.format(
                                    DateTimeFormatter.ofPattern("dd MMM yyyy")
                                )
                            }", fontSize = 14.sp, color = Color.LightGray
                        )
                    }
                } else {
                    // If no appointment data is found
                    Text(
                        text = "No upcoming appointments", fontSize = 14.sp, color = Color.LightGray
                    )
                }
            }
        }

        // Show loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            // If appointments are loaded, display them
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
                // Display appointments in LazyColumn
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

        // Add appointment dialog
        if (showAddAppointmentDialog) {
            AddAppointmentDialog(
                onDismiss = { showAddAppointmentDialog = false },
                exerciseList = exerciseList,
            )
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

    // Use remember to persist the state across recompositions
    var detailsDisplayed by remember { mutableStateOf(false) }

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
            // Display doctor information first
            appointment.doctors.forEachIndexed { index, doctor ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // point form
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
                                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }, // sentence case
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = doctor.specialty.split(" ")
                                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }, // sentence case
                            fontSize = 14.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Check if the appointment details have been displayed
            if (!detailsDisplayed) {
                // Show the appointment details only once
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
                    Row(
                        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                    ) {
                        Text(text = "Your appointment is today!", color = Color.Gray)
                    }
                } else if (daysDifference < 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {},
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                        ) {
                            Text("This appointment has already passed.")
                        }
                    }
                }

                // Mark that the details have been displayed
                detailsDisplayed = true
            }
        }
    }
}

@Composable
fun AddAppointmentDialog(
    onDismiss: () -> Unit,
    exerciseList: List<Pair<String, String>> // List of exercises with names and locations
) {
    var doctorName by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedExercise by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    // Function to update the location based on selected exercise
    val onExerciseSelected: (String, String) -> Unit = { exerciseName, exerciseLocation ->
        selectedExercise = exerciseName
        location = exerciseLocation // Update the location based on the selected exercise
    }
    exerciseList.forEach { exercise ->
        Log.d(
            "Exercise split", "Exercise: ${exercise.first}, Location: ${exercise.second}"
        )
    }

    // AlertDialog for adding an appointment
    AlertDialog(
        onDismissRequest = onDismiss, title = {
            Text(
                text = "Add New Appointment", fontSize = 18.sp, fontWeight = FontWeight.Bold
            )
        },

        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Dropdown for Exercise Selection
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedExercise,
                        onValueChange = {},
                        label = { Text("Select Exercise") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown Arrow",
                                modifier = Modifier.clickable { expanded = !expanded })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null // This removes the ripple effect
                            ) { expanded = !expanded })

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f) // Make the dropdown almost as wide as the parent
                            .background(Color.White) // Light background for contrast
                    ) {
                        if (exerciseList.isEmpty()) {
                            // Display message when no exercises exist
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "No exercises available",
                                        color = Color.Gray,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                onClick = { /* No action needed */ },
                                enabled = false // Make it non-clickable
                            )
                        } else {
                            exerciseList.forEach { exercise ->
                                DropdownMenuItem(text = {
                                    Text(
                                        text = exercise.first,
                                        color = Color.Black // Ensure text is visible
                                    )
                                }, onClick = {
                                    onExerciseSelected(exercise.first, exercise.second)
                                    expanded = false
                                })
                            }
                        }
                    }
                }


                // Display location set automatically  based on the selected exercise
                TextField(
                    value = if (selectedExercise.isEmpty()) "No Exercise is selected" else location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    readOnly = true, // Location set when exercise is selected
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                // Doctor details and appointment info fields
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
            }
        }, confirmButton = {
            Button(
                onClick = {
                    Log.d("Add appointment", "add appointment clicked")
                    // Here, add the appointment to the database
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("ADD")
            }
        }, dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        })
}
