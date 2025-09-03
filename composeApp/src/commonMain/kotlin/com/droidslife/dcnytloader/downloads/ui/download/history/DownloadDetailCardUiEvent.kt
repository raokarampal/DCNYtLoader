package com.droidslife.dcnytloader.downloads.ui.download.history

sealed interface DownloadDetailCardUiEvent {
    object ReDownload : DownloadDetailCardUiEvent
    object UpdatePathAndCategory : DownloadDetailCardUiEvent
}