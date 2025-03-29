import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
class SpeechViewModllel : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SpeechTestScreen()
        }
    }

    @Composable
    fun SpeechTestScreen() {
        var text by remember { mutableStateOf(TextFieldValue()) }
        var isLoading by remember { mutableStateOf(false) }
        var resultMessage by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Text input field
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter text") },
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
                        // Hardcoding the voice value here
                        val voice = "coral"  // Set this to whatever voice you want
                        generateSpeech(text.text, voice, "YOUR_API_KEY") { result ->
                            isLoading = false
                            resultMessage = result
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Generate Speech")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            }

            if (resultMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(resultMessage)
            }
        }
    }

    // Function to call the OpenAI API
    private fun generateSpeech(
        text: String,
        voice: String,
        apiKey: String,
        callback: (String) -> Unit
    ) {
        val url =
            "https://api.openai.com/v1/audio/speech/generate"  // Placeholder URL (replace with actual endpoint)

        val json = """
            {
                "model": "gpt-4o-mini-tts",
                "voice": "$voice",
                "input": "$text",
                "instructions": "Speak in a cheerful and positive tone."
            }
        """

        val body = json.toRequestBody("application/json".toMediaTypeOrNull())

        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Failed to generate speech: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Assuming the response contains the audio file in MP3 format as a stream
                    val audioStream = response.body?.byteStream()
                    audioStream?.let { stream ->
                        val file = File(cacheDir, "speech.mp3")
                        FileOutputStream(file).use { output ->
                            stream.copyTo(output)
                        }
                        callback("Audio saved as speech.mp3")
                        // Optionally, you can play the audio here using Android APIs (MediaPlayer)
                    }
                } else {
                    callback("Failed to generate speech: ${response.code}")
                }
            }
        })
    }
}
