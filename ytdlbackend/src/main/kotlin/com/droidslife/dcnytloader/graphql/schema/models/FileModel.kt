package com.droidslife.dcnytloader.graphql.schema.models

import kotlinx.serialization.Serializable

@Serializable
data class DownloadFolder(val folderName: String, val fileDetails: List<FileDetail>)
@Serializable
data class FileDetail(val name: String, val files: List<String>)