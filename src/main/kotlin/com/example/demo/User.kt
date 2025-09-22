package com.example.demo

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.util.LinkedHashSet
import java.util.UUID

@Entity
@Table(name = "users")
open class User protected constructor() {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    open var id: UUID? = null

    @Column(nullable = false, unique = true)
    open var username: String = ""

    @Column(nullable = false)
    open var email: String = ""

    @OneToMany(mappedBy = "createdBy", cascade = [CascadeType.ALL], orphanRemoval = true)
    open val created: MutableSet<Poll> = LinkedHashSet()

    constructor(username: String, email: String) : this() {
        this.username = username
        this.email = email
    }

    fun createPoll(question: String): Poll {
        val p = Poll(this, question)
        created.add(p)
        return p
    }

    fun voteFor(option: VoteOption): Vote {
        val v = Vote(this, option)
        option.votes.add(v)
        return v
    }
}
