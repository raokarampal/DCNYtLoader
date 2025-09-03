package com.droidslife.dcnytloader.downloads.ui.download.history

import com.droidslife.dcnytloader.downloads.data.SimpleMsg
import com.droidslife.dcnytloader.graphql.fragment.DownloadHistoryListFragment.DownloadFolder
import com.droidslife.dcnytloader.graphql.fragment.DownloadHistoryListFragment.DownloadHistory
import com.droidslife.dcnytloader.graphql.fragment.DownloadUpdateFragment
import com.droidslife.dcnytloader.graphql.fragment.VideoInfoFragment

data class DownloadHistoryState(
    val downloadsHistory: List<DownloadHistory> = emptyList(),
    val selectedDownload: DownloadHistory? = null,
    val isLoading: Boolean = false,
    val messages: Map<String, SimpleMsg> = emptyMap(),
    val folders: List<DownloadFolder> = emptyList(),
)

data class NewDownloadState(
    val isLoading: Boolean = false,
    val messages: Map<String, SimpleMsg> = emptyMap(),
    val onGoingDownloadsDetails: Map<String, DownloadUpdateFragment> = emptyMap(),
    val currentRequestInfo: VideoInfoFragment? = null,
    val categoryList: List<DownloadCategory> = DownloadCategory.entries,
)

enum class DownloadCategory {
    Music,
    Haryanvi,
    Punjabi,
    Filmi,
    Ragni,
    Bakti,
    Stage,
    Natak,
    other,
}
