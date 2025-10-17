package com.github.rphlfc.tabnews_kotlin.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.rphlfc.tabnews_kotlin.cache.model.CachedMetadata

@Dao
interface MetadataCacheDao {

    @Query("SELECT * FROM cached_metadata WHERE key = :key")
    suspend fun getMetadataByKey(key: String): CachedMetadata?

    @Query("SELECT * FROM cached_metadata WHERE strategy = :strategy")
    suspend fun getMetadataByStrategy(strategy: String): List<CachedMetadata>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: CachedMetadata)

    @Query("DELETE FROM cached_metadata WHERE key = :key")
    suspend fun deleteMetadataByKey(key: String)

    @Query("DELETE FROM cached_metadata WHERE strategy = :strategy")
    suspend fun deleteMetadataByStrategy(strategy: String)

    @Query("DELETE FROM cached_metadata WHERE expires_at < :currentTime")
    suspend fun deleteExpiredMetadata(currentTime: Long)

    @Query("DELETE FROM cached_metadata")
    suspend fun deleteAllMetadata()

    @Query("SELECT COUNT(*) FROM cached_metadata WHERE strategy = :strategy")
    suspend fun getMetadataCountByStrategy(strategy: String): Int
}