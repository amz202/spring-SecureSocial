package com.example.securesocial.controllers

import com.example.securesocial.data.repositories.PostLikeRepository
import com.example.securesocial.service.PostInteractionService
import org.bson.types.ObjectId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/demo")
class TamperController(
    private val postLikeRepository: PostLikeRepository,
    private val postInteractionService: PostInteractionService
) {

    @PostMapping("/corrupt-like/{likeId}")
    fun corruptLikeData(@PathVariable likeId: String): ResponseEntity<String> {
        val like = postLikeRepository.findById(ObjectId(likeId)).orElseThrow()

        // We change the timestamp but keep the old signature
        val corruptedLike = like.copy(likedAt = System.currentTimeMillis())

        postLikeRepository.save(corruptedLike)
        return ResponseEntity.ok("Like record $likeId has been corrupted. Signature is now invalid.")
    }

    @GetMapping("/verify-like/{likeId}")
    fun verifyLike(@PathVariable likeId: String): ResponseEntity<Map<String, Any>> {
        val isValid = postInteractionService.verifyLikeIntegrity(likeId)

        val status = if (isValid) "SECURE" else "TAMPERED"
        return ResponseEntity.ok(mapOf(
            "status" to status,
            "isValid" to isValid,
            "message" to if (isValid) "Signature matches data." else "WARNING: Data has been modified!"
        ))
    }
}