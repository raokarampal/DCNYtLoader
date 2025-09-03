package com.droidslife.dcnytloader.downloads.ui

import com.droidslife.dcnytloader.utils.ContentType

sealed interface ShareIntentUiState {
    val type: ContentType
    val msg: String

    data class NoData(
        override val type: ContentType,
        override val msg: String,
    ) : ShareIntentUiState

    data class HasImage(
        override val type: ContentType,
        override val msg: String,
        val image: ByteArray?,
    ) : ShareIntentUiState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as HasImage

            if (type != other.type) return false
            if (msg != other.msg) return false
            if (!image.contentEquals(other.image)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = type.hashCode()
            result = 31 * result + msg.hashCode()
            result = 31 * result + (image?.contentHashCode() ?: 0)
            return result
        }
    }

    data class HasTxt(
        override val type: ContentType,
        override val msg: String,
        val txt: String,
    ) : ShareIntentUiState
}
