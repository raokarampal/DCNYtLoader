package com.droidslife.dcnytloader.downloads.ui.download.history

import coil3.Image
import com.droidslife.dcnytloader.graphql.type.VideoInfoInput

sealed interface DownloadBottomSheetEvent {
    object OnDismiss : DownloadBottomSheetEvent

    data class GetVideoDetails(
        val url: String,
    ) : DownloadBottomSheetEvent

    data class DownloadVideo(
        val info: VideoInfoInput,
        val thumbnail: Image? = null,
    ) : DownloadBottomSheetEvent
}
