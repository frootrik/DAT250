package com.example.demo

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/users")
@CrossOrigin
class UserController(private val pm: PollManager) {

    data class CreateUserRequest(val username: String, val email: String)
    data class UpdateUserRequest(val username: String?, val email: String?)
    data class UserDTO(val id: UUID, val username: String, val email: String)

    @PostMapping
    fun create(@RequestBody req: CreateUserRequest): ResponseEntity<UserDTO> {
        val u = pm.createUser(req.username, req.email)
        val id = requireNotNull(u.id)
        return ResponseEntity.created(URI.create("/users/$id"))
            .body(UserDTO(id, u.username, u.email))
    }

    @GetMapping
    fun list(): List<UserDTO> =
        pm.listUsers().map { UserDTO(requireNotNull(it.id), it.username, it.email) }

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ResponseEntity<Any> = try {
        val uid = UUID.fromString(id)
        pm.getUser(uid)
            ?.let { ResponseEntity.ok(UserDTO(requireNotNull(it.id), it.username, it.email)) }
            ?: ResponseEntity.notFound().build()
    } catch (_: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to "id must be a UUID"))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody req: UpdateUserRequest): ResponseEntity<Any> = try {
        val uid = UUID.fromString(id)
        pm.updateUser(uid, req.username, req.email)
            ?.let { ResponseEntity.ok(UserDTO(requireNotNull(it.id), it.username, it.email)) }
            ?: ResponseEntity.notFound().build()
    } catch (_: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to "id must be a UUID"))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): ResponseEntity<Any> = try {
        val uid = UUID.fromString(id)
        if (pm.deleteUser(uid)) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
    } catch (_: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to "id must be a UUID"))
    }
}
