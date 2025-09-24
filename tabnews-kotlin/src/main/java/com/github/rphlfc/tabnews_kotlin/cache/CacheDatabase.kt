package com.github.rphlfc.tabnews_kotlin.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.rphlfc.tabnews_kotlin.cache.dao.ContentCacheDao
import com.github.rphlfc.tabnews_kotlin.cache.dao.MetadataCacheDao
import com.github.rphlfc.tabnews_kotlin.cache.dao.PostDetailCacheDao
import com.github.rphlfc.tabnews_kotlin.cache.model.CachedContent
import com.github.rphlfc.tabnews_kotlin.cache.model.CachedMetadata
import com.github.rphlfc.tabnews_kotlin.cache.model.CachedPostDetail

@Database(
    entities = [CachedContent::class, CachedMetadata::class, CachedPostDetail::class],
    version = 1,
    exportSchema = false
)
abstract class CacheDatabase : RoomDatabase() {
    abstract fun contentCacheDao(): ContentCacheDao
    abstract fun cacheMetadataDao(): MetadataCacheDao
    abstract fun postDetailCacheDao(): PostDetailCacheDao

    companion object Companion {
        @Volatile
        private var INSTANCE: CacheDatabase? = null
        fun getDatabase(context: Context): CacheDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CacheDatabase::class.java,
                    "content_cache_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
