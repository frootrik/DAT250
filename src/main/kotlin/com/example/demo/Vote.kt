package com.example.demo

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "votes")
open class Vote protected constructor() {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    open var id: UUID? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "voter_id")
    open var createdBy: User? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "option_id")
    open var votesOn: VoteOption? = null

    @Column(nullable = false)
    open var createdAt: Instant = Instant.now()

    constructor(createdBy: User, votesOn: VoteOption) : this() {
        this.createdBy = createdBy
        this.votesOn = votesOn
    }
}
