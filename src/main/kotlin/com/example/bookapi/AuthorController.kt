package com.example.bookapi

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/authors")
class AuthorController(
    private val authorService: AuthorService,
    private val bookService: BookService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateAuthorRequest,
    ): AuthorResponse {
        return authorService.create(request)
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: Long,
    ): AuthorResponse {
        return authorService.findById(id)
    }

    @GetMapping
    fun getAll(): List<AuthorResponse> {
        return authorService.findAll()
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateAuthorRequest,
    ): AuthorResponse {
        return authorService.update(id, request)
    }

    @GetMapping("/{id}/books")
    fun getBooksByAuthor(
        @PathVariable id: Long,
    ): List<BookResponse> {
        return bookService.findByAuthorId(id)
    }
}
