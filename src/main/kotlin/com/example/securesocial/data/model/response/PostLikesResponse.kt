package com.example.securesocial.data.model.response

data class PostLikesResponse(
    val username: String,
    val postId: String,
    val likedAt: Long
)