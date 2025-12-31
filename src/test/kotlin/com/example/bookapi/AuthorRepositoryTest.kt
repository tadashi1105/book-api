package com.example.bookapi

import com.example.bookapi.jooq.tables.records.AuthorsRecord
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.context.annotation.Import
import java.time.LocalDate

@JooqTest
@Import(AuthorRepository::class)
class AuthorRepositoryTest {

    @Autowired
    private lateinit var repository: AuthorRepository

    @Test
    fun `should insert and find author`() {
        val record = AuthorsRecord().apply {
            name = "Test Author"
            birthDate = LocalDate.of(1990, 1, 1)
        }
        val inserted = repository.insert(record)
        assertNotNull(inserted.id)

        val found = repository.findById(inserted.id!!)
        assertNotNull(found)
        assertEquals("Test Author", found?.name)
    }
}
