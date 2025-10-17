package com.github.rphlfc.tabnews_kotlin.repository

import com.github.rphlfc.tabnews_kotlin.model.APIResult
import com.github.rphlfc.tabnews_kotlin.model.LoginResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): APIResult<LoginResponse>
    suspend fun logout()
}
