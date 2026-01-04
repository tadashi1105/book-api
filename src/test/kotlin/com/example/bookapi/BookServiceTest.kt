package com.example.bookapi

import java.time.LocalDate
import com.example.bookapi.jooq.tables.records.AuthorsRecord
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.context.annotation.Import

@JooqTest
@Import(BookService::class, BookRepository::class, AuthorRepository::class)
class BookServiceTest {
    @Autowired
    private lateinit var service: BookService

    @Autowired
    private lateinit var authorRepository: AuthorRepository

    @MockkBean
    private lateinit var authorService: AuthorService

    @Test
    fun `should create book`() {
        val authorRecord =
            AuthorsRecord().apply {
                name = "Real Author"
                birthDate = LocalDate.of(1980, 1, 1)
            }
        val savedAuthor = authorRepository.insert(authorRecord)
        val authorId = savedAuthor.id!!

        val authorResponse = AuthorResponse(authorId, "Real Author", LocalDate.of(1980, 1, 1))
        val request = CreateBookRequest("Book", 1000, listOf(authorId), "UNPUBLISHED")

        every { authorService.findByIds(listOf(authorId)) } returns listOf(authorResponse)

        val result = service.create(request)

        assertNotNull(result.id)
        assertEquals("Book", result.title)
        assertEquals(listOf(authorResponse), result.authors)
    }

    @Test
    fun `should throw exception when creating book with invalid author id`() {
        val request = CreateBookRequest("Book", 1000, listOf(999L), "UNPUBLISHED")

        every { authorService.findByIds(listOf(999L)) } throws IllegalArgumentException("Author(s) not found")

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                service.create(request)
            }

        assertEquals("Author(s) not found", exception.message)
    }

    @Test
    fun `should throw exception when updating book with invalid author id`() {
        val authorRecord =
            AuthorsRecord().apply {
                name = "Author"
                birthDate = LocalDate.of(1980, 1, 1)
            }
        val savedAuthor = authorRepository.insert(authorRecord)
        val authorId = savedAuthor.id!!

        val authorResponse = AuthorResponse(authorId, "Author", LocalDate.of(1980, 1, 1))
        every { authorService.findByIds(listOf(authorId)) } returns listOf(authorResponse)

        val savedBook = service.create(CreateBookRequest("Existing Book", 1000, listOf(authorId), "PUBLISHED"))

        every { authorService.findByIds(listOf(999L)) } throws IllegalArgumentException("Author(s) not found")

        val request = UpdateBookRequest("Updated Title", 1200, listOf(999L), "PUBLISHED")

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                service.update(savedBook.id, request)
            }

        assertEquals("Author(s) not found", exception.message)
    }

    @Test
    fun `should prevent changing status from PUBLISHED to UNPUBLISHED`() {
        val authorRecord =
            AuthorsRecord().apply {
                name = "Author"
                birthDate = LocalDate.of(1980, 1, 1)
            }
        val savedAuthor = authorRepository.insert(authorRecord)
        val authorId = savedAuthor.id!!

        val authorResponse = AuthorResponse(authorId, "Author", LocalDate.of(1980, 1, 1))
        every { authorService.findByIds(listOf(authorId)) } returns listOf(authorResponse)

        val savedBook = service.create(CreateBookRequest("Existing Book", 1000, listOf(authorId), "PUBLISHED"))

        val request = UpdateBookRequest("Updated Title", 1200, listOf(authorId), "UNPUBLISHED")

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                service.update(savedBook.id, request)
            }

        assertTrue(exception.message!!.contains("PUBLISHED to UNPUBLISHED"))
    }

    @Test
    fun `should create book with multiple authors`() {
        // Prepare real authors in DB
        val author1 =
            authorRepository.insert(
                AuthorsRecord().apply {
                    name = "Author1"
                    birthDate = LocalDate.of(1980, 1, 1)
                },
            )
        val author2 =
            authorRepository.insert(
                AuthorsRecord().apply {
                    name = "Author2"
                    birthDate = LocalDate.of(1985, 1, 1)
                },
            )
        val authorIds = listOf(author1.id!!, author2.id!!)

        val authorResponses =
            listOf(
                AuthorResponse(author1.id!!, "Author1", LocalDate.of(1980, 1, 1)),
                AuthorResponse(author2.id!!, "Author2", LocalDate.of(1985, 1, 1)),
            )

        val request = CreateBookRequest("Book", 1000, authorIds, "UNPUBLISHED")

        every { authorService.findByIds(authorIds) } returns authorResponses

        val result = service.create(request)

        assertNotNull(result.id)
        assertEquals("Book", result.title)
        assertEquals(authorResponses, result.authors)
    }

    @Test
    fun `should find book with multiple authors`() {
        val author1 =
            authorRepository.insert(
                AuthorsRecord().apply {
                    name = "Author1"
                    birthDate = LocalDate.of(1980, 1, 1)
                },
            )
        val author2 =
            authorRepository.insert(
                AuthorsRecord().apply {
                    name = "Author2"
                    birthDate = LocalDate.of(1985, 1, 1)
                },
            )
        val authorIds = listOf(author1.id!!, author2.id!!)

        val authorResponses =
            listOf(
                AuthorResponse(author1.id!!, "Author1", LocalDate.of(1980, 1, 1)),
                AuthorResponse(author2.id!!, "Author2", LocalDate.of(1985, 1, 1)),
            )

        val request = CreateBookRequest("Book", 1000, authorIds, "UNPUBLISHED")

        every { authorService.findByIds(authorIds) } returns authorResponses

        val created = service.create(request)

        // JOINを使用するため、findByIdにおけるfindByIdsのモック化は不要

        val result = service.findById(created.id)

        assertEquals(created.id, result.id)
        assertEquals("Book", result.title)
        assertEquals(authorResponses, result.authors)
    }

    @Test
    fun `should update book with multiple authors`() {
        // Prepare authors
        val author1 =
            authorRepository.insert(
                AuthorsRecord().apply {
                    name = "Author1"
                    birthDate = LocalDate.of(1980, 1, 1)
                },
            )
        val author2 =
            authorRepository.insert(
                AuthorsRecord().apply {
                    name = "Author2"
                    birthDate = LocalDate.of(1985, 1, 1)
                },
            )
        val author3 =
            authorRepository.insert(
                AuthorsRecord().apply {
                    name = "Author3"
                    birthDate = LocalDate.of(1990, 1, 1)
                },
            )

        val initialAuthorIds = listOf(author1.id!!)
        val initialAuthors = listOf(AuthorResponse(author1.id!!, "Author1", LocalDate.of(1980, 1, 1)))

        val updateAuthorIds = listOf(author2.id!!, author3.id!!)
        val updateAuthors =
            listOf(
                AuthorResponse(author2.id!!, "Author2", LocalDate.of(1985, 1, 1)),
                AuthorResponse(author3.id!!, "Author3", LocalDate.of(1990, 1, 1)),
            )

        every { authorService.findByIds(initialAuthorIds) } returns initialAuthors
        val created = service.create(CreateBookRequest("Book", 1000, initialAuthorIds, "UNPUBLISHED"))

        // Update to multiple authors
        every { authorService.findByIds(updateAuthorIds) } returns updateAuthors
        val updateRequest = UpdateBookRequest("Updated Book", 1500, updateAuthorIds, "UNPUBLISHED")

        val result = service.update(created.id, updateRequest)

        assertEquals(created.id, result.id)
        assertEquals("Updated Book", result.title)
        assertEquals(1500, result.price)
        assertEquals(updateAuthors, result.authors)
    }
}
