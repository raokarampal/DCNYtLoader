package com.droidslife.dcnytloader

import com.droidslife.dcnytloader.graphql.schema.models.DownloadInfo
import com.droidslife.dcnytloader.graphql.schema.models.DownloadStatus
import com.droidslife.dcnytloader.graphql.schema.models.OtherInfo
import com.droidslife.dcnytloader.graphql.schema.models.ParsedDownloadProgressInfo
import kotlin.text.contains
import kotlin.text.toDoubleOrNull
import kotlin.text.toRegex

fun createMessageFromString(message: String): ParsedDownloadProgressInfo {
    val downloadRegex = """\[download\]\s+.*""".toRegex()
    // val mergeRegex = """\[Merger\]\s+.*""".toRegex()
    // val deleteRegex = """Deleting original file\s+.*""".toRegex()

    return when {
        downloadRegex.matches(message) -> parseDownloadMessage(message)
        //  mergeRegex.matches(message) -> parseMergeMessage(message)
        //  deleteRegex.matches(message) -> parseDeleteMessage(message)
        else -> parseOtherInfoMessage(message)
    }
}

fun parseDownloadMessage(message: String): ParsedDownloadProgressInfo {
    // val regex = """\[download\]\s+([\d.]+)%\s+of\s+([\d.]+[KMGT]?i?B)\s+at\s+([\d.]+[KMGT]?i?B\/s)+\sETA+\s+(\d+:\d+)""".toRegex()
    val regex =
        """\[download\]\s+([\d.]+)%\s+of\s+(?:~\s*)?([\d.]+[KMGT]?i?B)\s+at\s+([\d.]+[KMGT]?i?B\/s)+\sETA\s+(\d+:\d+)""".toRegex()

    val matchResult = regex.find(message)
    return if (matchResult != null) {
        val (percentage, totalSize, speed, eta) = matchResult.destructured

        val downloadInfo =
            DownloadInfo(
                percentage.toDoubleOrNull() ?: 0.0,
                totalSize,
                speed,
                eta,
            )

        println(downloadInfo)
        downloadInfo
    } else if (message.contains("has already been recorded") || message.contains("has already been downloaded")) {
        parseOtherInfoMessage(message).copy(status = DownloadStatus.DUPLICATE)
    } else if (message.contains("100%")) {
        parseOtherInfoMessage(message).copy(status = DownloadStatus.COMPLETED)
    } else {
        OtherInfo(message = message)
    }
}

fun parseOtherInfoMessage(message: String): OtherInfo {
    val regexPattern2 = """^(\S+)""".toRegex()
    val regexPattern = """(\d{1,3}(?:\.\d{1,3}){3})([^\n]+)\\([^\n]+)""".toRegex()
    val matchResult = regexPattern.find(message)
    val action = regexPattern2.find(message)?.groupValues?.get(0) ?: ""
    return if (matchResult != null) {
        val (pc, filePath, fileName) = matchResult.destructured

        val otherInfo =
            OtherInfo(
                message = message,
                action = action,
                pc = pc,
                filePath = filePath,
                fileName = fileName,
                status = DownloadStatus.UNKNOWN,
            )

        println(otherInfo)
        otherInfo
    } else {
        OtherInfo(message = message)
    }
}

/*fun parseMergeMessage(message: String): ParsedDownloadProgressInfo {
    val regex =
        """(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})(\\\\*.*?\\[^\\]+.*\\?\\\\)([^\\]+\.*)""".toRegex()

    val matchResult = regex.find(message)
    return if (matchResult != null) {
        val (pc, filePath, fileName) = matchResult.destructured

        val mergeInfo = MergeInfo(
            pc,
            filePath,
            fileName
        )

        println(mergeInfo)
        mergeInfo
    } else OtherInfo(error = message)
}

fun parseDeleteMessage(message: String): ParsedDownloadProgressInfo {
    val regex =
        """(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})(\\\\*.*?\\[^\\]+.*\\?\\\\)([^\\]+\.*)""".toRegex()

    val matchResult = regex.find(message)
    return if (matchResult != null) {
        val (pc, filePath, fileName) = matchResult.destructured

        val deleteInfo = DeleteInfo(
            pc,
            filePath,
            fileName
        )

        println(deleteInfo)
        deleteInfo
    } else OtherInfo(error = message)

}*/
