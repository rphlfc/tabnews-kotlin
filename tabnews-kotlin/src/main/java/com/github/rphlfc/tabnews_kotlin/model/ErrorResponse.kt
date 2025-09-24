package com.github.rphlfc.tabnews_kotlin.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ErrorResponse(
    val name: String,
    val message: String,
    val action: String? = null,
    @SerialName("status_code")
    val statusCode: Int,
    @SerialName("error_id")
    val errorId: String? = null,
    @SerialName("request_id")
    val requestId: String? = null
)
