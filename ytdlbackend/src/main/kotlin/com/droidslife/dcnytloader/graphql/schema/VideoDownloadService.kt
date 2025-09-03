package com.droidslife.dcnytloader.graphql.schema

import com.droidslife.dcnytloader.AppConfig
import com.droidslife.dcnytloader.YtDlpService
import com.droidslife.dcnytloader.getDirectoryFileNames
import com.droidslife.dcnytloader.graphql.schema.models.DownloadHistoryList
import com.droidslife.dcnytloader.graphql.schema.models.DownloadUpdates
import com.droidslife.dcnytloader.graphql.schema.models.VideoDownloadRequest
import com.droidslife.dcnytloader.graphql.schema.models.VideoInfo
import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import com.expediagroup.graphql.server.operations.Subscription
import graphql.GraphQLError
import graphql.execution.DataFetcherResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class VideoInfoQuery(
    private val ytDlpService: YtDlpService,
    private val appConfig: AppConfig,
) : Query {
    suspend fun getVideoInfo(url: String): DataFetcherResult<VideoInfo?> {
        val videoDetails: VideoInfo = ytDlpService.fetchVideoDetails(url)
        return DataFetcherResult
            .newResult<VideoInfo?>()
            .data(videoDetails)
            .build()
    }

    suspend fun getDownloadHistory(): DataFetcherResult<DownloadHistoryList> {
        val history = ytDlpService.downloadHistory
        println(history.firstOrNull()?.downloadUpdates)
        val directoryFileNames =
            getDirectoryFileNames(appConfig.remotePath, appConfig.downloadPath)

        return DataFetcherResult
            .newResult<DownloadHistoryList>()
            .data(DownloadHistoryList(history, downloadFolders = directoryFileNames))
            .build()
    }
}

class VideoDownloadMutation(
    private val ytDlpService: YtDlpService,
) : Mutation {
    suspend fun fetchVideoInfo(url: String): DataFetcherResult<VideoInfo?> {
        val videoDetails: VideoInfo = ytDlpService.fetchVideoDetails(url)
        return DataFetcherResult
            .newResult<VideoInfo?>()
            .data(videoDetails)
            .build()
    }

    suspend fun downloadVideo(videoDownloadRequest: VideoDownloadRequest): DataFetcherResult<String> {
        ytDlpService.startVideoDownload(videoDownloadRequest)
        return DataFetcherResult
            .newResult<String>()
            .data("Download Started")
            .build()
    }

    suspend fun downloadVideoWithInfo(videoInfo: VideoInfo): DataFetcherResult<String> {
        ytDlpService.startVideoDownloadInfo(videoInfo)
        return DataFetcherResult
            .newResult<String>()
            .data("Download Started")
            .build()
    }
}

class VideoDownloadUpdatesSubscription(
    private val ytDlpService: YtDlpService,
) : Subscription {
    fun videoDownloadUpdates(videoId: String? = null): Flow<DownloadUpdates> =
        flow {
            ytDlpService.progressFlow.collect { update ->
                try {
                    emit(update)
                } catch (e: CancellationException) {
                    // Channel/session was cancelled; clean up if needed and exit gracefully
                    println("Progress flow emit cancelled: ${e.message}")
                }
            }
        }
}
