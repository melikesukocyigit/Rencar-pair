package com.turkcell.rencar_pair.domain.model

data class ActiveRental(
    val id: String,
    val vehicleId: String,
    val vehicle: RentalVehicleSummary,
    val currentCost: Double,
    val distanceKm: Double,
)
