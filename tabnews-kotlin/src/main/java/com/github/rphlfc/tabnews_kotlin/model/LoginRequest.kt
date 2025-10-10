package com.github.rphlfc.tabnews_kotlin.model

import kotlinx.serialization.Serializable

@Serializable
internal data class LoginRequest(
    val email: String,
    val password: String
)
