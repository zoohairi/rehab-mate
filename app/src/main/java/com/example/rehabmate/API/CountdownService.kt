import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.concurrent.thread

class CountdownService : Service() {

    private val client = OkHttpClient()
    private val handler = Handler()

    override fun onCreate() {
        super.onCreate()

        // Schedule periodic API calls every 10 minutes (600,000 ms)
        handler.postDelayed(object : Runnable {
            override fun run() {
                fetchCountdownData()
                handler.postDelayed(this, 600000) // Call again after 10 minutes
            }
        }, 600000) // First call after 10 minutes
    }

    private fun fetchCountdownData() {
        // Your API URL
        val url = "https://api.tickcounter.com/v1/countdown"  // Replace with your actual API URL

        // Create the request
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer YOUR_API_KEY")  // Replace with your API key
            .build()

        // Make the network request in a background thread
        thread {
            try {
                val response: Response = client.newCall(request).execute()

                // Parse the response
                if (response.isSuccessful) {
                    val responseData = response.body?.string() ?: ""
                    val jsonObject = JSONObject(responseData)

                    // Extract data (example: countdown value)
                    val countdown = jsonObject.getString("countdown")

                    // Optionally broadcast or store the result for use by the main activity
                    sendBroadcast(Intent("com.yourapp.COUNTDOWN_UPDATE").apply {
                        putExtra("countdown", countdown)
                    })
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        // We are not binding this service
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Clean up handlers when the service is destroyed
    }
}
