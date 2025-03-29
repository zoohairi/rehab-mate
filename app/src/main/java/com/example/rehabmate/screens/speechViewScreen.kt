package com.example.rehabmate.screens

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rehabmate.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SpeechScreen(navController: NavController, instructions: String) {
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer() }

    val apiKey = context.getString(R.string.TTS_API)

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
                    Text("Back")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Display instructions
        Text(
            text = "Instructions: $instructions",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Generate Speech button
        Button(
            onClick = {
                isLoading = true
                generateSpeech(
                    context,
                    instructions,
                    "en-us", // Example language
                    "Linda", // Example voice
                    apiKey
                ) { result, file ->
                    isLoading = false
                    resultMessage = result
                    audioFile = file
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && instructions.isNotEmpty()
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
            Handler(Looper.getMainLooper()).post {
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
                        Handler(Looper.getMainLooper()).post {
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
                        Handler(Looper.getMainLooper()).post {
                            callback("Speech generated successfully", file)
                        }
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            callback("Empty response from server", null)
                        }
                    }
                } catch (e: Exception) {
                    Handler(Looper.getMainLooper()).post {
                        callback("Error processing response: ${e.message}", null)
                    }
                }
            } else {
                val errorBody = response.body?.string() ?: "No error details"
                Handler(Looper.getMainLooper()).post {
                    callback("Failed to generate speech: ${response.code}, $errorBody", null)
                }
            }
        }
    })
}