package com.example.demo

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/polls")
@CrossOrigin
class PollController(private val pm: PollManager) {

    data class CreatePollRequest(
        val ownerUserId: String,
        val question: String,
        val options: List<String> = emptyList()
    )

    @PostMapping
    fun create(@RequestBody req: CreatePollRequest): ResponseEntity<Any> {
        return try {
            val ownerId = UUID.fromString(req.ownerUserId)
            val p = pm.createPoll(ownerId, req.question, req.options)
                ?: return ResponseEntity.badRequest().body(mapOf("error" to "ownerUserId not found"))
            val pid = requireNotNull(p.id)


            data class CreatePollResponse(val pollId: UUID)
            ResponseEntity.created(URI.create("/polls/$pid")).body(CreatePollResponse(pid))

        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to "ownerUserId must be a UUID"))
        }
    }

    @GetMapping
    fun list(): List<PollDTO> = pm.listPolls().map { it.toDto() }

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ResponseEntity<Any> = try {
        val pid = UUID.fromString(id)
        pm.getPoll(pid)?.let { ResponseEntity.ok(it.toDto()) } ?: ResponseEntity.notFound().build()
    } catch (_: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to "id must be a UUID"))
    }
    data class UpdatePollRequest(val question: String?)

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody req: UpdatePollRequest): ResponseEntity<Any> = try {
        val pid = UUID.fromString(id)
        pm.updatePoll(pid, req.question)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    } catch (_: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to "id must be a UUID"))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): ResponseEntity<Any> = try {
        val pid = UUID.fromString(id)
        if (pm.deletePoll(pid)) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()
    } catch (_: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to "id must be a UUID"))
    }

    data class CreateOptionRequest(val caption: String)

    @PostMapping("/{id}/options")
    fun addOption(@PathVariable id: String, @RequestBody req: CreateOptionRequest): ResponseEntity<Any> {
        return try {
            val pid = UUID.fromString(id)
            val vo = pm.addOption(pid, req.caption) ?: return ResponseEntity.notFound().build()
            ResponseEntity.created(URI.create("/polls/$id/options/${requireNotNull(vo.id)}")).body(vo)
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to "id must be a UUID"))
        }
    }

    @DeleteMapping("/{id}/options/{optId}")
    fun deleteOption(@PathVariable id: String, @PathVariable optId: String): ResponseEntity<Any> = try {
        val pid = UUID.fromString(id)
        val oid = UUID.fromString(optId)
        if (pm.deleteOption(pid, oid)) ResponseEntity.noContent().build() else ResponseEntity.badRequest().build()
    } catch (_: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to "id/optId must be UUIDs"))
    }

    data class VoteRequest(val userId: String, val optionId: String)

    @PostMapping("/{id}/votes")
    fun castVote(@PathVariable id: String, @RequestBody req: VoteRequest): ResponseEntity<Any> {
        return try {
            val pid = UUID.fromString(id)
            val uid = UUID.fromString(req.userId)
            val oid = UUID.fromString(req.optionId)
            val v = pm.castVote(pid, uid, oid)
                ?: return ResponseEntity.badRequest().body(mapOf("error" to "invalid poll/user/option"))
            ResponseEntity.ok(v)
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to "id/userId/optionId must be UUIDs"))
        }
    }

    @GetMapping("/{id}/votes")
    fun listVotes(@PathVariable id: String): ResponseEntity<Any> = try {
        val pid = UUID.fromString(id)
        ResponseEntity.ok(pm.listVotes(pid))
    } catch (_: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to "id must be a UUID"))
    }

    @DeleteMapping("/{id}/votes")
    fun clearVotes(@PathVariable id: String): ResponseEntity<Any> = try {
        val pid = UUID.fromString(id)
        if (pm.clearVotes(pid)) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()
    } catch (_: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to "id must be a UUID"))
    }

    data class PollResultsDTO(val presentationOrder: Int, val count: Long)

    @GetMapping("/{id}/results")
    fun results(@PathVariable id: String): ResponseEntity<Any> = try {
        val pid = UUID.fromString(id)
        val counts = pm.getResultsCached(pid)  // uses cache-aside flow
        val body = counts.entries.sortedBy { it.key }
            .map { (order, cnt) -> PollResultsDTO(order, cnt) }
        ResponseEntity.ok(body)
    } catch (_: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to "id must be a UUID"))
    }
    data class OptionDTO(val id: UUID, val caption: String, val presentationOrder: Int)
    data class PollDTO(val id: UUID, val question: String, val options: List<OptionDTO>)

    private fun Poll.toDto(): PollDTO =
        PollDTO(
            id = requireNotNull(this.id),
            question = this.question,
            options = this.options.map {
                OptionDTO(requireNotNull(it.id), it.caption, it.presentationOrder)
            }.sortedBy { it.presentationOrder }
        )
}
