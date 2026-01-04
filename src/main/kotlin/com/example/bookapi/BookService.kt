package com.example.bookapi

import com.example.bookapi.jooq.tables.records.BooksRecord
import com.example.bookapi.jooq.tables.references.AUTHORS
import com.example.bookapi.jooq.tables.references.BOOKS
import com.example.bookapi.jooq.tables.references.BOOK_AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class BookService(
    private val bookRepository: BookRepository,
    private val authorService: AuthorService,
    private val dsl: DSLContext,
) {
    fun create(request: CreateBookRequest): BookResponse {
        // データ不整合を防ぐため、DB変更前にID存在チェックを行いFail Fastさせる (Req 2.4)
        val authors = authorService.findByIds(request.authorIds!!)
        val record =
            BooksRecord().apply {
                title = request.title
                price = request.price
                publicationStatus = request.publicationStatus
            }
        val saved = bookRepository.insert(record)

        bookRepository.insertAuthorAssociations(saved.id!!, request.authorIds)

        // REST規約を満たすため、完全な著者オブジェクトを含むレスポンスを返す
        return saved.toResponse(authors)
    }

    fun findById(id: Long): BookResponse {
        // N+1問題回避のため、JOINで書籍と著者リストを一括取得する (Req 2.2)
        // 注意: 著者数分の行が返るためマッピング処理で集約している
        val result =
            dsl.select(BOOKS.asterisk(), AUTHORS.ID, AUTHORS.NAME, AUTHORS.BIRTH_DATE)
                .from(BOOKS)
                .leftJoin(BOOK_AUTHORS).on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
                .leftJoin(AUTHORS).on(BOOK_AUTHORS.AUTHOR_ID.eq(AUTHORS.ID))
                .where(BOOKS.ID.eq(id))
                .fetch()

        if (result.isEmpty()) {
            throw EntityNotFoundException("Book not found with id: $id")
        }

        val bookRecord = result[0].into(BOOKS)
        val authors =
            result.mapNotNull { r ->
                if (r.get(AUTHORS.ID) != null) {
                    AuthorResponse(
                        id = r.get(AUTHORS.ID)!!,
                        name = r.get(AUTHORS.NAME)!!,
                        birthDate = r.get(AUTHORS.BIRTH_DATE)!!,
                    )
                } else {
                    null
                }
            }

        return bookRecord.toResponse(authors)
    }

    fun findByAuthorId(authorId: Long): List<BookResponse> {
        // N+1回避とクエリ複雑化防止のため、2段階フェッチを行う
        // 1. 著者の書籍一覧を取得
        // 2. それら書籍に関連する全著者（共著者含む）を一括取得
        // 警告: 書籍数が膨大な場合、メモリ圧迫のリスクがあるため将来的にページネーション検討が必要

        val books = bookRepository.findByAuthorId(authorId)
        if (books.isEmpty()) return emptyList()

        val bookIds = books.map { it.id!! }

        val authorsResult =
            dsl.select(BOOK_AUTHORS.BOOK_ID, AUTHORS.ID, AUTHORS.NAME, AUTHORS.BIRTH_DATE)
                .from(BOOK_AUTHORS)
                .join(AUTHORS).on(BOOK_AUTHORS.AUTHOR_ID.eq(AUTHORS.ID))
                .where(BOOK_AUTHORS.BOOK_ID.`in`(bookIds))
                .fetch()

        val authorsMap =
            authorsResult.groupBy(
                { it.get(BOOK_AUTHORS.BOOK_ID) },
                { AuthorResponse(it.get(AUTHORS.ID)!!, it.get(AUTHORS.NAME)!!, it.get(AUTHORS.BIRTH_DATE)!!) },
            )

        return books.map { book ->
            book.toResponse(authorsMap[book.id] ?: emptyList())
        }
    }

    fun update(
        id: Long,
        request: UpdateBookRequest,
    ): BookResponse {
        val existing = bookRepository.findById(id) ?: throw EntityNotFoundException("Book not found with id: $id")

        // データ不整合を防ぐため、DB変更前にID存在チェックを行いFail Fastさせる (Req 2.4)
        val authors = authorService.findByIds(request.authorIds!!)

        // 仕様: 一度PUBLISHEDになった書籍はUNPUBLISHEDに戻せない (Req 2.6)
        if (existing.publicationStatus == "PUBLISHED" && request.publicationStatus == "UNPUBLISHED") {
            throw IllegalArgumentException("Status change from PUBLISHED to UNPUBLISHED is not allowed.")
        }

        existing.apply {
            title = request.title
            price = request.price
            publicationStatus = request.publicationStatus
        }
        val updated = bookRepository.update(existing)

        // 関連の洗い替え（削除→挿入）
        // トランザクションによりアトミック性が保証される
        bookRepository.deleteAuthorAssociations(id)
        bookRepository.insertAuthorAssociations(id, request.authorIds)

        return updated.toResponse(authors)
    }

    private fun fetchFirstAuthor(bookId: Long): AuthorResponse {
        val authorId =
            dsl.select(BOOK_AUTHORS.AUTHOR_ID)
                .from(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
                .fetchOneInto(Long::class.java) ?: throw EntityNotFoundException("No author found for book: $bookId")
        return authorService.findById(authorId)
    }

    private fun fetchAuthors(bookId: Long): List<AuthorResponse> {
        val authorIds = bookRepository.findAuthorIdsByBookId(bookId)
        return authorService.findByIds(authorIds)
    }

    private fun BooksRecord.toResponse(authors: List<AuthorResponse>) =
        BookResponse(
            id = this.id!!,
            title = this.title!!,
            price = this.price!!,
            publicationStatus = this.publicationStatus!!,
            authors = authors,
        )
}
