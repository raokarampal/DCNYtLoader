package com.droidslife.dcnytloader.utils

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Stable
fun Modifier.outlined(color: Color = Color.Unspecified) = this then this.border(1.dp, color, RoundedCornerShape(12.dp))
