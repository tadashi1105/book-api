package com.example.bookapi

import com.example.bookapi.jooq.tables.records.AuthorsRecord
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthorService(private val authorRepository: AuthorRepository) {
    fun create(request: CreateAuthorRequest): AuthorResponse {
        val record =
            AuthorsRecord().apply {
                name = request.name
                birthDate = request.birthDate
            }
        val saved = authorRepository.insert(record)
        return saved.toResponse()
    }

    fun findById(id: Long): AuthorResponse {
        val record = authorRepository.findById(id) ?: throw EntityNotFoundException("Author not found with id: $id")
        return record.toResponse()
    }

    fun findByIds(ids: List<Long>): List<AuthorResponse> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        val records = authorRepository.findByIds(ids)
        if (records.size != ids.size) {
            // 仕様: 部分的な成功は許容しない。要求された全IDが存在する必要がある (Req 2.4)
            val foundIds = records.map { it.id!! }.toSet()
            val missingIds = ids.filter { it !in foundIds }
            throw IllegalArgumentException("Author(s) not found with id(s): ${missingIds.joinToString(", ")}")
        }
        return records.map { it.toResponse() }
    }

    fun findAll(): List<AuthorResponse> {
        return authorRepository.findAll().map { it.toResponse() }
    }

    fun update(
        id: Long,
        request: UpdateAuthorRequest,
    ): AuthorResponse {
        val existing = authorRepository.findById(id) ?: throw EntityNotFoundException("Author not found with id: $id")
        existing.apply {
            name = request.name
            birthDate = request.birthDate
        }
        val updated = authorRepository.update(existing)
        return updated.toResponse()
    }

    private fun AuthorsRecord.toResponse() =
        AuthorResponse(
            id = this.id!!,
            name = this.name!!,
            birthDate = this.birthDate!!,
        )
}
