package com.example.bookapi

import com.example.bookapi.jooq.tables.records.BooksRecord
import com.example.bookapi.jooq.tables.references.BOOK_AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class BookService(
    private val bookRepository: BookRepository,
    private val authorService: AuthorService,
    private val dsl: DSLContext
) {
    fun create(request: CreateBookRequest): BookResponse {
        val author = authorService.findById(request.authorId)
        val record = BooksRecord().apply {
            title = request.title
            price = request.price
            publicationStatus = request.publicationStatus
        }
        val saved = bookRepository.insert(record)
        
        dsl.insertInto(BOOK_AUTHORS)
            .set(BOOK_AUTHORS.BOOK_ID, saved.id)
            .set(BOOK_AUTHORS.AUTHOR_ID, request.authorId)
            .execute()

        return saved.toResponse(author)
    }

    fun findById(id: Long): BookResponse {
        val record = bookRepository.findById(id) ?: throw EntityNotFoundException("Book not found with id: $id")
        val author = fetchFirstAuthor(id)
        return record.toResponse(author)
    }

    fun findByAuthorId(authorId: Long): List<BookResponse> {
        val author = authorService.findById(authorId)
        return bookRepository.findByAuthorId(authorId).map { it.toResponse(author) }
    }

    fun update(id: Long, request: UpdateBookRequest): BookResponse {
        val existing = bookRepository.findById(id) ?: throw EntityNotFoundException("Book not found with id: $id")

        if (existing.publicationStatus == "PUBLISHED" && request.publicationStatus == "UNPUBLISHED") {
            throw IllegalArgumentException("Status change from PUBLISHED to UNPUBLISHED is not allowed.")
        }

        existing.apply {
            title = request.title
            price = request.price
            publicationStatus = request.publicationStatus
        }
        val updated = bookRepository.update(existing)
        val author = fetchFirstAuthor(id)
        return updated.toResponse(author)
    }

    private fun fetchFirstAuthor(bookId: Long): AuthorResponse {
        val authorId = dsl.select(BOOK_AUTHORS.AUTHOR_ID)
            .from(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .fetchOneInto(Long::class.java) ?: throw RuntimeException("No author found for book: $bookId")
        return authorService.findById(authorId)
    }

    private fun BooksRecord.toResponse(author: AuthorResponse) = BookResponse(
        id = this.id!!,
        title = this.title!!,
        price = this.price!!,
        publicationStatus = this.publicationStatus!!,
        author = author
    )
}
