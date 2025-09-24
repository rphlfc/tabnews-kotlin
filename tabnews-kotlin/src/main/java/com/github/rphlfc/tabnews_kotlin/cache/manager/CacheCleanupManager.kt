package com.github.rphlfc.tabnews_kotlin.cache.manager

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit

class CacheCleanupManager(
    private val context: Context,
    private val cacheManager: CacheManager
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun startPeriodicCleanup() {
        scope.launch {
            while (true) {
                try {
                    // Clean up expired cache every 30 minutes
                    cacheManager.cleanupExpiredCache()

                    // Clean up old cache files
                    cleanupOldCacheFiles()

                } catch (e: Exception) {
                    // Log error but continue
                    e.printStackTrace()
                }

                // Wait 30 minutes before next cleanup
                delay(TimeUnit.MINUTES.toMillis(30))
            }
        }
    }

    private fun cleanupOldCacheFiles() {
        try {
            val cacheDir = context.cacheDir
            val httpCacheDir = File(cacheDir, "http_cache")

            if (httpCacheDir.exists()) {
                val files = httpCacheDir.listFiles()
                files?.forEach { file ->
                    val lastModified = file.lastModified()
                    val currentTime = System.currentTimeMillis()
                    val maxAge = TimeUnit.DAYS.toMillis(7) // 7 days

                    if (currentTime - lastModified > maxAge) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
