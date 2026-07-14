package com.turkcell.rencar_pair.ui.vehiclecondition

enum class VehicleSide(val label: String) {
    ON("Ön"),
    ARKA("Arka"),
    SOL("Sol"),
    SAG("Sağ"),
}

enum class VehicleConditionMode {
    BEFORE,
    AFTER,
}

data class VehicleConditionUiState(
    val mode: VehicleConditionMode = VehicleConditionMode.BEFORE,
    val rentalId: String = "",
    val vehicleId: String = "",
    val brand: String = "",
    val model: String = "",
    val plate: String = "",
    val pricePerDay: Double = 0.0,
    val durationSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val checkedSides: Set<VehicleSide> = emptySet(),
    val isSubmitting: Boolean = false,
) {
    val totalSides: Int = VehicleSide.entries.size
    val checkedCount: Int get() = checkedSides.size
    val remainingCount: Int get() = totalSides - checkedCount
    val isConfirmEnabled: Boolean get() = checkedCount == totalSides && !isSubmitting
}

sealed interface VehicleConditionIntent {
    data class PhotoMockCaptured(val side: VehicleSide) : VehicleConditionIntent
    data object ConfirmClicked : VehicleConditionIntent
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

    data class NavigateToTripSummary(
        val rentalId: String,
        val brand: String,
        val model: String,
        val plate: String,
        val durationSeconds: Long,
        val distanceMeters: Double,
        val totalPrice: Double,
    ) : VehicleConditionEffect

    data class ShowError(val message: String) : VehicleConditionEffect
}
