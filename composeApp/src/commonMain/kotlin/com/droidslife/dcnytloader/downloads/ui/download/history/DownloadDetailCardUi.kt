package com.droidslife.dcnytloader.downloads.ui.download.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.Image
import coil3.compose.AsyncImage
import com.droidslife.dcnytloader.downloads.data.MinimalVideoInfo
import com.droidslife.dcnytloader.utils.GradiantOverlay
import dcnytloader.composeapp.generated.resources.Res
import dcnytloader.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource

enum class VideoCardTheme {
    ThumbnailOnTop,
    ThumbnailBehind,
    ThumbnailOnLeft,
    NoThumbnail,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailCardUi(
    videoInfo: MinimalVideoInfo,
    modifier: Modifier = Modifier,
    videoCardTheme: VideoCardTheme = VideoCardTheme.NoThumbnail,
    onThumbnailLoad: (Image) -> Unit = {},
    subContent: @Composable () -> Unit = {},
    footerContent: @Composable () -> Unit = {},
) {
    Card(
        modifier.padding(8.dp).fillMaxWidth(),
    ) {
        when (videoCardTheme) {
            VideoCardTheme.ThumbnailOnTop -> {
                Column(Modifier.fillMaxWidth().heightIn(max = 350.dp)) {
                    if (videoInfo.thumbnail != null) {
                        VideoThumbnailUi(
                            videoInfo.thumbnail,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f),
                            onThumbnailLoad = onThumbnailLoad,
                        )
                    }
                    VideoDetailTextInfoUi(
                        videoInfo,
                        modifier = Modifier.height(130.dp),
                        subContent = subContent,
                    )
                    footerContent()
                }
            }

            VideoCardTheme.ThumbnailOnLeft -> {
                Row(Modifier.fillMaxWidth().requiredHeight(150.dp)) {
                    if (videoInfo.thumbnail != null) {
                        VideoThumbnailUi(
                            videoInfo.thumbnail,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f),
                            onThumbnailLoad = onThumbnailLoad,
                        )
                    }
                    Column(Modifier.weight(2f)) {
                        VideoDetailTextInfoUi(
                            videoInfo,
                            subContent = subContent,
                        )
                        footerContent()
                    }
                }
            }

            VideoCardTheme.ThumbnailBehind -> {
                Column(Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        if (videoInfo.thumbnail != null) {
                            GradiantOverlay {
                                VideoThumbnailUi(
                                    videoInfo.thumbnail,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth().height(height = 250.dp),
                                    onThumbnailLoad = onThumbnailLoad,
                                )
                            }
                        }
                        VideoDetailTextInfoUi(
                            videoInfo,
                            modifier = Modifier,
                            textOutlined = true,
                            subContent = subContent,
                        )
                    }
                    footerContent()
                }
            }

            VideoCardTheme.NoThumbnail -> {
                Column(Modifier.fillMaxWidth().height(120.dp)) {
                    VideoDetailTextInfoUi(
                        videoInfo,
                        modifier = Modifier,
                        subContent = subContent,
                    )
                    footerContent()
                }
            }
        }
    }
}

@Composable
fun VideoDetailTextInfoUi(
    videoInfo: MinimalVideoInfo,
    modifier: Modifier = Modifier,
    textOutlined: Boolean = false,
    subContent: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.fillMaxWidth()) {
            VideoTitle(videoInfo.title, isOutlined = textOutlined)
        }
        Column(Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                RoundBox()
                Spacer(modifier = Modifier.size(4.dp))
                VideoChannelName(videoInfo.channelName)
            }
            Spacer(modifier = Modifier.size(4.dp))
            HorizontalDivider()
            subContent()
        }
    }
}

@Composable
fun RoundBox(
    size: Dp = 10.dp,
    text: String? = null,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
) {
    Box(
        modifier =
            modifier.size(size).background(
                shape = CircleShape,
                color = color,
            ),
        contentAlignment = Alignment.Center,
    ) {
        text?.let { txt ->
            val initials =
                txt
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    .joinToString("")
                    .take(2)

            Text(
                text = initials,
                style = textStyle,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier =
                    Modifier.padding(2.dp).background(
                        color = Color.Transparent, // Ensure text background is transparent
                        shape = RoundedCornerShape(percent = 50), // Circular shape for text background
                    ),
            )
        }
    }
}

@Composable
fun VideoChannelName(
    channelName: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = channelName,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style =
            MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
        modifier = modifier,
    )
}

@Composable
fun VideoDescription(
    desc: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = desc,
        maxLines = 2,
        softWrap = true,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier,
    )
}

@Composable
fun VideoTitle(
    title: String,
    modifier: Modifier = Modifier,
    isOutlined: Boolean = false,
) {
    Box {
        if (isOutlined) {
            Text(
                text = title,
                maxLines = 2,
                softWrap = true,
                overflow = TextOverflow.Ellipsis,
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        drawStyle =
                            Stroke(
                                width = 20f,
                                join = StrokeJoin.Round,
                            ),
                    ),
                modifier = modifier,
            )
        }
        Text(
            text = title,
            maxLines = 2,
            softWrap = true,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier,
        )
    }
}

@Composable
fun VideoThumbnailUi(
    url: String,
    modifier: Modifier = Modifier,
    opacity: Float = 1.0f,
    contentScale: ContentScale = ContentScale.Fit,
    onThumbnailLoad: (Image) -> Unit = {},
) {
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier,
        alpha = opacity,
        onSuccess = {
            onThumbnailLoad(it.result.image)
        },
    )
}
