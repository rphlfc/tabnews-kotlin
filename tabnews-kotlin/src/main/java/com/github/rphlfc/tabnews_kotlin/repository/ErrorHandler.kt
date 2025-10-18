package com.github.rphlfc.tabnews_kotlin.repository

import com.github.rphlfc.tabnews_kotlin.model.APIResult
import com.github.rphlfc.tabnews_kotlin.model.ErrorResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import retrofit2.HttpException

internal object ErrorHandler {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun parseHttpError(e: HttpException, defaultMessage: String): ErrorResponse {
        return try {
            val errorBody = e.response()?.errorBody()?.string()
            if (errorBody != null) {
                json.decodeFromString<ErrorResponse>(errorBody)
            } else {
                ErrorResponse(
                    name = "HttpError",
                    message = defaultMessage.ifEmpty { "Erro HTTP ${e.code()}: ${e.message()}" },
                    statusCode = e.code()
                )
            }
        } catch (_: Exception) {
            ErrorResponse(
                name = "HttpError",
                message = defaultMessage.ifEmpty { "Erro HTTP ${e.code()}: ${e.message()}" },
                statusCode = e.code()
            )
        }
    }

    fun mapExceptionToError(e: Exception, defaultMessage: String): ErrorResponse {
        return when (e) {
            is HttpException -> parseHttpError(e, defaultMessage)
            else -> ErrorResponse(
                name = "UnexpectedError",
                message = e.message ?: defaultMessage,
                statusCode = -1
            )
        }
    }

    suspend fun <T> executeApiCall(
        defaultErrorMessage: String,
        block: suspend () -> T
    ): APIResult<T> {
        return try {
            withContext(Dispatchers.IO) {
                APIResult.Success(block())
            }
        } catch (e: Exception) {
            APIResult.Failure(mapExceptionToError(e, defaultErrorMessage))
        }
    }
}

