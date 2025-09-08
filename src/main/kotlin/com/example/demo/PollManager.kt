package com.example.demo
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class PollManager {

    private val users = ConcurrentHashMap<String, User>()


    fun createUser(name: String, email: String): User {
        val u = User(name = name, email = email)
        users[u.id] = u
        return u
    }

    fun listUsers(): List<User> = users.values.toList()

    fun getUser(id: String): User? = users[id]

    fun updateUser(id: String, name: String?, email: String?): User? {
        val u = users[id] ?: return null
        name?.let {u.name = it}
        email?.let {u.email = it}
        return u
    }

    fun deleteUser(id: String): Boolean = users.remove(id) != null

    fun createPoll(ownerUserId: String, question: String, options: List<String>): Poll?{
        val owner = getUser(ownerUserId) ?: return null
        val p = Poll(ownerUserId = owner.id, question = question)
        options.forEach { opt -> p.options.add(VoteOption(text = opt)) }
        polls[p.id] = p
        return p
    }

    private val polls = java.util.concurrent.ConcurrentHashMap<String, Poll>()

    fun listPolls(): List<Poll> = polls.values.toList()
    fun getPoll(id: String): Poll? = polls[id]

    fun updatePoll(id: String, question: String?): Poll?{
        val p = polls[id] ?: return null
        question?.let { p.question = it}
        return p
    }

    fun deletePoll(id: String): Boolean {
        return polls.remove(id) != null
    }

    fun addOption(pollId: String, text: String): VoteOption?{
        val p = polls[pollId] ?: return null
        val vo = VoteOption(text = text)
        p.options.add(vo)
        return vo
    }

    fun deleteOption(pollId: String, optionId: String): Boolean {
        val p = polls[pollId] ?: return false
        val used = p.votes.values.any { it.optionId == optionId }
        if (used) return false
        return p.options.removeIf { it.id == optionId }
    }

    fun castVote(pollId: String, userId: String, optionId: String): Vote?{
        val p = polls[pollId] ?: return null
        val user = getUser(userId) ?: return null
        val isOption = p.options.any { it.id == optionId }
        if (!isOption) return null

        val v = Vote(userId = user.id, pollId = p.id, optionId = optionId)
        p.votes[user.id] = v
        return v
    }

    fun listVotes(pollId: String): List<Vote>? {
        val p = polls[pollId] ?: return null
        return p.votes.values.toList()
    }

    fun clearVotes(pollId: String): Boolean {
        val p = polls[pollId] ?: return false
        p.votes.clear()
        return true
    }

}




