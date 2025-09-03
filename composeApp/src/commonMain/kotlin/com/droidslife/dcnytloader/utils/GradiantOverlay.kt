package com.droidslife.dcnytloader.utils

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class GradientType {
    VERTICAL,
    HORIZONTAL,
    RADIAL,
}

@Composable
fun GradiantOverlay(
    modifier: Modifier = Modifier,
    gradientType: GradientType = GradientType.VERTICAL,
    startColor: Color = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.5f),
    endColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 1.0f),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        content()
        Canvas(
            modifier = Modifier.Companion.matchParentSize(),
        ) {
            val brush =
                when (gradientType) {
                    GradientType.VERTICAL ->
                        Brush.Companion.verticalGradient(
                            colors =
                                listOf(
                                    startColor,
                                    endColor,
                                ),
                            startY = 50.0f,
                            endY = size.height.div(1.2f),
                        )

                    GradientType.HORIZONTAL ->
                        Brush.Companion.horizontalGradient(
                            colors = listOf(startColor, endColor),
                            startX = 0f,
                            endX = size.width,
                        )

                    GradientType.RADIAL ->
                        Brush.Companion.radialGradient(
                            colors = listOf(startColor, endColor),
                            center = center,
                            radius = size.minDimension / 2,
                        )
                }
            drawRect(
                brush = brush,
                size = size,
            )
        }
    }
}
