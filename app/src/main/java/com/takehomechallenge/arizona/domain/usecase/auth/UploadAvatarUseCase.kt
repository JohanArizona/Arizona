package com.takehomechallenge.arizona.domain.usecase.auth

import com.takehomechallenge.arizona.domain.repository.AuthRepository
import javax.inject.Inject

class UploadAvatarUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(byteArray: ByteArray, fileName: String): String =
        repository.uploadAvatar(byteArray, fileName)
}
