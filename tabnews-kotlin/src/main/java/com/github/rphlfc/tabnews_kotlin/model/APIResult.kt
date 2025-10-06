package com.github.rphlfc.tabnews_kotlin.model

sealed class APIResult<out T> {
    data class Success<T>(val data: T) : APIResult<T>()
    data class Failure(val error: ErrorResponse) : APIResult<Nothing>()
}


