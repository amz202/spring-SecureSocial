package com.example.securesocial.controllers

import com.example.securesocial.data.model.request.PostRequest
import com.example.securesocial.data.model.response.PostLikesResponse
import com.example.securesocial.data.model.response.PostResponse
import com.example.securesocial.security.JwtService
import com.example.securesocial.service.PostInteractionService
import com.example.securesocial.service.PostService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postInteractionService: PostInteractionService,
    private val jwtService: JwtService,
    private val postService: PostService
) {

    // Create a Post
    @PostMapping
    fun createPost(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: PostRequest
    ): ResponseEntity<PostResponse> {
        val userId = jwtService.getUserIdFromToken(token)
        return ResponseEntity.ok(postService.createPost(request, userId))
    }

    // Get All Posts (with counts)
    @GetMapping
    fun getAllPosts(): ResponseEntity<List<PostResponse>> {
        return ResponseEntity.ok(postService.getAllPosts())
    }

    @GetMapping("/{postId}")
    fun getPost(
        @RequestHeader("Authorization") token: String,
        @PathVariable postId: String
    ): ResponseEntity<PostResponse> {
        val userId = jwtService.getUserIdFromToken(token)
        return ResponseEntity.ok(
            postService.getPost(postId, userId)
        )
    }

    @GetMapping("/tag/{tagName}")
    fun getPostsByTag(
        @PathVariable tagName: String
    ): ResponseEntity<List<PostResponse>> {
        return ResponseEntity.ok(postService.getPostsByTag(tagName))
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
        return ResponseEntity.ok(postService.getMyPosts(userId))
    }

    @GetMapping("/{postId}/likes")
    fun getPostLikes(
        @PathVariable postId: String
    ): ResponseEntity<List<PostLikesResponse>> {
        return ResponseEntity.ok(postInteractionService.getPostLikes(postId))
    }
}