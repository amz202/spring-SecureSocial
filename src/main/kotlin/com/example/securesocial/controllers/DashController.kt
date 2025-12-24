package com.example.securesocial.controllers

import com.example.securesocial.data.model.response.ActivityLogResponse
import com.example.securesocial.security.JwtService
import com.example.securesocial.service.DashService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class DashController(
    private val dashService: DashService,
    private val jwtService: JwtService
) {

    @GetMapping("/activity-log")
    fun getActivityLog(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<List<ActivityLogResponse>> {

        val userId = jwtService.getUserIdFromToken(token)
        return ResponseEntity.ok(dashService.getActivityLog(userId))

    }

}