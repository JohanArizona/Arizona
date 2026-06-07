package com.takehomechallenge.arizona.domain.repository

import com.takehomechallenge.arizona.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<UserProfile?>
    suspend fun signUp(email: String, password: String, username: String, fullName: String)
    suspend fun signIn(email: String, password: String)
    suspend fun signOut()
    suspend fun getSession(): String?
    suspend fun updateProfile(username: String, fullName: String, avatarUrl: String?)
    suspend fun uploadAvatar(byteArray: ByteArray, fileName: String): String
}
