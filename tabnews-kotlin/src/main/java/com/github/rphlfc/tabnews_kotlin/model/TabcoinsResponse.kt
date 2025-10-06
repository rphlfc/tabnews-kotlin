package com.github.rphlfc.tabnews_kotlin.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TabcoinsResponse(
    @SerialName("tabcoins") val tabcoins: Int,
    @SerialName("tabcoins_credit") val tabcoinsCredit: Int,
    @SerialName("tabcoins_debit") val tabcoinsDebit: Int
)


