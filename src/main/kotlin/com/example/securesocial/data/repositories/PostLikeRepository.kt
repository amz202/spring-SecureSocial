package com.example.securesocial.data.repositories

import com.example.securesocial.data.model.PostLike
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface PostLikeRepository: MongoRepository<PostLike, ObjectId> {
    fun countByPostId(postId: ObjectId): Long
    fun existsByPostIdAndUserId(postId: ObjectId, userId: ObjectId): Boolean //if a user has already liked the post
    fun findByPostIdAndUserId(id: ObjectId, userId: ObjectId): PostLike? //for unlike
    fun findByPostId(postId: ObjectId): List<PostLike>
}