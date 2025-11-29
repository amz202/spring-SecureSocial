package com.example.securesocial.data.repositories

import com.example.securesocial.data.model.Post
import com.example.securesocial.data.model.PostTag
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface PostRepository: MongoRepository<Post, ObjectId> {
    fun findByAuthorId(authorId: ObjectId): List<Post>?
    fun findByTag(tag: PostTag): List<Post>?
}