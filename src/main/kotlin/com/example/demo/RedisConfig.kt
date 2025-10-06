package com.example.demo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import redis.clients.jedis.UnifiedJedis

@Configuration
class RedisConfig {
    @Bean
    fun jedis(): UnifiedJedis =
        UnifiedJedis(System.getenv("REDIS_URL") ?: "redis://localhost:6379")
}
