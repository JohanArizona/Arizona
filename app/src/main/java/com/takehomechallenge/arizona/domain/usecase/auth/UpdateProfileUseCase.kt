package com.takehomechallenge.arizona.domain.usecase.auth

import com.takehomechallenge.arizona.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(username: String, fullName: String, avatarUrl: String?) =
        repository.updateProfile(username, fullName, avatarUrl)
}
