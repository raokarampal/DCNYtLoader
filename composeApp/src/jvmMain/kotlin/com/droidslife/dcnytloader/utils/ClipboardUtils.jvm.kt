package com.droidslife.dcnytloader.utils

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.asAwtTransferable
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

actual fun getPlatformClipEntry(data: String): ClipEntry = ClipEntry(StringSelection(data))

@OptIn(ExperimentalComposeUiApi::class)
actual fun setPlatformClipEntry(clipEntry: ClipEntry): CharSequence? {
    val t = clipEntry.asAwtTransferable
    t?.transferDataFlavors?.forEach {
        println(it.humanPresentableName)
        println(it.getReaderForText(t).readText())
        return when (it) {
            DataFlavor.stringFlavor -> it.getReaderForText(t).readText()
//            DataFlavor.allHtmlFlavor -> it.getReaderForText(t).readText()
//            DataFlavor.fragmentHtmlFlavor -> it.getReaderForText(t).readText()
//            DataFlavor.selectionHtmlFlavor -> it.getReaderForText(t).readText()
            else -> null
        }
    }
    return null
}
