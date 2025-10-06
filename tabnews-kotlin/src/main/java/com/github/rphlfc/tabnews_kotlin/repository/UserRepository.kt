package com.github.rphlfc.tabnews_kotlin.repository

import com.github.rphlfc.tabnews_kotlin.api.APIService
import com.github.rphlfc.tabnews_kotlin.model.User
import com.github.rphlfc.tabnews_kotlin.model.ErrorResponse
import com.github.rphlfc.tabnews_kotlin.model.APIResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import retrofit2.HttpException

interface UserRepository {
    suspend fun getLoggedUser(): APIResult<User>
}

class UserRepositoryImpl(
    private val api: APIService
) : UserRepository {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun getLoggedUser(): APIResult<User> {
        return try {
            withContext(Dispatchers.IO) {
                val profile = api.getUserProfile()
                APIResult.Success(profile)
            }
        } catch (e: Exception) {
            val error = when (e) {
                is HttpException -> {
                    try {
                        val errorBody = e.response()?.errorBody()?.string()
                        if (errorBody != null) {
                            json.decodeFromString<ErrorResponse>(errorBody)
                        } else {
                            ErrorResponse(
                                name = "HttpError",
                                message = "Erro HTTP ${e.code()}: ${e.message()}",
                                statusCode = e.code()
                            )
                        }
                    } catch (_: Exception) {
                        ErrorResponse(
                            name = "HttpError",
                            message = "Erro HTTP ${e.code()}: ${e.message()}",
                            statusCode = e.code()
                        )
                    }
                }
                else -> ErrorResponse(
                    name = "UnexpectedError",
                    message = e.message ?: "Erro ao carregar perfil",
                    statusCode = -1
                )
            }
            APIResult.Failure(error)
        }
    }
}
