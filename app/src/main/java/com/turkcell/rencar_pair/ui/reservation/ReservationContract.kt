package com.turkcell.rencar_pair.ui.reservation

enum class RentalPlan {
    DAKIKALIK,
    SAATLIK,
    GUNLUK,
}

data class ReservationUiState(
    val vehicleId: String = "",
    val brand: String = "",
    val model: String = "",
    val plate: String = "",
    val pricePerDay: Double = 0.0,
    val selectedPlan: RentalPlan = RentalPlan.DAKIKALIK,
    val termsAccepted: Boolean = false,
    val isSubmitting: Boolean = false,
) {
    // Backend yalnizca gunluk fiyat (pricePerDay) donduruyor; dakikalik/saatlik
    // degerler bundan orantili turetiliyor (uydurma sabit degil, gercek pricePerDay'e
    // orantili hesaplama) - VehicleDetailBottomSheet'teki ayni yaklasim.
    val pricePerMinute: Double get() = pricePerDay / 1440.0
    val pricePerHour: Double get() = pricePerDay / 24.0

    val estimatedDurationLabel: String
        get() = when (selectedPlan) {
            RentalPlan.DAKIKALIK -> "30 dk"
            RentalPlan.SAATLIK -> "1 sa"
            RentalPlan.GUNLUK -> "1 gün"
        }

    val estimatedCost: Double
        get() = when (selectedPlan) {
            RentalPlan.DAKIKALIK -> pricePerMinute * 30
            RentalPlan.SAATLIK -> pricePerHour
            RentalPlan.GUNLUK -> pricePerDay
        }

    val isConfirmEnabled: Boolean
        get() = termsAccepted && !isSubmitting
}

sealed interface ReservationIntent {
    data class PlanSelected(val plan: RentalPlan) : ReservationIntent
    data class TermsToggled(val accepted: Boolean) : ReservationIntent
    data object ConfirmClicked : ReservationIntent
}

sealed interface ReservationEffect {
    data class NavigateToVehicleCondition(
        val rentalId: String,
        val vehicleId: String,
        val brand: String,
        val model: String,
        val plate: String,
        val pricePerDay: Double,
    ) : ReservationEffect
    data class ShowError(val message: String) : ReservationEffect
}
