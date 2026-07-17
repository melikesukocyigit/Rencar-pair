package com.turkcell.rencar_pair.ui.activerental

data class ActiveRentalUiState(
    val rentalId: String = "",
    val vehicleId: String = "",
    val brand: String = "",
    val model: String = "",
    val plate: String = "",
    val startEpochMillis: Long? = null,
    val nowEpochMillis: Long = System.currentTimeMillis(),
    // GET /rentals/active'ten periyodik olarak gelir (bkz. ActiveRentalViewModel).
    // Onceki "gunluk fiyat / 1440 * gecen dakika" orantili tahmini (uydurma, baslangic/
    // hizmet bedelini bilmiyordu) ve yerel Haversine mesafe birikimi (ekran kapaninca
    // sifirlaniyordu) yerine artik sunucunun gercek hesabi kullaniliyor.
    val currentCost: Double = 0.0,
    val distanceKm: Double = 0.0,
    val isVehicleLocked: Boolean = false,
) {
    // Saat yereldeki gibi akmaya devam eder (saniyede bir Tick) - bu sadece gorsel
    // sayac, para hesabina girmiyor; gercek ucret/mesafe currentCost/distanceKm'den gelir.
    val elapsedSeconds: Long
        get() {
            val start = startEpochMillis ?: return 0L
            return ((nowEpochMillis - start) / 1000L).coerceAtLeast(0L)
        }

    val elapsedTimeLabel: String
        get() {
            val total = elapsedSeconds
            val hours = total / 3600
            val minutes = (total % 3600) / 60
            val seconds = total % 60
            return "%02d:%02d:%02d".format(hours, minutes, seconds)
        }
}

sealed interface ActiveRentalIntent {
    data object LockToggleClicked : ActiveRentalIntent
    data object EndRentalClicked : ActiveRentalIntent
    data object Tick : ActiveRentalIntent
}

sealed interface ActiveRentalEffect {
    data class NavigateToVehicleCondition(
        val rentalId: String,
        val vehicleId: String,
        val brand: String,
        val model: String,
        val plate: String,
        val durationSeconds: Long,
        val distanceMeters: Double,
    ) : ActiveRentalEffect
}
