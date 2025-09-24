package com.github.rphlfc.tabnews_kotlin.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TabcoinsRequest(
    @SerialName("transaction_type")
    val transactionType: String
)
