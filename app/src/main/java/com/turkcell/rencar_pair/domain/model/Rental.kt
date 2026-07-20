package com.turkcell.rencar_pair.domain.model

data class Rental(
    val id: String,
    val vehicleId: String,
    val status: String,
    val startDate: String,
    val plan: String,
    val startFee: Double,
    val totalPrice: Double?,
    val durationMinutes: Int,
    val paymentMethod: String?,
)
