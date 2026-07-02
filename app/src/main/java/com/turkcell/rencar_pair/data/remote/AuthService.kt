package com.turkcell.rencar_pair.data.remote

import com.turkcell.rencar_pair.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthService {
    @POST("auth/register")
    suspend fun register(@Body dto: RegisterDto): Response<AuthResponseDto>

    @POST("auth/login")
    suspend fun login(@Body dto: LoginDto): Response<OtpRequiredResponseDto>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body dto: VerifyOtpDto): Response<AuthResponseDto>

    @POST("auth/refresh")
    suspend fun refreshTokens(@Body dto: RefreshTokenDto): Response<AuthResponseDto>

    @POST("auth/logout")
    suspend fun logout(): Response<MessageResponseDto>

    @GET("auth/me")
    suspend fun getMe(): Response<UserResponseDto>
}
