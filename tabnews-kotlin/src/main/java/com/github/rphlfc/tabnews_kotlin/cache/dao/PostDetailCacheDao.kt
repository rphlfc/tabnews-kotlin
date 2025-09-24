package com.github.rphlfc.tabnews_kotlin.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.rphlfc.tabnews_kotlin.cache.model.CachedPostDetail

@Dao
interface PostDetailCacheDao {

    @Query("SELECT * FROM cached_post_detail WHERE id = :contentId")
    suspend fun getPostDetailById(contentId: String): CachedPostDetail?

    @Query("SELECT * FROM cached_post_detail WHERE owner_username = :ownerUsername AND slug = :slug")
    suspend fun getPostDetailByUsernameAndSlug(
        ownerUsername: String,
        slug: String
    ): CachedPostDetail?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPostDetail(postDetail: CachedPostDetail)

    @Query("DELETE FROM cached_post_detail WHERE id = :contentId")
    suspend fun deletePostDetailById(contentId: String)

    @Query("DELETE FROM cached_post_detail WHERE cached_at < :expiredTime")
    suspend fun deleteExpiredPostDetails(expiredTime: Long)

    @Query("DELETE FROM cached_post_detail")
    suspend fun deleteAllPostDetails()

    @Query("SELECT COUNT(*) FROM cached_post_detail")
    suspend fun getPostDetailCount(): Int
}
