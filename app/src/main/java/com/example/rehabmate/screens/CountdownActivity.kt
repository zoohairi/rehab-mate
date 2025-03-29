package com.example.rehabmate

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.rehabmate.R
import kotlinx.coroutines.*

class CountdownActivity : AppCompatActivity() {

    private var countdownJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView: TextView = findViewById(R.id.textViewCountdown)

        // Start countdown in Coroutine
        startCountdown(10, textView)
    }

    private fun startCountdown(seconds: Int, textView: TextView) {
        countdownJob = CoroutineScope(Dispatchers.Main).launch {
            for (i in seconds downTo 1) {
                textView.text = "Time remaining: $i seconds"
                delay(1000) // Wait for 1 second
            }
            textView.text = "Countdown finished!"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the countdown job when activity is destroyed to avoid memory leaks
        countdownJob?.cancel()
    }
}