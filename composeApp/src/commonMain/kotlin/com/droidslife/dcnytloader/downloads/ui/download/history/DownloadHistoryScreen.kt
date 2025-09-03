package com.droidslife.dcnytloader.downloads.ui.download.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.droidslife.dcnytloader.downloads.data.toMinimalVideoInfo
import com.droidslife.dcnytloader.downloads.ui.MainViewModel
import com.droidslife.dcnytloader.downloads.ui.ShareIntentUiState
import com.droidslife.dcnytloader.graphql.fragment.DownloadHistoryFragment
import com.droidslife.dcnytloader.graphql.fragment.DownloadHistoryListFragment.DownloadFolder
import com.droidslife.dcnytloader.graphql.fragment.DownloadHistoryListFragment.DownloadHistory
import com.droidslife.dcnytloader.graphql.fragment.DownloadUpdateFragment
import com.droidslife.dcnytloader.graphql.type.DownloadStatus
import com.droidslife.dcnytloader.theme.LocalThemeIsDark
import com.droidslife.dcnytloader.utils.ContentByState
import com.droidslife.dcnytloader.utils.ScreenUiState
import dcnytloader.composeapp.generated.resources.Res
import dcnytloader.composeapp.generated.resources.check_circle
import dcnytloader.composeapp.generated.resources.clear_day
import dcnytloader.composeapp.generated.resources.dark_mode
import dcnytloader.composeapp.generated.resources.file_download
import dcnytloader.composeapp.generated.resources.outline_cloud_alert
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel(),
) {
    val sharedContentUiState by viewModel.sharedContentUiState.collectAsStateWithLifecycle()
    val historyUiState by viewModel.historyUiState.collectAsStateWithLifecycle()
    val folderUiState by viewModel.folderUiState.collectAsStateWithLifecycle()
    val videoInfoUiState by viewModel.videoInfoUiState.collectAsStateWithLifecycle()
    val onGoingDownloadsDetailUiState by viewModel.onGoingDownloadsDetailUiState.collectAsStateWithLifecycle()

    var openBottomSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(sharedContentUiState.type) {
        when (val it = sharedContentUiState) {
            is ShareIntentUiState.HasTxt -> {
                openBottomSheet = true
                println("text received in ui state : ${it.txt}")
            }

            else -> {}
        }
    }
    Scaffold(
        topBar = {
            var isDark by LocalThemeIsDark.current
            val icon = if (isDark) Res.drawable.clear_day else Res.drawable.dark_mode
            TopAppBar(
                title = { Text(text = "DCN YT Downloader") },
                actions = {
                    IconButton(onClick = { isDark = !isDark }) {
                        Icon(
                            vectorResource(icon),
                            contentDescription = "Toggle Theme",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "Download") },
                icon = {
                    Icon(
                        vectorResource(Res.drawable.file_download),
                        contentDescription = "Download",
                    )
                },
                onClick = {
                    openBottomSheet = !openBottomSheet
                },
            )
        },
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues).fillMaxSize()) {
            Box(
                Modifier
                    .padding(8.dp)
                    .border(
                        width = 1.dp,
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ),
            ) {
                Row(Modifier.fillMaxWidth()) {
                    ContentByState(
                        uiState = folderUiState,
                        emptyMessage = "No Folder Found",
                        successContent = { DownloadFoldersUi(folders = it) },
                    )
                }
            }

            ContentByState(
                uiState = historyUiState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                retryOnError = viewModel::getDownloadHistory,
                successContent = {
                    DownloadHistoryUi(
                        it,
                        onGoingDownloadsDetailUiState,
                        modifier =
                            Modifier.pullToRefresh(
                                isRefreshing = historyUiState is ScreenUiState.Loading,
                                state = rememberPullToRefreshState(),
                                onRefresh = viewModel::getDownloadHistory,
                            ),
                    )
                },
            )
        }
    }
    if (openBottomSheet) {
        DownloadBottomSheet(sharedContentUiState, videoInfoUiState) {
            when (it) {
                DownloadBottomSheetEvent.OnDismiss -> {
                    openBottomSheet = false
                }

                is DownloadBottomSheetEvent.DownloadVideo -> {
                    viewModel.startDownload(it.info, it.thumbnail)
                    openBottomSheet = false
                }

                is DownloadBottomSheetEvent.GetVideoDetails -> {
                    viewModel.fetchVideoDetails(it.url)
                }
            }
        }
    }
}

@Composable
fun DownloadHistoryUi(
    histories: List<DownloadHistory>,
    onGoingDownloads: ScreenUiState<Map<String, DownloadUpdateFragment>>,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(columns = GridCells.Adaptive(400.dp), modifier = modifier) {
        items(histories) { item ->
            val info = item.downloadHistoryFragment.toMinimalVideoInfo()

            VideoDetailCardUi(
                videoInfo = info,
                videoCardTheme = VideoCardTheme.ThumbnailOnTop,
                subContent = {
                    Row(
                        modifier = Modifier.padding(4.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        VideoFormatAndCategoryUi(
                            item.downloadHistoryFragment.selectedFormats,
                            item.downloadHistoryFragment.downloadCategory,
                            modifier = Modifier.weight(1f),
                        )

                        DownloadStatusUi(info.status, Modifier.size(32.dp))
                    }
                },
                footerContent = {
                    if (onGoingDownloads is ScreenUiState.Success) {
                        onGoingDownloads.data[info.videoId]?.let {
                            if (info.status != DownloadStatus.COMPLETED) {
                                Row(Modifier.fillMaxWidth()) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth(),
                                        progress = { it.progress.toFloat().div(100f) },
                                    )
                                }
                            }
                        }
                    }
                },
            )
        }
    }
}

@Composable
fun VideoFormatAndCategoryUi(
    selectedFormats: List<DownloadHistoryFragment.SelectedFormat?>,
    downloadCategory: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val boxModifier =
        Modifier
            .background(
                color = colorScheme.background,
                shape = RoundedCornerShape(12.dp),
            )
    FlowRow(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        selectedFormats.firstOrNull()?.let {
            Box(
                boxModifier,
            ) {
                Text(
                    it.formatDetailFragment.format,
                    Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Box(
            boxModifier,
        ) {
            Text(
                downloadCategory,
                Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun DownloadStatusUi(
    status: DownloadStatus,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    when (status) {
        DownloadStatus.PENDING, DownloadStatus.DOWNLOADING -> {
            Icon(
                vectorResource(Res.drawable.file_download),
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = modifier,
            )
        }

        DownloadStatus.DUPLICATE -> {
            Icon(
                vectorResource(Res.drawable.check_circle),
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = modifier.padding(4.dp),
            )
        }

        DownloadStatus.COMPLETED -> {
            Icon(
                vectorResource(Res.drawable.check_circle),
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = modifier.padding(4.dp),
            )
        }

        else -> {
            IconButton(onClick = {}, modifier = modifier) {
                Icon(
                    vectorResource(Res.drawable.outline_cloud_alert),
                    contentDescription = null,
                    tint = colorScheme.error,
                )
            }
        }
    }
}

@Composable
fun DownloadFoldersUi(
    folders: List<DownloadFolder>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(folders) { item ->
            OutlinedCard {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = item.folderName)
                    Text(
                        text =
                            item.fileDetails
                                .flatMap { it.files }
                                .size
                                .toString(),
                    )
                }
            }
        }
    }
}
