package com.tab.notificationscheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val PRIMARY_CHANNEL_ID: String = "primary_notification_channel"
const val NOTIFICATION_ID: Int = 0

class NotificationJobService : JobService() {

    lateinit var mNotificationManager: NotificationManager

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        CoroutineScope(Dispatchers.Default).launch {
            executeJob(params)
        }
        // return true when Job is done in background and needs to be rescheduled if it is not finished yet
        return true
    }

    private suspend fun executeJob(params: JobParameters?) {
        //Create Notification Channel
        createNotificationChannel()

        //Set content intent
        val intent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = createNotification(contentPendingIntent)
        mNotificationManager.notify(NOTIFICATION_ID, builder.build())
        jobFinished(params, false)
    }

    private fun createNotification(contentPendingIntent: PendingIntent): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content_text))
            .setContentIntent(contentPendingIntent)
            .setSmallIcon(R.drawable.ic_baseline_refresh_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
    }

    private fun createNotificationChannel() {
        //Get Notificaion Manager
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Check SDK version since Notif. Channel is only available on Oreo or above
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                PRIMARY_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.setLightColor(Color.RED)
            notificationChannel.enableVibration(true)
            notificationChannel.setDescription(getString(R.string.notification_channel_description))

            mNotificationManager.createNotificationChannel(notificationChannel)
        }
    }
}