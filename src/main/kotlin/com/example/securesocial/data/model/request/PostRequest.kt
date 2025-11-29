package com.example.securesocial.data.model.request

import jakarta.validation.constraints.NotBlank

data class PostRequest(
    @field:NotBlank(message = "Title cannot be empty")
    val title: String,
    val content: String,
    val id: String?,
    val tag: String,
)