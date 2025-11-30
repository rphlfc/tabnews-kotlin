package com.github.rphlfc.tabnews_kotlin.repository

import com.github.rphlfc.tabnews_kotlin.api.APIResult
import com.github.rphlfc.tabnews_kotlin.model.Content
import com.github.rphlfc.tabnews_kotlin.model.PublishStatus
import com.github.rphlfc.tabnews_kotlin.model.Strategy
import com.github.rphlfc.tabnews_kotlin.model.TabcoinsResponse
import com.github.rphlfc.tabnews_kotlin.model.TransactionType

interface ContentRepository {
    suspend fun getContents(
        page: Int = 1,
        perPage: Int = 20,
        strategy: Strategy = Strategy.RELEVANT,
        clearCache: Boolean = false
    ): APIResult<List<Content>>

    suspend fun getPostDetail(
        ownerUsername: String,
        slug: String,
        clearCache: Boolean = false
    ): APIResult<Content>

    suspend fun getComments(
        ownerUsername: String,
        slug: String
    ): APIResult<List<Content>>

    suspend fun tabcoins(
        ownerUsername: String,
        slug: String,
        transactionType: TransactionType
    ): APIResult<TabcoinsResponse>

    suspend fun createComment(
        parent: Content,
        body: String,
        status: PublishStatus = PublishStatus.PUBLISHED
    ): APIResult<Content>

    suspend fun createContent(
        title: String,
        body: String,
        slug: String? = null,
        sourceUrl: String? = null,
        status: PublishStatus = PublishStatus.PUBLISHED
    ): APIResult<Content>
}