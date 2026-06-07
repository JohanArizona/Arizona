package com.takehomechallenge.arizona.presentation

import com.google.common.truth.Truth.assertThat
import com.takehomechallenge.arizona.domain.model.Character
import com.takehomechallenge.arizona.domain.usecase.favorite.GetFavoritesUseCase
import com.takehomechallenge.arizona.domain.usecase.favorite.RemoveFavoriteUseCase
import com.takehomechallenge.arizona.presentation.screen.favorite.FavoriteViewModel
import com.takehomechallenge.arizona.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class FavoriteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getFavoritesUseCase = mockk<GetFavoritesUseCase>()
    private val removeFavoriteUseCase = mockk<RemoveFavoriteUseCase>(relaxed = true)

    // ngecek apakah FavoriteViewModel memuat list favorit saat init dan menghapus favorit dengan id yang benar.
    // ── TC-UNIT-010a: init — load favorites ───────────────────────
    @Test
    fun `FavoriteViewModel init should load favorites into state`() = runTest {
        // Given
        val dummyFavorites = listOf(mockk<Character>(relaxed = true))
        every { getFavoritesUseCase() } returns flowOf(dummyFavorites)

        // When
        val viewModel = FavoriteViewModel(getFavoritesUseCase, removeFavoriteUseCase)

        // Then
        assertThat(viewModel.favorites.value).hasSize(1)
    }

    // ── TC-UNIT-010b: init — empty favorites ──────────────────────
    @Test
    fun `FavoriteViewModel init should show empty list when no favorites`() = runTest {
        // Given
        every { getFavoritesUseCase() } returns flowOf(emptyList())

        // When
        val viewModel = FavoriteViewModel(getFavoritesUseCase, removeFavoriteUseCase)

        // Then
        assertThat(viewModel.favorites.value).isEmpty()
    }

    // ── TC-UNIT-010c: removeFromFavorites ─────────────────────────
    // Catatan: removeFavoriteUseCase menerima id: Int, bukan character object
    @Test
    fun `removeFromFavorites should call removeFavoriteUseCase with character id`() = runTest {
        // Given
        val characterId = 42
        val dummyCharacter = mockk<Character>(relaxed = true) {
            every { id } returns characterId
        }
        every { getFavoritesUseCase() } returns flowOf(emptyList())

        val viewModel = FavoriteViewModel(getFavoritesUseCase, removeFavoriteUseCase)

        // When
        viewModel.removeFromFavorites(dummyCharacter)

        // Then
        coVerify { removeFavoriteUseCase(characterId) }
    }
}