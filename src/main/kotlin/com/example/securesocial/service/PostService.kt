package com.example.securesocial.service

import com.example.securesocial.data.model.LogType
import com.example.securesocial.data.model.Post
import com.example.securesocial.data.model.PostTag
import com.example.securesocial.data.model.request.PostRequest
import com.example.securesocial.data.model.response.PostResponse
import com.example.securesocial.data.repositories.PostRepository
import com.example.securesocial.data.repositories.UserRepository
import com.example.securesocial.security.JwtService
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class PostService(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val postInteractionService: PostInteractionService,
    private val jwtService: JwtService,
    private val activityLogService: ActivityLogService
) {
    fun createPost(request: PostRequest, userId: String): PostResponse{
        val selectedTag = PostTag.valueOf(request.tag.uppercase())
        val post = Post(
            authorId = ObjectId(userId),
            title = request.title,
            content = request.content,
            tag = selectedTag
        )
        val savedPost = postRepository.save(post)
        activityLogService.log(userId, LogType.POST, savedPost.id.toHexString())

        val authorName = userRepository.findById(ObjectId(userId)).orElse(null)?.username ?: "Unknown"

        val response = PostResponse(
            id = savedPost.id.toHexString(),
            authorName = authorName,
            title = savedPost.title,
            content = savedPost.content,
            tag = savedPost.tag.toString(),
            createdAt = savedPost.createdAt,
            likeCount = 0,
            viewCount = 0
        )

        return response
    }

    fun getAllPosts(): List<PostResponse>{
        val posts = postRepository.findAll()
        val response = posts.map { post ->
            PostResponse(
                id = post.id.toHexString(),
                title = post.title,
                content = post.content,
                createdAt = post.createdAt,
                authorName = userRepository.findById(post.authorId).orElse(null)?.username ?: "Unknown",
                tag = post.tag.toString(),
                likeCount = postInteractionService.getLikeCount(post.id.toHexString()),
                viewCount = postInteractionService.getViewCount(post.id.toHexString())
            )
        }
        return response
    }
}