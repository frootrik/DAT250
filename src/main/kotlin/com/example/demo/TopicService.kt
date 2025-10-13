package com.example.demo

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.errors.TopicExistsException
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutionException

@Component
class TopicService(private val admin: AdminClient) {
    fun ensureTopic(name: String, partitions: Int = 1, replicationFactor: Short = 1) {
        try {
            val existing = admin.listTopics().names().get()
            if (name in existing) return
            admin.createTopics(listOf(NewTopic(name, partitions, replicationFactor))).all().get()
        } catch (e: ExecutionException) {
            if (e.cause !is TopicExistsException) throw e
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt(); throw e
        }
    }
}
