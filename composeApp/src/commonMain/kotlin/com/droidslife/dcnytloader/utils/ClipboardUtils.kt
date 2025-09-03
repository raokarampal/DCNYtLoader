package com.droidslife.dcnytloader.utils

import androidx.compose.ui.platform.ClipEntry

expect fun getPlatformClipEntry(data: String): ClipEntry

expect fun setPlatformClipEntry(clipEntry: ClipEntry): CharSequence?
