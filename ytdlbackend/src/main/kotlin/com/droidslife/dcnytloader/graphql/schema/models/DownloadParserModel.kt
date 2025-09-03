package com.droidslife.dcnytloader.graphql.schema.models

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = ParsedDownloadProgressInfoSerializer::class)
@SerialName("DownloadProgressInfo")
sealed interface ParsedDownloadProgressInfo

@Serializable
@SerialName("DownloadInfo")
data class DownloadInfo(
    val percentage: Double,
    val totalSize: String,
    val speed: String,
    val eta: String,
    val status: DownloadStatus = DownloadStatus.DOWNLOADING,
) : ParsedDownloadProgressInfo

/*@Serializable
@SerialName("MergeInfo")
data class MergeInfo(val pc:String, val filePath: String, val fileName: String):
    ParsedDownloadProgressInfo
@Serializable
@SerialName("DeleteInfo")
data class DeleteInfo(val pc:String, val filePath: String, val fileName: String):
    ParsedDownloadProgressInfo*/
@Serializable
@SerialName("OtherInfo")
data class OtherInfo(
    val message: String,
    val status: DownloadStatus = DownloadStatus.UNKNOWN,
    val action: String = "",
    val pc: String = "",
    val filePath: String = "",
    val fileName: String = "",
) : ParsedDownloadProgressInfo

object ParsedDownloadProgressInfoSerializer :
    JsonContentPolymorphicSerializer<ParsedDownloadProgressInfo>(ParsedDownloadProgressInfo::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ParsedDownloadProgressInfo> =
        when ((element.jsonObject["type"])?.jsonPrimitive?.contentOrNull) {
            "DownloadInfo" -> DownloadInfo.serializer()
            /*   "MergeInfo" -> MergeInfo.serializer()
               "DeleteInfo" -> DeleteInfo.serializer()*/
            "OtherInfo" -> OtherInfo.serializer()
            else -> throw kotlin.Exception("Unknown Module: key 'type' not found or does not matches any module type $element")
        }
}
