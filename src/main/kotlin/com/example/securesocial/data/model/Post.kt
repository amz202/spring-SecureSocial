package com.example.securesocial.data.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("posts")
data class Post(
    @Id val id: ObjectId = ObjectId.get(),
    val authorId: ObjectId,
    val title: String,
    val content: String,
    val tag: PostTag,
    val createdAt: Long = System.currentTimeMillis()
)

//might include comments in future
