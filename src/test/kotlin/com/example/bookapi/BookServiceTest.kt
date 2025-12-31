package com.example.bookapi

import com.example.bookapi.jooq.tables.records.AuthorsRecord
import com.example.bookapi.jooq.tables.records.BooksRecord
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.context.annotation.Import
import java.time.LocalDate

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
        // Prepare real author in DB
        val authorRecord = AuthorsRecord().apply {
            name = "Real Author"
            birthDate = LocalDate.of(1980, 1, 1)
        }
        val savedAuthor = authorRepository.insert(authorRecord)
        val authorId = savedAuthor.id!!

        val authorResponse = AuthorResponse(authorId, "Real Author", LocalDate.of(1980, 1, 1))
        val request = CreateBookRequest("Book", 1000, authorId, "UNPUBLISHED")

        every { authorService.findById(authorId) } returns authorResponse

        val result = service.create(request)

        assertNotNull(result.id)
        assertEquals("Book", result.title)
        assertEquals(authorResponse, result.author)
    }

    @Test
    fun `should prevent changing status from PUBLISHED to UNPUBLISHED`() {
        // Prepare author and book
        val authorRecord = AuthorsRecord().apply {
            name = "Author"
            birthDate = LocalDate.of(1980, 1, 1)
        }
        val savedAuthor = authorRepository.insert(authorRecord)
        val authorId = savedAuthor.id!!

        val bookRecord = BooksRecord().apply {
            title = "Existing Book"
            price = 1000
            publicationStatus = "PUBLISHED"
        }
        val authorResponse = AuthorResponse(authorId, "Author", LocalDate.of(1980, 1, 1))
        every { authorService.findById(authorId) } returns authorResponse
        
        val savedBook = service.create(CreateBookRequest("Existing Book", 1000, authorId, "PUBLISHED"))

        val request = UpdateBookRequest("Updated Title", 1200, "UNPUBLISHED")

        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.update(savedBook.id, request)
        }

        assertTrue(exception.message!!.contains("PUBLISHED to UNPUBLISHED"))
    }
}
