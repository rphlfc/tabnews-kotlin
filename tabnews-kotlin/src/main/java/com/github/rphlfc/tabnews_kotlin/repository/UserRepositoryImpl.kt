package com.github.rphlfc.tabnews_kotlin.repository

import com.github.rphlfc.tabnews_kotlin.api.APIService
import com.github.rphlfc.tabnews_kotlin.model.APIResult
import com.github.rphlfc.tabnews_kotlin.model.User

internal class UserRepositoryImpl(
    private val api: APIService
) : UserRepository {

    override suspend fun getLoggedUser(): APIResult<User> {
        return ErrorHandler.executeApiCall("Erro ao carregar perfil") {
            api.getUserProfile()
        }
    }
}

