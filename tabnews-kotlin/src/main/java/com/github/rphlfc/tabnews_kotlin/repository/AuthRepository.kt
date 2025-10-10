package com.github.rphlfc.tabnews_kotlin.repository

import com.github.rphlfc.tabnews_kotlin.api.APIService
import com.github.rphlfc.tabnews_kotlin.model.LoginRequest
import com.github.rphlfc.tabnews_kotlin.model.LoginResponse
import com.github.rphlfc.tabnews_kotlin.security.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<LoginResponse>
    suspend fun logout()
}

internal class AuthRepositoryImpl(
    private val api: APIService,
    private val authManager: AuthManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val loginRequest = LoginRequest(email = email, password = password)
                val response = api.login(loginRequest)

                authManager.setToken(
                    token = response.token,
                    userId = response.id,
                    expiresAt = response.expiresAt
                )

                Result.success(response)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        authManager.logout()
    }
}