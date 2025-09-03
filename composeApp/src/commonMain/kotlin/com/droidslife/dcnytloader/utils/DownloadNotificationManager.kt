package com.droidslife.dcnytloader.utils

import coil3.Image

interface DownloadNotificationManager {
    fun notifyDownloadResult(
        key: String,
        isError: Boolean,
        notificationInfo: NotificationInfo,
        showProgress: Boolean = false,
        progress: Int = 0,
    )

    fun updateNotification(
        id: String,
        progress: Int,
        completed: Boolean,
    )

    fun removeNotification(id: String)
}

data class NotificationInfo(
    val title: String,
    val description: String,
    val status: String,
    val key: String,
    val thumbnail: Image? = null,
)
