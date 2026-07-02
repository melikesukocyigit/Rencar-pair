package com.turkcell.rencar_pair.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateRentalDto(
    val vehicleId: String,
    val endDate: String // ISO 8601 string
)

@Serializable
data class RentalResponseDto(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val startDate: String,
    val endDate: String,
    val totalPrice: Double,
    val status: String, // ACTIVE, COMPLETED, CANCELLED
    val createdAt: String
)

@Serializable
data class RentalUserSummary(
    val id: String,
    val email: String,
    val fullName: String
)

@Serializable
data class RentalVehicleSummary(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val status: String
)

@Serializable
data class AdminRentalResponseDto(
    val id: String,
    val startDate: String,
    val endDate: String,
    val totalPrice: Double,
    val status: String,
    val createdAt: String,
    val user: RentalUserSummary,
    val vehicle: RentalVehicleSummary
)
