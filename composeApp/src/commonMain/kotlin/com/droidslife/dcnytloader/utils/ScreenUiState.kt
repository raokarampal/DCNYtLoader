package com.droidslife.dcnytloader.utils

sealed interface ScreenUiState<out T> {
    data object Loading : ScreenUiState<Nothing>

    data class Success<T>(
        val data: T,
    ) : ScreenUiState<T>

    data object Empty : ScreenUiState<Nothing>

    data class Error(
        val message: String? = null,
        val throwable: Throwable? = null,
    ) : ScreenUiState<Nothing>
}
