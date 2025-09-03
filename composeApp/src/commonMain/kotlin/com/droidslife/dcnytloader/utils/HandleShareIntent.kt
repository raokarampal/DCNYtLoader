package com.droidslife.dcnytloader.utils

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.Flow


expect abstract class PlatformContext


expect fun SavedStateHandle.handleShareIntent(context:PlatformContext): Flow<SharedContent>



enum class ContentType {
    IMAGE,
    PLAIN_TEXT,
    EMPTY
}

sealed interface SharedContent {
    data class SharedTxtContent(
        val textContent: String,
        val contentType: ContentType = ContentType.PLAIN_TEXT
    ) : SharedContent

    data class SharedImageContent(
        val imageBytes: ByteArray,
        val contentType: ContentType = ContentType.IMAGE
    ) : SharedContent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SharedImageContent

            if (!imageBytes.contentEquals(other.imageBytes)) return false
            if (contentType != other.contentType) return false

            return true
        }

        override fun hashCode(): Int {
            var result = imageBytes.contentHashCode()
            result = 31 * result + contentType.hashCode()
            return result
        }
    }

    data object EmptyContent : SharedContent
}