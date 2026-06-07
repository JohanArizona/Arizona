package com.takehomechallenge.arizona.data.remote.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LikeInsertDto(
    @SerialName("user_id")
    val userId: String,
    @SerialName("character_id")
    val characterId: Int
)
