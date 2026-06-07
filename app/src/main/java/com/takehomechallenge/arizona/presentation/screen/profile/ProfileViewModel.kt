package com.takehomechallenge.arizona.presentation.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takehomechallenge.arizona.domain.model.Character
import com.takehomechallenge.arizona.domain.model.UserProfile
import com.takehomechallenge.arizona.domain.usecase.auth.GetCurrentUserUseCase
import com.takehomechallenge.arizona.domain.usecase.favorite.GetFavoritesUseCase
import com.takehomechallenge.arizona.domain.usecase.favorite.RemoveFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        combine(
            getCurrentUserUseCase(),
            getFavoritesUseCase()
        ) { user, favorites ->
            _uiState.update { it.copy(user = user, favorites = favorites) }
        }.launchIn(viewModelScope)
    }

    fun removeFromFavorites(character: Character) {
        viewModelScope.launch {
            removeFavoriteUseCase(character.id)
        }
    }
}

data class ProfileUiState(
    val user: UserProfile? = null,
    val favorites: List<Character> = emptyList()
)
