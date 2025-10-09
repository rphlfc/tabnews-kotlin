package com.github.rphlfc.tabnews_kotlin.cache.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.rphlfc.tabnews_kotlin.model.Content

@Entity(tableName = "cached_post_detail")
data class CachedPostDetail(
    @PrimaryKey
    val id: String,
    val owner_id: String,
    val parent_id: String?,
    val slug: String,
    val title: String?,
    val body: String?,
    val status: String,
    val source_url: String?,
    val created_at: String,
    val updated_at: String,
    val published_at: String?,
    val deleted_at: String?,
    val tabcoins: Int,
    val owner_username: String,
    val children_deep_count: Int,
    val cached_at: Long = System.currentTimeMillis()
) {
    fun toContent(): Content {
        return Content(
            id = id,
            ownerId = owner_id,
            parentId = parent_id,
            slug = slug,
            title = title,
            body = body,
            status = status,
            sourceUrl = source_url,
            createdAt = created_at,
            updatedAt = updated_at,
            publishedAt = published_at,
            deletedAt = deleted_at,
            tabcoins = tabcoins,
            ownerUsername = owner_username,
            childrenDeepCount = children_deep_count
        )
    }

    companion object {
        fun fromContent(content: Content): CachedPostDetail {
            return CachedPostDetail(
                id = content.id,
                owner_id = content.ownerId,
                parent_id = content.parentId,
                slug = content.slug,
                title = content.title,
                body = content.body,
                status = content.status,
                source_url = content.sourceUrl,
                created_at = content.createdAt,
                updated_at = content.updatedAt,
                published_at = content.publishedAt,
                deleted_at = content.deletedAt,
                tabcoins = content.tabcoins,
                owner_username = content.ownerUsername,
                children_deep_count = content.childrenDeepCount ?: 0
            )
        }
    }
}
