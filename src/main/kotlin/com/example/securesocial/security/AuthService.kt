package com.example.securesocial.security

import com.example.securesocial.data.model.LogType
import com.example.securesocial.data.model.RefreshToken
import com.example.securesocial.data.model.User
import com.example.securesocial.data.model.VerificationToken
import com.example.securesocial.data.model.request.OtpRequest
import com.example.securesocial.data.repositories.RefreshTokenRepository
import com.example.securesocial.data.repositories.UserRepository
import com.example.securesocial.data.repositories.VerificationTokenRepository
import com.example.securesocial.service.ActivityLogService
import com.example.securesocial.service.EmailService
import com.example.securesocialapp.data.model.response.AuthResponse
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import java.time.Instant
import java.util.*
import kotlin.text.encodeToByteArray
import kotlin.text.trim
import kotlin.time.ExperimentalTime

@Service
class AuthService(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val hashEncoder: HashEncoder,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val verificationTokenRepository: VerificationTokenRepository,
    private val emailService: EmailService,
    private val activityLogService: ActivityLogService
) {
    data class TokenPair(
        val accessToken: String,
        val refreshToken: String
    )

    fun register(username: String, password: String, email: String): User {
        val existingUser = userRepository.findByUsername(username)
        if (existingUser != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "A user with that username already exists.")
        }

        //Generate OTP
        val plainOtp = (100000..999999).random().toString()
        val hashedOtp = hashEncoder.encode(plainOtp) ?: throw RuntimeException("Encoding error")

        verificationTokenRepository.deleteByEmail(email)
        val token = VerificationToken(
            email = email,
            hashedOtp = hashedOtp
        )
        verificationTokenRepository.save(token)
        emailService.sendVerificationEmail(email, plainOtp)

        return userRepository.save(
            User(
                username = username,
                hashedPassword = hashEncoder.encode(password),
                email = email
            )
        )
    }

    fun verify(request: OtpRequest): String {
        val token = verificationTokenRepository.findByEmail(request.email)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired or invalid")

        //compare
        if (!hashEncoder.matches(request.otp, token.hashedOtp)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP")
        }

        val user = userRepository.findByEmail(request.email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        user.isVerified = true
        userRepository.save(user)

        verificationTokenRepository.deleteByEmail(request.email)

        return "Verified! Login enabled."
    }

    fun checkAvailableUsername(username: String): Boolean {
        return userRepository.findByUsername(username) == null
    }

    fun login(email: String, password: String): AuthResponse {
        val user = userRepository.findByEmail(email)
            ?: throw BadCredentialsException("Invalid credentials.")

        if(!hashEncoder.matches(password, user.hashedPassword)) {
            throw BadCredentialsException("Invalid credentials.")
        }

        val newAccessToken = jwtService.generateAccessToken(user.id.toHexString())
        val newRefreshToken = jwtService.generateRefreshToken(user.id.toHexString())

        storeRefreshToken(user.id, newRefreshToken)
        val tokenPair = TokenPair(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )

        activityLogService.log(
            userId = user.id.toHexString(),
            action = LogType.LOGIN,
            details = null
        )

        return AuthResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            userId = user.id.toHexString(),
            username = user.username,
            email = user.email
        )
    }

    @Transactional //if any of db query fail, we want to rollback the whole transaction
    fun refresh(refreshToken: String): TokenPair { //rotating
        if(!jwtService.validateRefreshToken(refreshToken)) {
            throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid refresh token.")
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = userRepository.findById(ObjectId(userId)).orElseThrow {
            ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid refresh token.")
        }

        val hashed = hashToken(refreshToken)
        refreshTokenRepository.findByUserIdAndHashedToken(user.id, hashed)
            ?: throw ResponseStatusException(
                HttpStatusCode.valueOf(401),
                "Refresh token not recognized (maybe used or expired?)"
            )

        refreshTokenRepository.deleteByUserIdAndHashedToken(user.id, hashed)

        val newAccessToken = jwtService.generateAccessToken(userId)
        val newRefreshToken = jwtService.generateRefreshToken(userId)

        storeRefreshToken(user.id, newRefreshToken)

        return TokenPair(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    fun resendOtp(email: String): String {
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        if (user.isVerified) {
            return "Account is already verified. Please login."
        }

        val plainOtp = (100000..999999).random().toString()
        val hashedOtp = hashEncoder.encode(plainOtp) ?: throw RuntimeException("Encoding error")

        verificationTokenRepository.deleteByEmail(email)

        val token = VerificationToken(
            email = email,
            hashedOtp = hashedOtp
        )
        verificationTokenRepository.save(token)

        emailService.sendVerificationEmail(email, plainOtp)

        return "New code sent! Check your email."
    }

    @OptIn(ExperimentalTime::class)
    private fun storeRefreshToken(userId: ObjectId, rawRefreshToken: String) {
        val hashed = hashToken(rawRefreshToken)
        val expiryMs = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshToken(
                userId = userId,
                expiresAt = expiresAt,
                hashedToken = hashed
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}