package com.example.bookapi

import java.time.LocalDate
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past

data class CreateAuthorRequest(
    @field:NotBlank val name: String,
    @field:Past val birthDate: LocalDate,
)

data class UpdateAuthorRequest(
    @field:NotBlank val name: String,
    @field:Past val birthDate: LocalDate,
)

data class AuthorResponse(
    val id: Long,
    val name: String,
    val birthDate: LocalDate,
)

data class CreateBookRequest(
    @field:NotBlank val title: String,
    @field:Min(0) val price: Int,
    @field:NotNull val authorId: Long,
    @field:NotBlank val publicationStatus: String,
)

data class UpdateBookRequest(
    @field:NotBlank val title: String,
    @field:Min(0) val price: Int,
    @field:NotBlank val publicationStatus: String,
)

data class BookResponse(
    val id: Long,
    val title: String,
    val price: Int,
    val publicationStatus: String,
    val author: AuthorResponse,
)
