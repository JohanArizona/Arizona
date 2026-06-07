package com.takehomechallenge.arizona.presentation

import com.google.common.truth.Truth.assertThat
import com.takehomechallenge.arizona.domain.model.Character
import com.takehomechallenge.arizona.domain.state.ResourceState
import com.takehomechallenge.arizona.domain.usecase.character.GetCharactersUseCase
import com.takehomechallenge.arizona.domain.usecase.favorite.AddFavoriteUseCase
import com.takehomechallenge.arizona.domain.usecase.favorite.GetFavoritesUseCase
import com.takehomechallenge.arizona.domain.usecase.favorite.RemoveFavoriteUseCase
import com.takehomechallenge.arizona.presentation.screen.home.HomeViewModel
import com.takehomechallenge.arizona.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCharactersUseCase = mockk<GetCharactersUseCase>()
    private val getFavoritesUseCase = mockk<GetFavoritesUseCase>()
    private val addFavoriteUseCase = mockk<AddFavoriteUseCase>(relaxed = true)
    private val removeFavoriteUseCase = mockk<RemoveFavoriteUseCase>(relaxed = true)

    //ngecek apakah HomeViewModel memuat karakter saat init dengan benar dan menangani kondisi error.
    @Test
    fun `HomeViewModel initial state should load characters successfully`() = runTest {
        // Given
        val dummyCharacter = mockk<Character>(relaxed = true)
        val dummyList = listOf(dummyCharacter)
        coEvery { getFavoritesUseCase() } returns flowOf(emptyList())
        coEvery { getCharactersUseCase(1) } returns flowOf(ResourceState.Success(dummyList))

        // When (Init ViewModel)
        val viewModel = HomeViewModel(
            getCharactersUseCase,
            getFavoritesUseCase,
            addFavoriteUseCase,
            removeFavoriteUseCase
        )

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.characters).isNotEmpty()
        assertThat(state.page).isEqualTo(2)
    }

    @Test
    fun `HomeViewModel should handle Error state`() = runTest {
        // Given
        val errorMessage = "Server Down"
        coEvery { getFavoritesUseCase() } returns flowOf(emptyList())
        coEvery { getCharactersUseCase(1) } returns flowOf(ResourceState.Error(errorMessage))

        // When
        val viewModel = HomeViewModel(
            getCharactersUseCase,
            getFavoritesUseCase,
            addFavoriteUseCase,
            removeFavoriteUseCase
        )

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.characters).isEmpty()
        assertThat(state.error).isEqualTo(errorMessage)
    }
}