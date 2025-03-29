package com.example.rehabmate.screens

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeechScreen(navController: NavController) {
    var text by remember { mutableStateOf(TextFieldValue()) }
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Voice and language selection state
    var selectedLanguage by remember { mutableStateOf("en-us") }
    var selectedVoice by remember { mutableStateOf("Linda") }

    // Audio playback state
    var isPlaying by remember { mutableStateOf(false) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    val mediaPlayer = remember { MediaPlayer() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Add a top app bar with back button
        SmallTopAppBar(
            title = { Text("Text to Speech") },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    // You can replace this with your back icon
                    Text("Back")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Language selection dropdown
        ExposedDropdownMenuBox(
            expanded = false, // You would need state for this
            onExpandedChange = { /* Handle expansion */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = "English (United States)",
                onValueChange = { },
                readOnly = true,
                label = { Text("Language") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            // You would implement the dropdown menu items here
            // This is a placeholder
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Voice selection dropdown
        ExposedDropdownMenuBox(
            expanded = false, // You would need state for this
            onExpandedChange = { /* Handle expansion */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = selectedVoice,
                onValueChange = { },
                readOnly = true,
                label = { Text("Voice") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            // You would implement the dropdown menu items here
            // This is a placeholder
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Text input field
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter text to convert to speech") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions.Default
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Generate Speech button
        Button(
            onClick = {
                if (text.text.isNotEmpty()) {
                    isLoading = true
                    generateSpeech(
                        context,
                        text.text,
                        selectedLanguage,
                        selectedVoice,
                        "21cc98a066fd4dc2a421ec55141264e1" // API
                    ) { result, file ->
                        isLoading = false
                        resultMessage = result
                        audioFile = file
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && text.text.isNotEmpty()
        ) {
            Text("Generate Speech")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        }

        if (resultMessage.isNotEmpty() && audioFile != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = resultMessage,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                Button(
                    onClick = {
                        if (isPlaying) {
                            mediaPlayer.stop()
                            mediaPlayer.reset()
                            isPlaying = false
                        } else {
                            try {
                                mediaPlayer.reset()
                                mediaPlayer.setDataSource(audioFile!!.path)
                                mediaPlayer.setAudioAttributes(
                                    AudioAttributes.Builder()
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .build()
                                )
                                mediaPlayer.prepare()
                                mediaPlayer.start()
                                isPlaying = true

                                mediaPlayer.setOnCompletionListener {
                                    isPlaying = false
                                }
                            } catch (e: Exception) {
                                resultMessage = "Error playing audio: ${e.message}"
                            }
                        }
                    }
                ) {
                    Text(if (isPlaying) "Stop" else "Play")
                }
            }
        }
    }
}

// Function to call the Voice RSS API
private fun generateSpeech(
    context: Context,
    text: String,
    language: String,
    voice: String,
    apiKey: String,
    callback: (String, File?) -> Unit
) {
    val client = OkHttpClient()

    // Encode the text for URL
    val encodedText = URLEncoder.encode(text, "UTF-8")

    // Build the Voice RSS API URL
    val url = "https://api.voicerss.org/?" +
            "key=$apiKey" +
            "&hl=$language" +
            "&v=$voice" +
            "&c=MP3" +
            "&f=16khz_16bit_stereo" +
            "&src=$encodedText"

    val request = Request.Builder()
        .url(url)
        .get()
        .build()

    // Make the API request
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // Run on the main thread
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                callback("Failed to generate speech: ${e.message}", null)
            }
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                try {
                    // Check if the response is an error message
                    val contentType = response.headers["Content-Type"] ?: ""

                    if (contentType.contains("text/plain")) {
                        val errorMessage = response.body?.string() ?: "Unknown error"
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            callback("Error: $errorMessage", null)
                        }
                        return
                    }

                    // Process the audio data
                    val audioStream = response.body?.byteStream()
                    if (audioStream != null) {
                        // Save the audio file to the cache directory
                        val file =
                            File(context.cacheDir, "speech_${System.currentTimeMillis()}.mp3")
                        FileOutputStream(file).use { output ->
                            audioStream.copyTo(output)
                        }

                        // Run on the main thread
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            callback("Speech generated successfully", file)
                        }
                    } else {
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            callback("Empty response from server", null)
                        }
                    }
                } catch (e: Exception) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        callback("Error processing response: ${e.message}", null)
                    }
                }
            } else {
                val errorBody = response.body?.string() ?: "No error details"
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    callback("Failed to generate speech: ${response.code}, $errorBody", null)
                }
            }
        }
    })
}