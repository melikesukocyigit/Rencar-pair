package com.turkcell.rencar_pair.ui.auth.login

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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.PhoneNumberChanged -> _uiState.update {
                it.copy(phoneNumber = intent.value.filter(Char::isDigit).take(10))
            }
            is LoginIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isSubmitEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.login("+90${state.phoneNumber}")
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { _effect.send(LoginEffect.NavigateToOtp(state.phoneNumber)) }
                .onFailure { _effect.send(LoginEffect.ShowError(it.message ?: "Bir hata olustu.")) }
        }
    }
}
