package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.domain.model.ActiveRental
import com.turkcell.rencar_pair.domain.model.PaymentRequest
import com.turkcell.rencar_pair.domain.model.Rental
import com.turkcell.rencar_pair.domain.model.RentalPhotos
import com.turkcell.rencar_pair.domain.model.VehicleLocation
import kotlinx.coroutines.flow.Flow

interface RentalRepository {
    suspend fun createRental(vehicleId: String, endDate: String?, plan: String = "DAILY"): Result<Rental>
    suspend fun getMyRentals(): Result<List<Rental>>
    suspend fun getRentalDetails(rentalId: String): Result<Rental>

    // Eski uc: yalniz DAILY plani icin gecerli.
    suspend fun returnVehicle(rentalId: String): Result<Rental>

    // Yalniz PREPARING durumundaki (henuz start edilmemis) yolculuk icin gecerli.
    suspend fun cancelPreparingRental(rentalId: String): Result<Unit>

    // PREPARING -> ACTIVE. 4 foto tamamlanmadan 409 doner.
    suspend fun startRental(rentalId: String): Result<Rental>

    // ACTIVE -> COMPLETED, ucret dokumunu kilitler (PER_MINUTE/HOURLY icin).
    suspend fun finishRental(rentalId: String): Result<Rental>

    suspend fun payRental(rentalId: String, request: PaymentRequest): Result<Unit>

    suspend fun getPhotosState(rentalId: String): Result<RentalPhotos>

    suspend fun uploadPhoto(
        rentalId: String,
        side: String,
        fileBytes: ByteArray,
        fileName: String = "photo.jpg",
    ): Result<RentalPhotos>

    // Aktif kiralama yoksa (404) basarili sonuc icinde null doner; gercek hatalarda
    // (ag/sunucu) Result.failure doner. GET /rentals'in aksine (bilinen 500 hatasi)
    // bu uc nokta canlida calisiyor - aktif kiralama kontrolu icin bu kullanilmali.
    suspend fun getActiveRental(): Result<ActiveRental?>

    // Aktif kiralamadaki aracin canli konumu (Socket.IO). Aktif kiralama yoksa akis hic veri
    // yaymaz; sunucu rentalId parametresi almadan token'a gore filtreler.
    fun vehiclePositionStream(): Flow<VehicleLocation>
}
