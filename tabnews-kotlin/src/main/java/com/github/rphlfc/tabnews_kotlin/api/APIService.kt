package com.github.rphlfc.tabnews_kotlin.api

import com.github.rphlfc.tabnews_kotlin.model.CommentRequest
import com.github.rphlfc.tabnews_kotlin.model.Content
import com.github.rphlfc.tabnews_kotlin.model.ContentRequest
import com.github.rphlfc.tabnews_kotlin.model.LoginRequest
import com.github.rphlfc.tabnews_kotlin.model.LoginResponse
import com.github.rphlfc.tabnews_kotlin.model.TabcoinsRequest
import com.github.rphlfc.tabnews_kotlin.model.User
import com.github.rphlfc.tabnews_kotlin.model.TabcoinsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

internal interface APIService {
    @GET("api/v1/contents")
    suspend fun getContents(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 20,
        @Query("strategy") strategy: String = "relevant"
    ): List<Content>

    @GET("api/v1/contents/{owner_username}/{slug}")
    suspend fun getContentDetail(
        @Path("owner_username") ownerUsername: String,
        @Path("slug") slug: String
    ): Content

    @GET("api/v1/contents/{owner_username}/{slug}/children")
    suspend fun getComments(
        @Path("owner_username") ownerUsername: String,
        @Path("slug") slug: String
    ): List<Content>

    @POST("api/v1/sessions")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @AuthRequired
    @POST("api/v1/contents/{owner_username}/{slug}/tabcoins")
    suspend fun voteOnContent(
        @Path("owner_username") ownerUsername: String,
        @Path("slug") slug: String,
        @Body request: TabcoinsRequest
    ): TabcoinsResponse

    @AuthRequired
    @GET("api/v1/user")
    suspend fun getUserProfile(): User

    @AuthRequired
    @POST("api/v1/contents")
    suspend fun createContent(@Body request: ContentRequest): Content

    @AuthRequired
    @POST("api/v1/contents")
    suspend fun createComment(@Body request: CommentRequest): Content
}
