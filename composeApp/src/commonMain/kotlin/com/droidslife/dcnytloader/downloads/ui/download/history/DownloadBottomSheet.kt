package com.droidslife.dcnytloader.downloads.ui.download.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.Image
import com.droidslife.dcnytloader.downloads.data.toMinimalVideoInfo
import com.droidslife.dcnytloader.downloads.data.toVideoInfoInput
import com.droidslife.dcnytloader.downloads.ui.ShareIntentUiState
import com.droidslife.dcnytloader.graphql.fragment.VideoInfoFragment
import com.droidslife.dcnytloader.utils.ContentByState
import com.droidslife.dcnytloader.utils.CustomFilterChip
import com.droidslife.dcnytloader.utils.ScreenUiState
import com.droidslife.dcnytloader.utils.setPlatformClipEntry
import dcnytloader.composeapp.generated.resources.Res
import dcnytloader.composeapp.generated.resources.search
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun DownloadBottomSheet(
    sharedContentUiState: ShareIntentUiState,
    videoInfoUiState: ScreenUiState<VideoInfoFragment>,
    onEvent: (DownloadBottomSheetEvent) -> Unit,
) {
    val clipboard = LocalClipboard.current
    val (url, onValueChange) = remember { mutableStateOf("") }
    val (thumbnail, onThumbnailLoad) = remember { mutableStateOf<Image?>(null) }
    LaunchedEffect(sharedContentUiState.type) {
        when (sharedContentUiState) {
            is ShareIntentUiState.HasTxt -> {
                onValueChange(sharedContentUiState.txt)
            }

            else -> {
                runCatching {
                    clipboard.getClipEntry()?.let { clip ->
                        setPlatformClipEntry(clip)?.let {
                            println(it)
                            onValueChange(it.toString())
                        }
                    }
                }.getOrElse {
                    println("Failed to get clipboard data: ${it.message}")
                }
            }
        }
    }
    LaunchedEffect(url, clipboard) {
        if (url.isNotEmpty()) {
            delay(1000)
            onEvent(DownloadBottomSheetEvent.GetVideoDetails(url))
        }
    }

    ModalBottomSheet(
        onDismissRequest = { onEvent(DownloadBottomSheetEvent.OnDismiss) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = onValueChange,
                    modifier =
                        Modifier.fillMaxWidth(),
                    label = { Text(text = "Url") },
                    shape = RoundedCornerShape(32.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (url.isNotEmpty()) {
                                onEvent(DownloadBottomSheetEvent.GetVideoDetails(url))
                            }
                        }) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.search),
                                contentDescription = "search",
                            )
                        }
                    },
                )
            }
            ContentByState(
                uiState = videoInfoUiState,
                modifier = Modifier.padding(16.dp),
                emptyMessage = "No video found",
                successContent = { info ->
                    val videoInfo = info.toMinimalVideoInfo()
                    var defaultVideoFormat by remember { mutableStateOf(info.defaultFormatVideo?.formatDetailFragment) }
                    var defaultAudioFormat by remember { mutableStateOf(info.defaultFormatAudio?.formatDetailFragment) }
                    var defaultCategory by remember { mutableStateOf(DownloadCategory.Music) }
                    val videoList by remember {
                        derivedStateOf {
                            videoInfo.videoFormats?.map { it.format.replace(" ", "") }
                        }
                    }
                    val audioList by remember {
                        derivedStateOf {
                            videoInfo.audioFormats?.map { it.formatId }
                        }
                    }
                    val categoryList by remember {
                        derivedStateOf {
                            DownloadCategory.entries.map { it.name }
                        }
                    }
                    VideoDetailCardUi(
                        videoInfo,
                        videoCardTheme = VideoCardTheme.ThumbnailBehind,
                        onThumbnailLoad = onThumbnailLoad,
                        subContent = {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    videoList?.let { formatIds ->
                                        CustomFilterChip(
                                            defaultOption =
                                                videoInfo.defaultVideoFormatIdIndex
                                                    ?: -1,
                                            list = formatIds,
                                            onClick = {
                                                defaultVideoFormat =
                                                    info.formats
                                                        ?.get(it)
                                                        ?.formatDetailFragment
                                            },
                                        )
                                    }
                                    audioList?.let { formatIds ->
                                        CustomFilterChip(
                                            defaultOption =
                                                videoInfo.defaultAudioFormatIdIndex
                                                    ?: -1,
                                            list = formatIds,
                                            onClick = {
                                                defaultAudioFormat =
                                                    info.formats
                                                        ?.get(it)
                                                        ?.formatDetailFragment
                                            },
                                        )
                                    }
                                }
                                CustomFilterChip(
                                    defaultOption = categoryList.indexOf(videoInfo.defaultCategory.name),
                                    list = categoryList,
                                    onClick = {
                                        defaultCategory = DownloadCategory.entries[it]
                                    },
                                )
                            }
                        },
                        footerContent = {
                            Row(
                                Modifier
                                    .padding(4.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                ElevatedButton(
                                    onClick = {
                                        onEvent(
                                            DownloadBottomSheetEvent.DownloadVideo(
                                                info.toVideoInfoInput(
                                                    defaultVideoFormat,
                                                    defaultAudioFormat,
                                                    defaultCategory,
                                                ),
                                                thumbnail,
                                            ),
                                        )
                                        onValueChange("")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(text = "Download on Server", textAlign = TextAlign.Center)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                ElevatedButton(
                                    onClick = {
                                        onValueChange("")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    enabled = false,
                                ) {
                                    Text(text = "Download on Device", textAlign = TextAlign.Center)
                                }
                            }
                        },
                    )
                },
            )
        }
    }
}
