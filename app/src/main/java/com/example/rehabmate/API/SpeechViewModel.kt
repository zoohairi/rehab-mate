package com.example.rehabmate

import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.content.Context
import android.os.Looper
import android.os.Handler
import androidx.compose.ui.platform.LocalContext

//@Composable
//fun SpeechTestScreen(exercise_des: String?) {
//    val context = LocalContext.current // Get the current context
//    var voiceOverEnabled by remember { mutableStateOf(false) }
//
//    var text by remember { mutableStateOf(TextFieldValue()) }
//    var isLoading by remember { mutableStateOf(false) }
//    var resultMessage by remember { mutableStateOf("") }
//    val context = LocalContext.current
//
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        TextField(
//            value = text,
//            onValueChange = { text = it },
//            label = { Text("Enter text") },
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        // Auto-fill text field with exercise description
//        LaunchedEffect(text.text) {
//            if (text.text.isNotEmpty() && voiceOverEnabled) {
//                isLoading = true
//                generateSpeech(
//                    context = context,
//                    text = text.text,
//                    callback = { msg, mediaPlayer ->  // Correcting the callback signature
//                        isLoading = false
//                        resultMessage = msg
//                        // You can use mediaPlayer if needed for further playback control
//                    }
//                )
//            }
//        }
//
//
//        Button(
//            onClick = {
//                if (text.text.isNotEmpty()) {
//                    isLoading = true
//                    generateSpeech(context, text.text, "en") { msg, _ ->
//                        isLoading = false
//                        resultMessage = msg
//                    }
//                }
//            },
//            modifier = Modifier.fillMaxWidth(),
//            enabled = !isLoading
//        ) {
//            Text("Generate Speech")
//        }
//
//        if (isLoading) CircularProgressIndicator()
//        if (resultMessage.isNotEmpty()) Text(resultMessage)
//    }
//}
//

// Updated function to return the MediaPlayer
public fun generateSpeech(
    context: Context,
    text: String,
    callback: (String, MediaPlayer?) -> Unit
) {

    val apiKey = context.getString(R.string.TTS_API)
    val voice = "coral"
    val url = "https://api.voicerss.org/?key=$apiKey&hl=en-us&src=$text&v=$voice&c=MP3"
    val client = OkHttpClient()

    client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            callback("Failed to generate speech: ${e.message}", null)
        }

        override fun onResponse(call: Call, response: Response) {
            if (!response.isSuccessful) {
                callback("API error: ${response.code}", null)
                return
            }

            try {
                val file = File(context.cacheDir, "speech.mp3").apply {
                    response.body?.byteStream()?.use { input ->
                        FileOutputStream(this).use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                // Switch to main thread for MediaPlayer operations
                Handler(Looper.getMainLooper()).post {
                    val player = MediaPlayer().apply {
                        setDataSource(file.absolutePath)
                        prepareAsync() // Use asynchronous preparation
                        setOnPreparedListener { mp ->
                            mp.start() // Autoplay when prepared
                            callback("Audio playing now", this)
                        }
                        setOnErrorListener { _, what, extra ->
                            callback("Playback error: $what, $extra", null)
                            false
                        }
                    }
                }
            } catch (e: Exception) {
                callback("Processing error: ${e.message}", null)
            }
        }
    })
}