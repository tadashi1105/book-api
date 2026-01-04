package com.example.bookapi

import java.time.LocalDate
import com.example.bookapi.jooq.tables.records.AuthorsRecord
import com.example.bookapi.jooq.tables.records.BooksRecord
import com.example.bookapi.jooq.tables.references.BOOK_AUTHORS
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.context.annotation.Import

@JooqTest
@Import(BookRepository::class, AuthorRepository::class)
class BookRepositoryTest {
    @Autowired
    private lateinit var repository: BookRepository

    @Autowired
    private lateinit var authorRepository: AuthorRepository

    @Autowired
    private lateinit var dsl: DSLContext

    @Test
    fun `should insert and find book`() {
        val record =
            BooksRecord().apply {
                title = "Sample Book"
                price = 1000
                publicationStatus = "UNPUBLISHED"
            }
        val inserted = repository.insert(record)
        assertNotNull(inserted.id)

        val found = repository.findById(inserted.id!!)
        assertNotNull(found)
        assertEquals("Sample Book", found?.title)
    }

    @Test
    fun `should find author ids by book id`() {
        val author1 =
            authorRepository.insert(
                AuthorsRecord().apply {
                    name = "Author One"
                    birthDate = LocalDate.of(1980, 1, 1)
                },
            )
        val author2 =
            authorRepository.insert(
                AuthorsRecord().apply {
                    name = "Author Two"
                    birthDate = LocalDate.of(1985, 2, 2)
                },
            )
        val book =
            repository.insert(
                BooksRecord().apply {
                    title = "Test Book"
                    price = 1000
                    publicationStatus = "UNPUBLISHED"
                },
            )

        dsl.insertInto(BOOK_AUTHORS)
            .set(BOOK_AUTHORS.BOOK_ID, book.id)
            .set(BOOK_AUTHORS.AUTHOR_ID, author1.id)
            .execute()
        dsl.insertInto(BOOK_AUTHORS)
            .set(BOOK_AUTHORS.BOOK_ID, book.id)
            .set(BOOK_AUTHORS.AUTHOR_ID, author2.id)
            .execute()

        val authorIds = repository.findAuthorIdsByBookId(book.id!!)

        assertEquals(2, authorIds.size)
        assertTrue(authorIds.contains(author1.id))
        assertTrue(authorIds.contains(author2.id))
    }

    @Test
    fun `should delete author associations`() {
        val author =
            authorRepository.insert(
                AuthorsRecord().apply {
                    name = "Author"
                    birthDate = LocalDate.of(1980, 1, 1)
                },
            )
        val book =
            repository.insert(
                BooksRecord().apply {
                    title = "Test Book"
                    price = 1000
                    publicationStatus = "UNPUBLISHED"
                },
            )

        dsl.insertInto(BOOK_AUTHORS)
            .set(BOOK_AUTHORS.BOOK_ID, book.id)
            .set(BOOK_AUTHORS.AUTHOR_ID, author.id)
            .execute()

        repository.deleteAuthorAssociations(book.id!!)

        val authorIds = repository.findAuthorIdsByBookId(book.id!!)
        assertEquals(0, authorIds.size)
    }

    @Test
    fun `should insert author associations in batch`() {
        val author1 =
            authorRepository.insert(
                AuthorsRecord().apply {
                    name = "Author One"
                    birthDate = LocalDate.of(1980, 1, 1)
                },
            )
        val author2 =
            authorRepository.insert(
                AuthorsRecord().apply {
                    name = "Author Two"
                    birthDate = LocalDate.of(1985, 2, 2)
                },
            )
        val author3 =
            authorRepository.insert(
                AuthorsRecord().apply {
                    name = "Author Three"
                    birthDate = LocalDate.of(1990, 3, 3)
                },
            )
        val book =
            repository.insert(
                BooksRecord().apply {
                    title = "Test Book"
                    price = 1000
                    publicationStatus = "UNPUBLISHED"
                },
            )

        repository.insertAuthorAssociations(book.id!!, listOf(author1.id!!, author2.id!!, author3.id!!))

        val authorIds = repository.findAuthorIdsByBookId(book.id!!)
        assertEquals(3, authorIds.size)
        assertTrue(authorIds.contains(author1.id))
        assertTrue(authorIds.contains(author2.id))
        assertTrue(authorIds.contains(author3.id))
    }
}
