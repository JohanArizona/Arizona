package com.takehomechallenge.arizona.domain.usecase.social

import com.takehomechallenge.arizona.domain.model.Comment
import com.takehomechallenge.arizona.domain.repository.SocialRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLikesCountUseCase @Inject constructor(private val repository: SocialRepository) {
    operator fun invoke(characterId: Int): Flow<Int> = repository.getLikesCount(characterId)
}

class IsLikedUseCase @Inject constructor(private val repository: SocialRepository) {
    operator fun invoke(characterId: Int): Flow<Boolean> = repository.isLiked(characterId)
}

class ToggleLikeUseCase @Inject constructor(private val repository: SocialRepository) {
    suspend operator fun invoke(characterId: Int) = repository.toggleLike(characterId)
}

class GetCommentsUseCase @Inject constructor(private val repository: SocialRepository) {
    operator fun invoke(characterId: Int): Flow<List<Comment>> = repository.getComments(characterId)
}

class AddCommentUseCase @Inject constructor(private val repository: SocialRepository) {
    suspend operator fun invoke(characterId: Int, content: String) = repository.addComment(characterId, content)
}

class DeleteCommentUseCase @Inject constructor(private val repository: SocialRepository) {
    suspend operator fun invoke(commentId: Long) = repository.deleteComment(commentId)
}

class UpdateCommentUseCase @Inject constructor(private val repository: SocialRepository) {
    suspend operator fun invoke(commentId: Long, content: String) = repository.updateComment(commentId, content)
}
