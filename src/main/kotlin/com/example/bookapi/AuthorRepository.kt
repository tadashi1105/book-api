package com.example.bookapi

import com.example.bookapi.jooq.tables.records.AuthorsRecord
import com.example.bookapi.jooq.tables.references.AUTHORS
import org.jooq.DSLContext
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository

@Repository
class AuthorRepository(private val dsl: DSLContext) {
    fun insert(author: AuthorsRecord): AuthorsRecord {
        return dsl.insertInto(AUTHORS)
            .set(author)
            .returning()
            .fetchOne() ?: throw DataIntegrityViolationException("Failed to insert author")
    }

    fun findById(id: Long): AuthorsRecord? {
        return dsl.selectFrom(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOne()
    }

    fun findByIds(ids: List<Long>): List<AuthorsRecord> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        return dsl.selectFrom(AUTHORS)
            .where(AUTHORS.ID.`in`(ids))
            .fetch()
    }

    fun findAll(): List<AuthorsRecord> {
        return dsl.selectFrom(AUTHORS)
            .fetch()
    }

    fun findAuthorIdsByBookId(bookId: Long): List<Long> {
        return dsl.select(com.example.bookapi.jooq.tables.references.BOOK_AUTHORS.AUTHOR_ID)
            .from(com.example.bookapi.jooq.tables.references.BOOK_AUTHORS)
            .where(com.example.bookapi.jooq.tables.references.BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .fetchInto(Long::class.java)
    }

    fun update(author: AuthorsRecord): AuthorsRecord {
        return dsl.update(AUTHORS)
            .set(author)
            .where(AUTHORS.ID.eq(author.id))
            .returning()
            .fetchOne() ?: throw EntityNotFoundException("Author not found with id: ${author.id}")
    }
}
