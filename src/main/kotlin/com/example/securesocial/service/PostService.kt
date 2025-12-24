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

    fun getPost(postId:String, userId: String): PostResponse{
        //Anonymously count the view
        postInteractionService.viewPost(userId, postId)

        val post = postRepository.findById(ObjectId(postId)).orElseThrow()
        val authorName = userRepository.findById(ObjectId(post.authorId.toHexString())).orElse(null)?.username ?: "Unknown"

        val response = PostResponse(
            id = post.id.toHexString(),
            title = post.title,
            content = post.content,
            createdAt = post.createdAt,
            authorName = authorName,
            tag = post.tag.toString(),
            likeCount = postInteractionService.getLikeCount(postId),
            viewCount = postInteractionService.getViewCount(postId)
        )
        return response
    }

    fun getPostsByTag(tagName: String): List<PostResponse>?{
        val tag = PostTag.valueOf(tagName.uppercase())
        val posts = postRepository.findByTag(tag)

        val response = posts?.map { post ->
            PostResponse(
                id = post.id.toHexString(),
                title = post.title,
                content = post.content,
                tag = post.tag.name,
                createdAt = post.createdAt,
                authorName = userRepository.findById(post.authorId).orElse(null)?.username ?: "Unknown",
                likeCount = postInteractionService.getLikeCount(post.id.toHexString()),
                viewCount = postInteractionService.getViewCount(post.id.toHexString())
            )
        }
        return response
    }

    fun getMyPosts(userId: String):List<PostResponse>?{
        val posts = postRepository.findByAuthorId(ObjectId(userId))

        val response = posts?.map { post ->
            PostResponse(
                id = post.id.toHexString(),
                title = post.title,
                content = post.content,
                tag = post.tag.name,
                createdAt = post.createdAt,
                authorName = userRepository.findById(post.authorId).orElse(null)?.username ?: "Unknown",
                likeCount = postInteractionService.getLikeCount(post.id.toHexString()),
                viewCount = postInteractionService.getViewCount(post.id.toHexString())
            )
        }
        return response
    }
}