package com.example.bookapi

import java.time.LocalDate
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size

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
    // 要件2.5: 価格は非負であること
    @field:Min(0) val price: Int,
    // 要件2.1: 書籍には最低1名の著者が必須
    @field:NotEmpty @field:Size(min = 1) val authorIds: List<Long>?,
    @field:NotBlank val publicationStatus: String,
)

data class UpdateBookRequest(
    @field:NotBlank val title: String,
    // 要件2.5: 価格は非負であること
    @field:Min(0) val price: Int,
    // 要件2.3: データ整合性のため、著者が0名になる更新は不可
    @field:NotEmpty @field:Size(min = 1) val authorIds: List<Long>?,
    @field:NotBlank val publicationStatus: String,
)

data class BookResponse(
    val id: Long,
    val title: String,
    val price: Int,
    val publicationStatus: String,
    val authors: List<AuthorResponse>,
)
