package com.example.securesocial.controllers

import com.example.securesocial.data.model.LogType
import com.example.securesocial.data.model.Post
import com.example.securesocial.data.model.PostLike
import com.example.securesocial.data.model.PostTag
import com.example.securesocial.data.model.request.PostRequest
import com.example.securesocial.data.model.response.PostLikesResponse
import com.example.securesocial.data.model.response.PostResponse
import com.example.securesocial.data.repositories.PostRepository
import com.example.securesocial.data.repositories.UserRepository
import com.example.securesocial.security.JwtService
import com.example.securesocial.service.ActivityLogService
import com.example.securesocial.service.PostInteractionService
import org.bson.types.ObjectId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.text.toHexString
import kotlin.toString

@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val postInteractionService: PostInteractionService,
    private val jwtService: JwtService,
    private val activityLogService: ActivityLogService
) {

    // Create a Post
    @PostMapping
    fun createPost(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: PostRequest
    ): ResponseEntity<PostResponse> {
        val userId = jwtService.getUserIdFromToken(token)
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

        return ResponseEntity.ok(response)
    }

    // Get All Posts (with counts)
    @GetMapping
    fun getAllPosts(@RequestHeader("Authorization") token: String): ResponseEntity<List<PostResponse>> {
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
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{postId}")
    fun getPost(
        @RequestHeader("Authorization") token: String,
        @PathVariable postId: String
    ): ResponseEntity<PostResponse> {
        val userId = jwtService.getUserIdFromToken(token)

        //Anonymously count the view
        postInteractionService.viewPost(userId, postId)

        val post = postRepository.findById(ObjectId(postId)).orElseThrow()
        val authorName = userRepository.findById(ObjectId(post.authorId.toHexString())).orElse(null)?.username ?: "Unknown"

        return ResponseEntity.ok(
            PostResponse(
                id = post.id.toHexString(),
                title = post.title,
                content = post.content,
                createdAt = post.createdAt,
                authorName = authorName,
                tag = post.tag.toString(),
                likeCount = postInteractionService.getLikeCount(postId),
                viewCount = postInteractionService.getViewCount(postId)
            )
        )
    }

    @GetMapping("/tag/{tagName}")
    fun getPostsByTag(
        @RequestHeader("Authorization") token: String,
        @PathVariable tagName: String
    ): ResponseEntity<List<PostResponse>> {

        val tag = PostTag.valueOf(tagName.uppercase())
        val userId = jwtService.getUserIdFromToken(token)
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

        return ResponseEntity.ok(response)
    }

    @PostMapping("/{postId}/like")
    fun likePost(
        @RequestHeader("Authorization") token: String,
        @PathVariable postId: String
    ): ResponseEntity<Any> {
        val userId = jwtService.getUserIdFromToken(token)
        val like = postInteractionService.likePost(userId, postId)
        return ResponseEntity.ok(like)
    }

    @GetMapping("/myPosts")
    fun getMyPosts(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<List<PostResponse>> {
        val userId = jwtService.getUserIdFromToken(token)
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

        return ResponseEntity.ok(response)
    }

    @GetMapping("/{postId}/likes")
    fun getPostLikes(
        @PathVariable postId: String
    ): ResponseEntity<List<PostLikesResponse>> {
        return ResponseEntity.ok(postInteractionService.getPostLikes(postId))
    }
}