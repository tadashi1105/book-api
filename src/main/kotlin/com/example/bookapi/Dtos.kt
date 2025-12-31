package com.example.bookapi

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import java.time.LocalDate

data class CreateAuthorRequest(
    @field:NotBlank val name: String,
    @field:Past val birthDate: LocalDate
)

data class UpdateAuthorRequest(
    @field:NotBlank val name: String,
    @field:Past val birthDate: LocalDate
)

data class AuthorResponse(
    val id: Long,
    val name: String,
    val birthDate: LocalDate
)

