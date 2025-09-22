package com.example.demo

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "polls")
open class Poll protected constructor() {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    open var id: UUID? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_id")
    open var createdBy: User? = null

    @Column(nullable = false)
    open var question: String = ""

    @Column(nullable = false)
    open var createdAt: Instant = Instant.now()

    @OneToMany(mappedBy = "poll", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("presentationOrder ASC")
    open val options: MutableList<VoteOption> = mutableListOf()

    constructor(createdBy: User, question: String) : this() {
        this.createdBy = createdBy
        this.question = question
    }

    fun addVoteOption(caption: String): VoteOption {
        val o = VoteOption(this, caption, options.size)
        options.add(o)
        return o
    }

    fun addOption(caption: String): VoteOption = addVoteOption(caption)
}
