package com.github.rphlfc.tabnews_kotlin.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.rphlfc.tabnews_kotlin.cache.model.CachedContent

@Dao
interface ContentCacheDao {

    @Query("SELECT * FROM cached_content WHERE strategy = :strategy AND page = :page ORDER BY cached_at DESC")
    suspend fun getContentsByStrategyAndPage(strategy: String, page: Int): List<CachedContent>

    @Query("SELECT * FROM cached_content WHERE strategy = :strategy ORDER BY page ASC, cached_at DESC")
    suspend fun getAllContentsByStrategy(strategy: String): List<CachedContent>

    @Query("SELECT * FROM cached_content WHERE id = :contentId")
    suspend fun getContentById(contentId: String): CachedContent?

    @Query("SELECT * FROM cached_content WHERE strategy = :strategy AND page <= :maxPage ORDER BY page ASC, cached_at DESC")
    suspend fun getContentsUpToPage(strategy: String, maxPage: Int): List<CachedContent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: CachedContent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContents(contents: List<CachedContent>)

    @Query("DELETE FROM cached_content WHERE strategy = :strategy AND page = :page")
    suspend fun deleteContentsByStrategyAndPage(strategy: String, page: Int)

    @Query("DELETE FROM cached_content WHERE strategy = :strategy")
    suspend fun deleteContentsByStrategy(strategy: String)

    @Query("DELETE FROM cached_content WHERE cached_at < :expiredTime")
    suspend fun deleteExpiredContents(expiredTime: Long)

    @Query("DELETE FROM cached_content")
    suspend fun deleteAllContents()

    @Query("SELECT COUNT(*) FROM cached_content WHERE strategy = :strategy")
    suspend fun getContentCountByStrategy(strategy: String): Int

    @Query("SELECT MAX(page) FROM cached_content WHERE strategy = :strategy")
    suspend fun getMaxPageByStrategy(strategy: String): Int?
}