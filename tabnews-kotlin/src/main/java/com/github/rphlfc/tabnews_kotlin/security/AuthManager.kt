package com.github.rphlfc.tabnews_kotlin.security

import kotlinx.coroutines.flow.MutableStateFlow

class AuthManager(
    private val tokenProvider: TokenProvider
) {
    private val _isAuthenticated = MutableStateFlow(false)

    fun setToken(token: String, userId: String, expiresAt: String) {
        tokenProvider.setToken(token, userId, expiresAt)
    }

    fun getToken(): String? {
        return tokenProvider.getToken()
    }

    fun logout() {
        tokenProvider.clearToken()
        _isAuthenticated.value = false
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}