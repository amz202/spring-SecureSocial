package com.example.securesocial.service

import com.example.securesocial.data.model.Post
import com.example.securesocial.data.model.PostLike
import com.example.securesocial.data.model.PostView
import com.example.securesocial.data.repositories.PostLikeRepository
import com.example.securesocial.data.repositories.PostViewRepository
import com.example.securesocial.security.CryptoService
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class PostInteractionService(
    private val postLikeRepository: PostLikeRepository,
    private val postViewRepository: PostViewRepository,
    private val cryptoService: CryptoService
){
    fun likePost(userId: ObjectId, postId: ObjectId): PostLike{

        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw IllegalArgumentException("User has already liked this post")
        }

        val digitalSignature = cryptoService.signLike(
            userId.toString(),
            postId.toString(),
            System.currentTimeMillis()
        )

        val like = PostLike(
            postId = postId,
            userId = userId,
            signature = digitalSignature
        )

        return postLikeRepository.save(like)
    }

    fun viewPost(userId: ObjectId, postId: ObjectId){
        val hashedToken = cryptoService.generateAnonymousViewToken(userId.toString(), postId.toString())

        if(!postViewRepository.existsByPostIdAndHashedViewToken(postId, hashedToken)){
            val view = PostView(
                postId = postId, hashedViewToken = hashedToken
            )
            postViewRepository.save(view)
        }
    }

    fun getLikeCount(postId: ObjectId): Long {
        return postLikeRepository.countByPostId(postId)
    }

    fun getViewCount(postId: ObjectId): Long {
        return postViewRepository.countByPostId(postId)
    }

    fun verifyLikeIntegrity(likeId: String): Boolean {
        val like = postLikeRepository.findById(ObjectId(likeId)).orElse(null) ?: return false

        // Check if the signature in the DB matches the math
        return cryptoService.verifyLikeSignature(
            like.userId.toHexString(),
            like.postId.toHexString(),
            like.likedAt,
            like.signature
        )
    }
}