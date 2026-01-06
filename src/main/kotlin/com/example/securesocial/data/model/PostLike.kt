package com.example.securesocial.data.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed

data class PostLike(
    @Id val id: ObjectId = ObjectId(),
    @Indexed val postId: ObjectId,
    val userId: ObjectId,
    val signature: String,
    val likedAt: Long
)