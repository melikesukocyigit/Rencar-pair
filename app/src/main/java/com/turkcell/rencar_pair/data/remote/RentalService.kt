package com.turkcell.rencar_pair.data.remote

import com.turkcell.rencar_pair.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface RentalService {
    @POST("rentals")
    suspend fun createRental(@Body dto: CreateRentalDto): Response<RentalResponseDto>

    @GET("rentals")
    suspend fun getMyRentals(): Response<List<RentalResponseDto>>

    @GET("rentals/{id}")
    suspend fun getRentalDetails(@Path("id") id: String): Response<RentalResponseDto>

    @POST("rentals/{id}/return")
    suspend fun returnVehicle(@Path("id") id: String): Response<RentalResponseDto>
}
