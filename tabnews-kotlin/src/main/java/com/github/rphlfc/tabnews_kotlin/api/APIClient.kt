package com.github.rphlfc.tabnews_kotlin.api


import android.content.Context
import com.github.rphlfc.tabnews_kotlin.cache.CacheDatabase
import com.github.rphlfc.tabnews_kotlin.cache.manager.CacheCleanupManager
import com.github.rphlfc.tabnews_kotlin.cache.manager.CacheManager
import com.github.rphlfc.tabnews_kotlin.repository.AuthRepository
import com.github.rphlfc.tabnews_kotlin.repository.AuthRepositoryImpl
import com.github.rphlfc.tabnews_kotlin.repository.ContentRepository
import com.github.rphlfc.tabnews_kotlin.repository.ContentRepositoryImpl
import com.github.rphlfc.tabnews_kotlin.repository.UserRepository
import com.github.rphlfc.tabnews_kotlin.repository.UserRepositoryImpl
import com.github.rphlfc.tabnews_kotlin.security.AuthInterceptor
import com.github.rphlfc.tabnews_kotlin.security.AuthManager
import com.github.rphlfc.tabnews_kotlin.security.TokenProvider
import com.github.rphlfc.tabnews_kotlin.security.TokenProviderImpl
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

data class APIClientConfig(
    val baseUrl: String = "https://www.tabnews.com.br/",
    val connectTimeoutSeconds: Long = 30,
    val readTimeoutSeconds: Long = 30,
    val writeTimeoutSeconds: Long = 30,
    val enableLogging: Boolean = false,
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    },
    val dispatcher: CoroutineDispatcher = Dispatchers.IO
)

class APIClient private constructor(
    private val context: Context,
    private val apiService: APIService,
    private val config: APIClientConfig
) {
    class Builder(ctx: Context) {

        val context: Context = ctx.applicationContext
        private var config: APIClientConfig = APIClientConfig()

        fun baseUrl(url: String) = apply { config = config.copy(baseUrl = url) }
        fun timeouts(connectSeconds: Long, readSeconds: Long, writeSeconds: Long) = apply {
            config = config.copy(
                connectTimeoutSeconds = connectSeconds,
                readTimeoutSeconds = readSeconds,
                writeTimeoutSeconds = writeSeconds
            )
        }

        fun enableLogging(enable: Boolean) = apply { config = config.copy(enableLogging = enable) }
        fun json(json: Json) = apply { config = config.copy(json = json) }
        fun dispatcher(dispatcher: CoroutineDispatcher) =
            apply { config = config.copy(dispatcher = dispatcher) }

        fun build(): APIClient {
            val mediaType = "application/json".toMediaType()
            val okBuilder = getOkBuilder(context)
            val okHttpClient = okBuilder.build()

            val retrofit = Retrofit.Builder()
                .baseUrl(config.baseUrl)
                .addConverterFactory(config.json.asConverterFactory(mediaType))
                .client(okHttpClient)
                .build()

            val apiService = retrofit.create(APIService::class.java)

            return APIClient(context, apiService, config).apply {
                initializeCacheCleanup()
            }
        }

        private fun getOkBuilder(context: Context): OkHttpClient.Builder {
            val logging = HttpLoggingInterceptor().apply {
                level =
                    if (config.enableLogging) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
            }

            return OkHttpClient.Builder()
                .connectTimeout(config.connectTimeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(config.readTimeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(config.writeTimeoutSeconds, TimeUnit.SECONDS)
                .addInterceptor(AuthInterceptor(TokenProviderImpl(context)))
                .addInterceptor(logging)
        }
    }

    private val cacheDatabase: CacheDatabase by lazy {
        CacheDatabase.getDatabase(context)
    }

    private fun initializeCacheCleanup() {
        cacheCleanupManager.startPeriodicCleanup()
    }

    private val cacheManager: CacheManager by lazy {
        CacheManager(cacheDatabase)
    }

    private val cacheCleanupManager: CacheCleanupManager by lazy {
        CacheCleanupManager(context, cacheManager)
    }

    private val tokenProvider: TokenProvider by lazy {
        TokenProviderImpl(context)
    }

    val authManager: AuthManager by lazy {
        AuthManager(tokenProvider, ioDispatcher = config.dispatcher)
    }

    val contentRepository: ContentRepository by lazy {
        ContentRepositoryImpl(apiService, cacheManager)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(apiService, authManager)
    }

    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(apiService)
    }
}