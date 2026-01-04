package com.example.bookapi

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@WebMvcTest(controllers = [TestController::class])
@Import(GlobalExceptionHandler::class)
class GlobalExceptionHandlerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should return 404 when EntityNotFoundException is thrown`() {
        mockMvc.perform(get("/test/not-found"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return 400 when IllegalArgumentException is thrown`() {
        mockMvc.perform(get("/test/bad-request"))
            .andExpect(status().isBadRequest)
    }
}

@RestController
class TestController {
    @GetMapping("/test/not-found")
    fun throwNotFound() {
        throw EntityNotFoundException("Entity not found")
    }

    @GetMapping("/test/bad-request")
    fun throwBadRequest() {
        throw IllegalArgumentException("Bad request")
    }
}
