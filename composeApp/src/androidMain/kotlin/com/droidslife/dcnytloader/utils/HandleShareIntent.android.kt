package com.droidslife.dcnytloader.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

actual fun SavedStateHandle.handleShareIntent(context: PlatformContext): Flow<SharedContent> =
    getStateFlow(
        NavController.KEY_DEEP_LINK_INTENT,
        Intent().also {
            println("${it.getStringExtra(Intent.EXTRA_TEXT)}")
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
    ).map { intent -> intent.parseSharedContent(context) }

fun Intent.parseSharedContent(context: Context?): SharedContent {
    if (action != Intent.ACTION_SEND) return SharedContent.EmptyContent

    return if (isTextMimeType()) {
        val textContent = getStringExtra(Intent.EXTRA_TEXT) ?: ""

        SharedContent.SharedTxtContent(textContent, ContentType.PLAIN_TEXT)
    } else if (isImageMimeType()) {
        val imageContent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelableExtra(Intent.EXTRA_STREAM, Parcelable::class.java) as? Uri
            } else {
                @Suppress("DEPRECATION")
                getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
            }

        imageContent?.let {
            val bytes =
                context?.contentResolver?.openInputStream(it)?.use { input ->
                    input.readBytes()
                } ?: byteArrayOf()
            SharedContent.SharedImageContent(bytes, ContentType.IMAGE)
        } ?: SharedContent.EmptyContent
    } else {
        SharedContent.EmptyContent
    }
}

private fun Intent.isTextMimeType() = type?.startsWith(MIME_TYPE_TEXT) == true

private fun Intent.isImageMimeType() = type?.startsWith(MIME_TYPE_IMAGE) == true

private const val MIME_TYPE_TEXT = "text/"
private const val MIME_TYPE_IMAGE = "image/"

actual typealias PlatformContext = Context
