package com.github.rphlfc.tabnews_kotlin.repository

import com.github.rphlfc.tabnews_kotlin.api.APIService
import com.github.rphlfc.tabnews_kotlin.cache.manager.CacheManager
import com.github.rphlfc.tabnews_kotlin.cache.model.CachedContent
import com.github.rphlfc.tabnews_kotlin.cache.model.CachedPostDetail
import com.github.rphlfc.tabnews_kotlin.model.APIResult
import com.github.rphlfc.tabnews_kotlin.model.CommentRequest
import com.github.rphlfc.tabnews_kotlin.model.Content
import com.github.rphlfc.tabnews_kotlin.model.ContentRequest
import com.github.rphlfc.tabnews_kotlin.model.ErrorResponse
import com.github.rphlfc.tabnews_kotlin.model.PublishStatus
import com.github.rphlfc.tabnews_kotlin.model.TabcoinsRequest
import com.github.rphlfc.tabnews_kotlin.model.TabcoinsResponse
import com.github.rphlfc.tabnews_kotlin.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.HttpException

class ContentRepositoryImpl(
    private val api: APIService,
    private val cacheManager: CacheManager
) : ContentRepository {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun parseHttpError(e: HttpException, defaultMessage: String): ErrorResponse {
        return try {
            val errorBody = e.response()?.errorBody()?.string()
            if (errorBody != null) {
                json.decodeFromString<ErrorResponse>(errorBody)
            } else {
                ErrorResponse(
                    name = "HttpError",
                    message = defaultMessage.ifEmpty { "Erro HTTP ${e.code()}: ${e.message()}" },
                    statusCode = e.code()
                )
            }
        } catch (_: Exception) {
            ErrorResponse(
                name = "HttpError",
                message = defaultMessage.ifEmpty { "Erro HTTP ${e.code()}: ${e.message()}" },
                statusCode = e.code()
            )
        }
    }

    private fun mapExceptionToError(e: Exception, defaultMessage: String): ErrorResponse {
        return when (e) {
            is HttpException -> parseHttpError(e, defaultMessage)
            else -> ErrorResponse(
                name = "UnexpectedError",
                message = e.message ?: defaultMessage,
                statusCode = -1
            )
        }
    }

    private suspend fun <T> executeApiCall(
        defaultErrorMessage: String,
        block: suspend () -> T
    ): APIResult<T> {
        return try {
            withContext(Dispatchers.IO) { APIResult.Success(block()) }
        } catch (e: Exception) {
            APIResult.Failure(mapExceptionToError(e, defaultErrorMessage))
        }
    }

    override suspend fun getContents(
        page: Int,
        perPage: Int,
        strategy: String,
        clearCache: Boolean
    ): APIResult<List<Content>> {
        if (clearCache) {
            refreshContents(strategy)
        }

        return try {
            withContext(Dispatchers.IO) {
                val cachedContents = cacheManager.getContents(strategy, page)

                if (cachedContents != null && cachedContents.isNotEmpty()) {
                    val contents = cachedContents.map { it.toContent() }
                    APIResult.Success(contents)
                } else {
                    val contents =
                        api.getContents(page = page, perPage = perPage, strategy = strategy)

                    val cachedContentsToStore = contents.map { content ->
                        CachedContent.fromContent(content, strategy, page)
                    }
                    cacheManager.cacheContents(cachedContentsToStore, strategy, page)

                    APIResult.Success(contents)
                }
            }
        } catch (e: Exception) {
            val error = mapExceptionToError(e, "Erro ao carregar conteúdos.")
            // Try serve cached data on error
            return try {
                val cachedContents = cacheManager.getContents(strategy, page)
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

    private suspend fun refreshContents(strategy: String) {
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
                refreshPostDetail(content)
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
            val error = mapExceptionToError(e, "Erro ao carregar detalhes do post.")
            // Try serve cached data on error
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

    private suspend fun refreshPostDetail(content: Content) {
        withContext(Dispatchers.IO) {
            cacheManager.invalidateCache(content)
        }
    }

    override suspend fun getComments(
        ownerUsername: String,
        slug: String
    ): APIResult<List<Content>> {
        return executeApiCall("Erro ao carregar comentários.") {
            api.getComments(ownerUsername, slug)
        }
    }

    override suspend fun tabcoins(
        ownerUsername: String,
        slug: String,
        transactionType: TransactionType
    ): APIResult<TabcoinsResponse> {
        return executeApiCall("Erro ao votar. Tente novamente.") {
            val request = TabcoinsRequest(transactionType = transactionType.rawValue)
            api.voteOnContent(
                ownerUsername = ownerUsername,
                slug = slug,
                request = request
            )
        }
    }

    override suspend fun createComment(
        parent: Content,
        body: String,
        status: PublishStatus
    ): APIResult<Content> {
        return executeApiCall("Erro ao criar comentário.") {
            val request = CommentRequest(
                parentId = parent.id,
                body = body,
                status = status.rawValue
            )
            api.createComment(request)
        }
    }

    override suspend fun createContent(request: ContentRequest): APIResult<Content> {
        return executeApiCall("Erro ao criar conteúdo.") {
            api.createContent(request)
        }
    }
}