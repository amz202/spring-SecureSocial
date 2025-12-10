package com.example.securesocial.controllers

import com.example.securesocial.data.model.ActivityLog
import com.example.securesocial.data.model.response.ActivityLogResponse
import com.example.securesocial.data.repositories.ActivityLogRepository
import com.example.securesocial.security.AuthService
import com.example.securesocial.security.JwtService
import org.bson.types.ObjectId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.text.toHexString

@RestController
class DashController(
    private val authService: AuthService,
    private val activityLogRepository: ActivityLogRepository,
    private val jwtService: JwtService
) {

    @GetMapping("/activity-log")
    fun getActivityLog(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<List<ActivityLogResponse>> {

        val userId = jwtService.getUserIdFromToken(token)
        val logs = activityLogRepository.findByUserId(userId)

        val response = logs?.sortedByDescending { it.createdAt }?.map { log ->
            ActivityLogResponse(
                id = log.id.toHexString(),
                userId = log.userId,
                action = log.action,
                createdAt = log.createdAt,
                details = log.details
            )
        } ?: emptyList()

        return ResponseEntity.ok(response)

    }

}