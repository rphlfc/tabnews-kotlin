package com.github.rphlfc.tabnews_kotlin.model

import kotlinx.serialization.Serializable

@Serializable
internal data class UserUpdateRequest(
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val description: String? = null,
    val notifications: Boolean? = null
)
