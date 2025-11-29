package com.example.securesocial.controllers

import com.example.securesocial.data.model.Post
import com.example.securesocial.data.model.PostTag
import com.example.securesocial.data.model.request.PostRequest
import com.example.securesocial.data.model.response.PostResponse
import com.example.securesocial.data.repositories.PostRepository
import com.example.securesocial.security.JwtService
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
    private val postInteractionService: PostInteractionService,
    private val jwtService: JwtService
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

        val response = PostResponse(
            id = savedPost.id.toHexString(),
            authorId = savedPost.authorId.toHexString(),
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
                authorId = post.authorId.toHexString(),
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

        return ResponseEntity.ok(
            PostResponse(
                id = post.id.toHexString(),
                title = post.title,
                content = post.content,
                createdAt = post.createdAt,
                authorId = post.authorId.toHexString(),
                tag = post.tag.toString(),
                likeCount = postInteractionService.getLikeCount(postId),
                viewCount = postInteractionService.getViewCount(postId)
            )
        )
    }

    @GetMapping("/tag/{tagName}")
    fun getPostsByTag(
        @PathVariable tagName: String
    ): ResponseEntity<List<PostResponse>> {

        val tag = PostTag.valueOf(tagName.uppercase())

        val posts = postRepository.findByTag(tag)

        val response = posts?.map { post ->
            PostResponse(
                id = post.id.toHexString(),
                title = post.title,
                content = post.content,
                tag = post.tag.name,
                createdAt = post.createdAt,
                authorId = post.authorId.toHexString(),
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
}