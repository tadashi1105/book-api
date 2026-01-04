package com.example.bookapi

import java.time.LocalDate
import com.example.bookapi.jooq.tables.records.AuthorsRecord
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AuthorServiceTest {
    private val repository = mockk<AuthorRepository>()
    private val service = AuthorService(repository)

    @Test
    fun `should create author`() {
        val request = CreateAuthorRequest("New Author", LocalDate.of(1980, 5, 10))
        val record =
            AuthorsRecord().apply {
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

    @Test
    fun `should find multiple authors by ids`() {
        val record1 =
            AuthorsRecord().apply {
                id = 1L
                name = "Author One"
                birthDate = LocalDate.of(1980, 1, 1)
            }
        val record2 =
            AuthorsRecord().apply {
                id = 2L
                name = "Author Two"
                birthDate = LocalDate.of(1985, 2, 2)
            }

        every { repository.findByIds(listOf(1L, 2L)) } returns listOf(record1, record2)

        val result = service.findByIds(listOf(1L, 2L))

        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(2L, result[1].id)
        verify { repository.findByIds(listOf(1L, 2L)) }
    }

    @Test
    fun `should throw exception when author id does not exist`() {
        every { repository.findByIds(listOf(999L)) } returns emptyList()

        val exception =
            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException::class.java) {
                service.findByIds(listOf(999L))
            }

        org.junit.jupiter.api.Assertions.assertTrue(exception.message!!.contains("999"))
        verify { repository.findByIds(listOf(999L)) }
    }

    @Test
    fun `should throw exception when some author ids do not exist`() {
        val record1 =
            AuthorsRecord().apply {
                id = 1L
                name = "Author One"
                birthDate = LocalDate.of(1980, 1, 1)
            }

        every { repository.findByIds(listOf(1L, 999L)) } returns listOf(record1)

        val exception =
            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException::class.java) {
                service.findByIds(listOf(1L, 999L))
            }

        org.junit.jupiter.api.Assertions.assertTrue(exception.message!!.contains("999"))
        verify { repository.findByIds(listOf(1L, 999L)) }
    }
}
