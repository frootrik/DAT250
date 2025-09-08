package com.example.demo

import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var email: String
)


