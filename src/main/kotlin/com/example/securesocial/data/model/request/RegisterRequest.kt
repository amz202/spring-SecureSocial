package com.example.securesocial.data.model.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern

data class RegisterRequest(
    @field:Pattern(
        regexp = "^[a-z0-9_]{5,}$",
        message = "Username must be at least 5 characters long and use only lowercase letters, digits or underscores."
    )
    val username: String,

    @field:Email(message = "Invalid email address")
    val email: String,

    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{9,}\$",
        message = "Password must be at least 9 characters long and contain at least one digit, uppercase and lowercase character."
    )
    val password: String
)
