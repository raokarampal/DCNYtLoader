package com.droidslife.dcnytloader

import com.droidslife.dcnytloader.utils.DownloadNotificationManager
import com.droidslife.dcnytloader.utils.NotificationInfo

class DownloadNotificationManagerImpl : DownloadNotificationManager {
    override fun notifyDownloadResult(
        key: String,
        isError: Boolean,
        notificationInfo: NotificationInfo,
        showProgress: Boolean,
        progress: Int,
    ) {
        println("notifyDownloadResult $key - $isError - $notificationInfo - $showProgress - $progress")
    }

    override fun updateNotification(
        id: String,
        progress: Int,
        completed: Boolean,
    ) {
        println("downloadProgress $id - $progress - $completed")
    }

    override fun removeNotification(id: String) {
        println("removeNotification $id")
    }
}
