package com.takehomechallenge.arizona.data.repository

import com.takehomechallenge.arizona.data.remote.api.RickMortyApi
import com.takehomechallenge.arizona.data.remote.supabase.dto.LikeDto
import com.takehomechallenge.arizona.data.remote.supabase.dto.LikeInsertDto
import com.takehomechallenge.arizona.data.repository.mapper.CharacterMapper
import com.takehomechallenge.arizona.domain.model.Character
import com.takehomechallenge.arizona.domain.repository.FavoriteRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth,
    private val api: RickMortyApi
) : FavoriteRepository {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    override fun getAllFavorites(): Flow<List<Character>> = flow {
        refreshTrigger.onStart { emit(Unit) }.collect {
            val userId = auth.currentSessionOrNull()?.user?.id
            if (userId == null) {
                emit(emptyList())
            } else {
                val likedIds = postgrest["likes"].select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<LikeDto>().map { it.characterId }

                if (likedIds.isEmpty()) {
                    emit(emptyList())
                } else {
                    val characters = likedIds.map { id ->
                        val response = api.getCharacterDetail(id)
                        CharacterMapper.mapDtoToDomain(response)
                    }
                    emit(characters)
                }
            }
        }
    }

    override fun checkFavoriteStatus(id: Int): Flow<Boolean> = flow {
        refreshTrigger.onStart { emit(Unit) }.collect {
            val userId = auth.currentSessionOrNull()?.user?.id
            if (userId == null) {
                emit(false)
            } else {
                val isLiked = postgrest["likes"].select {
                    filter {
                        eq("character_id", id)
                        eq("user_id", userId)
                    }
                }.decodeSingleOrNull<LikeDto>() != null
                emit(isLiked)
            }
        }
    }

    override suspend fun addFavorite(character: Character) {
        val userId = auth.currentSessionOrNull()?.user?.id ?: return
        postgrest["likes"].upsert(LikeInsertDto(userId = userId, characterId = character.id))
        refreshTrigger.emit(Unit)
    }

    override suspend fun removeFavorite(id: Int) {
        val userId = auth.currentSessionOrNull()?.user?.id ?: return
        postgrest["likes"].delete {
            filter {
                eq("character_id", id)
                eq("user_id", userId)
            }
        }
        refreshTrigger.emit(Unit)
    }
}
