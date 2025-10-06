package com.example.demo

import org.springframework.stereotype.Component
import redis.clients.jedis.UnifiedJedis
import java.util.UUID

@Component
class PollCacheRepository(private val jedis: UnifiedJedis) {
    private fun key(pollId: UUID) = "cache:poll:$pollId"
    private val ttlSeconds = 120

    fun get(pollId: UUID): Map<Int, Long>? {
        val k = key(pollId)
        if (!jedis.exists(k)) return null
        val raw = jedis.hgetAll(k) ?: return null
        if (raw.isEmpty()) return null
        return raw.entries
            .associate { it.key.toInt() to it.value.toLong() }
            .toSortedMap()
    }

    fun put(pollId: UUID, counts: Map<Int, Long>) {
        if (counts.isEmpty()) return
        val k = key(pollId)
        jedis.hset(k, counts.mapKeys { it.key.toString() }
            .mapValues { it.value.toString() })
        jedis.expire(k, ttlSeconds.toLong())
    }

    fun invalidate(pollId: UUID) {
        jedis.del(key(pollId))
    }

    fun incrementIfPresent(pollId: UUID, presentationOrder: Int, delta: Long = 1) {
        val k = key(pollId)
        if (jedis.exists(k)) {
            jedis.hincrBy(k, presentationOrder.toString(), delta)
        }
    }
}
