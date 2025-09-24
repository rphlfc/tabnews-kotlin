package com.github.rphlfc.tabnews_kotlin.repository

import com.github.rphlfc.tabnews_kotlin.api.APIService
import com.github.rphlfc.tabnews_kotlin.cache.manager.CacheManager
import com.github.rphlfc.tabnews_kotlin.cache.model.CachedContent
import com.github.rphlfc.tabnews_kotlin.cache.model.CachedPostDetail
import com.github.rphlfc.tabnews_kotlin.model.CommentRequest
import com.github.rphlfc.tabnews_kotlin.model.Content
import com.github.rphlfc.tabnews_kotlin.model.ContentRequest
import com.github.rphlfc.tabnews_kotlin.model.TabcoinsRequest
import com.github.rphlfc.tabnews_kotlin.model.TransactionType
import com.github.rphlfc.tabnews_kotlin.security.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ContentRepository {
    suspend fun getContents(
        page: Int,
        perPage: Int = 20,
        strategy: String = "relevant",
        clearCache: Boolean = false
    ): Result<List<Content>>

    suspend fun getPostDetail(
        ownerUsername: String,
        slug: String,
        clearCache: Boolean = false
    ): Result<Content>

    suspend fun getComments(ownerUsername: String, slug: String): Result<List<Content>>

    suspend fun tabcoins(
        ownerUsername: String,
        slug: String,
        transactionType: TransactionType
    ): Result<Unit>

    suspend fun createContent(contentRequest: ContentRequest): Result<Content>

    suspend fun createComment(commentRequest: CommentRequest): Result<Content>
}

class ContentRepositoryImpl(
    private val api: APIService,
    private val cacheManager: CacheManager
) : ContentRepository {

    override suspend fun getContents(
        page: Int,
        perPage: Int,
        strategy: String,
        clearCache: Boolean
    ): Result<List<Content>> {
        if (clearCache) {
            refreshContents(strategy)
        }

        return try {
            withContext(Dispatchers.IO) {
                val cachedContents = cacheManager.getContents(strategy, page)

                if (cachedContents != null && cachedContents.isNotEmpty()) {
                    val contents = cachedContents.map { it.toContent() }
                    Result.success(contents)
                } else {
                    val contents =
                        api.getContents(page = page, perPage = perPage, strategy = strategy)

                    val cachedContentsToStore = contents.map { content ->
                        CachedContent.fromContent(content, strategy, page)
                    }
                    cacheManager.cacheContents(cachedContentsToStore, strategy, page)

                    Result.success(contents)
                }
            }
        } catch (e: Exception) {
            try {
                val cachedContents = cacheManager.getContents(strategy, page)
                if (cachedContents != null && cachedContents.isNotEmpty()) {
                    val contents = cachedContents.map { it.toContent() }
                    Result.success(contents)
                } else {
                    Result.failure(e)
                }
            } catch (_: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun refreshContents(strategy: String) {
        withContext(Dispatchers.IO) {
            cacheManager.invalidateCache(strategy)
        }
    }

    override suspend fun getPostDetail(
        ownerUsername: String,
        slug: String,
        clearCache: Boolean
    ): Result<Content> {
        if (clearCache) {
            cacheManager.getPostDetailByUsernameAndSlug(ownerUsername, slug)?.let {
                val content = it.toContent()
                refreshPostDetail(content)
            }
        }

        return try {
            withContext(Dispatchers.IO) {
                val cachedPostDetail =
                    cacheManager.getPostDetailByUsernameAndSlug(ownerUsername, slug)

                if (cachedPostDetail != null) {
                    val content = cachedPostDetail.toContent()
                    Result.success(content)

                } else {
                    val content = api.getContentDetail(ownerUsername, slug)

                    val cachedPostDetailToStore = CachedPostDetail.fromContent(content)
                    cacheManager.cachePostDetail(cachedPostDetailToStore)

                    Result.success(content)
                }
            }
        } catch (e: Exception) {
            try {
                val cachedPostDetail =
                    cacheManager.getPostDetailByUsernameAndSlug(ownerUsername, slug)
                if (cachedPostDetail != null) {
                    val content = cachedPostDetail.toContent()
                    Result.success(content)
                } else {
                    Result.failure(e)
                }
            } catch (_: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun refreshPostDetail(content: Content) {
        withContext(Dispatchers.IO) {
            cacheManager.invalidateCache(content)
        }
    }

    override suspend fun getComments(ownerUsername: String, slug: String): Result<List<Content>> {
        return try {
            withContext(Dispatchers.IO) {
                val comments = api.getComments(ownerUsername, slug)
                Result.success(comments)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun tabcoins(
        ownerUsername: String,
        slug: String,
        transactionType: TransactionType
    ): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                val tabcoinsRequest = TabcoinsRequest(transactionType = transactionType.rawValue)

                api.voteOnContent(
                    ownerUsername = ownerUsername,
                    slug = slug,
                    tabcoinsRequest = tabcoinsRequest
                )

                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createContent(contentRequest: ContentRequest): Result<Content> {
        return try {
            withContext(Dispatchers.IO) {
                val content = api.createContent(contentRequest)
                Result.success(content)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createComment(commentRequest: CommentRequest): Result<Content> {
        return try {
            withContext(Dispatchers.IO) {
                val content = api.createComment(commentRequest)
                Result.success(content)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
