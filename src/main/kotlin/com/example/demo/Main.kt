import redis.clients.jedis.UnifiedJedis

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        UnifiedJedis("redis://localhost:6379").use { jedis ->
            jedis.del("online")
            jedis.sadd("online", "alice")
            jedis.sadd("online", "bob")
            jedis.srem("online", "alice")
            jedis.sadd("online", "eve")
            val online = jedis.smembers("online")
            println("Online users: $online")

            jedis.del("poll:1", "poll:1:options", "option1", "option2", "option3")

            jedis.hset(
                "poll:1", mapOf(
                    "id" to "1",
                    "title" to "Pineapple on Pizza?",
                    "options" to "poll:1:options"
                )
            )

            jedis.sadd("poll:1:options", "option1", "option2", "option3")

            jedis.hset("option1", mapOf("caption" to "Yes", "voteCount" to "0"))
            jedis.hset("option2", mapOf("caption" to "No", "voteCount" to "0"))
            jedis.hset("option3", mapOf("caption" to "Maybe", "voteCount" to "0"))

            jedis.hincrBy("option3", "voteCount", 1)

            for (opt in jedis.smembers("poll:1:options")) {
                println(opt + " -> " + jedis.hgetAll(opt))
            }
        }
    }
}