package com.github.rphlfc.tabnews_kotlin.repository

import com.github.rphlfc.tabnews_kotlin.api.APIService
import com.github.rphlfc.tabnews_kotlin.api.APIResult
import com.github.rphlfc.tabnews_kotlin.api.APIRequest
import com.github.rphlfc.tabnews_kotlin.model.User
import com.github.rphlfc.tabnews_kotlin.model.UserUpdateRequest

internal class UserRepositoryImpl(
    private val api: APIService
) : UserRepository {

    override suspend fun getLoggedUser(): APIResult<User> {
        return APIRequest.executeApiCall("Erro ao carregar perfil") {
            api.getUserProfile()
        }
    }

    override suspend fun getUserByUsername(username: String): APIResult<User> {
        return APIRequest.executeApiCall("Erro ao carregar perfil do usuário") {
            api.getUserByUsername(username)
        }
    }

    override suspend fun updateUser(
        username: String,
        newUsername: String?,
        email: String?,
        password: String?,
        description: String?,
        notifications: Boolean?
    ): APIResult<User> {
        val request = UserUpdateRequest(
            username = newUsername,
            email = email,
            password = password,
            description = description,
            notifications = notifications
        )
        return APIRequest.executeApiCall("Erro ao atualizar perfil") {
            api.updateUser(username, request)
        }
    }
}

