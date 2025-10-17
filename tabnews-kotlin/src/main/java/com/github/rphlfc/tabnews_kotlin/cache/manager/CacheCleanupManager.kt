package com.github.rphlfc.tabnews_kotlin.cache.manager

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    private var cleanupJob: Job? = null

    fun startPeriodicCleanup() {
        cleanupJob?.cancel()

        cleanupJob = scope.launch {
            while (true) {
                try {
                    cacheManager.cleanupExpiredCache()

                    cleanupOldCacheFiles()

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                delay(TimeUnit.MINUTES.toMillis(30))
            }
        }
    }

    fun stopPeriodicCleanup() {
        cleanupJob?.cancel()
        cleanupJob = null
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
                    val maxAge = TimeUnit.DAYS.toMillis(7)

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
