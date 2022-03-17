package com.nghanyi.bitcoinsciousness

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.text.DecimalFormat

class NotiService : Service(), CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private var current10MA: Double = 0.0
    private var previous10MA: Double = 0.0

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * Retrieve current and previous 10MA from intent
     * and start the foreground service.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        Log.d(TAG, "Current 10 MA: $current10MA")
        Log.d(TAG, "Previous 10 MA: $previous10MA")

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        current10MA = intent?.getDoubleExtra("current10MA", 0.0)!!
        previous10MA = intent.getDoubleExtra("previous10MA", 0.0)

        startForeground()
        return START_NOT_STICKY
    }

    /**
     * 1. Create a notification channel
     * 2. Create a pending intent
     * 3. Create a notification
     * 4. Start the foreground service
     */
    private fun startForeground() {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Foreground Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = createNotification(pendingIntent)

        startForeground(1, notification)
    }

    /**
     * Create a notification.
     */
    private fun createNotification(pendingIntent: PendingIntent) = notificationBuilder
        .setContentTitle(getString(R.string.noti_title))
        .setContentText(getNotiText())
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentIntent(pendingIntent)
        .build()

    /**
     * Determine the text of the notification
     * and round the current 10 MA to 2 decimal places.
     */
    private fun getNotiText(): String {
        val df = DecimalFormat("#.##")
        val roundedCurrent10MA = df.format(current10MA)
        return when {
            current10MA > previous10MA -> {
                "10MA: $roundedCurrent10MA UP"
            }
            current10MA < previous10MA -> {
                "10MA: $roundedCurrent10MA DOWN"
            }
            else -> {
                "10MA: $roundedCurrent10MA FLAT"
            }
        }
    }

    /**
     * Create a Notification channel
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val channel = NotificationChannel(channelId,
        channelName, NotificationManager.IMPORTANCE_NONE)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }

    companion object {
        private val TAG = NotiService::class.java.simpleName
    }
}