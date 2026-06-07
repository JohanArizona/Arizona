package com.takehomechallenge.arizona.data.remote.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    val id: Long? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("character_id")
    val characterId: Int,
    val content: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("profiles")
    val profile: ProfileDto? = null
)
