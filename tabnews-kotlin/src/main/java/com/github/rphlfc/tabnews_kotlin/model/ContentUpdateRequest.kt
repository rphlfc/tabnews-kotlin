package com.github.rphlfc.tabnews_kotlin.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ContentUpdateRequest(
    val title: String? = null,
    val body: String? = null,
    val status: String? = null,
    @SerialName("source_url")
    val sourceUrl: String? = null,
    val slug: String? = null
)
