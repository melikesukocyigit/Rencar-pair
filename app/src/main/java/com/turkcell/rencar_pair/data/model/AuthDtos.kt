package com.turkcell.rencar_pair.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDto(
    val email: String,
    val password: String,
    val fullName: String,
    val phone: String
)

@Serializable
data class LoginDto(
    val phone: String
)

@Serializable
data class OtpRequiredResponseDto(
    val message: String,
    val phone: String,
    val expiresAt: String
)

@Serializable
data class VerifyOtpDto(
    val phone: String,
    val code: String
)

@Serializable
data class RefreshTokenDto(
    val refreshToken: String
)

@Serializable
data class UserResponseDto(
    val id: String,
    val email: String,
    val fullName: String,
    val phone: String? = null,
    val role: String // PENDING, CUSTOMER, ADMIN
)

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponseDto
)

@Serializable
data class MessageResponseDto(
    val message: String
)
