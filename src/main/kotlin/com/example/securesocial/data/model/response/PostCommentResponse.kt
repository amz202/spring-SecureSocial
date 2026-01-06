package com.example.securesocial.data.model.response

data class PostCommentResponse(
    val comment: String,
    val createdAt: Long,
    val id: String,
    val username: String,
    val postId: String
)
