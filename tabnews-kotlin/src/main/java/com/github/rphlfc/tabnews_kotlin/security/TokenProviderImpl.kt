package com.github.rphlfc.tabnews_kotlin.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenProviderImpl(context: Context) : TokenProvider {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_tokens",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TOKEN_EXPIRES_AT = "token_expires_at"
    }

    override fun setToken(token: String, userId: String, expiresAt: String) {
        sharedPreferences.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_TOKEN_EXPIRES_AT, expiresAt)
            .apply()
    }

    private fun getAuthTokenFromPreferences(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    private fun getTokenExpiresAt(): String? {
        return sharedPreferences.getString(KEY_TOKEN_EXPIRES_AT, null)
    }

    override fun clearToken() {
        sharedPreferences.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_TOKEN_EXPIRES_AT)
            .apply()
    }

    private fun isTokenExpired(): Boolean {
        val expiresAt = getTokenExpiresAt() ?: return true

        return try {
            // Parse ISO 8601 date format
            val expiresTime = java.time.Instant.parse(expiresAt).toEpochMilli()
            val currentTime = System.currentTimeMillis()
            currentTime >= expiresTime
        } catch (e: Exception) {
            // If we can't parse the date, consider it expired
            true
        }
    }

    override fun getToken(): String? {
        val authToken = getAuthTokenFromPreferences() ?: return null
        
        return if (!isTokenExpired()) {
            authToken
        } else {
            clearToken()
            null
        }
    }
}
