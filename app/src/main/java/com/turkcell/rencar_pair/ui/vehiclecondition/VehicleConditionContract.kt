package com.turkcell.rencar_pair.ui.vehiclecondition

enum class VehicleSide(val label: String) {
    ON("Ön"),
    ARKA("Arka"),
    SOL("Sol"),
    SAG("Sağ"),
}

data class VehicleConditionUiState(
    val rentalId: String = "",
    val vehicleId: String = "",
    val brand: String = "",
    val model: String = "",
    val plate: String = "",
    val pricePerDay: Double = 0.0,
    val checkedSides: Set<VehicleSide> = emptySet(),
) {
    val totalSides: Int = VehicleSide.entries.size
    val checkedCount: Int get() = checkedSides.size
    val remainingCount: Int get() = totalSides - checkedCount
    val isStartEnabled: Boolean get() = checkedCount == totalSides
}

sealed interface VehicleConditionIntent {
    data class PhotoMockCaptured(val side: VehicleSide) : VehicleConditionIntent
    data object StartRentalClicked : VehicleConditionIntent
}

sealed interface VehicleConditionEffect {
    data class NavigateToActiveRental(
        val rentalId: String,
        val vehicleId: String,
        val brand: String,
        val model: String,
        val plate: String,
        val pricePerDay: Double,
    ) : VehicleConditionEffect
}
