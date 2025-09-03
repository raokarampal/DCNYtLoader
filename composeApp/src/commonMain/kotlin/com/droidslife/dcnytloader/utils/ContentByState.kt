package com.droidslife.dcnytloader.utils

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ContentByState(
    uiState: ScreenUiState<T>,
    modifier: Modifier = Modifier,
    emptyMessage: String? = null,
    retryOnError: Function0<Unit>? = null,
    loadingContent: @Composable () -> Unit = { DefaultLoadingIndicator() },
    emptyContent: @Composable () -> Unit = {
        DefaultEmptyContent(
            emptyMessage,
            retryOnError = retryOnError,
        )
    },
    errorContent: @Composable (String?) -> Unit = {
        DefaultErrorContent(
            it,
            retryOnError = retryOnError,
        )
    },
    successContent: @Composable (data: T) -> Unit,
) {
    Crossfade(
        targetState = uiState,
        label = "ContentByStateCrossfade",
        modifier = modifier,
    ) { state ->
        when (state) {
            is ScreenUiState.Loading -> loadingContent()
            is ScreenUiState.Success -> successContent(state.data)
            is ScreenUiState.Empty -> emptyContent()
            is ScreenUiState.Error -> errorContent(state.message)
        }
    }
}

@Composable
fun DefaultLoadingIndicator(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(modifier = Modifier.padding(32.dp))
    }
}

@Composable
fun DefaultEmptyContent(
    emptyMessage: String?,
    retryOnError: Function0<Unit>?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emptyMessage ?: "No data available", modifier = Modifier.padding(32.dp))
        retryOnError?.let {
            TextButton(onClick = it) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun DefaultErrorContent(
    message: String?,
    retryOnError: Function0<Unit>?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = message ?: "Error loading data",
            modifier = Modifier.padding(32.dp),
            style =
                MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.error,
                ),
        )
        retryOnError?.let {
            TextButton(onClick = it) {
                Text("Retry")
            }
        }
    }
}
