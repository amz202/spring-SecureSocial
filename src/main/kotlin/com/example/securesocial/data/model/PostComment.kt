package com.example.securesocial.data.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

data class PostComment(
    @Id val id: ObjectId = ObjectId(),
    val postId: ObjectId,
    val comment: String,
    val userId: ObjectId,
    val createdAt: Long,
    val signature: String
)
