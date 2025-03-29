import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.content.Context
import android.os.Looper
import android.os.Handler

@Composable
fun SpeechTestScreen(context: Context) {
    var text by remember { mutableStateOf(TextFieldValue()) }
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter text") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (text.text.isNotEmpty()) {
                    isLoading = true
                    generateSpeech(context, text.text, "coral", "YOUR_API_KEY") { msg, _ ->
                        isLoading = false
                        resultMessage = msg
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Generate Speech")
        }

        if (isLoading) CircularProgressIndicator()
        if (resultMessage.isNotEmpty()) Text(resultMessage)
    }
}

// Updated function to return the MediaPlayer
private fun generateSpeech(
    context: Context,
    text: String,
    voice: String,
    apiKey: String,
    callback: (String, MediaPlayer?) -> Unit
) {
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