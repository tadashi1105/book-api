package com.example.bookapi

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
import java.time.LocalDate

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
        val request = """
            {
                "name": "New Author",
                "birthDate": "1990-01-01"
            }
        """.trimIndent()

        val response = AuthorResponse(1L, "New Author", LocalDate.of(1990, 1, 1))
        every { authorService.create(any()) } returns response

        mockMvc.perform(post("/authors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
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
        val author = AuthorResponse(1L, "Author One", LocalDate.of(1980, 5, 20))
        val books = listOf(BookResponse(10L, "Book One", 1000, "PUBLISHED", author))
        every { bookService.findByAuthorId(1L) } returns books

        mockMvc.perform(get("/authors/1/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(10L))
            .andExpect(jsonPath("$[0].title").value("Book One"))
    }
}
