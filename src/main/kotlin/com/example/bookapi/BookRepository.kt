package com.example.bookapi

import com.example.bookapi.jooq.tables.records.BooksRecord
import com.example.bookapi.jooq.tables.references.BOOKS
import com.example.bookapi.jooq.tables.references.BOOK_AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

import org.springframework.dao.DataAccessException

@Repository
class BookRepository(private val dsl: DSLContext) {
    fun insert(book: BooksRecord): BooksRecord {
        return dsl.insertInto(BOOKS)
            .set(book)
            .returning()
            .fetchOne() ?: throw DataAccessException("Failed to insert book")
    }

    fun findById(id: Long): BooksRecord? {
        return dsl.selectFrom(BOOKS)
            .where(BOOKS.ID.eq(id))
            .fetchOne()
    }

    fun findByAuthorId(authorId: Long): List<BooksRecord> {
        return dsl.select(BOOKS)
            .from(BOOKS)
            .join(BOOK_AUTHORS).on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
            .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
            .fetchInto(BooksRecord::class.java)
    }

    fun update(book: BooksRecord): BooksRecord {
        return dsl.update(BOOKS)
            .set(book)
            .where(BOOKS.ID.eq(book.id))
            .returning()
            .fetchOne() ?: throw EntityNotFoundException("Book not found with id: ${book.id}")
    }
}
