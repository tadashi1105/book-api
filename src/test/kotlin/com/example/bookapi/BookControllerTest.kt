package com.example.bookapi

import java.time.LocalDate
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(BookController::class)
@Import(GlobalExceptionHandler::class)
class BookControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var bookService: BookService

    @Test
    fun `should create book`() {
        val authors = listOf(AuthorResponse(1L, "Author", LocalDate.of(1990, 1, 1)))
        val request =
            """
            {
                "title": "New Book",
                "price": 1500,
                "authorIds": [1],
                "publicationStatus": "UNPUBLISHED"
            }
            """.trimIndent()

        val response = BookResponse(10L, "New Book", 1500, "UNPUBLISHED", authors)
        every { bookService.create(any()) } returns response

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(10L))
            .andExpect(jsonPath("$.title").value("New Book"))
            .andExpect(jsonPath("$.authors").isArray)
            .andExpect(jsonPath("$.authors[0].id").value(1L))
    }

    @Test
    fun `should create book with multiple authors`() {
        val authors =
            listOf(
                AuthorResponse(1L, "Author One", LocalDate.of(1990, 1, 1)),
                AuthorResponse(2L, "Author Two", LocalDate.of(1985, 2, 2)),
            )
        val request =
            """
            {
                "title": "Collaborative Book",
                "price": 2000,
                "authorIds": [1, 2],
                "publicationStatus": "UNPUBLISHED"
            }
            """.trimIndent()

        val response = BookResponse(11L, "Collaborative Book", 2000, "UNPUBLISHED", authors)
        every { bookService.create(any()) } returns response

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(11L))
            .andExpect(jsonPath("$.title").value("Collaborative Book"))
            .andExpect(jsonPath("$.authors").isArray)
            .andExpect(jsonPath("$.authors.length()").value(2))
            .andExpect(jsonPath("$.authors[0].id").value(1L))
            .andExpect(jsonPath("$.authors[1].id").value(2L))
    }

    @Test
    fun `should get book by id`() {
        val authors = listOf(AuthorResponse(1L, "Author", LocalDate.of(1990, 1, 1)))
        val response = BookResponse(10L, "Sample Book", 1000, "PUBLISHED", authors)
        every { bookService.findById(10L) } returns response

        mockMvc.perform(get("/books/10"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(10L))
            .andExpect(jsonPath("$.title").value("Sample Book"))
            .andExpect(jsonPath("$.authors").isArray)
            .andExpect(jsonPath("$.authors[0].id").value(1L))
    }

    @Test
    fun `should reject create book with empty authorIds`() {
        val request =
            """
            {
                "title": "New Book",
                "price": 1500,
                "authorIds": [],
                "publicationStatus": "UNPUBLISHED"
            }
            """.trimIndent()

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should reject create book with null authorIds`() {
        val request =
            """
            {
                "title": "New Book",
                "price": 1500,
                "authorIds": null,
                "publicationStatus": "UNPUBLISHED"
            }
            """.trimIndent()

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should reject update book with empty authorIds`() {
        val request =
            """
            {
                "title": "Updated Book",
                "price": 1500,
                "authorIds": [],
                "publicationStatus": "UNPUBLISHED"
            }
            """.trimIndent()

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should update book with multiple authors`() {
        val authors =
            listOf(
                AuthorResponse(1L, "Author One", LocalDate.of(1990, 1, 1)),
                AuthorResponse(2L, "Author Two", LocalDate.of(1985, 2, 2)),
            )
        val request =
            """
            {
                "title": "Updated Book Title",
                "price": 2000,
                "authorIds": [1, 2],
                "publicationStatus": "PUBLISHED"
            }
            """.trimIndent()

        val response = BookResponse(10L, "Updated Book Title", 2000, "PUBLISHED", authors)
        every { bookService.update(10L, any()) } returns response

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/books/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(10L))
            .andExpect(jsonPath("$.title").value("Updated Book Title"))
            .andExpect(jsonPath("$.authors").isArray)
            .andExpect(jsonPath("$.authors[0].id").value(1L))
            .andExpect(jsonPath("$.authors[1].id").value(2L))
    }

    @Test
    fun `should reject create book with non-existent authorIds`() {
        val request =
            """
            {
                "title": "Book with invalid author",
                "price": 1000,
                "authorIds": [999],
                "publicationStatus": "UNPUBLISHED"
            }
            """.trimIndent()

        every {
            bookService.create(any())
        } throws IllegalArgumentException("Invalid author ID(s) provided: Author(s) with ID(s) [999] not found")

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 404 when book not found`() {
        every { bookService.findById(99L) } throws EntityNotFoundException("Book not found with id: 99")

        mockMvc.perform(get("/books/99"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Book not found with id: 99"))
    }
}
