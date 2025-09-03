package com.droidslife.dcnytloader.downloads.data

import com.apollographql.apollo.api.Optional
import com.droidslife.dcnytloader.downloads.ui.download.history.DownloadCategory
import com.droidslife.dcnytloader.graphql.fragment.DownloadHistoryFragment
import com.droidslife.dcnytloader.graphql.fragment.FormatDetailFragment
import com.droidslife.dcnytloader.graphql.fragment.VideoInfoFragment
import com.droidslife.dcnytloader.graphql.type.DownloadStatus
import com.droidslife.dcnytloader.graphql.type.FormatDetailsInput
import com.droidslife.dcnytloader.graphql.type.VideoInfoInput

data class MinimalVideoInfo(
    val videoId: String,
    val title: String,
    val desc: String?,
    val thumbnail: String?,
    val channelName: String,
    val status: DownloadStatus,
    val defaultCategory: DownloadCategory = DownloadCategory.Music,
    val defaultVideoFormatIdIndex: Int? = null,
    val defaultAudioFormatIdIndex: Int? = null,
    val videoFormats: List<FormatDetailFragment>? = null,
    val audioFormats: List<FormatDetailFragment>? = null,
//    val videoFormatIds: List<String>? = null,
//    val audioFormatIds: List<String>? = null,
)

fun VideoInfoFragment.toMinimalVideoInfo(): MinimalVideoInfo {
    val (audioFormats, videoFormats) =
        formats?.map { it.formatDetailFragment }?.partition {
            it.resolution.contains("audio")
        } ?: Pair(emptyList<FormatDetailFragment>(), emptyList<FormatDetailFragment>())

    return MinimalVideoInfo(
        videoId = videoID,
        title = title,
        desc = desc,
        thumbnail = thumbnail,
        channelName = channelName,
        status = status,
        defaultCategory = title.toCategory(),
        defaultVideoFormatIdIndex =
            videoFormats.indexOfFirst { format ->
                defaultFormatVideo
                    ?.formatDetailFragment
                    ?.formatId
                    ?.let { it == format.formatId }
                    ?: false
            },
        defaultAudioFormatIdIndex =
            audioFormats.indexOfFirst { format ->
                defaultFormatAudio
                    ?.formatDetailFragment
                    ?.formatId
                    ?.let { it == format.formatId }
                    ?: false
            },
        videoFormats = videoFormats,
        audioFormats = audioFormats,
//        videoFormatIds =
//            videoFormats.map {
//                it.formatDetailFragment.formatId.replaceFirst(
//                    Regex("""^(\d{1,3}).*"""),
//                    "$1",
//                )
//            },
//        audioFormatIds =
//            audioFormats.map {
//                it.formatDetailFragment.formatId.replaceFirst(
//                    Regex("""^(\d{1,3}).*"""),
//                    "$1",
//                )
//            },
    )
}

private fun String.toCategory(): DownloadCategory =
    when {
        contains(
            DownloadCategory.Haryanvi.name,
            ignoreCase = true,
        ) -> DownloadCategory.Haryanvi

        contains(DownloadCategory.Punjabi.name, ignoreCase = true) -> DownloadCategory.Punjabi
        contains(DownloadCategory.Natak.name, ignoreCase = true) -> DownloadCategory.Natak
        contains(DownloadCategory.Filmi.name, ignoreCase = true) -> DownloadCategory.Filmi
        contains(DownloadCategory.Ragni.name, ignoreCase = true) -> DownloadCategory.Ragni
        contains(DownloadCategory.Stage.name, ignoreCase = true) -> DownloadCategory.Stage
        else -> DownloadCategory.Music
    }

fun FormatDetailFragment.toFormatDetailInput(): FormatDetailsInput =
    FormatDetailsInput(
        acodec = acodec,
        ext = ext,
        format = format,
        formatId = formatId,
        resolution = resolution,
        url = url,
        vcodec = vcodec,
    )

fun VideoInfoFragment.toVideoInfoInput(
    defaultVideoFormat: FormatDetailFragment?,
    defaultAudioFormat: FormatDetailFragment?,
    defaultCategory: DownloadCategory,
): VideoInfoInput =
    VideoInfoInput(
        category = category,
        channelName = channelName,
        defaultFormatAudio = Optional.present(defaultAudioFormat?.toFormatDetailInput()),
        defaultFormatVideo = Optional.present(defaultVideoFormat?.toFormatDetailInput()),
        desc = Optional.present(desc),
        downloadCategory = defaultCategory.name,
        downloadPath = downloadPath,
        formats = Optional.present(formats?.map { it.formatDetailFragment.toFormatDetailInput() }),
        status = status,
        thumbnail = Optional.present(thumbnail),
        title = title,
        videoID = videoID,
    )

fun DownloadHistoryFragment.toMinimalVideoInfo() =
    MinimalVideoInfo(
        videoId = videoID,
        title = title,
        desc = desc,
        thumbnail = thumbnail,
        channelName = channelName,
        status = status,
    )
