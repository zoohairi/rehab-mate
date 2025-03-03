//package com.example.rehabmate.screens
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.net.Uri
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.AccountCircle
////import androidx.compose.material.icons.filled.AddAPhoto
//import androidx.compose.material3.Button
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.RadioButton
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.testTag
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.material3.Scaffold
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.res.vectorResource
//
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.zIndex
//import androidx.core.content.ContextCompat
//import androidx.core.content.FileProvider
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.rememberNavController
//import com.example.rehabmate.R
//import java.io.File
//
//@Composable
//fun ProfileScreen(navController: NavHostController, username: String, showForm: MutableState<Boolean> = mutableStateOf(false)) {
//    val context = LocalContext.current
//    var photoUri by remember { mutableStateOf<Uri?>(null) }
//    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
//
//    var age by remember { mutableStateOf("") }
//    var occupation by remember { mutableStateOf("") }
//    var selectedSex by remember { mutableStateOf("") }
//
//    // Camera launcher to capture a photo
//    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
//        if (success && tempPhotoUri != null) {
//            photoUri = tempPhotoUri
//            showForm.value = true
//        }
//    }
//
//    // Permission request for camera
//    val requestPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//        if (isGranted) {
//            val file = File(context.cacheDir, "profile_image.jpg")
//            tempPhotoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
//            cameraLauncher.launch(tempPhotoUri)
//        }
//    }
//
//    Scaffold { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Top
//        ) {
//            Text(
//                text = "Hi, $username! Take a photo of yourself to let us know you are real!",
//                style = MaterialTheme.typography.headlineSmall,
//                modifier = Modifier.testTag("greetingText")
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//
//            if (!showForm.value) {
//                Text(
//                    text = "Take Photo",
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier.testTag("takePhotoText")
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//
//                // Display either placeholder or captured image
//                Image(
//                    painter = if (photoUri != null) rememberAsyncImagePainter(photoUri)
//                    else painterResource(id = R.drawable.ic_launcher_foreground),
//                    contentDescription = "Profile Photo",
//                    modifier = Modifier
//                        .height(120.dp)
//                        .clickable {
//                            // **2.3.2 Test Mode** → Uses Dummy URI to simulate a successful capture
//                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//                                val file = File(context.cacheDir, "profile_image.jpg")
//                                tempPhotoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
//                                cameraLauncher.launch(tempPhotoUri)
//                            } else {
//                                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
//                            }
//                        }
//                        .testTag("imageIcon")
//                )
//            }
//
//            if (showForm.value) {
//                Spacer(modifier = Modifier.height(16.dp))
//                Text(
//                    text = "Thank you for providing your real photo. Now, please tell us about yourself.",
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier.testTag("requestMoreInfoText")
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//
//                OutlinedTextField(
//                    value = age,
//                    onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
//                    label = { Text("Age") },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    modifier = Modifier.fillMaxWidth().testTag("ageTextField")
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//
//                OutlinedTextField(
//                    value = occupation,
//                    onValueChange = { occupation = it },
//                    label = { Text("Occupation") },
//                    modifier = Modifier.fillMaxWidth().testTag("occupationTextField")
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Text(text = "Gender:")
//
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        RadioButton(
//                            selected = selectedSex == "male",
//                            onClick = { selectedSex = "male" },
//                            modifier = Modifier.testTag("maleRadioButton")
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(text = "Male")
//                    }
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        RadioButton(
//                            selected = selectedSex == "female",
//                            onClick = { selectedSex = "female" },
//                            modifier = Modifier.testTag("femaleRadioButton")
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(text = "Female")
//                    }
//                }
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Button(
//                    onClick = {
//                        navController.navigate("choose_screen/${username}/${selectedSex}/${age}/${occupation}")
//                    },
//                    enabled = age.isNotEmpty() && occupation.isNotEmpty() && selectedSex.isNotEmpty(),
//                    modifier = Modifier.testTag("nextButton")
//                ) {
//                    Text("Next")
//                }
//            }
//        }
//    }
//}