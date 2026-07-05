package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.VehicleResponseDto
import com.turkcell.rencar_pair.data.remote.VehicleService
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val vehicleService: VehicleService,
) : VehicleRepository {

    override suspend fun getAvailableVehicles(): Result<List<VehicleResponseDto>> = runCatching {
        val response = vehicleService.getAvailableVehicles()
        if (!response.isSuccessful) error(response.apiMessage())
        response.body() ?: emptyList()
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
