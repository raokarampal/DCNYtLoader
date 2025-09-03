package com.droidslife.dcnytloader.utils

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.toClipEntry

actual fun getPlatformClipEntry(data: String): ClipEntry = ClipEntry(ClipData.newPlainText("text", data))

actual fun setPlatformClipEntry(clipEntry: ClipEntry): CharSequence? = clipEntry.clipData.getItemAt(0).text
