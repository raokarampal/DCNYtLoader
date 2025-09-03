package com.droidslife.dcnytloader

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform