package com.example.demo
import java.util.UUID


object Slug {
    private val nonAllowed = Regex("[^a-z0-9\\s._-]")
    fun of(input: String): String =
        input.lowercase()
            .replace(nonAllowed, "")
            .trim()
            .replace(Regex("\\s+"), "-")
            .ifBlank { "poll" }
}

fun pollTopicName(pollId: UUID, question: String): String =
    "poll-${Slug.of(question)}-$pollId"
