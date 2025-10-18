package com.github.rphlfc.tabnews_kotlin.repository

import com.github.rphlfc.tabnews_kotlin.api.APIService
import com.github.rphlfc.tabnews_kotlin.cache.manager.CacheManager
import com.github.rphlfc.tabnews_kotlin.cache.model.CachedContent
import com.github.rphlfc.tabnews_kotlin.cache.model.CachedPostDetail
import com.github.rphlfc.tabnews_kotlin.model.APIResult
import com.github.rphlfc.tabnews_kotlin.model.CommentRequest
import com.github.rphlfc.tabnews_kotlin.model.Content
import com.github.rphlfc.tabnews_kotlin.model.ContentRequest
import com.github.rphlfc.tabnews_kotlin.model.PublishStatus
import com.github.rphlfc.tabnews_kotlin.model.Strategy
import com.github.rphlfc.tabnews_kotlin.model.TabcoinsRequest
import com.github.rphlfc.tabnews_kotlin.model.TabcoinsResponse
import com.github.rphlfc.tabnews_kotlin.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ContentRepositoryImpl(
    private val api: APIService,
    private val cacheManager: CacheManager
) : ContentRepository {

    override suspend fun getContents(
        page: Int,
        perPage: Int,
        strategy: Strategy,
        clearCache: Boolean
    ): APIResult<List<Content>> {
        if (clearCache) {
            clearContentsCache(strategy.param)
        }

        return try {
            withContext(Dispatchers.IO) {
                val cachedContents = cacheManager.getContents(strategy.param, page)

                if (cachedContents != null && cachedContents.isNotEmpty()) {
                    val contents = cachedContents.map { it.toContent() }
                    APIResult.Success(contents)
                } else {
                    val contents =
                        api.getContents(page = page, perPage = perPage, strategy = strategy.param)

                    val cachedContentsToStore = contents.map { content ->
                        CachedContent.fromContent(content, strategy.param, page)
                    }
                    cacheManager.cacheContents(cachedContentsToStore, strategy.param, page)

                    APIResult.Success(contents)
                }
            }
        } catch (e: Exception) {
            val error = ErrorHandler.mapExceptionToError(e, "Erro ao carregar conteúdos.")
            return try {
                val cachedContents = cacheManager.getContents(strategy.param, page)
                if (cachedContents != null && cachedContents.isNotEmpty()) {
                    val contents = cachedContents.map { it.toContent() }
                    APIResult.Success(contents)
                } else {
                    APIResult.Failure(error)
                }
            } catch (_: Exception) {
                APIResult.Failure(error)
            }
        }
    }

    private suspend fun clearContentsCache(strategy: String) {
        withContext(Dispatchers.IO) {
            cacheManager.invalidateCache(strategy)
        }
    }

    override suspend fun getPostDetail(
        ownerUsername: String,
        slug: String,
        clearCache: Boolean
    ): APIResult<Content> {
        if (clearCache) {
            cacheManager.getPostDetailByUsernameAndSlug(ownerUsername, slug)?.let {
                val content = it.toContent()
                clearPostDetailCache(content)
            }
        }

        return try {
            withContext(Dispatchers.IO) {
                val cachedPostDetail =
                    cacheManager.getPostDetailByUsernameAndSlug(ownerUsername, slug)

                if (cachedPostDetail != null) {
                    val content = cachedPostDetail.toContent()
                    APIResult.Success(content)

                } else {
                    val content = api.getContentDetail(ownerUsername, slug)

                    val cachedPostDetailToStore = CachedPostDetail.fromContent(content)
                    cacheManager.cachePostDetail(cachedPostDetailToStore)

                    APIResult.Success(content)
                }
            }
        } catch (e: Exception) {
            val error = ErrorHandler.mapExceptionToError(e, "Erro ao carregar detalhes do post.")
            return try {
                val cachedPostDetail =
                    cacheManager.getPostDetailByUsernameAndSlug(ownerUsername, slug)
                if (cachedPostDetail != null) {
                    val content = cachedPostDetail.toContent()
                    APIResult.Success(content)
                } else {
                    APIResult.Failure(error)
                }
            } catch (_: Exception) {
                APIResult.Failure(error)
            }
        }
    }

    private suspend fun clearPostDetailCache(content: Content) {
        withContext(Dispatchers.IO) {
            cacheManager.invalidateCache(content)
        }
    }

    override suspend fun getComments(
        ownerUsername: String,
        slug: String
    ): APIResult<List<Content>> {
        return ErrorHandler.executeApiCall("Erro ao carregar comentários.") {
            api.getComments(ownerUsername, slug)
        }
    }

    override suspend fun tabcoins(
        ownerUsername: String,
        slug: String,
        transactionType: TransactionType
    ): APIResult<TabcoinsResponse> {
        val request = TabcoinsRequest(transactionType = transactionType.rawValue)
        return ErrorHandler.executeApiCall("Erro ao votar. Tente novamente.") {
            api.voteOnContent(ownerUsername = ownerUsername, slug = slug, request = request)
        }
    }

    override suspend fun createComment(
        parent: Content,
        body: String,
        status: PublishStatus
    ): APIResult<Content> {
        val request = CommentRequest(parentId = parent.id, body = body, status = status.rawValue)
        return ErrorHandler.executeApiCall("Erro ao criar comentário.") {
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
        return ErrorHandler.executeApiCall("Erro ao criar conteúdo.") {
            api.createContent(request)
        }
    }
}