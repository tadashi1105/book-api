package com.example.bookapi

import java.time.LocalDateTime
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(ex: EntityNotFoundException): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                message = ex.message ?: "Not Found",
                timestamp = LocalDateTime.now(),
            )
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        // 戦略: IllegalArgumentExceptionを400 Bad Requestにマッピングする
        // ビジネスルール違反（例：無効なステータス変更）や独自のバリデーション失敗に使用
        val error =
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                message = ex.message ?: "Bad Request",
                timestamp = LocalDateTime.now(),
            )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val details = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }.joinToString(", ")
        val error =
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                message = "Validation failed: $details",
                timestamp = LocalDateTime.now(),
            )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericError(ex: Exception): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message = "An unexpected error occurred",
                timestamp = LocalDateTime.now(),
            )
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

data class ErrorResponse(
    val status: Int,
    val message: String,
    val timestamp: LocalDateTime,
)
