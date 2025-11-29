package com.example.securesocial.controllers

import com.example.securesocial.data.model.Post
import com.example.securesocial.data.model.request.PostRequest
import com.example.securesocial.data.model.response.PostResponse
import com.example.securesocial.data.repositories.PostRepository
import com.example.securesocial.security.JwtService
import com.example.securesocial.service.PostInteractionService
import org.bson.types.ObjectId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postRepository: PostRepository,
    private val postInteractionService: PostInteractionService,
    private val jwtService: JwtService
) {

    // Create a Post
    @PostMapping
    fun createPost(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: PostRequest
    ): ResponseEntity<Post> {
        val userId = jwtService.getUserIdFromToken(token)

        val post = Post(
            authorId = ObjectId(userId),
            title = request.title,
            content = request.content
        )
        return ResponseEntity.ok(postRepository.save(post))
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
                authorId = post.authorId.toHexString(),
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

        return ResponseEntity.ok(
            PostResponse(
                id = post.id.toHexString(),
                title = post.title,
                content = post.content,
                createdAt = post.createdAt,
                authorId = post.authorId.toHexString(),
                likeCount = postInteractionService.getLikeCount(postId),
                viewCount = postInteractionService.getViewCount(postId)
            )
        )
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
}