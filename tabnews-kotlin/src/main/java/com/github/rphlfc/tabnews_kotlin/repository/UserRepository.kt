package com.github.rphlfc.tabnews_kotlin.repository

import com.github.rphlfc.tabnews_kotlin.model.User
import com.github.rphlfc.tabnews_kotlin.model.APIResult

interface UserRepository {
    suspend fun getLoggedUser(): APIResult<User>
}
