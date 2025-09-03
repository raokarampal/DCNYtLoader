package com.droidslife.dcnytloader.graphql.schema.models

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.collections.firstOrNull
import kotlin.collections.lastOrNull

@Serializable
@SerialName("VideoDownloadRequest")
data class VideoDownloadRequest(
    val videoId: String,
    val path: String,
    val category: String,
    val videoFormat: String,
    val audioFormat: String,
)

@Serializable
@SerialName("VideoInfo")
data class VideoInfo(
    val videoID: String,
    val title: String,
    val desc: String?,
    val category: List<String>,
    val channelName: String,
    val thumbnail: String?,
    val formats: List<FormatDetails>?,
    val status: DownloadStatus = DownloadStatus.UNKNOWN,
    val defaultFormatVideo: FormatDetails? = FormatDetails(),
    val defaultFormatAudio: FormatDetails? = FormatDetails(),
    val downloadPath: String = "download",
    val downloadCategory: String = "Music",
) {
    fun toDownloadHistory(): DownloadHistory =
        DownloadHistory(
            videoID = videoID,
            title = title,
            thumbnail = thumbnail,
            desc = desc,
            category = category,
            channelName = channelName,
            selectedFormats = listOf(defaultFormatVideo, defaultFormatAudio),
            status = status,
            downloadPath = downloadPath,
            downloadCategory = downloadCategory,
        )

    // Custom equals and hashCode to ensure uniqueness based on 'videoId' for Set operations
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DownloadHistory) return false
        return videoID == other.videoID
    }

    override fun hashCode(): Int = videoID.hashCode()
}

@Serializable
@SerialName("FormatDetails")
data class FormatDetails(
    val acodec: String = "",
    val vcodec: String = "",
    val ext: String = "",
    val resolution: String = "",
    val format: String = "",
    val url: String = "",
    @SerialName("format_id")
    val formatId: String = "",
)

@Serializable
@SerialName("DownloadUpdates")
data class DownloadUpdates(
    val id: String,
    val progress: Double,
    @Polymorphic
    val msg: ParsedDownloadProgressInfo?,
) {
    // Custom equals and hashCode to ensure uniqueness based on 'id' for Set operations
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DownloadUpdates) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

@Serializable
@SerialName("DownloadHistory")
data class DownloadHistory(
    val videoID: String,
    val title: String,
    val desc: String?,
    val category: List<String>,
    val channelName: String,
    val thumbnail: String?,
    val selectedFormats: List<FormatDetails?> = emptyList(),
    val status: DownloadStatus,
    val downloadPath: String,
    val downloadCategory: String,
    val downloadUpdates: DownloadUpdates? = null,
) {
    fun toVideoInfo(): VideoInfo =
        VideoInfo(
            videoID = videoID,
            title = title,
            thumbnail = thumbnail,
            desc = desc,
            category = category,
            channelName = channelName,
            formats = null,
            status = status,
            defaultFormatVideo = selectedFormats.firstOrNull(),
            defaultFormatAudio = selectedFormats.lastOrNull(),
            downloadPath = downloadPath,
            downloadCategory = downloadCategory,
        )

    // Custom equals and hashCode to ensure uniqueness based on 'videoId' for Set operations
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DownloadHistory) return false
        return videoID == other.videoID
    }

    override fun hashCode(): Int = videoID.hashCode()
}

@Serializable
@SerialName("SimpleMsg")
data class SimpleMsg(
    val id: String? = "",
    val msg: String? = "",
)

@Serializable
@SerialName("DownloadHistoryList")
data class DownloadHistoryList(
    val downloadHistory: List<DownloadHistory>,
    val downloadFolders: List<DownloadFolder> = emptyList(),
)

@Serializable
@SerialName("DownloadPath")
data class DownloadPath(
    val downloadPath: String = "",
    val downloadCategory: String = "",
)

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    DUPLICATE,
    COMPLETED,
    FAILED,
    UNKNOWN,
}
