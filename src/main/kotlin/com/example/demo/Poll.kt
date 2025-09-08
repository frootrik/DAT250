package com.example.demo

import java.time.Instant
import java.util.UUID

data class VoteOption(
    val id: String = UUID.randomUUID().toString(),
    var text: String
)

data class Vote(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val pollId: String,
    var optionId: String,
    val createdAt: Instant = Instant.now()
)

data class Poll(
    val id: String = UUID.randomUUID().toString(),
    val ownerUserId: String,
    var question: String,
    val createdAt: Instant = Instant.now(),
    val options: MutableList<VoteOption> = mutableListOf(),
    val votes: MutableMap<String, Vote> = mutableMapOf()
)


