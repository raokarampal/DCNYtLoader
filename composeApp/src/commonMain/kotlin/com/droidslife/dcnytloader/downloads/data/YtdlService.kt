package com.droidslife.dcnytloader.downloads.data

import com.droidslife.dcnytloader.graphql.DownloadVideoWithInfoMutation
import com.droidslife.dcnytloader.graphql.FetchVideoInfoMutation
import com.droidslife.dcnytloader.graphql.GetDownloadHistoryQuery
import com.droidslife.dcnytloader.graphql.VideoDownloadUpdatesSubscription
import com.droidslife.dcnytloader.graphql.fragment.DownloadHistoryListFragment
import com.droidslife.dcnytloader.graphql.fragment.DownloadUpdateFragment
import com.droidslife.dcnytloader.graphql.fragment.VideoInfoFragment
import com.droidslife.dcnytloader.graphql.type.VideoInfoInput
import com.droidslife.dcnytloader.network.YtdlApolloClient
import com.droidslife.dcnytloader.network.resultFlow
import kotlinx.coroutines.flow.Flow

interface YtdlService {
    suspend fun fetchDownloadHistory(): Flow<Result<DownloadHistoryListFragment>>

    suspend fun fetchVideoDetails(url: String): Flow<Result<VideoInfoFragment>>

    suspend fun startDownloadingVideo(videoInfo: VideoInfoInput): Flow<Result<SimpleMsg>>

    suspend fun subscribeToDownloadUpdates(videoId: String? = null): Flow<Result<DownloadUpdateFragment>>
}

class YtdlServiceImpl(
    val client: YtdlApolloClient,
) : YtdlService {
    override suspend fun fetchDownloadHistory(): Flow<Result<DownloadHistoryListFragment>> =
        client.getClient().query(GetDownloadHistoryQuery()).resultFlow {
            getDownloadHistory.downloadHistoryListFragment
        }

    override suspend fun fetchVideoDetails(url: String): Flow<Result<VideoInfoFragment>> =
        client.getClient().mutation(FetchVideoInfoMutation(url)).resultFlow {
            fetchVideoInfo?.videoInfoFragment ?: throw NoSuchElementException()
        }

    override suspend fun startDownloadingVideo(videoInfo: VideoInfoInput): Flow<Result<SimpleMsg>> =
        client.getClient().mutation(DownloadVideoWithInfoMutation(videoInfo)).resultFlow {
            SimpleMsg(msg = downloadVideoWithInfo)
        }

    override suspend fun subscribeToDownloadUpdates(videoId: String?): Flow<Result<DownloadUpdateFragment>> =
        client.getClient().subscription(VideoDownloadUpdatesSubscription()).resultFlow {
            videoDownloadUpdates.downloadUpdateFragment
        }
}
