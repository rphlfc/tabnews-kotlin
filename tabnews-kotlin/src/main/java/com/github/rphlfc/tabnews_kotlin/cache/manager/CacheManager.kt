package com.github.rphlfc.tabnews_kotlin.cache.manager

import com.github.rphlfc.tabnews_kotlin.cache.CacheDatabase
import com.github.rphlfc.tabnews_kotlin.cache.model.CachedContent
import com.github.rphlfc.tabnews_kotlin.cache.model.CachedMetadata
import com.github.rphlfc.tabnews_kotlin.cache.model.CachedPostDetail
import com.github.rphlfc.tabnews_kotlin.model.Content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CacheManager(
    database: CacheDatabase
) {
    private val contentCacheDao = database.contentCacheDao()
    private val metadataDao = database.cacheMetadataDao()
    private val postDetailCacheDao = database.postDetailCacheDao()

    companion object {
        private const val CACHE_EXPIRY_TIME = 5 * 60 * 1000L
        private const val MAX_CACHE_SIZE = 1000
    }

    suspend fun getContents(strategy: String, page: Int): List<CachedContent>? {
        return withContext(Dispatchers.IO) {
            val cacheKey = "$strategy:$page"

            val metadata = metadataDao.getMetadataByKey(cacheKey)
            if (metadata != null && metadata.expires_at > System.currentTimeMillis()) {
                val contents = contentCacheDao.getContentsByStrategyAndPage(strategy, page)
                if (contents.isNotEmpty()) {
                    return@withContext contents
                }
            }

            return@withContext null
        }
    }

    suspend fun cacheContents(contents: List<CachedContent>, strategy: String, page: Int) {
        withContext(Dispatchers.IO) {
            val cacheKey = "$strategy:$page"

            contentCacheDao.insertContents(contents)

            val metadata = CachedMetadata(
                key = cacheKey,
                strategy = strategy,
                page = page,
                content_count = contents.size
            )
            metadataDao.insertMetadata(metadata)

            cleanupOldCache()
        }
    }

    suspend fun getPostDetailByUsernameAndSlug(
        ownerUsername: String,
        slug: String
    ): CachedPostDetail? {
        postDetailCacheDao.getPostDetailByUsernameAndSlug(ownerUsername, slug)?.let {
            val cacheAge = System.currentTimeMillis() - it.cached_at
            if (cacheAge < CACHE_EXPIRY_TIME) {
                return it
            }
        }
        return null
    }

    suspend fun cachePostDetail(cachedPostDetailToStore: CachedPostDetail) {
        postDetailCacheDao.insertPostDetail(cachedPostDetailToStore)
    }

    suspend fun invalidateCache(strategy: String) {
        withContext(Dispatchers.IO) {
            contentCacheDao.deleteContentsByStrategy(strategy)
            metadataDao.deleteMetadataByStrategy(strategy)
        }
    }

    suspend fun invalidateCache(content: Content) {
        withContext(Dispatchers.IO) {
            postDetailCacheDao.deletePostDetailById(content.id)
        }
    }

    suspend fun clearAllCache() {
        withContext(Dispatchers.IO) {
            contentCacheDao.deleteAllContents()
            metadataDao.deleteAllMetadata()
            postDetailCacheDao.deleteAllPostDetails()
        }
    }

    suspend fun cleanupExpiredCache() {
        withContext(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            contentCacheDao.deleteExpiredContents(currentTime - CACHE_EXPIRY_TIME)
            metadataDao.deleteExpiredMetadata(currentTime)
            postDetailCacheDao.deleteExpiredPostDetails(currentTime)
        }
    }

    private suspend fun cleanupOldCache() {
        val totalCount = contentCacheDao.getContentCountByStrategy("relevant") +
                contentCacheDao.getContentCountByStrategy("new")

        if (totalCount > MAX_CACHE_SIZE) {
            val expiredTime = System.currentTimeMillis() - (CACHE_EXPIRY_TIME * 2)
            contentCacheDao.deleteExpiredContents(expiredTime)
            metadataDao.deleteExpiredMetadata(expiredTime)
            postDetailCacheDao.deleteExpiredPostDetails(expiredTime)
        }
    }
}