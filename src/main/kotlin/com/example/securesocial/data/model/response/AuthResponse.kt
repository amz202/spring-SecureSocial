package com.example.securesocialapp.data.model.response

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val username: String,
    val email: String
)