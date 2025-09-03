package com.droidslife.dcnytloader.utils

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


actual fun SavedStateHandle.handleShareIntent(context: PlatformContext): Flow<SharedContent> = flow {
     emit(SharedContent.EmptyContent)
}

actual abstract class PlatformContext private constructor() {
    companion object {
        val INSTANCE = object : PlatformContext() {}
    }
}
