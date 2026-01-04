package com.example.bookapi

import com.example.bookapi.jooq.tables.records.BooksRecord
import com.example.bookapi.jooq.tables.references.BOOKS
import com.example.bookapi.jooq.tables.references.BOOK_AUTHORS
import org.jooq.DSLContext
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository

@Repository
class BookRepository(private val dsl: DSLContext) {
    fun insert(book: BooksRecord): BooksRecord {
        return dsl.insertInto(BOOKS)
            .set(book)
            .returning()
            .fetchOne() ?: throw DataIntegrityViolationException("Failed to insert book")
    }

    fun findById(id: Long): BooksRecord? {
        return dsl.selectFrom(BOOKS)
            .where(BOOKS.ID.eq(id))
            .fetchOne()
    }

    fun findByAuthorId(authorId: Long): List<BooksRecord> {
        return dsl.select(BOOKS.asterisk())
            .from(BOOKS)
            .join(BOOK_AUTHORS).on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
            .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
            // メモ: ここでは書籍データのみを取得する。著者関連情報のEager Loadは行っていない。
            .fetchInto(BooksRecord::class.java)
    }

    fun update(book: BooksRecord): BooksRecord {
        return dsl.update(BOOKS)
            .set(book)
            .where(BOOKS.ID.eq(book.id))
            .returning()
            .fetchOne() ?: throw EntityNotFoundException("Book not found with id: ${book.id}")
    }

    fun findAuthorIdsByBookId(bookId: Long): List<Long> {
        return dsl.select(BOOK_AUTHORS.AUTHOR_ID)
            .from(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .fetchInto(Long::class.java)
    }

    fun deleteAuthorAssociations(bookId: Long) {
        dsl.deleteFrom(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .execute()
    }

    fun insertAuthorAssociations(
        bookId: Long,
        authorIds: List<Long>,
    ) {
        if (authorIds.isEmpty()) {
            return
        }
        // パフォーマンス: 関連付け作成の効率化のためバッチインサートを使用
        val insert = dsl.insertInto(BOOK_AUTHORS, BOOK_AUTHORS.BOOK_ID, BOOK_AUTHORS.AUTHOR_ID)
        authorIds.forEach { authorId ->
            insert.values(bookId, authorId)
        }
        insert.execute()
    }
}
