package com.github.rphlfc.tabnews_kotlin.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    val description: String,
    val notifications: Boolean,
    val features: List<String>,
    val tabcoins: Int,
    val tabcash: Int,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)
