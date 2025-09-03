package com.droidslife.dcnytloader

data class AppConfig(
    val serverPort: Int,
    val downloadCategory: String,
    val downloadPath: String,
    val remoteIp: String,
    val remotePath: String
)
