package com.example.securesocial.data.model.response

data class PostResponse(
    val id: String,
    val title: String,
    val content: String,
    val tag: String,
    val createdAt: Long,
    val authorId: String,
    val likeCount: Long,
    val viewCount: Long
)
