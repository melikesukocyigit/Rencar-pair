package com.turkcell.rencar_pair.ui.home

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class LatLng(
    val latitude: Double,
    val longitude: Double,
)

private const val EARTH_RADIUS_METERS = 6_371_000.0

// Backend'de rota/ETA endpoint'i yok; yalnizca gercek GPS konumundan duz (kus ucusu)
// mesafe hesaplanabiliyor. Suru bu gercek mesafe uzerinden hesaplaniyor (uydurma degil).
private fun haversineMeters(from: LatLng, to: LatLng): Double {
    val lat1 = Math.toRadians(from.latitude)
    val lat2 = Math.toRadians(to.latitude)
    val dLat = Math.toRadians(to.latitude - from.latitude)
    val dLng = Math.toRadians(to.longitude - from.longitude)
    val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)
    return 2 * EARTH_RADIUS_METERS * asin(sqrt(h))
}

// "Konumuma git" butonuna basildiktan sonra "Yakinimda N arac" gercekten yakindaki
// araclari saysin diye kullanilan esik. Backend'de konum bazli bir filtre parametresi
// olmadigindan (GET /vehicles'ta lat/lng/radius yok) bu filtre tamamen istemci
// tarafinda, gercek Haversine mesafesiyle yapiliyor.
private const val NEARBY_RADIUS_METERS = 5_000.0

// Gercek rota/trafik verisi olmadigindan varsayilan bir ortalama sehir-ici surus hizi
// (docs/decisions.md'de tahmini oldugu belirtilmistir) ile yaklasik dakikaya cevriliyor.
private const val ASSUMED_AVERAGE_SPEED_KMH = 25.0

private fun etaMinutes(distanceMeters: Double): Int {
    val speedMetersPerMinute = (ASSUMED_AVERAGE_SPEED_KMH * 1000.0) / 60.0
    return (distanceMeters / speedMetersPerMinute).roundToInt().coerceAtLeast(1)
}

enum class VehicleCategory {
    EKONOMIK,
    KONFOR,
    SUV,
}

enum class CategoryFilter {
    TUMU,
    EKONOMIK,
    KONFOR,
    SUV,
}

data class VehicleMarker(
    val id: String,
    val position: LatLng,
    val priceLabel: String,
    val category: VehicleCategory,
    val inUse: Boolean = false,
    val brand: String = "",
    val model: String = "",
    val plate: String = "",
    val pricePerDay: Int = 0,
    // Backend'in gercek telemetri alanlari (VehicleResponseDto): daha once
    // HomeScreen'de sabit placeholder degerlerle gosteriliyordu.
    val fuelPercent: Int = 0,
    val rangeKm: Int = 0,
    val transmissionLabel: String = "",
    val seats: Int = 0,
)

data class ActiveRentalVehicle(
    val brand: String,
    val model: String,
    val plate: String,
    val pricePerDay: Double,
)

data class ActiveRentalSummary(
    val rentalId: String,
    val vehicleId: String,
    // Arac detayi cagrisi basarisiz olursa null kalir. Kiralamayi bitirmek icin
    // rentalId tek basina yettiginden banner ve yonlendirme yine de calisir.
    val vehicle: ActiveRentalVehicle? = null,
)

data class HomeUiState(
    val vehicles: List<VehicleMarker> = emptyList(),
    val selectedFilter: CategoryFilter = CategoryFilter.TUMU,
    val hasLocationPermission: Boolean = false,
    val userLocation: LatLng? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedVehicleId: String? = null,
    val isLocationAccuracyHigh: Boolean = true,
    val activeRental: ActiveRentalSummary? = null,
    // "Konumuma git" butonuna basilinca true olur; "Yakinimda N arac" o andan
    // itibaren tum listeyi degil, gercekten NEARBY_RADIUS_METERS icindeki araclari
    // sayar. Harita Turkiye geneli gorunumdeyken (buton hic basilmadan) tum
    // eslesen araclarin sayisi gosterilmeye devam eder.
    val isFocusedOnUserLocation: Boolean = false,
) {
    val visibleVehicles: List<VehicleMarker>
        get() = if (selectedFilter == CategoryFilter.TUMU) {
            vehicles
        } else {
            vehicles.filter { it.category.name == selectedFilter.name }
        }

    val nearbyVehicleCount: Int
        get() {
            val location = userLocation
            if (!isFocusedOnUserLocation || location == null) return visibleVehicles.size
            return visibleVehicles.count { haversineMeters(location, it.position) <= NEARBY_RADIUS_METERS }
        }

    val selectedVehicle: VehicleMarker?
        get() = vehicles.find { it.id == selectedVehicleId }

    val nearestVehicle: VehicleMarker?
        get() {
            val location = userLocation ?: return null
            return visibleVehicles.minByOrNull { haversineMeters(location, it.position) }
        }

    val nearestVehicleDistanceMeters: Double?
        get() {
            val location = userLocation ?: return null
            return nearestVehicle?.let { haversineMeters(location, it.position) }
        }

    val nearestVehicleEtaMinutes: Int?
        get() = nearestVehicleDistanceMeters?.let { etaMinutes(it) }
}

sealed interface HomeIntent {
    data class FilterSelected(val filter: CategoryFilter) : HomeIntent
    data class LocationPermissionResult(val granted: Boolean) : HomeIntent
    data class UserLocationChanged(val location: LatLng) : HomeIntent
    data class SearchQueryChanged(val query: String) : HomeIntent
    data class VehicleSelected(val vehicleId: String) : HomeIntent
    data object VehicleDetailDismissed : HomeIntent
    data object LocateMeClicked : HomeIntent
    data object FindNearestVehicleClicked : HomeIntent
    data object RefreshSettings : HomeIntent
}

sealed interface HomeEffect {
    data object RequestLocationPermission : HomeEffect
    data object CenterOnUserLocation : HomeEffect
    data class CenterOnLocation(val location: LatLng) : HomeEffect
    data class ShowError(val message: String) : HomeEffect
    data class NavigateToActiveRental(val activeRental: ActiveRentalSummary) : HomeEffect
}
