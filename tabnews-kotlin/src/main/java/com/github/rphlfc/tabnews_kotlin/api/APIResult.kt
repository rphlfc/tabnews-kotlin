package com.github.rphlfc.tabnews_kotlin.api

import com.github.rphlfc.tabnews_kotlin.model.ErrorResponse

sealed class APIResult<out T> {
    data class Success<T>(val data: T) : APIResult<T>()
    data class Failure(val error: ErrorResponse) : APIResult<Nothing>()

    inline fun onSuccess(action: (T) -> Unit): APIResult<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }

    inline fun onFailure(action: (ErrorResponse) -> Unit): APIResult<T> {
        if (this is Failure) {
            action(error)
        }
        return this
    }
}