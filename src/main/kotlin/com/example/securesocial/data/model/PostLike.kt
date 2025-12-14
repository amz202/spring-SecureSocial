package com.example.securesocial.data.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

data class PostLike(
    @Id val id: ObjectId = ObjectId(),
    val postId: ObjectId,
    val userId: ObjectId,
    val signature: String,
    val likedAt: Long
)