package com.takehomechallenge.arizona.domain.usecase.auth

import com.takehomechallenge.arizona.domain.model.UserProfile
import com.takehomechallenge.arizona.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignUpUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, username: String, fullName: String) =
        repository.signUp(email, password, username, fullName)
}

class SignInUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) =
        repository.signIn(email, password)
}

class SignOutUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.signOut()
}

class GetCurrentUserUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(): Flow<UserProfile?> = repository.currentUser
}
