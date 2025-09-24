package com.github.rphlfc.tabnews_kotlin.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentRequest(
    @SerialName("parent_id")
    val parentId: String,
    val body: String,
    val status: String = "published"
)
