package com.example.bookapi

import com.example.bookapi.jooq.tables.records.BooksRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.context.annotation.Import

@JooqTest
@Import(BookRepository::class)
class BookRepositoryTest {
    @Autowired
    private lateinit var repository: BookRepository

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
}
