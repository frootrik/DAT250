package com.example.demo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties


fun main(args: Array<String>) {
    require(args.size == 2) { "Usage: StandaloneProducer <pollId> <optionId>" }
    val pollId = args[0]
    val optionId = args[1]
    val topic = "poll-best-ice-cream-$pollId"

    val json = jacksonObjectMapper().writeValueAsString(
        VoteCast(pollId, optionId, System.currentTimeMillis())
    )

    val props = Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.ACKS_CONFIG, "all")
    }

    KafkaProducer<String, String>(props).use { p ->
        p.send(ProducerRecord(topic, pollId, json)).get()
        println("Sent VoteCast to $topic (pollId=$pollId, optionId=$optionId)")
    }
}

