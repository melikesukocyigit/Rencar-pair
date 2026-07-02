package com.turkcell.rencar_pair.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RegisterEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.FullNameChanged -> _uiState.update { it.copy(fullName = intent.value) }
            is RegisterIntent.EmailChanged    -> _uiState.update { it.copy(email = intent.value) }
            is RegisterIntent.PhoneChanged    -> _uiState.update {
                it.copy(phone = intent.value.filter(Char::isDigit).take(10))
            }
            is RegisterIntent.PasswordChanged -> _uiState.update { it.copy(password = intent.value) }
            is RegisterIntent.TogglePasswordVisibility -> _uiState.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }
            is RegisterIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isSubmitEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.register(
                email = state.email.trim(),
                password = state.password,
                fullName = state.fullName.trim(),
                phone = "+90${state.phone}",
            )
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { _effect.send(RegisterEffect.ShowSuccessAndNavigate) }
                .onFailure { _effect.send(RegisterEffect.ShowError(it.message ?: "Kayit basarisiz.")) }
        }
    }
}
