package com.example.bookapi

import java.time.LocalDate
import com.example.bookapi.jooq.tables.records.AuthorsRecord
import com.example.bookapi.jooq.tables.references.BOOK_AUTHORS
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.context.annotation.Import

@JooqTest
@Import(AuthorRepository::class)
class AuthorRepositoryTest {
    @Autowired
    private lateinit var repository: AuthorRepository

    @Autowired
    private lateinit var dsl: DSLContext

    @Test
    fun `should insert and find author`() {
        // ... (existing test)
    }

    // ... (existing tests)

    @Test
    fun `should find author ids by book id`() {
        val author1 =
            repository.insert(
                AuthorsRecord().apply {
                    name = "Book Author 1"
                    birthDate = LocalDate.of(1980, 1, 1)
                },
            )
        val author2 =
            repository.insert(
                AuthorsRecord().apply {
                    name = "Book Author 2"
                    birthDate = LocalDate.of(1985, 2, 2)
                },
            )

        // Use dsl directly to insert book and associations since BookRepository is not in context yet
        val bookId =
            dsl.insertInto(com.example.bookapi.jooq.tables.references.BOOKS)
                .set(com.example.bookapi.jooq.tables.references.BOOKS.TITLE, "Test Book") // Assuming title is required
                .set(com.example.bookapi.jooq.tables.references.BOOKS.PRICE, 1000)
                .set(com.example.bookapi.jooq.tables.references.BOOKS.PUBLICATION_STATUS, "PUBLISHED")
                .returning(com.example.bookapi.jooq.tables.references.BOOKS.ID)
                .fetchOne()!!.id

        dsl.insertInto(BOOK_AUTHORS)
            .set(BOOK_AUTHORS.BOOK_ID, bookId)
            .set(BOOK_AUTHORS.AUTHOR_ID, author1.id)
            .execute()

        dsl.insertInto(BOOK_AUTHORS)
            .set(BOOK_AUTHORS.BOOK_ID, bookId)
            .set(BOOK_AUTHORS.AUTHOR_ID, author2.id)
            .execute()

        val authorIds = repository.findAuthorIdsByBookId(bookId!!)

        assertEquals(2, authorIds.size)
        assertTrue(authorIds.contains(author1.id))
        assertTrue(authorIds.contains(author2.id))
    }

    @Test
    fun `should return empty list when no authors associated with book`() {
        val bookId =
            dsl.insertInto(com.example.bookapi.jooq.tables.references.BOOKS)
                .set(com.example.bookapi.jooq.tables.references.BOOKS.TITLE, "No Author Book")
                .set(com.example.bookapi.jooq.tables.references.BOOKS.PRICE, 1000)
                .set(com.example.bookapi.jooq.tables.references.BOOKS.PUBLICATION_STATUS, "PUBLISHED")
                .returning(com.example.bookapi.jooq.tables.references.BOOKS.ID)
                .fetchOne()!!.id

        val authorIds = repository.findAuthorIdsByBookId(bookId!!)
        assertTrue(authorIds.isEmpty())
    }
}
