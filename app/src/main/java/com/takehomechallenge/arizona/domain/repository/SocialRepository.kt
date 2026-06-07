package com.takehomechallenge.arizona.domain.repository

import com.takehomechallenge.arizona.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    fun getLikesCount(characterId: Int): Flow<Int>
    fun isLiked(characterId: Int): Flow<Boolean>
    suspend fun toggleLike(characterId: Int)
    
    fun getComments(characterId: Int): Flow<List<Comment>>
    suspend fun addComment(characterId: Int, content: String)
    suspend fun deleteComment(commentId: Long)
    suspend fun updateComment(commentId: Long, content: String)
}
