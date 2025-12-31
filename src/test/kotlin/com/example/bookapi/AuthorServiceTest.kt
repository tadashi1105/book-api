package com.example.bookapi

import com.example.bookapi.jooq.tables.records.AuthorsRecord
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AuthorServiceTest {
    private val repository = mockk<AuthorRepository>()
    private val service = AuthorService(repository)

    @Test
    fun `should create author`() {
        val request = CreateAuthorRequest("New Author", LocalDate.of(1980, 5, 10))
        val record = AuthorsRecord().apply {
            id = 1L
            name = request.name
            birthDate = request.birthDate
        }

        every { repository.insert(any()) } returns record

        val result = service.create(request)

        assertEquals(1L, result.id)
        assertEquals("New Author", result.name)
        verify { repository.insert(any()) }
    }
}
