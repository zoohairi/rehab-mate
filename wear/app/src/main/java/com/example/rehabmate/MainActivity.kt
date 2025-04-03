package com.example.rehabmate

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject
import kotlin.random.Random
import com.example.rehabmate.R

class MainActivity : ComponentActivity(), SensorEventListener, MessageClient.OnMessageReceivedListener {

    private lateinit var timerTextView: TextView
    private lateinit var heartRateTextView: TextView
    private lateinit var startStopButton: Button

    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null
    private var rotationSensor: Sensor? = null
    private var heartRateSensor: Sensor? = null

    // Variables to store sensor values for sending
    private var lastGyroX = 0f
    private var lastGyroY = 0f
    private var lastGyroZ = 0f
    private var lastRotationX = 0f
    private var lastRotationY = 0f
    private var lastRotationZ = 0f
    private var lastHeartRate = 0f

    private var sensorActiveGyro = false
    private var sensorActiveRotation = false
    private var sensorActiveHeartRate = false

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var sensorCheckRunnableGyro: Runnable
    private lateinit var sensorCheckRunnableRotation: Runnable
    private lateinit var sensorCheckRunnableHeartRate: Runnable

    private var simulationModeGyro = false
    private var simulationModeRotation = false
    private var simulationModeHeartRate = false

    private lateinit var simulationRunnableGyro: Runnable
    private lateinit var simulationRunnableRotation: Runnable
    private lateinit var simulationRunnableHeartRate: Runnable

    private var lastGyroUpdateTime: Long = 0
    private var lastRotationUpdateTime: Long = 0
    private val updateInterval = 3000L

    // Timer variables for count-up timer
    private var timerStartTime: Long = 0
    private val timerHandler = Handler(Looper.getMainLooper())
    private lateinit var timerRunnable: Runnable

    // Handler and Runnable for sending sensor data periodically
    private val sendHandler = Handler(Looper.getMainLooper())
    private lateinit var sendRunnable: Runnable

    // Flag for whether the timer is running (i.e. exercise is active)
    private var isExerciseActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        timerTextView = findViewById(R.id.timerTextView)
        heartRateTextView = findViewById(R.id.heartRateTextView)
        startStopButton = findViewById(R.id.startStopButton) // make sure this is in your layout

        // Example exercise name (update as needed)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        setTheme(android.R.style.Theme_DeviceDefault)

        // Set button click listener to toggle start/stop
        startStopButton.setOnClickListener {
            if (!isExerciseActive) {
                // Start exercise: count-up timer, sensor recording, and sending sensor data.
                isExerciseActive = true
                startStopButton.text = "Stop Exercise"
                startTimerCountUp()
                startSensorRecording()
                startSendingSensorData()
                // Optionally, you can also send a message to the mobile app to indicate start.
            } else {
                // Stop exercise: stop timer, sensor recording, and data sending.
                isExerciseActive = false
                startStopButton.text = "Start Exercise"
                stopExercise()
                // Optionally, you can also send a stop message to the mobile app.
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Add message listener (if you also need to receive commands)
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        handler.removeCallbacks(sensorCheckRunnableGyro)
        handler.removeCallbacks(sensorCheckRunnableRotation)
        handler.removeCallbacks(sensorCheckRunnableHeartRate)
        if (simulationModeGyro) handler.removeCallbacks(simulationRunnableGyro)
        if (simulationModeRotation) handler.removeCallbacks(simulationRunnableRotation)
        if (simulationModeHeartRate) handler.removeCallbacks(simulationRunnableHeartRate)
        Wearable.getMessageClient(this).removeListener(this)
        timerHandler.removeCallbacks(timerRunnable)
        sendHandler.removeCallbacks(sendRunnable)
    }

    // Stop all ongoing activities (timer, sensor recording, sending sensor data)
    private fun stopExercise() {
        sensorManager.unregisterListener(this)
        timerHandler.removeCallbacks(timerRunnable)
        sendHandler.removeCallbacks(sendRunnable)
    }

    // Start a count-up timer.
    private fun startTimerCountUp() {
        timerStartTime = System.currentTimeMillis()
        timerRunnable = object : Runnable {
            override fun run() {
                val elapsedMillis = System.currentTimeMillis() - timerStartTime
                val seconds = (elapsedMillis / 1000) % 60
                val minutes = (elapsedMillis / 60000)
                timerTextView.text = "%02d:%02d".format(minutes, seconds)
                timerHandler.postDelayed(this, 1000)
            }
        }
        timerHandler.post(timerRunnable)
    }

    // Start sensor recording and simulation checks.
    private fun startSensorRecording() {
        gyroscope?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        rotationSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        heartRateSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        sensorActiveGyro = false
        sensorActiveRotation = false
        sensorActiveHeartRate = false

        sensorCheckRunnableGyro = Runnable {
            if (!sensorActiveGyro) {
                sensorManager.unregisterListener(this, gyroscope)
                startGyroSimulation()
            }
        }
        handler.postDelayed(sensorCheckRunnableGyro, 5000)

        sensorCheckRunnableRotation = Runnable {
            if (!sensorActiveRotation) {
                sensorManager.unregisterListener(this, rotationSensor)
                startRotationSimulation()
            }
        }
        handler.postDelayed(sensorCheckRunnableRotation, 5000)

        sensorCheckRunnableHeartRate = Runnable {
            if (!sensorActiveHeartRate) {
                sensorManager.unregisterListener(this, heartRateSensor)
                startHeartRateSimulation()
            }
        }
        handler.postDelayed(sensorCheckRunnableHeartRate, 5000)
    }

    // Periodically send sensor data every updateInterval milliseconds.
    private fun startSendingSensorData() {
        sendRunnable = object : Runnable {
            override fun run() {
                sendSensorDataMessage()
                sendHandler.postDelayed(this, updateInterval)
            }
        }
        sendHandler.post(sendRunnable)
    }

    // Package sensor values into JSON and send to the mobile app.
    private fun sendSensorDataMessage() {
        val json = JSONObject().apply {
            put("gyro", JSONObject().apply {
                put("x", lastGyroX)
                put("y", lastGyroY)
                put("z", lastGyroZ)
            })
            put("rotation", JSONObject().apply {
                put("x", lastRotationX)
                put("y", lastRotationY)
                put("z", lastRotationZ)
            })
            put("heartRate", lastHeartRate)
        }
        val payload = json.toString().toByteArray()
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                Wearable.getMessageClient(this)
                    .sendMessage(node.id, "/sensor-data", payload)
                    .addOnSuccessListener {
                        Log.d("WearOS", "Sent sensor data to mobile: ${node.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("WearOS", "Failed to send sensor data", e)
                    }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val currentTime = System.currentTimeMillis()
            when (it.sensor.type) {
                Sensor.TYPE_GYROSCOPE -> {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]
                    if (x != 0f || y != 0f || z != 0f) sensorActiveGyro = true
                    if (currentTime - lastGyroUpdateTime >= updateInterval) {
                        Log.d("GyroData", "Gyro (Real) -> X: $x, Y: $y, Z: $z")
                        lastGyroUpdateTime = currentTime
                    }
                    lastGyroX = x
                    lastGyroY = y
                    lastGyroZ = z
                }
                Sensor.TYPE_ROTATION_VECTOR -> {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]
                    if (x != 0f || y != 0f || z != 0f) sensorActiveRotation = true
                    if (currentTime - lastRotationUpdateTime >= updateInterval) {
                        Log.d("RotationData", "Rotation (Real) -> X: $x, Y: $y, Z: $z")
                        lastRotationUpdateTime = currentTime
                    }
                    lastRotationX = x
                    lastRotationY = y
                    lastRotationZ = z
                }
                Sensor.TYPE_HEART_RATE -> {
                    val hr = it.values[0]
                    if (hr != 0f) sensorActiveHeartRate = true
                    heartRateTextView.text = "${hr.toInt()} BPM"
                    Log.d("HeartRateData", "Heart Rate (Real): $hr BPM")
                    //lastHeartRate = hr
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.w("MainActivity", "Sensor accuracy is unreliable")
        }
    }

    private fun startGyroSimulation() {
        simulationModeGyro = true
        simulationRunnableGyro = object : Runnable {
            override fun run() {
                val x = Random.nextFloat() * 10 - 5
                val y = Random.nextFloat() * 10 - 5
                val z = Random.nextFloat() * 10 - 5
                Log.d("GyroData", "Gyro (Simulated) -> X: %.2f, Y: %.2f, Z: %.2f".format(x, y, z))
                handler.postDelayed(this, updateInterval)

                lastGyroX = x
                lastGyroY = y
                lastGyroZ = z
            }
        }
        handler.post(simulationRunnableGyro)

    }

    private fun startRotationSimulation() {
        simulationModeRotation = true
        simulationRunnableRotation = object : Runnable {
            override fun run() {
                val x = Random.nextFloat() * 2 - 1
                val y = Random.nextFloat() * 2 - 1
                val z = Random.nextFloat() * 2 - 1
                Log.d("RotationData", "Rotation (Simulated) -> X: %.2f, Y: %.2f, Z: %.2f".format(x, y, z))
                handler.postDelayed(this, updateInterval)

                lastRotationX = x
                lastRotationY = y
                lastRotationZ = z
            }
        }
        handler.post(simulationRunnableRotation)
    }

    private fun startHeartRateSimulation() {
        simulationModeHeartRate = true
        simulationRunnableHeartRate = object : Runnable {
            override fun run() {
                val simulatedHR = Random.nextInt(60, 180)
                heartRateTextView.text = "$simulatedHR BPM"
                Log.d("HeartRateData", "Heart Rate (Simulated): $simulatedHR BPM")
                handler.postDelayed(this, updateInterval)
                lastHeartRate = simulatedHR.toFloat()
            }

        }
        handler.post(simulationRunnableHeartRate)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/start-recording") {
            Log.d("WearOS", "Received start-recording message from mobile")
            val durationSeconds = try {
                val payload = String(messageEvent.data)
                val json = JSONObject(payload)
                json.getInt("duration")
            } catch (e: Exception) {
                Log.e("WearOS", "Failed to parse timer duration; defaulting to 240 seconds", e)
                240
            }
            // Optionally update exercise title if included
            try {
                val payload = String(messageEvent.data)
                val json = JSONObject(payload)
                val title = json.optString("title", "Exercise")
            } catch (e: Exception) {
                Log.e("WearOS", "Failed to parse exercise title", e)
            }
            // Start timer with provided duration.
            startTimerCountUp()
            // Start sensor recording.
            startSensorRecording()
            // Start sending sensor data periodically.
            startSendingSensorData()
        }
    }
}
