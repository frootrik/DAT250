package com.example.demo

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(name = "vote_options")
open class VoteOption protected constructor() {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    open var id: UUID? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "poll_id")
    open var poll: Poll? = null

    @Column(nullable = false)
    open var caption: String = ""

    @Column(nullable = false)
    open var presentationOrder: Int = 0

    @OneToMany(mappedBy = "votesOn", cascade = [CascadeType.ALL], orphanRemoval = true)
    open val votes: MutableList<Vote> = mutableListOf()

    constructor(poll: Poll, caption: String, presentationOrder: Int) : this() {
        this.poll = poll
        this.caption = caption
        this.presentationOrder = presentationOrder
    }
}