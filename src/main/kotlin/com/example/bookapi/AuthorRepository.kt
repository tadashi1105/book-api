package com.example.bookapi

import com.example.bookapi.jooq.tables.records.AuthorsRecord
import com.example.bookapi.jooq.tables.references.AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

import org.springframework.dao.DataAccessException

@Repository
class AuthorRepository(private val dsl: DSLContext) {
    fun insert(author: AuthorsRecord): AuthorsRecord {
        return dsl.insertInto(AUTHORS)
            .set(author)
            .returning()
            .fetchOne() ?: throw DataAccessException("Failed to insert author")
    }

    fun findById(id: Long): AuthorsRecord? {
        return dsl.selectFrom(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOne()
    }

    fun findAll(): List<AuthorsRecord> {
        return dsl.selectFrom(AUTHORS)
            .fetch()
    }

    fun update(author: AuthorsRecord): AuthorsRecord {
        return dsl.update(AUTHORS)
            .set(author)
            .where(AUTHORS.ID.eq(author.id))
            .returning()
            .fetchOne() ?: throw EntityNotFoundException("Author not found with id: ${author.id}")
    }
}
