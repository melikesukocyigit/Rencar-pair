package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.local.TokenManager
import com.turkcell.rencar_pair.data.model.LoginDto
import com.turkcell.rencar_pair.data.model.RegisterDto
import com.turkcell.rencar_pair.data.model.VerifyOtpDto
import com.turkcell.rencar_pair.data.remote.AuthService
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val tokenManager: TokenManager,
) : AuthRepository {

    override suspend fun login(phone: String): Result<Unit> = runCatching {
        val response = authService.login(LoginDto(phone = phone))
        if (!response.isSuccessful) error(response.apiMessage())
    }

    override suspend fun verifyOtp(phone: String, code: String): Result<String> = runCatching {
        val response = authService.verifyOtp(VerifyOtpDto(phone = phone, code = code))
        if (!response.isSuccessful) error(response.apiMessage())
        val body = response.body() ?: error("Sunucudan gecersiz yanit.")
        tokenManager.saveTokens(body.accessToken, body.refreshToken)
        body.user.role
    }

    override suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String,
    ): Result<Unit> = runCatching {
        val response = authService.register(RegisterDto(email, password, fullName, phone))
        if (!response.isSuccessful) error(response.apiMessage())
    }

    // API hata body'sindeki "message" alanini parse eder.
    // response.message() bazi HTTP kodlarinda bos string donebilir; bu fonksiyon bunu onler.
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
