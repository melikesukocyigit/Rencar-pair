package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.LoginDto
import com.turkcell.rencar_pair.data.model.VerifyOtpDto
import com.turkcell.rencar_pair.data.remote.AdminApprovalService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Yuz eslestirme esigini gecen basvurulari "AI ile Anında Onayla" demo butonuyla
 * aninda onaylamak icin kullanilir. Backend'de bu proje icin tanimli sabit bir demo
 * admin hesabini kullanir; OTP dogrulamasi da backend'de gercek SMS yerine sabit
 * "123456" koduyla simule edilmektedir (bkz. docs/ml-face-matching.md).
 *
 * ONEMLI: Bu, yalnizca demo/MVP kapsaminda kabul edilebilir bir kisayoldur. Gercek
 * bir ufounda admin yetkisi asla istemciye gomulmez; onay karari sunucu tarafinda
 * (ornegin ayri bir backend servisinde) verilir (bkz. architecture plan'daki
 * Zero-Trust/Yontem-B alternatifi).
 */
@Singleton
class AdminApprovalRepository @Inject constructor(
    private val adminApprovalService: AdminApprovalService,
) {
    suspend fun approveViaAi(licenseId: String): Result<Unit> = try {
        val loginResponse = adminApprovalService.login(LoginDto(phone = ADMIN_PHONE))
        if (!loginResponse.isSuccessful) {
            Result.failure(Exception("Admin girişi başlatılamadı."))
        } else {
            val otpResponse = adminApprovalService.verifyOtp(
                VerifyOtpDto(phone = ADMIN_PHONE, code = ADMIN_OTP),
            )
            val adminToken = otpResponse.body()?.accessToken
            if (!otpResponse.isSuccessful || adminToken == null) {
                Result.failure(Exception("Admin doğrulaması başarısız."))
            } else {
                val approveResponse = adminApprovalService.approve(licenseId, "Bearer $adminToken")
                if (approveResponse.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(
                        Exception(approveResponse.errorBody()?.string() ?: "Onay başarısız."),
                    )
                }
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private companion object {
        const val ADMIN_PHONE = "+905550000000"
        const val ADMIN_OTP = "123456"
    }
}
