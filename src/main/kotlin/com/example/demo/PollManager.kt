package com.example.demo

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PollManager(
    @PersistenceContext private val em: EntityManager
) {


    @Transactional
    fun createUser(username: String, email: String): User {
        val u = User(username, email)
        em.persist(u)
        return u
    }

    fun listUsers(): List<User> =
        em.createQuery("select u from User u", User::class.java).resultList

    fun getUser(id: UUID): User? = em.find(User::class.java, id)

    @Transactional
    fun updateUser(id: UUID, username: String?, email: String?): User? {
        val u = em.find(User::class.java, id) ?: return null
        username?.let { u.username = it }
        email?.let { u.email = it }
        return u
    }

    @Transactional
    fun deleteUser(id: UUID): Boolean {
        val u = em.find(User::class.java, id) ?: return false
        em.remove(u)
        return true
    }


    @Transactional
    fun createPoll(ownerUserId: UUID, question: String, options: List<String>): Poll? {
        val owner = em.find(User::class.java, ownerUserId) ?: return null
        val p = owner.createPoll(question)
        options.forEach { caption -> p.addVoteOption(caption) }
        em.persist(p)
        return p
    }

    fun listPolls(): List<Poll> =
        em.createQuery("select p from Poll p", Poll::class.java).resultList

    fun getPoll(id: UUID): Poll? = em.find(Poll::class.java, id)

    @Transactional
    fun updatePoll(id: UUID, question: String?): Poll? {
        val p = em.find(Poll::class.java, id) ?: return null
        question?.let { p.question = it }
        return p
    }

    @Transactional
    fun deletePoll(id: UUID): Boolean {
        val p = em.find(Poll::class.java, id) ?: return false
        em.remove(p)
        return true
    }

    @Transactional
    fun addOption(pollId: UUID, caption: String): VoteOption? {
        val p = em.find(Poll::class.java, pollId) ?: return null
        val vo = p.addVoteOption(caption)
        em.persist(vo)
        return vo
    }

    @Transactional
    fun deleteOption(pollId: UUID, optionId: UUID): Boolean {
        val opt = em.find(VoteOption::class.java, optionId) ?: return false
        if (opt.poll?.id != pollId) return false

        val used = em.createQuery(
            "select count(v) from Vote v where v.votesOn = :opt",
            java.lang.Long::class.java
        ).setParameter("opt", opt).singleResult > 0L
        if (used) return false

        opt.poll?.options?.remove(opt)
        em.remove(opt)
        return true
    }


    @Transactional
    fun castVote(pollId: UUID, userId: UUID, optionId: UUID): Vote? {
        val user = em.find(User::class.java, userId) ?: return null
        val option = em.find(VoteOption::class.java, optionId) ?: return null
        if (option.poll?.id != pollId) return null
        val vote = user.voteFor(option)
        em.persist(vote)
        return vote
    }

    fun listVotes(pollId: UUID): List<Vote> =
        em.createQuery(
            "select v from Vote v where v.votesOn.poll.id = :pid",
            Vote::class.java
        ).setParameter("pid", pollId).resultList

    @Transactional
    fun clearVotes(pollId: UUID): Boolean {
        val vs = listVotes(pollId)
        vs.forEach { em.remove(it) }
        return vs.isNotEmpty()
    }
}
