package com.imjori.flickrstroll.data.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.imjori.flickrstroll.R
import com.imjori.flickrstroll.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@AndroidEntryPoint
class LocationPollingService : LifecycleService() {

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var trackDistanceSinceLastPhotoRequest: TrackDistanceSinceLastPhotoRequest

    private var locationFlow: Job? = null

    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        subscribeToLocationUpdates()

        return super.onStartCommand(intent, flags, START_STICKY)
    }

    override fun onDestroy() {
        unsubscribeFromLocationUpdates()
        super.onDestroy()
    }

    private fun subscribeToLocationUpdates() {
        startForeground(NOTIFICATION_ID, getNotification())

        locationFlow = locationRepository.locationFlow()
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { currentLocation ->
                trackDistanceSinceLastPhotoRequest(currentLocation)
            }
            .launchIn(lifecycleScope)
    }

    private fun unsubscribeFromLocationUpdates() {
        trackDistanceSinceLastPhotoRequest.reset()
        locationFlow?.cancel()
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
            Intent(context, LocationPollingService::class.java)
    }
}
