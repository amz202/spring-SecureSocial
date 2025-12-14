package com.example.securesocial.service

import com.example.securesocial.data.model.LogType
import com.example.securesocial.data.model.Post
import com.example.securesocial.data.model.PostLike
import com.example.securesocial.data.model.PostView
import com.example.securesocial.data.model.response.PostLikesResponse
import com.example.securesocial.data.repositories.PostLikeRepository
import com.example.securesocial.data.repositories.PostViewRepository
import com.example.securesocial.data.repositories.UserRepository
import com.example.securesocial.security.CryptoService
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class PostInteractionService(
    private val postLikeRepository: PostLikeRepository,
    private val postViewRepository: PostViewRepository,
    private val cryptoService: CryptoService,
    private val activityLogService: ActivityLogService,
    private val userRepository: UserRepository
){
    fun likePost(userId: String, postId: String): PostLike {

        val userObjectId = ObjectId(userId)
        val postObjectId = ObjectId(postId)

        if (postLikeRepository.existsByPostIdAndUserId(postObjectId, userObjectId)) {
            throw IllegalArgumentException("User has already liked this post")
        }

        val digitalSignature = cryptoService.signLike(userId, postId, System.currentTimeMillis())
        val like = PostLike(
            postId = postObjectId,
            userId = userObjectId,
            signature = digitalSignature,
            likedAt = System.currentTimeMillis()
        )
        val savedLike = postLikeRepository.save(like)

        activityLogService.log(userId, LogType.LIKE, postId)
        return savedLike
    }

    fun viewPost(userId: String, postId: String) {
        val postObjectId = ObjectId(postId)
        val hashedToken = cryptoService.generateAnonymousViewToken(userId, postId)

        if (!postViewRepository.existsByPostIdAndHashedViewToken(postObjectId, hashedToken)) {
            val view = PostView(
                postId = postObjectId,
                hashedViewToken = hashedToken
            )
            postViewRepository.save(view)
        }
    }

    fun getLikeCount(postId: String): Long {
        return postLikeRepository.countByPostId(ObjectId(postId))
    }

    fun getViewCount(postId: String): Long {
        return postViewRepository.countByPostId(ObjectId(postId))
    }

    fun getPostLikes(postId: String): List<PostLikesResponse> {
        return postLikeRepository.findByPostId(ObjectId(postId)).map { like ->
            PostLikesResponse(
                username = userRepository.findById(like.userId).orElse(null)?.username ?: "Unknown",
                likedAt = like.likedAt,
                postId = like.postId.toHexString()
            )
        }
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