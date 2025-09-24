package com.github.rphlfc.tabnews_kotlin.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentRequest(
    val title: String,
    val body: String,
    val status: String = "published",
    @SerialName("source_url")
    val sourceUrl: String? = null,
    val slug: String? = null
)
