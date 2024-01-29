package com.imjori.flickrstroll.data.location

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.imjori.flickrstroll.R
import com.imjori.flickrstroll.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationService : Service() {

    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate() {
        super.onCreate()

        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "FlickrScroll:NotificationServiceWakeLockTag"
        )

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    @SuppressLint("WakelockTimeout")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        wakeLock.acquire()
        startForeground(NOTIFICATION_ID, getNotification())

        return super.onStartCommand(intent, flags, START_STICKY)
    }

    override fun onDestroy() {
        wakeLock.release()

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun getNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            MAIN_INTENT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this)
            .setContentTitle(getString(R.string.notification_content_title))
            .setContentText(getString(R.string.notification_content_text))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(IMPORTANCE_HIGH)
            .setSmallIcon(R.mipmap.ic_launcher)

        builder.setChannelId(NOTIFICATION_CHANNEL_ID)

        return builder.build()
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "1337"
        private const val NOTIFICATION_ID = 123
        private const val MAIN_INTENT_REQUEST_CODE = 147

        fun getIntent(context: Context) =
            Intent(context, NotificationService::class.java)
    }
}
