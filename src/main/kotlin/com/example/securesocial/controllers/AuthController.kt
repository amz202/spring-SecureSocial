package com.example.securesocial.controllers

import com.example.securesocial.data.model.request.OtpRequest
import com.example.securesocial.data.model.request.RegisterRequest
import com.example.securesocial.data.model.request.RefreshRequest
import com.example.securesocial.security.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    val authService: AuthService
) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody body: RegisterRequest
    ) {
        authService.register(body.username, body.password, body.email)
    }

    @PostMapping("/login")
    fun login(
        @RequestBody body: RegisterRequest
    ): AuthService.TokenPair {
        return authService.login(body.username, body.password)
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody body: RefreshRequest
    ): AuthService.TokenPair {
        return authService.refresh(body.refreshToken)
    }

    @GetMapping("/checkUsername")
    fun checkUsernameAvailability(username: String) = authService.checkAvailableUsername(username)

    @PostMapping("/verify-otp")
    fun verify(@RequestBody body: OtpRequest): ResponseEntity<String> {
        return ResponseEntity.ok(authService.verify(body))
    }
}