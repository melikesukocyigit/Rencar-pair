package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.ActiveRentalResponseDto
import com.turkcell.rencar_pair.data.model.CreateRentalDto
import com.turkcell.rencar_pair.data.model.FinishRentalResponseDto
import com.turkcell.rencar_pair.data.model.PayRentalDto
import com.turkcell.rencar_pair.data.model.RentalPhotosStateDto
import com.turkcell.rencar_pair.data.model.RentalResponseDto
import com.turkcell.rencar_pair.data.model.RentalVehicleSummaryDto
import com.turkcell.rencar_pair.data.model.VehicleLocationPoint
import com.turkcell.rencar_pair.data.remote.RentalService
import com.turkcell.rencar_pair.data.remote.RideLocationClient
import com.turkcell.rencar_pair.domain.model.ActiveRental
import com.turkcell.rencar_pair.domain.model.PaymentRequest
import com.turkcell.rencar_pair.domain.model.Rental
import com.turkcell.rencar_pair.domain.model.RentalPhotos
import com.turkcell.rencar_pair.domain.model.RentalVehicleSummary
import com.turkcell.rencar_pair.domain.model.VehicleLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

private fun RentalResponseDto.toDomain(): Rental = Rental(
    id = id,
    vehicleId = vehicleId,
    status = status,
    startDate = startDate,
    plan = plan,
    startFee = startFee,
    totalPrice = totalPrice,
    durationMinutes = durationMinutes,
    paymentMethod = paymentMethod,
)

private fun FinishRentalResponseDto.toDomain(): Rental = Rental(
    id = id,
    vehicleId = vehicleId,
    status = status,
    startDate = startDate,
    plan = plan,
    startFee = startFee,
    totalPrice = totalPrice,
    durationMinutes = durationMinutes,
    paymentMethod = paymentMethod,
)

private fun RentalVehicleSummaryDto.toDomain(): RentalVehicleSummary = RentalVehicleSummary(
    brand = brand,
    model = model,
    plate = plate,
)

private fun ActiveRentalResponseDto.toDomain(): ActiveRental = ActiveRental(
    id = id,
    vehicleId = vehicleId,
    vehicle = vehicle.toDomain(),
    currentCost = currentCost,
    distanceKm = distanceKm,
)

private fun RentalPhotosStateDto.toDomain(): RentalPhotos = RentalPhotos(sides = photos.map { it.side })

private fun VehicleLocationPoint.toDomain(): VehicleLocation = VehicleLocation(latitude = latitude, longitude = longitude)

private fun PaymentRequest.toDto(): PayRentalDto = when (this) {
    PaymentRequest.Wallet -> PayRentalDto(method = "WALLET")
    is PaymentRequest.Card -> PayRentalDto(method = "CARD", cardId = cardId)
    is PaymentRequest.Iyzico -> PayRentalDto(method = "IYZICO", iyzicoPaymentId = paymentId)
}

class RentalRepositoryImpl @Inject constructor(
    private val rentalService: RentalService,
    private val rideLocationClient: RideLocationClient,
) : RentalRepository {

    override suspend fun createRental(vehicleId: String, endDate: String?, plan: String): Result<Rental> =
        runCatching {
            val response = rentalService.createRental(
                CreateRentalDto(vehicleId = vehicleId, endDate = endDate, plan = plan)
            )
            if (!response.isSuccessful) error(response.apiMessage())
            response.body()?.toDomain() ?: error("Sunucudan bos yanit alindi.")
        }

    override suspend fun getMyRentals(): Result<List<Rental>> = runCatching {
        val response = rentalService.getMyRentals()
        if (!response.isSuccessful) error(response.apiMessage())
        response.body()?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun getRentalDetails(rentalId: String): Result<Rental> = runCatching {
        val response = rentalService.getRentalDetails(rentalId)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body()?.toDomain() ?: error("Sunucudan bos yanit alindi.")
    }

    override suspend fun returnVehicle(rentalId: String): Result<Rental> = runCatching {
        val response = rentalService.returnVehicle(rentalId)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body()?.toDomain() ?: error("Sunucudan bos yanit alindi.")
    }

    override suspend fun cancelPreparingRental(rentalId: String): Result<Unit> = runCatching {
        val response = rentalService.cancelPreparingRental(rentalId)
        if (!response.isSuccessful) error(response.apiMessage())
    }

    override suspend fun startRental(rentalId: String): Result<Rental> = runCatching {
        val response = rentalService.startRental(rentalId)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body()?.toDomain() ?: error("Sunucudan bos yanit alindi.")
    }

    override suspend fun finishRental(rentalId: String): Result<Rental> = runCatching {
        val response = rentalService.finishRental(rentalId)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body()?.toDomain() ?: error("Sunucudan bos yanit alindi.")
    }

    override suspend fun payRental(rentalId: String, request: PaymentRequest): Result<Unit> = runCatching {
        val response = rentalService.payRental(rentalId, request.toDto())
        if (!response.isSuccessful) error(response.apiMessage())
    }

    override suspend fun getPhotosState(rentalId: String): Result<RentalPhotos> = runCatching {
        val response = rentalService.getPhotosState(rentalId)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body()?.toDomain() ?: error("Sunucudan bos yanit alindi.")
    }

    override suspend fun uploadPhoto(
        rentalId: String,
        side: String,
        fileBytes: ByteArray,
        fileName: String,
    ): Result<RentalPhotos> = runCatching {
        val sideBody = side.toRequestBody("text/plain".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData(
            "file",
            fileName,
            fileBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        )
        val response = rentalService.uploadPhoto(rentalId, sideBody, filePart)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body()?.toDomain() ?: error("Sunucudan bos yanit alindi.")
    }

    override fun vehiclePositionStream(): Flow<VehicleLocation> =
        rideLocationClient.vehiclePositionStream().map { it.toDomain() }

    override suspend fun getActiveRental(): Result<ActiveRental?> = runCatching {
        val response = rentalService.getActiveRental()
        if (response.code() == 404) return@runCatching null
        if (!response.isSuccessful) error(response.apiMessage())
        response.body()?.toDomain()
    }

    // API hata body'sindeki "message" alanini parse eder.
    private fun Response<*>.apiMessage(): String {
        val bodyString = errorBody()?.string()
        if (!bodyString.isNullOrBlank()) {
            try {
                return JSONObject(bodyString).getString("message")
            } catch (_: Exception) { }
        }
        return message().ifBlank { "Bir hata olustu. (${code()})" }
    }
}
