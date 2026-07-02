package com.turkcell.rencar_pair.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LicenseResponseDto(
    val id: String,
    val status: String, // NOT_SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED
    val frontImageUrl: String,
    val backImageUrl: String,
    val rejectReason: String? = null,
    val reviewedAt: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class LicenseStatusResponseDto(
    val status: String, // NOT_SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED
    val frontImageUrl: String? = null,
    val backImageUrl: String? = null,
    val rejectReason: String? = null,
    val reviewedAt: String? = null
)

@Serializable
data class AdminLicenseResponseDto(
    val id: String,
    val userId: String,
    val status: String,
    val frontImageUrl: String,
    val backImageUrl: String,
    val rejectReason: String? = null,
    val reviewedAt: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val user: UserResponseDto
)

@Serializable
data class RejectLicenseDto(
    val reason: String
)
