package com.turkcell.rencar_pair.ui.auth.otp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val phone: String = savedStateHandle["phone"] ?: ""

    private val _uiState = MutableStateFlow(OtpUiState(phone = phone))
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    private val _effect = Channel<OtpEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var timerJob: Job? = null

    init {
        startTimer()
    }

    fun onIntent(intent: OtpIntent) {
        when (intent) {
            is OtpIntent.OtpCodeChanged -> _uiState.update {
                it.copy(otpCode = intent.value.filter(Char::isDigit).take(6))
            }
            is OtpIntent.Submit      -> submit()
            is OtpIntent.ResendCode  -> resendCode()
            is OtpIntent.ChangePhone -> viewModelScope.launch { _effect.send(OtpEffect.NavigateBack) }
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isSubmitEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // AuthRepositoryImpl already caches fullName+phone into TokenManager on success
            val result = authRepository.verifyOtp("+90${state.phone}", state.otpCode)
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { role ->
                    _effect.send(OtpEffect.ShowSuccess("Giris basarili!"))
                    _effect.send(OtpEffect.NavigateToHome(role))
                }
                .onFailure { _effect.send(OtpEffect.ShowError(it.message ?: "Dogrulama basarisiz.")) }
        }
    }

    private fun resendCode() {
        val state = _uiState.value
        if (!state.isTimerExpired) return
        viewModelScope.launch {
            authRepository.login("+90${state.phone}")
            _uiState.update { it.copy(timerSeconds = 60, otpCode = "") }
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = _uiState.value.timerSeconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _uiState.update { it.copy(timerSeconds = remaining) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
