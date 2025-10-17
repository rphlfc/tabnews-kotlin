package com.github.rphlfc.tabnews_kotlin.repository

import com.github.rphlfc.tabnews_kotlin.api.APIService
import com.github.rphlfc.tabnews_kotlin.model.APIResult
import com.github.rphlfc.tabnews_kotlin.model.LoginRequest
import com.github.rphlfc.tabnews_kotlin.model.LoginResponse
import com.github.rphlfc.tabnews_kotlin.security.AuthManager

internal class AuthRepositoryImpl(
    private val api: APIService,
    private val authManager: AuthManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): APIResult<LoginResponse> {
        val request = LoginRequest(email = email, password = password)
        return ErrorHandler.executeApiCall("Erro ao fazer login.") {
            val response = api.login(request)

            authManager.setToken(
                token = response.token,
                userId = response.id,
                expiresAt = response.expiresAt
            )

            response
        }
    }

    override suspend fun logout() {
        authManager.logout()
    }
}

