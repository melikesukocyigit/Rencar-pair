package com.turkcell.rencar_pair.ui.auth.login

data class LoginUiState(
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
) {
    val isSubmitEnabled: Boolean get() = phoneNumber.length == 10 && !isLoading
}

sealed interface LoginIntent {
    data class PhoneNumberChanged(val value: String) : LoginIntent
    data object Submit : LoginIntent
}

sealed interface LoginEffect {
    data class NavigateToOtp(val phone: String) : LoginEffect
    data class ShowError(val message: String) : LoginEffect
}
