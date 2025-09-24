package com.github.rphlfc.tabnews_kotlin.cache.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_metadata")
data class CachedMetadata(
    @PrimaryKey
    val key: String, // Combination of strategy and page
    val strategy: String,
    val page: Int,
    val last_updated: Long = System.currentTimeMillis(),
    val expires_at: Long = System.currentTimeMillis() + (5 * 60 * 1000), // 5 minutes default
    val content_count: Int = 0
)
