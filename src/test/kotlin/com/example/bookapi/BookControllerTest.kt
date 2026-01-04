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

@WebMvcTest(BookController::class)
class BookControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var bookService: BookService

    @Test
    fun `should create book`() {
        val author = AuthorResponse(1L, "Author", LocalDate.of(1990, 1, 1))
        val request =
            """
            {
                "title": "New Book",
                "price": 1500,
                "authorId": 1,
                "publicationStatus": "UNPUBLISHED"
            }
            """.trimIndent()

        val response = BookResponse(10L, "New Book", 1500, "UNPUBLISHED", author)
        every { bookService.create(any()) } returns response

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(10L))
            .andExpect(jsonPath("$.title").value("New Book"))
    }

    @Test
    fun `should get book by id`() {
        val author = AuthorResponse(1L, "Author", LocalDate.of(1990, 1, 1))
        val response = BookResponse(10L, "Sample Book", 1000, "PUBLISHED", author)
        every { bookService.findById(10L) } returns response

        mockMvc.perform(get("/books/10"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(10L))
            .andExpect(jsonPath("$.title").value("Sample Book"))
    }
}
