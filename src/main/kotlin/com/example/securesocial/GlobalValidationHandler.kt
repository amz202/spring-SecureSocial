package com.example.securesocial

import com.example.securesocial.data.model.PostTag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.DisabledException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import kotlin.collections.map
import kotlin.to

@RestControllerAdvice
class GlobalValidationHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = e.bindingResult.allErrors.map {
            it.defaultMessage ?: "Invalid value"
        }
        return ResponseEntity
            .status(400)
            .body(mapOf("errors" to errors))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleEnumError(e: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(400)
            .body(mapOf("error" to "Invalid input or Tag. Allowed tags: ${PostTag.entries.joinToString()}"))
    }

    @ExceptionHandler(DisabledException::class)
    fun handleDisabledException(e: DisabledException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(mapOf("error" to (e.message ?: "Account disabled")))
    }
}