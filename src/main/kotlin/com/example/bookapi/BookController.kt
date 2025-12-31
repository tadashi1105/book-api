package com.example.bookapi

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/books")
class BookController(private val bookService: BookService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateBookRequest): BookResponse {
        return bookService.create(request)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): BookResponse {
        return bookService.findById(id)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdateBookRequest): BookResponse {
        return bookService.update(id, request)
    }
}
