package com.turkcell.rencar_pair.data.repository

interface AuthRepository {
    suspend fun login(phone: String): Result<Unit>
    suspend fun verifyOtp(phone: String, code: String): Result<String>
    suspend fun register(email: String, password: String, fullName: String, phone: String): Result<Unit>
}
