package com.github.rphlfc.tabnews_kotlin.security

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow

class AuthManager(
    private val tokenProvider: TokenProvider,
    private val ioDispatcher: CoroutineDispatcher
) {
    private val _isAuthenticated = MutableStateFlow(false)
//    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

//    fun refreshState() {
//        val token = tokenProvider.getToken()
//        _isAuthenticated.value = !token.isNullOrBlank()
//    }

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