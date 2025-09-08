package com.example.demo

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/users")
class UserController (private val pm: PollManager){

    data class CreateUserRequest(val name: String, val email: String)
    data class UpdateUserRequest(val name: String?, val email: String?)
    data class UserDTO(val id: String, val name: String, val email: String)

    @PostMapping
    fun create(@RequestBody req: CreateUserRequest): ResponseEntity<UserDTO>{
        val u = pm.createUser(req.name, req.email)
        return ResponseEntity.created(URI.create("/users/${u.id}")).body(UserDTO(u.id, u.name, u.email))
    }

    @GetMapping
    fun list(): List<UserDTO> =
        pm.listUsers().map { UserDTO(it.id, it.name, it.email) }

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ResponseEntity<UserDTO> =
        pm.getUser(id)?.let { ResponseEntity.ok(UserDTO(it.id, it.name, it.email)) }
            ?: ResponseEntity.notFound().build()

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody req: UpdateUserRequest): ResponseEntity<UserDTO> =
        pm.updateUser(id, req.name, req.email)
            ?.let { ResponseEntity.ok(UserDTO(it.id, it.name, it.email)) }
            ?: ResponseEntity.notFound().build()

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): ResponseEntity<Void> =
        if (pm.deleteUser(id)) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
}