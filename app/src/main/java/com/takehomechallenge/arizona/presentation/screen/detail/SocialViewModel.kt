package com.takehomechallenge.arizona.presentation.screen.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takehomechallenge.arizona.domain.model.Comment
import com.takehomechallenge.arizona.domain.usecase.social.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.github.jan.supabase.auth.Auth

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val getLikesCountUseCase: GetLikesCountUseCase,
    private val isLikedUseCase: IsLikedUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val updateCommentUseCase: UpdateCommentUseCase,
    private val auth: Auth
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()

    private var socialJob: Job? = null

    fun loadSocialData(characterId: Int) {
        val currentUserId = auth.currentSessionOrNull()?.user?.id
        socialJob?.cancel()
        socialJob = viewModelScope.launch {
            launch {
                getLikesCountUseCase(characterId)
                    .catch { e -> Log.e("SocialVM", "Error loading likes count", e) }
                    .collect { count ->
                        _uiState.update { it.copy(likesCount = count) }
                    }
            }
            launch {
                isLikedUseCase(characterId)
                    .catch { e -> Log.e("SocialVM", "Error checking liked status", e) }
                    .collect { isLiked ->
                        _uiState.update { it.copy(isLiked = isLiked) }
                    }
            }
            launch {
                getCommentsUseCase(characterId)
                    .catch { e -> 
                        Log.e("SocialVM", "Error loading comments", e)
                        _uiState.update { it.copy(error = "Database Error") }
                    }
                    .collect { comments ->
                        // Sort: User's own comments first, then by date
                        val sortedComments = comments.sortedWith(
                            compareByDescending<Comment> { it.userId == currentUserId }
                                .thenByDescending { it.createdAt }
                        )
                        _uiState.update { it.copy(comments = sortedComments, currentUserId = currentUserId) }
                    }
            }
        }
    }

    fun toggleLike(characterId: Int) {
        viewModelScope.launch {
            try {
                toggleLikeUseCase(characterId)
                loadSocialData(characterId)
            } catch (e: Exception) {
                Log.e("SocialVM", "Error toggling like", e)
                _uiState.update { it.copy(error = "Like failed: ${e.message}") }
            }
        }
    }

    fun addComment(characterId: Int, content: String) {
        viewModelScope.launch {
            try {
                addCommentUseCase(characterId, content)
                loadSocialData(characterId)
            } catch (e: Exception) {
                Log.e("SocialVM", "Error adding comment", e)
                _uiState.update { it.copy(error = "Comment failed: ${e.message}") }
            }
        }
    }

    fun deleteComment(commentId: Long, characterId: Int) {
        viewModelScope.launch {
            try {
                deleteCommentUseCase(commentId)
                loadSocialData(characterId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Delete failed: ${e.message}") }
            }
        }
    }

    fun updateComment(commentId: Long, content: String, characterId: Int) {
        viewModelScope.launch {
            try {
                updateCommentUseCase(commentId, content)
                loadSocialData(characterId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Update failed: ${e.message}") }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class SocialUiState(
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val error: String? = null,
    val currentUserId: String? = null
)
