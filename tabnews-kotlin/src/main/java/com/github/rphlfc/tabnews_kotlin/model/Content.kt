package com.github.rphlfc.tabnews_kotlin.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
data class Content(
    val id: String,
    @SerialName("owner_id")
    val ownerId: String,
    @SerialName("parent_id")
    val parentId: String?,
    val slug: String,
    val title: String? = null,
    val body: String? = null,
    val status: String,
    @SerialName("source_url")
    val sourceUrl: String?,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("published_at")
    val publishedAt: String?,
    @SerialName("deleted_at")
    val deletedAt: String?,
    val type: String? = null,
    val tabcoins: Int,
    @SerialName("tabcoins_credit")
    val tabcoinsCredit: Int? = null,
    @SerialName("tabcoins_debit")
    val tabcoinsDebit: Int? = null,
    @SerialName("owner_username")
    val ownerUsername: String,
    val children: List<Content>? = null,
    @SerialName("children_deep_count")
    val childrenDeepCount: Int
) : JavaSerializable
