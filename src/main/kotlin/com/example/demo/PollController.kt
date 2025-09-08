package com.example.demo

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/polls")
class PollController(private val pm: PollManager) {

    // --- Create poll ---
    data class CreatePollRequest(val ownerUserId: String, val question: String, val options: List<String> = emptyList())

    @PostMapping
    fun create(@RequestBody req: CreatePollRequest): ResponseEntity<Any> {
        val p = pm.createPoll(req.ownerUserId, req.question, req.options)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "ownerUserId not found"))
        return ResponseEntity.created(URI.create("/polls/${p.id}")).body(p)
    }

    // --- Read polls ---
    @GetMapping
    fun list(): List<Poll> = pm.listPolls()

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ResponseEntity<Poll> =
        pm.getPoll(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    // --- Update/Delete poll ---
    data class UpdatePollRequest(val question: String?)

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody req: UpdatePollRequest): ResponseEntity<Poll> =
        pm.updatePoll(id, req.question)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): ResponseEntity<Void> =
        if (pm.deletePoll(id)) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()

    // --- Options ---
    data class CreateOptionRequest(val text: String)

    @PostMapping("/{id}/options")
    fun addOption(@PathVariable id: String, @RequestBody req: CreateOptionRequest): ResponseEntity<Any> {
        val vo = pm.addOption(id, req.text) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.created(URI.create("/polls/$id/options/${vo.id}")).body(vo)
    }

    @DeleteMapping("/{id}/options/{optId}")
    fun deleteOption(@PathVariable id: String, @PathVariable optId: String): ResponseEntity<Void> =
        if (pm.deleteOption(id, optId)) ResponseEntity.noContent().build() else ResponseEntity.badRequest().build()

    // --- Votes ---
    data class VoteRequest(val userId: String, val optionId: String)

    @PostMapping("/{id}/votes")
    fun castVote(@PathVariable id: String, @RequestBody req: VoteRequest): ResponseEntity<Any> {
        val v = pm.castVote(id, req.userId, req.optionId)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "invalid poll/user/option"))
        return ResponseEntity.ok(v)
    }

    @GetMapping("/{id}/votes")
    fun listVotes(@PathVariable id: String): ResponseEntity<List<Vote>> {
        val votes = pm.listVotes(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(votes)
    }

    @DeleteMapping("/{id}/votes")
    fun clearVotes(@PathVariable id: String): ResponseEntity<Void> =
        if (pm.clearVotes(id)) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()
}