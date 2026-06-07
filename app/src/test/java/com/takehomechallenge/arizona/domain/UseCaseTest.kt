package com.takehomechallenge.arizona.domain

import com.google.common.truth.Truth.assertThat
import com.takehomechallenge.arizona.domain.model.Character
import com.takehomechallenge.arizona.domain.repository.CharacterRepository
import com.takehomechallenge.arizona.domain.repository.FavoriteRepository
import com.takehomechallenge.arizona.domain.repository.SearchRepository
import com.takehomechallenge.arizona.domain.state.ResourceState
import com.takehomechallenge.arizona.domain.usecase.character.GetCharactersUseCase
import com.takehomechallenge.arizona.domain.usecase.character.SearchCharactersUseCase
import com.takehomechallenge.arizona.domain.usecase.favorite.AddFavoriteUseCase
import com.takehomechallenge.arizona.domain.usecase.favorite.GetFavoritesUseCase
import com.takehomechallenge.arizona.domain.usecase.favorite.RemoveFavoriteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UseCaseTest {

    private val characterRepo = mockk<CharacterRepository>()
    private val searchRepo = mockk<SearchRepository>(relaxed = true)
    private val favoriteRepo  = mockk<FavoriteRepository>(relaxed = true)

    // ════════════════════════════════════════════════════════════════════════
    // TC-UNIT-003 : GetCharactersUseCase
    // ngecek apakah GetCharactersUseCase meneruskan hasil dari repository dengan benar tanpa mengubah apapun.
    // Happy path-nya mastiin kalau repo kasih data sukses, use case ikut kembalikan sukses dengan datanya utuh.
    // Edge case-nya ngecek kalau repo bilang kosong atau error, use case juga ikut meneruskan kondisi itu apa adanya.
    // ════════════════════════════════════════════════════════════════════════

    // ── Happy path: returns Success with data ────────────────────────────────
    @Test
    fun `GetCharactersUseCase should return Success with data`() = runTest {
        // Given
        val dummyData = listOf(mockk<Character>())
        coEvery { characterRepo.getCharacters(1) } returns flowOf(ResourceState.Success(dummyData))

        // When
        val result = GetCharactersUseCase(characterRepo)(1).first()

        // Then
        assertThat(result).isInstanceOf(ResourceState.Success::class.java)
        assertThat((result as ResourceState.Success).data).hasSize(1)
    }

    // ── Error path: returns Error when repository fails ──────────────────────
    @Test
    fun `GetCharactersUseCase should return Error when repository fails`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { characterRepo.getCharacters(1) } returns
                flowOf(ResourceState.Error(errorMessage))

        // When
        val result = GetCharactersUseCase(characterRepo)(1).first()

        // Then
        assertThat(result).isInstanceOf(ResourceState.Error::class.java)
        assertThat((result as ResourceState.Error).message).isEqualTo(errorMessage)
    }

    // ── Edge case: returns Empty when no data available ──────────────────────
    @Test
    fun `GetCharactersUseCase should return Empty when no data`() = runTest {
        // Given
        coEvery { characterRepo.getCharacters(1) } returns flowOf(ResourceState.Empty)

        // When
        val result = GetCharactersUseCase(characterRepo)(1).first()

        // Then
        assertThat(result).isInstanceOf(ResourceState.Empty::class.java)
    }

    // ════════════════════════════════════════════════════════════════════════
    // TC-UNIT-004 : SearchCharactersUseCase
    // ngecek apakah SearchCharactersUseCase meneruskan hasil pencarian dari repository dengan benar.
    // Happy path-nya mastiin kalau ada hasil pencarian, datanya diteruskan dengan jumlah yang tepat.
    // Edge case-nya ngecek kalau tidak ada hasil pencarian (Empty) atau jaringan error, use case tetap meneruskan kondisi itu tanpa distorsi.
    // ════════════════════════════════════════════════════════════════════════

    // ── Happy path: returns Success with matched characters ──────────────────
    @Test
    fun `SearchCharactersUseCase should return Success when results found`() = runTest {
        // Given
        val query     = "Rick"
        val dummyData = listOf(mockk<Character>(), mockk<Character>())
        coEvery {
            searchRepo.searchCharacters(query, null, null, null, null, 1)
        } returns flowOf(ResourceState.Success(dummyData))

        // When
        val result = SearchCharactersUseCase(searchRepo)(query).first()

        // Then
        assertThat(result).isInstanceOf(ResourceState.Success::class.java)
        assertThat((result as ResourceState.Success).data).hasSize(2)
    }

    // ── Error path: returns Empty when no results ────────────────────────────
    @Test
    fun `SearchCharactersUseCase should return Empty when no result found`() = runTest {
        // Given
        val query = "Zorglub"
        coEvery {
            searchRepo.searchCharacters(query, null, null, null, null, 1)
        } returns flowOf(ResourceState.Empty)

        // When
        val result = SearchCharactersUseCase(searchRepo)(query).first()

        // Then
        assertThat(result).isInstanceOf(ResourceState.Empty::class.java)
    }

    // ── Error path: returns Error on network failure ─────────────────────────
    @Test
    fun `SearchCharactersUseCase should return Error on network failure`() = runTest {
        // Given
        val query        = "Rick"
        val errorMessage = "No internet connection"
        coEvery {
            searchRepo.searchCharacters(query, null, null, null, null, 1)
        } returns flowOf(ResourceState.Error(errorMessage))

        // When
        val result = SearchCharactersUseCase(searchRepo)(query).first()

        // Then
        assertThat(result).isInstanceOf(ResourceState.Error::class.java)
        assertThat((result as ResourceState.Error).message).isEqualTo(errorMessage)
    }

    // ════════════════════════════════════════════════════════════════════════
    // TC-UNIT-005 : AddFavoriteUseCase
    // ngecek apakah AddFavoriteUseCase benar-benar memanggil repository saat dieksekusi.
    // Happy path-nya simpel: mastiin repo.addFavorite() dipanggil tepat 1 kali dengan objek karakter yang benar, tidak lebih tidak kurang.
    // ════════════════════════════════════════════════════════════════════════

    // ── Happy path: calls repository.addFavorite exactly once ───────────────
    @Test
    fun `AddFavoriteUseCase should call repository addFavorite exactly once`() = runTest {
        // Given
        val character = mockk<Character>()

        // When
        AddFavoriteUseCase(favoriteRepo)(character)

        // Then
        coVerify(exactly = 1) { favoriteRepo.addFavorite(character) }
    }

    // ════════════════════════════════════════════════════════════════════════
    // TC-UNIT-006 : RemoveFavoriteUseCase
    // ngecek apakah RemoveFavoriteUseCase benar-benar memanggil repository dengan id yang tepat.
    // Happy path-nya mastiin repo.removeFavorite() dipanggil dengan id yang sama persis seperti yang dikirim, dan id itu tidak berubah saat melewati use case.
    // ════════════════════════════════════════════════════════════════════════

    // ── Happy path: calls repository.removeFavorite with correct id ──────────
    @Test
    fun `RemoveFavoriteUseCase should call repository removeFavorite with correct id`() = runTest {
        // Given
        val characterId = 42

        // When
        RemoveFavoriteUseCase(favoriteRepo)(characterId)

        // Then
        coVerify(exactly = 1) { favoriteRepo.removeFavorite(characterId) }
    }

    // ── Edge case: id tidak berubah saat melewati use case ───────────────────
    @Test
    fun `RemoveFavoriteUseCase should pass exact id to repository`() = runTest {
        // Given
        val characterId = 999

        // When
        RemoveFavoriteUseCase(favoriteRepo)(characterId)

        // Then
        coVerify { favoriteRepo.removeFavorite(999) }
        coVerify(exactly = 0) { favoriteRepo.removeFavorite(1) }
    }

    // ════════════════════════════════════════════════════════════════════════
    // TC-UNIT-007 : GetFavoritesUseCase
    // ngecek apakah GetFavoritesUseCase mengalirkan data favorit dari repository dengan benar dalam bentuk Flow.
    // Happy path-nya mastiin Flow memancarkan list dengan jumlah item yang sesuai.
    // Edge case-nya mastiin kalau tidak ada favorit sama sekali, Flow tetap memancarkan list kosong tanpa error.
    // ════════════════════════════════════════════════════════════════════════

    // ── Happy path: emits list of favorites ──────────────────────────────────
    @Test
    fun `GetFavoritesUseCase should emit list of favorites`() = runTest {
        // Given
        val dummyFavorites = listOf(mockk<Character>(), mockk<Character>())
        coEvery { favoriteRepo.getAllFavorites() } returns flowOf(dummyFavorites)

        // When
        val result = GetFavoritesUseCase(favoriteRepo)().first()

        // Then
        assertThat(result).hasSize(2)
    }

    // ── Edge case: emits empty list when no favorites ────────────────────────
    @Test
    fun `GetFavoritesUseCase should emit empty list when no favorites exist`() = runTest {
        // Given
        coEvery { favoriteRepo.getAllFavorites() } returns flowOf(emptyList())

        // When
        val result = GetFavoritesUseCase(favoriteRepo)().first()

        // Then
        assertThat(result).isEmpty()
    }

    // ── TC-UNIT-008: SearchCharactersUseCase — Search History ─────
    // Search history ada di SearchCharactersUseCase (bukan UseCase terpisah)
    //ngecek apakah fitur riwayat pencarian di SearchCharactersUseCase bekerja dengan benar untuk ketiga operasinya.
    // Mastiin addToHistory memanggil repo untuk menyimpan, getHistory mengembalikan Flow riwayat yang sesuai, dan removeFromHistory memanggil repo untuk menghapus entri yang tepat.
    @Test
    fun `SearchCharactersUseCase addToHistory should call repository addSearchHistory`() = runTest {
        // Given
        val query = "Rick"
        val useCase = SearchCharactersUseCase(searchRepo)

        // When
        useCase.addToHistory(query)

        // Then
        coVerify { searchRepo.addSearchHistory(query) }
    }

    @Test
    fun `SearchCharactersUseCase removeFromHistory should call repository removeSearchHistory`() = runTest {
        // Given
        val query = "Rick"
        val useCase = SearchCharactersUseCase(searchRepo)

        // When
        useCase.removeFromHistory(query)

        // Then
        coVerify { searchRepo.removeSearchHistory(query) }
    }

    @Test
    fun `SearchCharactersUseCase getHistory should return search history flow`() = runTest {
        // Given
        val dummyHistory = listOf("Rick", "Morty")
        coEvery { searchRepo.getSearchHistory() } returns flowOf(dummyHistory)
        val useCase = SearchCharactersUseCase(searchRepo)

        // When
        val result = useCase.getHistory().first()

        // Then
        assertThat(result).containsExactly("Rick", "Morty")
    }

}