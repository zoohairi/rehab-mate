package com.example.rehabmate.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.rehabmate.R


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New Token: $token")

        // ✅ Save new token to Firestore
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("Users")
                .document(uid)
                .update("fcm_token", token)
                .addOnSuccessListener { Log.d("FCM", "Token updated in Firestore") }
                .addOnFailureListener { Log.e("FCM", "Failed to update token", it) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Log the message
        Log.d("FCM", "Message received: ${message.data}")

        // Extract notification data (you can also get data payload if you use data messages)
        val title = message.notification?.title ?: "RehabMate"
        val body = message.notification?.body ?: "You have a new notification"

        // Show system notification
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "rehabmate_channel"

        // Create channel (for Android 8+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "RehabMate Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for RehabMate notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app icon
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Show notification
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

}
