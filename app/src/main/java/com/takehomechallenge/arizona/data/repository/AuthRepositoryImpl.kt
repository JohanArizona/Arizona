package com.takehomechallenge.arizona.data.repository

import com.takehomechallenge.arizona.domain.model.UserProfile
import com.takehomechallenge.arizona.domain.repository.AuthRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepositoryImpl @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val storage: Storage
) : AuthRepository {

    override val currentUser: Flow<UserProfile?> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> mapToUserProfile(status.session.user!!)
            else -> null
        }
    }

    override suspend fun signUp(email: String, password: String, username: String, fullName: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("username", username)
                put("full_name", fullName)
            }
        }
    }

    override suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun getSession(): String? {
        return auth.currentSessionOrNull()?.accessToken
    }

    override suspend fun updateProfile(username: String, fullName: String, avatarUrl: String?) {
        val userId = auth.currentSessionOrNull()?.user?.id ?: return
        
        // Update profiles table
        postgrest["profiles"].update(
            buildJsonObject {
                put("username", username)
                put("full_name", fullName)
                if (avatarUrl != null) put("avatar_url", avatarUrl)
            }
        ) {
            filter {
                eq("id", userId)
            }
        }

        // Update auth metadata
        auth.updateUser {
            data = buildJsonObject {
                put("username", username)
                put("full_name", fullName)
                if (avatarUrl != null) put("avatar_url", avatarUrl)
            }
        }
    }

    override suspend fun uploadAvatar(byteArray: ByteArray, fileName: String): String {
        val bucket = storage.from("avatars")
        bucket.upload(fileName, byteArray)
        return bucket.publicUrl(fileName)
    }

    private fun mapToUserProfile(user: UserInfo): UserProfile {
        return UserProfile(
            id = user.id,
            username = user.userMetadata?.get("username")?.toString()?.removeSurrounding("\""),
            fullName = user.userMetadata?.get("full_name")?.toString()?.removeSurrounding("\""),
            avatarUrl = user.userMetadata?.get("avatar_url")?.toString()?.removeSurrounding("\"")
        )
    }
}
