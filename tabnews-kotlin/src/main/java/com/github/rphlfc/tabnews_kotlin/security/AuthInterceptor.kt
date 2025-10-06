package com.github.rphlfc.tabnews_kotlin.security

import com.github.rphlfc.tabnews_kotlin.api.AuthRequired
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation

class AuthInterceptor(private val tokenProvider: TokenProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val invocation = request.tag(Invocation::class.java)
        val requiresAuth = invocation?.method()?.getAnnotation(AuthRequired::class.java) != null

        return if (requiresAuth) {
            val token = tokenProvider.getToken() ?: throw IllegalStateException("No auth token")
            val newRequest = request.newBuilder()
                .addHeader("session", token)
                .addHeader("Cookie", "session_id=$token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(request)
        }
    }
}