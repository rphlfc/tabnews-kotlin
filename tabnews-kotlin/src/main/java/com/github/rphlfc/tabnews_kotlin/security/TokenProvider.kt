package com.github.rphlfc.tabnews_kotlin.security

interface TokenProvider {
    fun getToken(): String?
    fun setToken(token: String, userId: String, expiresAt: String)
    fun clearToken()
}

