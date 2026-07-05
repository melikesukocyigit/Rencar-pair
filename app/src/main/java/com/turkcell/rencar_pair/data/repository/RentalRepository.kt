package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.RentalResponseDto

interface RentalRepository {
    suspend fun createRental(vehicleId: String, endDate: String): Result<RentalResponseDto>
}
