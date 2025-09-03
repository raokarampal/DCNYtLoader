package com.droidslife.dcnytloader

import com.droidslife.dcnytloader.graphql.schema.models.DownloadFolder
import com.droidslife.dcnytloader.graphql.schema.models.DownloadHistory
import com.droidslife.dcnytloader.graphql.schema.models.DownloadInfo
import com.droidslife.dcnytloader.graphql.schema.models.DownloadStatus
import com.droidslife.dcnytloader.graphql.schema.models.DownloadUpdates
import com.droidslife.dcnytloader.graphql.schema.models.FileDetail
import com.droidslife.dcnytloader.graphql.schema.models.OtherInfo
import com.droidslife.dcnytloader.graphql.schema.models.VideoDownloadRequest
import com.droidslife.dcnytloader.graphql.schema.models.VideoInfo
import com.droidslife.dcnytloader.graphql.schema.models.YtdlVideoInfo
import com.droidslife.dcnytloader.graphql.schema.models.toVideoInfo
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class YtDlpService(
    private val json: Json,
    private val appConfig: AppConfig,
) {
    internal val LOGGER = KtorSimpleLogger("com.droidslife.dcnytloader.YtDlpService")

    private val _downloadHistory = mutableMapOf<String, DownloadHistory>()
    val downloadHistory: List<DownloadHistory> get() = _downloadHistory.values.toList()

    private val _videoQueryDetail = mutableMapOf<String, VideoInfo>()
    val videoQueryDetail: List<VideoInfo> get() = _videoQueryDetail.values.toList()

    private val _progressFlow = MutableSharedFlow<DownloadUpdates>()
    val progressFlow = _progressFlow.asSharedFlow()

    private val downloadScopes = mutableMapOf<String, CoroutineScope>()

    fun startVideoDownload(request: VideoDownloadRequest) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        downloadScopes[request.videoId] = scope

        _videoQueryDetail[request.videoId]?.let {
            _downloadHistory[it.videoID] = it.toDownloadHistory()
        }
        scope.launch {
            startVideoDownloadInfo(
                videoId = request.videoId,
                videoFormat = request.videoFormat,
                audioFormat = request.audioFormat,
                path = request.path,
                category = request.category,
            )
        }
    }

    fun startVideoDownloadInfo(request: VideoInfo) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        downloadScopes[request.videoID] = scope

        scope.launch {
            try {
                val historyData = request.toDownloadHistory()
                if (_downloadHistory.containsKey(historyData.videoID)) {
                    println("duplicate download")
                } else {
                    _downloadHistory[historyData.videoID] = historyData
                }

                startVideoDownloadInfo(
                    videoId = request.videoID,
                    videoFormat = request.defaultFormatVideo?.formatId,
                    audioFormat = request.defaultFormatAudio?.formatId,
                    path = request.downloadPath,
                    category = request.downloadCategory,
                )
            } finally {
                // scope cleanup is handled in the common function via clearDownloadScope
            }
        }
    }

    private suspend fun startVideoDownloadInfo(
        videoId: String,
        videoFormat: String?,
        audioFormat: String?,
        path: String,
        category: String,
    ) {
        val processBuilder: ProcessBuilder
        var process: Process? = null
        var reader: BufferedReader? = null
        var errorReader: BufferedReader? = null
        try {
            val monthDir =
                LocalDate.now().format(
                    DateTimeFormatter.ofPattern("MMMyy"),
                )
            val archiveDir = Paths.get(appConfig.remotePath).toFile().absoluteFile
            val downloadDir =
                Paths.get(appConfig.remotePath, path, category, monthDir).toFile().absoluteFile
            LOGGER.info("downloadDir check>---$downloadDir")
            LOGGER.info("archiveDir check>---$archiveDir")

            if (!downloadDir.exists()) {
                if (!downloadDir.mkdirs()) {
                    throw kotlin.RuntimeException("Failed to create download directory.")
                }
            }

            val formats =
                "$videoFormat+$audioFormat".takeIf { videoFormat.isNullOrBlank() && audioFormat.isNullOrBlank() }
                    ?: "bestvideo[height=1080]+bestaudio"

            val command =
                listOf(
                    "yt-dlp",
                    "-N",
                    "8",
                    "-f",
                    formats,
                    "-o",
                    "${downloadDir.absolutePath}/%(title)s.%(ext)s",
                    "-ciw",
                    "https://www.youtube.com/watch?v=$videoId",
                    "--restrict-filenames",
                    "--download-archive",
                    "$archiveDir/downloaded.txt",
                )

            LOGGER.info("download cmd > " + command.joinToString(" "))

            processBuilder = ProcessBuilder(command)
            processBuilder.redirectErrorStream(true)

            process =
                withContext(Dispatchers.IO) {
                    processBuilder.start()
                }

            val inputStream = process.inputStream
            val errorStream = process.errorStream

            reader = BufferedReader(InputStreamReader(inputStream))
            errorReader = BufferedReader(InputStreamReader(errorStream))

            withContext(Dispatchers.IO) {
                reader.forEachLine { line ->
                    launch(Dispatchers.IO) {
                        println(line)

                        parseVideoDownloadUpdatesInfo(videoId, line)?.let { updates ->
                            _progressFlow.emit(updates)
                        }
                    }
                }
            }

            val exitCode =
                withContext(Dispatchers.IO) {
                    process.waitFor()
                }

            if (exitCode != 0) {
                throw kotlin.RuntimeException(errorReader.readLines().joinToString(",\n"))
            }
        } catch (e: Exception) {
            _progressFlow.emit(
                DownloadUpdates(
                    videoId,
                    0.0,
                    msg =
                        OtherInfo(
                            "Failed due to ${e.message}",
                            pc = appConfig.remoteIp,
                            filePath = appConfig.remotePath,
                            fileName = path,
                        ),
                ),
            )
        } finally {
            reader?.close()
            errorReader?.close()
            process?.destroyForcibly()
            clearDownloadScope(videoId)
        }
    }

    suspend fun fetchVideoDetails(videoId: String): VideoInfo {
        return _videoQueryDetail.getOrPut(videoId) {
            val option =
                listOf(
                    "yt-dlp",
                    "-j",
                    videoId,
                )
            LOGGER.info("fetchVideoDetails $option")
            val processBuilder = ProcessBuilder(option)
            processBuilder.redirectErrorStream(true)
            val process =
                withContext(Dispatchers.IO) {
                    processBuilder.start()
                }

            val inputStream = process.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val output = kotlin.text.StringBuilder()

            reader.forEachLine { line ->
                output.append(line)
            }

            val exitCode =
                withContext(Dispatchers.IO) {
                    process.waitFor()
                }

            if (exitCode == 0) {
                return@getOrPut withContext(Dispatchers.IO) {
                    val regex = Regex(".*?\\{")
                    val videoInfoJson = output.replaceFirst(regex, "{")
                    val result =
                        json
                            .decodeFromString(YtdlVideoInfo.serializer(), videoInfoJson)
                            .toVideoInfo(appConfig.downloadCategory, appConfig.downloadPath)

                    return@withContext result
                }
            } else {
                println("exitCode out $exitCode")
                println("is not a valid URL  : $output")
                throw RuntimeException(
                    output.toString(),
                )
            }
        }
    }

    private fun parseVideoDownloadUpdatesInfo(
        videoId: String,
        update: String,
    ): DownloadUpdates? {
        val upDateData = createMessageFromString(update)

        try {
            val msg: DownloadUpdates =
                when (upDateData) {
                    /*  is DeleteInfo -> {
                          DownloadUpdates(videoId, 100.00, upDateData)
                      }*/

                    is DownloadInfo -> {
                        val u = DownloadUpdates(videoId, upDateData.percentage, upDateData)
                        if (upDateData.percentage >= 99.0) {
                            _downloadHistory.updateByKey(videoId) {
                                it.copy(
                                    downloadUpdates = u,
                                    status = DownloadStatus.DOWNLOADING,
                                )
                            }
                        }
                        u
                    }

                    /*   is MergeInfo -> {
                           DownloadUpdates(videoId, 100.00, upDateData)
                       }

                     */

                    is OtherInfo -> {
                        val u =
                            DownloadUpdates(
                                videoId,
                                if (upDateData.status == DownloadStatus.COMPLETED) 100.0 else 0.0,
                                upDateData,
                            )
                        if (upDateData.status == DownloadStatus.COMPLETED) {
                            println("download completed")
                            _downloadHistory.updateByKey(videoId) {
                                it.copy(
                                    downloadUpdates = u,
                                    status = DownloadStatus.COMPLETED,
                                )
                            }
                        }
                        u
                    }
                }

            return msg
        } catch (e: Exception) {
            println("error during parseVideoDownloadUpdatesInfo $e")
            return null
        }
    }

    fun clearDownloadScope(downloadId: String) {
        downloadScopes[downloadId]?.cancel()
        downloadScopes.remove(downloadId)
    }

    // Optionally, cancel all active downloads
    fun cancelAllDownloads() {
        downloadScopes.forEach { (_, scope) -> scope.cancel() }
        downloadScopes.clear()
    }
}

private fun <T> MutableMap<String, T>.updateByKey(
    key: String,
    function: (T) -> T,
) {
    val existing = this[key]
    if (existing != null) {
        this[key] = function(existing)
    }
}



fun getDirectoryFileNames(
    basePath: String,
    vararg directoryPath: String,
): List<DownloadFolder> {
    val directory = Paths.get(basePath, *directoryPath).toFile().absoluteFile
    val downloadFolders = mutableListOf<DownloadFolder>()

    if (directory.exists() && directory.isDirectory) {
        directory.listFiles()?.forEach { topLevelFolder ->
            if (topLevelFolder.isDirectory) {
                val folderName = topLevelFolder.name
                val fileDetails = mutableListOf<FileDetail>()

                topLevelFolder.listFiles()?.forEach { subFolder ->
                    if (subFolder.isDirectory) {
                        val subFolderName = subFolder.name
                        val subFolderFiles =
                            subFolder.listFiles()?.filter { it.isFile }?.map { it.name }
                                ?: emptyList()
                        fileDetails.add(FileDetail(subFolderName, subFolderFiles))
                    }
                }

                if (fileDetails.isNotEmpty()) {
                    downloadFolders.add(DownloadFolder(folderName, fileDetails))
                }
            }
        }
    } else {
        println("The specified directory does not exist or is not a directory. $directory")
    }

    return downloadFolders
}
