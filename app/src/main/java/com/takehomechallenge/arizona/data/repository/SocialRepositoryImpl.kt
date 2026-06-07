package com.takehomechallenge.arizona.data.repository

import com.takehomechallenge.arizona.data.remote.supabase.dto.CommentDto
import com.takehomechallenge.arizona.data.remote.supabase.dto.CommentInsertDto
import com.takehomechallenge.arizona.data.remote.supabase.dto.LikeDto
import com.takehomechallenge.arizona.data.remote.supabase.dto.LikeInsertDto
import com.takehomechallenge.arizona.domain.model.Comment
import com.takehomechallenge.arizona.domain.model.UserProfile
import com.takehomechallenge.arizona.domain.repository.SocialRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SocialRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth
) : SocialRepository {

    override fun getLikesCount(characterId: Int): Flow<Int> = flow {
        val response = postgrest["likes"].select {
            filter {
                eq("character_id", characterId)
            }
        }.decodeList<LikeDto>()
        emit(response.size)
    }

    override fun isLiked(characterId: Int): Flow<Boolean> = flow {
        val userId = auth.currentSessionOrNull()?.user?.id ?: return@flow emit(false)
        val result = postgrest["likes"].select {
            filter {
                eq("character_id", characterId)
                eq("user_id", userId)
            }
        }.decodeSingleOrNull<LikeDto>()
        emit(result != null)
    }

    override suspend fun toggleLike(characterId: Int) {
        val userId = auth.currentSessionOrNull()?.user?.id ?: throw Exception("You must be logged in to like")
        val isLiked = postgrest["likes"].select {
            filter {
                eq("character_id", characterId)
                eq("user_id", userId)
            }
        }.decodeSingleOrNull<LikeDto>() != null

        if (isLiked) {
            postgrest["likes"].delete {
                filter {
                    eq("character_id", characterId)
                    eq("user_id", userId)
                }
            }
        } else {
            postgrest["likes"].insert(LikeInsertDto(userId = userId, characterId = characterId))
        }
    }

    override fun getComments(characterId: Int): Flow<List<Comment>> = flow {
        val response = postgrest["comments"].select(columns = Columns.raw("*, profiles(*)")) {
            filter {
                eq("character_id", characterId)
            }
        }
        val result = response.decodeList<CommentDto>()
        emit(result.map { it.toDomain() })
    }

    override suspend fun addComment(characterId: Int, content: String) {
        val userId = auth.currentSessionOrNull()?.user?.id ?: throw Exception("You must be logged in to comment")
        postgrest["comments"].insert(CommentInsertDto(userId = userId, characterId = characterId, content = content))
    }

    override suspend fun deleteComment(commentId: Long) {
        postgrest["comments"].delete {
            filter {
                eq("id", commentId)
            }
        }
    }

    override suspend fun updateComment(commentId: Long, content: String) {
        postgrest["comments"].update(
            buildJsonObject {
                put("content", content)
            }
        ) {
            filter {
                eq("id", commentId)
            }
        }
    }

    private fun CommentDto.toDomain(): Comment {
        return Comment(
            id = id ?: 0L,
            userId = userId,
            characterId = characterId,
            content = content,
            createdAt = createdAt ?: "",
            userProfile = profile?.let {
                UserProfile(
                    id = it.id,
                    username = it.username,
                    fullName = it.fullName,
                    avatarUrl = it.avatarUrl
                )
            }
        )
    }
}
