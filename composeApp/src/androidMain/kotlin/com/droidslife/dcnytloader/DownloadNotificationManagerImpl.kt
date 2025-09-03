package com.droidslife.dcnytloader

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import coil3.toBitmap
import com.droidslife.dcnytloader.utils.DownloadNotificationManager
import com.droidslife.dcnytloader.utils.NotificationInfo
import kotlin.random.Random

class DownloadNotificationManagerImpl(
    private val context: Context,
) : DownloadNotificationManager {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val activeNotifications = mutableMapOf<String, NotificationData>()

    fun showBasicNotification(
        info: NotificationInfo,
        notificationID: Int = Random.nextInt(),
    ): NotificationData {
        val notification =
            NotificationCompat
                .Builder(context, "Download_Notification")
                .setContentTitle(info.title)
                .setContentText(info.description)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setAutoCancel(true)
        // optionally: setStyle for bitmap
        return NotificationData(
            notification = notification,
            notificationID = notificationID,
            key = info.key,
        )
    }

    fun showExpandableNotification(
        info: NotificationInfo,
        notificationID: Int = Random.nextInt(),
    ): NotificationData {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                321,
                intent,
                PendingIntent.FLAG_MUTABLE,
            )

        val notification =
            NotificationCompat
                .Builder(context, "Download_Notification")
                .setContentTitle(info.title)
                .setContentText(info.description)
                .setSubText(info.status)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(
                    NotificationCompat.BigPictureStyle().bigPicture(info.thumbnail?.toBitmap()),
                )

        val notificationData =
            NotificationData(
                notification = notification,
                notificationID = notificationID,
                key = info.key,
            )
        addNotification(info.key, notificationData)

        return notificationData
    }

    fun addNotification(
        key: String,
        data: NotificationData,
    ) {
        activeNotifications[key] = data
    }

    override fun notifyDownloadResult(
        key: String,
        isError: Boolean,
        notificationInfo: NotificationInfo,
        showProgress: Boolean,
        progress: Int,
    ) {
        val notificationData =
            activeNotifications[key] ?: showExpandableNotification(notificationInfo)
        val builder = notificationData.notification

        if (isError) {
            notificationManager.notify(
                notificationData.notificationID,
                builder.setContentText(notificationInfo.description).build(),
            )
        } else {
            notificationManager.notify(
                notificationData.notificationID,
                builder.setProgress(100, progress, false).build(),
            )
        }
    }

    override fun updateNotification(
        id: String,
        progress: Int,
        completed: Boolean,
    ) {
        val notificationData = activeNotifications[id] ?: return
        val builder = notificationData.notification

        if (completed) {
            notificationManager.notify(
                notificationData.notificationID,
                builder
                    .setContentText("Download complete")
                    .setProgress(0, 0, false)
                    .setOngoing(false)
                    .setSilent(false)
                    .setAutoCancel(true)
                    .build(),
            )
            activeNotifications.remove(id)
        } else {
            notificationManager.notify(
                notificationData.notificationID,
                builder.setProgress(100, progress, false).setSilent(true).build(),
            )
        }
    }

    override fun removeNotification(id: String) {
        activeNotifications.remove(id)
    }
}

data class NotificationData(
    val notification: NotificationCompat.Builder,
    val notificationID: Int,
    val key: String,
)

fun ByteArray.toBitmap(): Bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
