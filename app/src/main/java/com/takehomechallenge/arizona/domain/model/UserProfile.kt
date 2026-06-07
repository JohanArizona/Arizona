package com.takehomechallenge.arizona.domain.model

data class UserProfile(
    val id: String,
    val username: String?,
    val fullName: String?,
    val avatarUrl: String?
)
