package com.takehomechallenge.arizona.domain.model

data class Comment(
    val id: Long,
    val userId: String,
    val characterId: Int,
    val content: String,
    val createdAt: String,
    val userProfile: UserProfile? = null
)
