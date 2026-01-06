package com.example.securesocial.data.repositories

import com.example.securesocial.data.model.PostComment
import com.example.securesocial.data.model.PostLike
import com.example.securesocial.data.model.response.PostCommentResponse
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface PostCommentRepository: MongoRepository<PostComment, ObjectId> {
    fun findByPostId(postId: ObjectId): List<PostComment>
    fun countByPostId(postId: ObjectId): Long
    fun findByIdAndUserId(postId: ObjectId, userId: ObjectId): PostComment? //for deleting comment
}