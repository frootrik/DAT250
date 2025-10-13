package com.example.demo

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PollManager(
    @PersistenceContext private val em: EntityManager,
    private val pollCache: PollCacheRepository,
    private val topicService: TopicService
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

        em.flush()
        val topic = pollTopicName(p.id!!, p.question)
        topicService.ensureTopic(topic)
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

        pollCache.invalidate(pollId)

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

    fun getResultsCached(pollId: UUID): Map<Int, Long> {
        pollCache.get(pollId)?.let { return it }

        val poll = em.find(Poll::class.java, pollId) ?: return emptyMap()
        val base = poll.options.associate { it.presentationOrder to 0L }.toMutableMap()
        val votes = listVotes(pollId)
        votes.forEach { v ->
            val order = requireNotNull(v.votesOn).presentationOrder
            base[order] = base.getOrDefault(order, 0L) + 1L
        }
        val counts = base.toSortedMap()

        pollCache.put(pollId, counts)
        return counts
    }

    @Transactional
    fun applyAnonymousVote(pollId: UUID, optionId: UUID): Boolean {
        println("DBG vote: pollId=$pollId optionId=$optionId")

        val option = em.find(VoteOption::class.java, optionId)
            ?: run { println("DBG -> option not found"); return false }

        val pol = option.poll
        println("DBG -> option.pollId=${pol?.id}")

        if (pol?.id != pollId) {
            println("DBG -> pollId mismatch (option belongs to ${pol?.id})")
            return false
        }

        val anon = em.createQuery(
            "select u from User u where u.username = :u", User::class.java
        ).setParameter("u", "anonymous").resultList.firstOrNull()
            ?: User("anonymous", "anonymous@example.com").also { em.persist(it) }

        val vote = anon.voteFor(option)
        em.persist(vote)

        pollCache.invalidate(pollId)
        println("DBG -> vote persisted")
        return true
    }

}
