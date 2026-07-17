package com.turkcell.rencar_pair.data.remote

import com.turkcell.rencar_pair.data.model.AdminLicenseResponseDto
import com.turkcell.rencar_pair.data.model.AuthResponseDto
import com.turkcell.rencar_pair.data.model.LoginDto
import com.turkcell.rencar_pair.data.model.OtpRequiredResponseDto
import com.turkcell.rencar_pair.data.model.VerifyOtpDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * "AI ile Anında Onayla" demo akisi icin izole servis arayuzu. AuthService/LicenseService
 * ile ayni ucnoktalari kullanir ama bilincli olarak AYRI bir Retrofit/OkHttp uzerinden
 * cagrilir (bkz. AdminApprovalModule) - musteri oturumunun Authorization header'ini
 * otomatik ekleyen AuthInterceptor bu akisa hic karismaz, admin token'i biz elle tasiriz.
 */
interface AdminApprovalService {
    @POST("auth/login")
    suspend fun login(@Body dto: LoginDto): Response<OtpRequiredResponseDto>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body dto: VerifyOtpDto): Response<AuthResponseDto>

    @PATCH("admin/licenses/{id}/approve")
    suspend fun approve(
        @Path("id") licenseId: String,
        @Header("Authorization") authorization: String,
    ): Response<AdminLicenseResponseDto>
}
