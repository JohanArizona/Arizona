package com.takehomechallenge.arizona.data

import com.google.common.truth.Truth.assertThat
import com.takehomechallenge.arizona.data.local.entity.CharacterEntity
import com.takehomechallenge.arizona.data.local.entity.FavoriteEntity
import com.takehomechallenge.arizona.data.repository.mapper.CharacterMapper
import com.takehomechallenge.arizona.domain.model.CharacterGender
import com.takehomechallenge.arizona.domain.model.CharacterStatus
import org.junit.Test

class MapperTest {

    // ── Helper: reusable base entity ─────────────────────────────────────────
    private fun buildEntity(
        status: String = "Alive",
        gender: String = "Male",
        isFavorite: Boolean = false
    ) = CharacterEntity(
        id          = 1,
        name        = "Morty",
        status      = status,
        species     = "Human",
        type        = "",
        gender      = gender,
        originName  = "Earth",
        locationName = "Earth",
        image       = "img.jpg",
        episode     = listOf("S01E01"),
        url         = "https://rickandmortyapi.com/api/character/1",
        page        = 1
    )

    // ════════════════════════════════════════════════════════════════════════
    // TC-UNIT-001 : CharacterMapper.mapEntityToDomain, mapper bisa dengan benar mengubah data karakter dari format database ke format yang dipakai aplikasi.
    // ngecek apakah mapper bisa mengubah data karakter dari format database ke format domain dengan benar.
    // Happy path-nya mastiin semua field kepindah dengan bener baik saat isFavorite = true maupun false.
    // Edge case-nya ngecek kalau list episode kosong tidak crash.
    // ════════════════════════════════════════════════════════════════════════

    // ── Happy path: isFavorite = true mastiin kalau semua field kepindah dengan bener dan status favoritnya ikut kebawa sebagai true ────────────────────────────────────────
    @Test
    fun `mapEntityToDomain converts all fields correctly when isFavorite is true`() {
        // Given
        val entity = buildEntity()

        // When
        val domain = CharacterMapper.mapEntityToDomain(entity, isFavorite = true)

        // Then — field by field
        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.name).isEqualTo("Morty")
        assertThat(domain.status).isEqualTo(CharacterStatus.Alive)
        assertThat(domain.species).isEqualTo("Human")
        assertThat(domain.gender).isEqualTo(CharacterGender.Male)
        assertThat(domain.originName).isEqualTo("Earth")
        assertThat(domain.locationName).isEqualTo("Earth")
        assertThat(domain.image).isEqualTo("img.jpg")
        assertThat(domain.episode).containsExactly("S01E01")
        assertThat(domain.isFavorite).isTrue()
    }

    // ── Happy path: isFavorite = false (default) mastiin flag favorit juga bisa kebawa sebagai false, bukan selalu true. ─────────────────────────────
    @Test
    fun `mapEntityToDomain sets isFavorite false when not favorited`() {
        // Given
        val entity = buildEntity()

        // When
        val domain = CharacterMapper.mapEntityToDomain(entity, isFavorite = false)

        // Then
        assertThat(domain.isFavorite).isFalse()
        assertThat(domain.name).isEqualTo("Morty")
    }

    // ── Edge case: episode list is emptylist episode kosong tidak menyebabkan crash ─────────────────────────────────────
    @Test
    fun `mapEntityToDomain handles empty episode list`() {
        // Given
        val entity = buildEntity().copy(episode = emptyList())

        // When
        val domain = CharacterMapper.mapEntityToDomain(entity)

        // Then
        assertThat(domain.episode).isEmpty()
    }

    // ════════════════════════════════════════════════════════════════════════
    // TC-UNIT-002 : CharacterStatus.fromString
    // ngecek apakah konversi string status ke enum aman untuk semua kemungkinan input.
    // Happy path-nya mastiin nilai valid seperti "Alive", "Dead", "unknown" terbaca dengan benar dan case-insensitive (jadi "ALIVE" dan "alive" tetap sama hasilnya).
    // Edge case-nya mastiin kalau inputnya aneh atau kosong, hasilnya jatuh ke Unknown dan tidak crash.
    // ════════════════════════════════════════════════════════════════════════

    // ── Happy path: valid status values ──────────────────────────────────────
    @Test
    fun `CharacterStatus fromString returns Alive for Alive string`() {
        assertThat(CharacterStatus.fromString("Alive")).isEqualTo(CharacterStatus.Alive)
    }

    @Test
    fun `CharacterStatus fromString returns Dead for Dead string`() {
        assertThat(CharacterStatus.fromString("Dead")).isEqualTo(CharacterStatus.Dead)
    }

    @Test
    fun `CharacterStatus fromString returns Unknown for unknown string`() {
        assertThat(CharacterStatus.fromString("unknown")).isEqualTo(CharacterStatus.Unknown)
    }

    // ── Edge case: invalid / unrecognized status → fallback Unknown ──────────
    @Test
    fun `CharacterStatus fromString returns Unknown for unrecognized value`() {
        assertThat(CharacterStatus.fromString("Mati Suri")).isEqualTo(CharacterStatus.Unknown)
    }

    @Test
    fun `CharacterStatus fromString returns Unknown for empty string`() {
        assertThat(CharacterStatus.fromString("")).isEqualTo(CharacterStatus.Unknown)
    }

    // ── Case-insensitive check ────────────────────────────────────────────────
    @Test
    fun `CharacterStatus fromString is case insensitive`() {
        assertThat(CharacterStatus.fromString("ALIVE")).isEqualTo(CharacterStatus.Alive)
        assertThat(CharacterStatus.fromString("dead")).isEqualTo(CharacterStatus.Dead)
    }

    // ════════════════════════════════════════════════════════════════════════
    // CharacterGender.fromString  (pendamping TC-UNIT-002, same mapper logic)
    // ════════════════════════════════════════════════════════════════════════

    // ── Happy path: valid gender values ──────────────────────────────────────
    @Test
    fun `CharacterGender fromString returns Male for Male string`() {
        assertThat(CharacterGender.fromString("Male")).isEqualTo(CharacterGender.Male)
    }

    @Test
    fun `CharacterGender fromString returns Female for Female string`() {
        assertThat(CharacterGender.fromString("Female")).isEqualTo(CharacterGender.Female)
    }

    @Test
    fun `CharacterGender fromString returns Genderless for Genderless string`() {
        assertThat(CharacterGender.fromString("Genderless")).isEqualTo(CharacterGender.Genderless)
    }

    // ── Edge case: invalid gender → fallback Unknown ─────────────────────────
    @Test
    fun `CharacterGender fromString returns Unknown for unrecognized value`() {
        assertThat(CharacterGender.fromString("???")).isEqualTo(CharacterGender.Unknown)
    }

    @Test
    fun `CharacterGender fromString returns Unknown for empty string`() {
        assertThat(CharacterGender.fromString("")).isEqualTo(CharacterGender.Unknown)
    }

    // ════════════════════════════════════════════════════════════════════════
    // mapFavoriteEntityToDomain
    // ════════════════════════════════════════════════════════════════════════

    // ── Happy path: favorite entity maps correctly ────────────────────────────
    @Test
    fun `mapFavoriteEntityToDomain always sets isFavorite true`() {
        // Given
        val favoriteEntity = FavoriteEntity(
            id           = 2,
            name         = "Rick",
            status       = "Alive",
            species      = "Human",
            gender       = "Male",
            originName   = "Earth",
            locationName = "Citadel of Ricks",
            image        = "rick.jpg"
        )

        // When
        val domain = CharacterMapper.mapFavoriteEntityToDomain(favoriteEntity)

        // Then
        assertThat(domain.id).isEqualTo(2)
        assertThat(domain.name).isEqualTo("Rick")
        assertThat(domain.isFavorite).isTrue()
        assertThat(domain.status).isEqualTo(CharacterStatus.Alive)
        assertThat(domain.episode).isEmpty()  // FavoriteEntity tidak menyimpan episode
        assertThat(domain.url).isEmpty()      // FavoriteEntity tidak menyimpan url
    }

    // ════════════════════════════════════════════════════════════════════════
    // mapDomainToFavoriteEntity
    // ════════════════════════════════════════════════════════════════════════

    // ── Happy path: domain maps ke FavoriteEntity dengan benar ───────────────
    @Test
    fun `mapDomainToFavoriteEntity converts domain to FavoriteEntity correctly`() {
        // Given
        val domain = CharacterMapper.mapEntityToDomain(buildEntity(), isFavorite = true)

        // When
        val favoriteEntity = CharacterMapper.mapDomainToFavoriteEntity(domain)

        // Then
        assertThat(favoriteEntity.id).isEqualTo(domain.id)
        assertThat(favoriteEntity.name).isEqualTo(domain.name)
        assertThat(favoriteEntity.status).isEqualTo(domain.status.value)
        assertThat(favoriteEntity.gender).isEqualTo(domain.gender.value)
        assertThat(favoriteEntity.image).isEqualTo(domain.image)
    }
}