package com.takehomechallenge.arizona.presentation.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takehomechallenge.arizona.domain.usecase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.github.jan.supabase.auth.exception.AuthRestException

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val signInUseCase: SignInUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _uiState.update { it.copy(
                    user = user, 
                    isAuthenticated = user != null,
                    isChecking = false // Done checking
                ) }
            }
        }
    }

    fun signUp(email: String, password: String, username: String, fullName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                signUpUseCase(email, password, username, fullName)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                val errorMsg = when(e) {
                    is AuthRestException -> "Registration failed. Please try again."
                    else -> e.message ?: "Unknown error occurred"
                }
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                signInUseCase(email, password)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("Invalid login credentials", ignoreCase = true) == true -> 
                        "Email atau password salah, BRO! Cek lagi ya."
                    else -> "Login gagal: ${e.message}"
                }
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
        }
    }

    fun updateProfile(username: String, fullName: String, avatarUrl: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                updateProfileUseCase(username, fullName, avatarUrl)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun uploadAvatar(byteArray: ByteArray, fileName: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val url = uploadAvatarUseCase(byteArray, fileName)
                _uiState.update { it.copy(isLoading = false) }
                onSuccess(url)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Upload failed: ${e.message}") }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class AuthUiState(
    val user: com.takehomechallenge.arizona.domain.model.UserProfile? = null,
    val isAuthenticated: Boolean = false,
    val isChecking: Boolean = true, // Initial state is checking
    val isLoading: Boolean = false,
    val error: String? = null
)
