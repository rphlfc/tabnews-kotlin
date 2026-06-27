package com.github.rphlfc.tabnews_kotlin.repository

import com.github.rphlfc.tabnews_kotlin.model.User
import com.github.rphlfc.tabnews_kotlin.api.APIResult

interface UserRepository {
    suspend fun getLoggedUser(): APIResult<User>
    suspend fun getUserByUsername(username: String): APIResult<User>
    suspend fun updateUser(
        username: String,
        newUsername: String? = null,
        email: String? = null,
        password: String? = null,
        description: String? = null,
        notifications: Boolean? = null
    ): APIResult<User>
}
