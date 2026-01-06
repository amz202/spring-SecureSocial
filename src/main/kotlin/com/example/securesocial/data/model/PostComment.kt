package com.example.securesocial.data.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed

data class PostComment(
    @Id val id: ObjectId = ObjectId(),
    @Indexed val postId: ObjectId,
    val comment: String,
    val userId: ObjectId,
    val createdAt: Long
)
