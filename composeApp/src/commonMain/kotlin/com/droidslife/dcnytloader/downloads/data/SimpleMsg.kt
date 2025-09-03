package com.droidslife.dcnytloader.downloads.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
@SerialName("SimpleMsg")
data class SimpleMsg(val id: String = Uuid.random().toString(), val msg: String? = ""){
    fun toPair()= this.id to this
}
