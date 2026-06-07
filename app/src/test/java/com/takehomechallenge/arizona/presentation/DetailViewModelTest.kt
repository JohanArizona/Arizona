package com.takehomechallenge.arizona.presentation

import com.google.common.truth.Truth.assertThat
import com.takehomechallenge.arizona.domain.model.Character
import com.takehomechallenge.arizona.domain.state.ResourceState
import com.takehomechallenge.arizona.domain.usecase.character.GetCharacterDetailUseCase
import com.takehomechallenge.arizona.domain.usecase.character.GetCharactersUseCase
import com.takehomechallenge.arizona.domain.usecase.favorite.AddFavoriteUseCase
import com.takehomechallenge.arizona.domain.usecase.favorite.CheckFavoriteUseCase
import com.takehomechallenge.arizona.domain.usecase.favorite.RemoveFavoriteUseCase
import com.takehomechallenge.arizona.presentation.screen.detail.DetailViewModel
import com.takehomechallenge.arizona.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCharacterDetailUseCase = mockk<GetCharacterDetailUseCase>()
    private val getCharactersUseCase = mockk<GetCharactersUseCase>()
    private val addFavoriteUseCase = mockk<AddFavoriteUseCase>(relaxed = true)
    private val removeFavoriteUseCase = mockk<RemoveFavoriteUseCase>(relaxed = true)
    private val checkFavoriteUseCase = mockk<CheckFavoriteUseCase>()

    private fun buildViewModel(): DetailViewModel {
        return DetailViewModel(
            getCharacterDetailUseCase,
            getCharactersUseCase,
            addFavoriteUseCase,
            removeFavoriteUseCase,
            checkFavoriteUseCase
        )
    }

    //ngecek apakah DetailViewModel mengelola state detail karakter dengan benar —
    // sukses, error, dan toggle favorit (add & remove).
    // ── TC-UNIT-009a: getCharacterDetail success ──────────────────
    @Test
    fun `getCharacterDetail should update uiState with character on success`() = runTest {
        // Given
        val characterId = 1
        val dummyCharacter = mockk<Character>(relaxed = true)
        val dummyPage = listOf(mockk<Character>(relaxed = true))

        every { checkFavoriteUseCase(characterId) } returns flowOf(false)
        every { getCharacterDetailUseCase(characterId) } returns flowOf(ResourceState.Success(dummyCharacter))
        every { getCharactersUseCase(any()) } returns flowOf(ResourceState.Success(dummyPage))

        val viewModel = buildViewModel()

        // When
        viewModel.getCharacterDetail(characterId)

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.character).isNotNull()
        assertThat(state.error).isNull()
    }

    // ── TC-UNIT-009b: getCharacterDetail error ────────────────────
    @Test
    fun `getCharacterDetail should update uiState with error on failure`() = runTest {
        // Given
        val characterId = 1
        val errorMessage = "Character not found"

        every { checkFavoriteUseCase(characterId) } returns flowOf(false)
        every { getCharacterDetailUseCase(characterId) } returns flowOf(ResourceState.Error(errorMessage))

        val viewModel = buildViewModel()

        // When
        viewModel.getCharacterDetail(characterId)

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isEqualTo(errorMessage)
    }

    // ── TC-UNIT-009c: toggleFavorite — add ───────────────────────
    @Test
    fun `toggleFavorite should call addFavoriteUseCase when not yet favorited`() = runTest {
        // Given — isFavorite awalnya false
        val characterId = 1
        val dummyCharacter = mockk<Character>(relaxed = true)

        every { checkFavoriteUseCase(characterId) } returns flowOf(false)
        every { getCharacterDetailUseCase(characterId) } returns flowOf(ResourceState.Success(dummyCharacter))
        every { getCharactersUseCase(any()) } returns flowOf(ResourceState.Success(emptyList()))

        val viewModel = buildViewModel()
        viewModel.getCharacterDetail(characterId)

        // When
        viewModel.toggleFavorite(dummyCharacter)

        // Then
        coVerify { addFavoriteUseCase(dummyCharacter) }
    }

    // ── TC-UNIT-009d: toggleFavorite — remove ────────────────────
    @Test
    fun `toggleFavorite should call removeFavoriteUseCase when already favorited`() = runTest {
        // Given — isFavorite awalnya true
        val characterId = 1
        val dummyCharacter = mockk<Character>(relaxed = true) {
            every { id } returns characterId
        }

        every { checkFavoriteUseCase(characterId) } returns flowOf(true)
        every { getCharacterDetailUseCase(characterId) } returns flowOf(ResourceState.Success(dummyCharacter))
        every { getCharactersUseCase(any()) } returns flowOf(ResourceState.Success(emptyList()))

        val viewModel = buildViewModel()
        viewModel.getCharacterDetail(characterId)

        // When
        viewModel.toggleFavorite(dummyCharacter)

        // Then
        coVerify { removeFavoriteUseCase(characterId) }
    }
}