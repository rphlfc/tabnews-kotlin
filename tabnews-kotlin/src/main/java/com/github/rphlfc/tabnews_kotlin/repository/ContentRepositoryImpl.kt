package com.github.rphlfc.tabnews_kotlin.repository

import com.github.rphlfc.tabnews_kotlin.api.APIRequest
import com.github.rphlfc.tabnews_kotlin.api.APIResult
import com.github.rphlfc.tabnews_kotlin.api.APIService
import com.github.rphlfc.tabnews_kotlin.cache.manager.CacheManager
import com.github.rphlfc.tabnews_kotlin.cache.model.CachedContent
import com.github.rphlfc.tabnews_kotlin.cache.model.CachedPostDetail
import com.github.rphlfc.tabnews_kotlin.model.CommentRequest
import com.github.rphlfc.tabnews_kotlin.model.Content
import com.github.rphlfc.tabnews_kotlin.model.ContentRequest
import com.github.rphlfc.tabnews_kotlin.model.PublishStatus
import com.github.rphlfc.tabnews_kotlin.model.Strategy
import com.github.rphlfc.tabnews_kotlin.model.TabcoinsRequest
import com.github.rphlfc.tabnews_kotlin.model.TabcoinsResponse
import com.github.rphlfc.tabnews_kotlin.model.TransactionType

internal class ContentRepositoryImpl(
    private val api: APIService,
    private val cacheManager: CacheManager
) : ContentRepository {

    private suspend fun <T> APIResult<T>.getOrFallbackToCache(
        cacheProvider: suspend () -> T?
    ): APIResult<T> {
        return if (this is APIResult.Failure) {
            val cachedData = cacheProvider()
            if (cachedData != null) {
                APIResult.Success(cachedData)
            } else {
                this
            }
        } else {
            this
        }
    }

    override suspend fun getContents(
        page: Int,
        perPage: Int,
        strategy: Strategy,
        clearCache: Boolean
    ): APIResult<List<Content>> {
        if (clearCache) {
            clearContentsCache(strategy.param)
        }

        if (!clearCache) {
            val cachedContents = cacheManager.getContents(strategy.param, page)
            if (cachedContents != null && cachedContents.isNotEmpty()) {
                val contents = cachedContents.map { it.toContent() }
                return APIResult.Success(contents)
            }
        }

        val result = APIRequest.executeApiCall("Erro ao carregar conteúdos.") {
            val contents =
                api.getContents(page = page, perPage = perPage, strategy = strategy.param)

            val cachedContentsToStore = contents.map { content ->
                CachedContent.fromContent(content, strategy.param, page)
            }
            cacheManager.cacheContents(cachedContentsToStore, strategy.param, page)

            contents
        }

        return result.getOrFallbackToCache {
            getCachedContents(strategy.param, page)
        }
    }

    private suspend fun getCachedContents(strategy: String, page: Int): List<Content>? {
        return try {
            val cachedContents = cacheManager.getContents(strategy, page)
            if (cachedContents != null && cachedContents.isNotEmpty()) {
                cachedContents.map { it.toContent() }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun clearContentsCache(strategy: String) {
        cacheManager.invalidateCache(strategy)
    }

    override suspend fun getPostDetail(
        ownerUsername: String,
        slug: String,
        clearCache: Boolean
    ): APIResult<Content> {
        if (clearCache) {
            clearPostDetailCache(ownerUsername, slug)
        }

        if (!clearCache) {
            val cachedPostDetail = cacheManager.getPostDetailByUsernameAndSlug(ownerUsername, slug)
            if (cachedPostDetail != null) {
                val content = cachedPostDetail.toContent()
                return APIResult.Success(content)
            }
        }

        val result = APIRequest.executeApiCall("Erro ao carregar detalhes do post.") {
            val content = api.getContentDetail(ownerUsername, slug)

            val cachedPostDetailToStore = CachedPostDetail.fromContent(content)
            cacheManager.cachePostDetail(cachedPostDetailToStore)

            content
        }

        return result.getOrFallbackToCache {
            getCachedPostDetail(ownerUsername, slug)
        }
    }

    private suspend fun getCachedPostDetail(ownerUsername: String, slug: String): Content? {
        return try {
            val cachedPostDetail = cacheManager.getPostDetailByUsernameAndSlug(ownerUsername, slug)
            cachedPostDetail?.toContent()
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun clearPostDetailCache(ownerUsername: String, slug: String) {
        cacheManager.getPostDetailByUsernameAndSlug(ownerUsername, slug)?.let {
            val content = it.toContent()
            cacheManager.invalidateCache(content)
        }
    }

    override suspend fun getComments(
        ownerUsername: String,
        slug: String
    ): APIResult<List<Content>> {
        return APIRequest.executeApiCall("Erro ao carregar comentários.") {
            api.getComments(ownerUsername, slug)
        }
    }

    override suspend fun tabcoins(
        ownerUsername: String,
        slug: String,
        transactionType: TransactionType
    ): APIResult<TabcoinsResponse> {
        val request = TabcoinsRequest(transactionType = transactionType.rawValue)
        return APIRequest.executeApiCall("Erro ao votar. Tente novamente.") {
            api.voteOnContent(ownerUsername = ownerUsername, slug = slug, request = request)
        }
    }

    override suspend fun createComment(
        parent: Content,
        body: String,
        status: PublishStatus
    ): APIResult<Content> {
        val request = CommentRequest(parentId = parent.id, body = body, status = status.rawValue)
        return APIRequest.executeApiCall("Erro ao criar comentário.") {
            api.createComment(request)
        }
    }

    override suspend fun createContent(
        title: String,
        body: String,
        slug: String?,
        sourceUrl: String?,
        status: PublishStatus
    ): APIResult<Content> {
        val request = ContentRequest(title, body, status.rawValue, sourceUrl, slug)
        return APIRequest.executeApiCall("Erro ao criar conteúdo.") {
            api.createContent(request)
        }
    }
}