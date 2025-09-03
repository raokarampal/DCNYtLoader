package com.droidslife.dcnytloader.downloads.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.Image
import com.droidslife.dcnytloader.downloads.data.SimpleMsg
import com.droidslife.dcnytloader.downloads.data.YtdlService
import com.droidslife.dcnytloader.downloads.ui.download.history.DownloadHistoryState
import com.droidslife.dcnytloader.downloads.ui.download.history.NewDownloadState
import com.droidslife.dcnytloader.graphql.fragment.DownloadHistoryListFragment.DownloadFolder
import com.droidslife.dcnytloader.graphql.fragment.DownloadHistoryListFragment.DownloadHistory
import com.droidslife.dcnytloader.graphql.fragment.DownloadUpdateFragment
import com.droidslife.dcnytloader.graphql.fragment.VideoInfoFragment
import com.droidslife.dcnytloader.graphql.type.DownloadStatus
import com.droidslife.dcnytloader.graphql.type.VideoInfoInput
import com.droidslife.dcnytloader.utils.ContentType
import com.droidslife.dcnytloader.utils.DownloadNotificationManager
import com.droidslife.dcnytloader.utils.NotificationInfo
import com.droidslife.dcnytloader.utils.PlatformContext
import com.droidslife.dcnytloader.utils.ScreenUiState
import com.droidslife.dcnytloader.utils.SharedContent
import com.droidslife.dcnytloader.utils.handleShareIntent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainViewModel(
    private val ytdlService: YtdlService,
    private val notificationManager: DownloadNotificationManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel(),
    KoinComponent {
    private val _downloadHistoryState = MutableStateFlow(DownloadHistoryState(isLoading = true))
    private val _newDownloadState = MutableStateFlow(NewDownloadState(isLoading = false))

    private val platformContext: PlatformContext by inject()
    val sharedContentUiState =
        savedStateHandle
            .handleShareIntent(platformContext)
            .map {
                when (it) {
                    SharedContent.EmptyContent ->
                        ShareIntentUiState.NoData(
                            ContentType.EMPTY,
                            "error",
                        )

                    is SharedContent.SharedImageContent ->
                        ShareIntentUiState.HasImage(
                            ContentType.IMAGE,
                            "success",
                            it.imageBytes,
                        )

                    is SharedContent.SharedTxtContent ->
                        ShareIntentUiState.HasTxt(
                            ContentType.PLAIN_TEXT,
                            "success",
                            it.textContent,
                        )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ShareIntentUiState.NoData(ContentType.EMPTY, "error"),
            )

    val folderUiState: StateFlow<ScreenUiState<List<DownloadFolder>>> =
        _downloadHistoryState
            .map {
                when {
                    it.isLoading -> ScreenUiState.Loading
                    it.folders.isEmpty() -> ScreenUiState.Empty
                    else -> ScreenUiState.Success(it.folders)
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, ScreenUiState.Loading)

    val historyUiState: StateFlow<ScreenUiState<List<DownloadHistory>>> =
        _downloadHistoryState
            .map {
                when {
                    it.isLoading -> ScreenUiState.Loading
                    it.downloadsHistory.isEmpty() -> ScreenUiState.Empty
                    else -> ScreenUiState.Success(it.downloadsHistory)
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, ScreenUiState.Loading)
    val videoInfoUiState: StateFlow<ScreenUiState<VideoInfoFragment>> =
        _newDownloadState
            .map {
                when {
                    it.isLoading -> ScreenUiState.Loading
                    it.currentRequestInfo == null -> ScreenUiState.Empty
                    else -> ScreenUiState.Success(it.currentRequestInfo)
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, ScreenUiState.Loading)

    val onGoingDownloadsDetailUiState: StateFlow<ScreenUiState<Map<String, DownloadUpdateFragment>>> =
        _newDownloadState
            .map {
                when {
                    it.isLoading -> ScreenUiState.Loading
                    it.onGoingDownloadsDetails.isEmpty() -> ScreenUiState.Empty
                    else -> ScreenUiState.Success(it.onGoingDownloadsDetails)
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, ScreenUiState.Loading)

    init {
        getDownloadHistory()
        subscribeToDownloadUpdates()
    }

    private fun subscribeToDownloadUpdates() {
        viewModelScope.launch {
            ytdlService.subscribeToDownloadUpdates().collect { result ->
                result.onSuccess { update ->
                    if (!_downloadHistoryState.value.downloadsHistory.any { it.downloadHistoryFragment.videoID == update.id } ||
                        update.msg?.otherInfoFragment?.status == DownloadStatus.COMPLETED
                    ) {
                        getDownloadHistory()
                    }
                    _newDownloadState.update { state ->
                        state.copy(
                            onGoingDownloadsDetails = state.onGoingDownloadsDetails.plus(update.id to update),
                        )
                    }

                    notificationManager.updateNotification(
                        update.id,
                        update.progress.toInt(),
                        update.progress.toInt() >= 100,
                    )
                }
            }
        }
    }

    fun getDownloadHistory() {
        _downloadHistoryState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            ytdlService.fetchDownloadHistory().collect { result ->
                result
                    .onSuccess {
                        _downloadHistoryState.update { state ->
                            state.copy(
                                isLoading = false,
                                downloadsHistory = it.downloadHistory.reversed(),
                                folders = it.downloadFolders,
                            )
                        }
                    }.onFailure {
                        _downloadHistoryState.update { it.copy(isLoading = false) }
                    }
            }
        }
    }

    fun fetchVideoDetails(url: String) {
        _newDownloadState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            ytdlService
                .fetchVideoDetails(url)
                .catch { e ->
                    _newDownloadState.update {
                        it.copy(
                            isLoading = false,
                            messages = mapOf(SimpleMsg(e.message ?: "Unknown error").toPair()),
                        )
                    }
                }.collect { result ->
                    result
                        .onSuccess {
                            _newDownloadState.update { state ->
                                state.copy(
                                    isLoading = false,
                                    currentRequestInfo = it,
                                )
                            }
                        }.onFailure { e ->
                            _newDownloadState.update {
                                it.copy(
                                    isLoading = false,
                                    messages =
                                        mapOf(
                                            SimpleMsg(
                                                e.message ?: "Unknown error",
                                            ).toPair(),
                                        ),
                                )
                            }
                        }
                }
        }
    }

    fun startDownload(
        videoInfo: VideoInfoInput,
        thumbnail: Image? = null,
    ) {
        _newDownloadState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            ytdlService.startDownloadingVideo(videoInfo).collect { result ->
                result
                    .onSuccess {
                        notificationManager.notifyDownloadResult(
                            key = videoInfo.videoID,
                            isError = false,
                            notificationInfo =
                                NotificationInfo(
                                    title = videoInfo.title,
                                    description = it.msg ?: "",
                                    status = "Downloading",
                                    key = videoInfo.videoID,
                                    thumbnail = thumbnail,
                                ),
                            showProgress = true,
                            progress = 0,
                        )
                        _newDownloadState.update { state ->
                            state.copy(
                                isLoading = false,
                                messages = state.messages.plus(it.id to it),
                                currentRequestInfo = null,
                            )
                        }
                    }.onFailure {
                        _newDownloadState.update { state ->
                            state.copy(
                                isLoading = false,
                                messages =
                                    state.messages.plus(
                                        SimpleMsg(
                                            it.message ?: "Unknown error",
                                        ).toPair(),
                                    ),
                            )
                        }
                        notificationManager.notifyDownloadResult(
                            key = videoInfo.videoID,
                            isError = true,
                            notificationInfo =
                                NotificationInfo(
                                    title = videoInfo.title,
                                    description = it.message ?: "Unknown error",
                                    status = "Failed",
                                    key = videoInfo.videoID,
                                ),
                        )
                    }
            }
        }
    }
}
