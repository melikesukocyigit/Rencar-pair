package com.turkcell.rencar_pair.ui.auth.register

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
) {
    val isSubmitEnabled: Boolean
        get() = fullName.isNotBlank()
                && email.contains("@")
                && phone.length == 10
                && password.length >= 6
                && !isLoading
}

sealed interface RegisterIntent {
    data class FullNameChanged(val value: String) : RegisterIntent
    data class EmailChanged(val value: String) : RegisterIntent
    data class PhoneChanged(val value: String) : RegisterIntent
    data class PasswordChanged(val value: String) : RegisterIntent
    data object TogglePasswordVisibility : RegisterIntent
    data object Submit : RegisterIntent
}

sealed interface RegisterEffect {
    data object ShowSuccessAndNavigate : RegisterEffect
    data class ShowError(val message: String) : RegisterEffect
}
