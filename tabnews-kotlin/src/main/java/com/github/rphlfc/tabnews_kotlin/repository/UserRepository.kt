package com.github.rphlfc.tabnews_kotlin.repository

import com.github.rphlfc.tabnews_kotlin.api.APIService
import com.github.rphlfc.tabnews_kotlin.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface UserRepository {
    suspend fun getLoggedUser(): Result<User>
}

class UserRepositoryImpl(
    private val api: APIService
) : UserRepository {

    override suspend fun getLoggedUser(): Result<User> {
        return try {
            withContext(Dispatchers.IO) {
                val profile = api.getUserProfile()
                Result.success(profile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
