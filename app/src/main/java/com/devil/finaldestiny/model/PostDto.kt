package com.devil.finaldestiny.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileBriefDto(
    @SerialName("username") val username: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

@Serializable
data class ProfileDto(
    @SerialName("id") val id: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("phone") val phone: String? = null
) {
    fun getResolvedName(): String {
        val u = username?.trim()?.takeIf { it.isNotBlank() && it.lowercase() != "null" }
        val f = fullName?.trim()?.takeIf { it.isNotBlank() && it.lowercase() != "null" }
        val d = displayName?.trim()?.takeIf { it.isNotBlank() && it.lowercase() != "null" }
        val e = email?.trim()?.takeIf { it.isNotBlank() && it.lowercase() != "null" }?.substringBefore("@")
        val p = phone?.trim()?.takeIf { it.isNotBlank() && it.lowercase() != "null" }

        return u ?: f ?: d ?: e ?: p ?: (id?.takeIf { it.isNotBlank() }?.let { "User_${it.take(5)}" } ?: "Destiny Creator")
    }

    fun getResolvedHandle(): String {
        val u = username?.trim()?.takeIf { it.isNotBlank() && it.lowercase() != "null" }
        val name = getResolvedName()
        return if (!u.isNullOrBlank()) {
            if (u.startsWith("@")) u else "@$u"
        } else {
            if (name.startsWith("@")) name else "@${name.lowercase().replace(" ", "_")}"
        }
    }
}

@Serializable
data class PostDto(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("media_url") val mediaUrl: String? = null,
    @SerialName("media_type") val mediaType: String? = "image",
    @SerialName("caption") val caption: String? = null,
    @SerialName("likes_count") val likesCount: Int? = 0,
    @SerialName("comments_count") val commentsCount: Int? = 0,
    @SerialName("views_count") val viewsCount: Long? = 0L,
    @SerialName("audio_url") val audioUrl: String? = null,
    @SerialName("cta_link") val ctaLink: String? = null,
    @SerialName("cta_label") val ctaLabel: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("profiles") val profile: ProfileBriefDto? = null
)

@Serializable
data class DiscoverUserDto(
    @SerialName("id") val id: String,
    @SerialName("username") val username: String,
    @SerialName("full_name") val full_name: String? = null,
    @SerialName("avatar_url") val avatar_url: String? = null,
    @SerialName("is_following") val is_following: Boolean = false
)
