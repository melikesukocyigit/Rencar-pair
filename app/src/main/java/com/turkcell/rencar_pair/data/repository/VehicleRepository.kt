package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.VehicleResponseDto

interface VehicleRepository {
    suspend fun getAvailableVehicles(page: Int = 1, limit: Int = 20): Result<List<VehicleResponseDto>>
    suspend fun getVehicleDetails(id: String): Result<VehicleResponseDto>
    suspend fun getQuote(id: String, plan: String, minutes: Int): Result<com.turkcell.rencar_pair.data.model.QuoteResponseDto>
}