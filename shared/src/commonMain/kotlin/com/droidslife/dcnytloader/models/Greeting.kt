package com.droidslife.dcnytloader.models

import com.droidslife.dcnytloader.getPlatform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String = "Hello, ${platform.name}!"
}

sealed interface IUser

data class HelloWorld(
    val id: String,
    val user: IUser,
)

data class User1(
    val name: String,
) : IUser

data class User2(
    val name: String,
    val age: Int,
) : IUser
