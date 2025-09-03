package com.droidslife.dcnytloader.graphql.schema.models

import kotlinx.serialization.Serializable

@Serializable
data class YtdlVideoInfo(
    val id: String? = "",
    val title: String? = "",
    val description: String? = "",
    val categories: List<String>? = emptyList(),
    val channel: String? = "",
    val thumbnail: String? = "",
    val formats: List<FormatDetails>? = emptyList(),
    val defaultFormatVideo: FormatDetails? = null,
    val defaultFormatAudio: FormatDetails? = null,
) {
    // Process formats and filter out those with "storyboard"
    val filteredFormats: List<FormatDetails>
        get() =
            formats?.let { list -> list.filterNot { it.format.contains("storyboard") } }
                ?: emptyList()

    // Get default video format
    val defaultVideoFormat: FormatDetails?
        get() =
            filteredFormats.firstOrNull {
                it.formatId == "137" || (
                    it.vcodec.contains("avc1") &&
                        it.resolution.contains(
                            "1920",
                        )
                )
            }

    // Get default audio format
    val defaultAudioFormat: FormatDetails?
        get() =
            filteredFormats.firstOrNull {
                it.formatId == "140" || (
                    it.resolution.contains("audio") &&
                        it.format.contains(
                            "high",
                        )
                )
            }
}

fun YtdlVideoInfo.toVideoInfo(
    downloadCategory: String,
    downloadPath: String,
): VideoInfo =
    VideoInfo(
        videoID = id ?: "",
        title = title ?: "",
        thumbnail = thumbnail,
        desc = description,
        category = categories ?: emptyList(),
        channelName = channel ?: "",
        formats = filteredFormats,
        defaultFormatVideo = defaultVideoFormat,
        defaultFormatAudio = defaultAudioFormat,
        downloadCategory = downloadCategory,
        downloadPath = downloadPath,
        status = DownloadStatus.UNKNOWN,
    )
