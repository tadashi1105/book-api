package com.example.bookapi

import java.time.LocalDate
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AuthorController::class)
class AuthorControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var authorService: AuthorService

    @MockkBean
    private lateinit var bookService: BookService

    @Test
    fun `should create author`() {
        val request =
            """
            {
                "name": "New Author",
                "birthDate": "1990-01-01"
            }
            """.trimIndent()

        val response = AuthorResponse(1L, "New Author", LocalDate.of(1990, 1, 1))
        every { authorService.create(any()) } returns response

        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("New Author"))
    }

    @Test
    fun `should get author by id`() {
        val response = AuthorResponse(1L, "Author One", LocalDate.of(1980, 5, 20))
        every { authorService.findById(1L) } returns response

        mockMvc.perform(get("/authors/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("Author One"))
    }

    @Test
    fun `should get books by author id`() {
        val author1 = AuthorResponse(1L, "Author One", LocalDate.of(1980, 5, 20))
        val author2 = AuthorResponse(2L, "Author Two", LocalDate.of(1985, 6, 15))
        val authorsForBook = listOf(author1, author2)

        val book1 = BookResponse(10L, "Book One", 1000, "PUBLISHED", authorsForBook)
        val book2 = BookResponse(11L, "Book Two", 1200, "UNPUBLISHED", listOf(author1)) // バリエーションとして単一著者の書籍を含める

        every { bookService.findByAuthorId(1L) } returns listOf(book1, book2)

        mockMvc.perform(get("/authors/1/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(10L))
            .andExpect(jsonPath("$[0].title").value("Book One"))
            .andExpect(jsonPath("$[0].authors").isArray)
            .andExpect(jsonPath("$[0].authors.length()").value(2))
            .andExpect(jsonPath("$[0].authors[0].id").value(1L))
            .andExpect(jsonPath("$[0].authors[0].name").value("Author One"))
            .andExpect(jsonPath("$[0].authors[1].id").value(2L))
            .andExpect(jsonPath("$[0].authors[1].name").value("Author Two"))
            .andExpect(jsonPath("$[1].id").value(11L))
            .andExpect(jsonPath("$[1].title").value("Book Two"))
            .andExpect(jsonPath("$[1].authors").isArray)
            .andExpect(jsonPath("$[1].authors.length()").value(1))
            .andExpect(jsonPath("$[1].authors[0].id").value(1L))
            .andExpect(jsonPath("$[1].authors[0].name").value("Author One"))
    }

    @Test
    fun `should get all authors`() {
        val author1 = AuthorResponse(1L, "Author One", LocalDate.of(1980, 5, 20))
        val author2 = AuthorResponse(2L, "Author Two", LocalDate.of(1985, 6, 15))
        every { authorService.findAll() } returns listOf(author1, author2)

        mockMvc.perform(get("/authors"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].name").value("Author One"))
            .andExpect(jsonPath("$[1].id").value(2L))
            .andExpect(jsonPath("$[1].name").value("Author Two"))
    }

    @Test
    fun `should update author`() {
        val request =
            """
            {
                "name": "Updated Author",
                "birthDate": "1980-01-01"
            }
            """.trimIndent()

        val response = AuthorResponse(1L, "Updated Author", LocalDate.of(1980, 1, 1))
        every { authorService.update(1L, any()) } returns response

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/authors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("Updated Author"))
    }

    @Test
    fun `should return 400 when creating author with invalid data`() {
        val request =
            """
            {
                "name": "",
                "birthDate": "2999-01-01"
            }
            """.trimIndent()

        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request),
        )
            .andExpect(status().isBadRequest)
    }
}
