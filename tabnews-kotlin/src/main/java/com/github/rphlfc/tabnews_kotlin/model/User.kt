package com.github.rphlfc.tabnews_kotlin.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String? = null,
    val description: String = "",
    val notifications: Boolean? = null,
    val features: List<String> = emptyList(),
    val tabcoins: Int,
    val tabcash: Int,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)
