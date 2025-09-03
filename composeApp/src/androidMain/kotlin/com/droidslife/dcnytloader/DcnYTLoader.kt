package com.droidslife.dcnytloader

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class DcnYTLoader : Application() {
    override fun onCreate() {
        super.onCreate()

        val notificationChannel =
            NotificationChannel(
                "Download_Notification",
                "YTDL",
                NotificationManager.IMPORTANCE_LOW,
            )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }
}
