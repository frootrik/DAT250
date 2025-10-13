package com.example.demo


import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import java.util.UUID;

data class VoteCast(
    val pollId: String,
    val optionId: String,
    val occurredAtEpochMs: Long
)


@Component
class VoteEventListener(
    private val objectMapper: ObjectMapper,
    private val pm: PollManager
) {
    @KafkaListener(topicPattern = "poll-.*", groupId = "pollapp")
    fun onMessage(payload: String) {
        try {
            val evt = objectMapper.readValue(payload, VoteCast::class.java)
            val ok = pm.applyAnonymousVote(UUID.fromString(evt.pollId), UUID.fromString(evt.optionId))
            if (ok) println("✅ Vote applied for poll=${evt.pollId} option=${evt.optionId}")
            else    println("⚠️  Vote rejected for poll=${evt.pollId} option=${evt.optionId}")
        } catch (e: Exception) {
            println("⚠️  Non-VoteCast message: $payload")
        }
    }
}
