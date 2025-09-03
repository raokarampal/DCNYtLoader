package com.droidslife.dcnytloader.network

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.apolloStore
import com.apollographql.apollo.cache.normalized.doNotStore
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.interceptor.RetryOnErrorInterceptor
import com.apollographql.apollo.network.NetworkMonitor
import com.apollographql.apollo.network.ws.GraphQLWsProtocol
import com.droidslife.dcnytloader.BuildKonfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class YtdlApolloClient : KoinComponent {
    private val _clients = mutableMapOf<String, ApolloClient>()
    private val mutex = Mutex()

    suspend fun getClient(clientName: String = "default"): ApolloClient {
        return mutex.withLock {
            _clients.getOrPut(clientName) {
                apolloClient()
            }
        }
    }

    @OptIn(ApolloExperimental::class)
    private fun apolloClient(): ApolloClient {
        val networkMonitor = object : NetworkMonitor {
            override val isOnline: StateFlow<Boolean?>
                get() = MutableStateFlow(true)

            override fun close() {}
        }
        return get<ApolloClient.Builder>()
            .serverUrl(BuildKonfig.BASE_URL)
            .webSocketServerUrl(BuildKonfig.BASE_URL_WS)
            .retryOnErrorInterceptor(RetryOnErrorInterceptor(networkMonitor))
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .doNotStore(true)
            .autoPersistedQueries(enableByDefault = false)
            .wsProtocol(
                GraphQLWsProtocol.Factory()
            )
            .webSocketReopenWhen { throwable, attempt ->

                if (throwable is WebSocketReconnectException) {
                    true
                } else {
                    delay(attempt * 5000)
                    attempt < 10
                }
            }
            .build()
    }

    fun close() {
        _clients.values.forEach {
            it.close()
        }
        _clients.clear()
    }

    fun clear() {
        _clients.values.forEach {
            it.apolloStore.clearAll()
            it.close()
        }
        _clients.clear()
    }
}

class WebSocketReconnectException : Exception("The WebSocket needs to be reopened")

inline fun<T : Operation.Data, R> ApolloCall<T>.resultFlow(crossinline transform: suspend T.() -> R): Flow<Result<R>> {
    return runCatching {
        this.toFlow().transform { value ->
            return@transform emit(value.toResult { it.transform() })
        }
    }.getOrElse { flow { emit(Result.failure(it)) } }
}

inline fun <T : Operation.Data, R> ApolloResponse<T>.toResult(
    transformDataType: (T) -> R
): Result<R> {
    return when {
        data == null && !errors.isNullOrEmpty() -> Result.failure(
            Throwable(errors?.firstOrNull()?.message)
        )

        data != null -> Result.success(transformDataType(data!!))
        else -> Result.failure(
            Throwable(errors?.firstOrNull()?.message ?: "Unknown error")
        )
    }
}