package com.turkcell.rencar_pair.ui.auth.otp

data class OtpUiState(
    val phone: String = "",
    val otpCode: String = "",
    val timerSeconds: Int = 60,
    val isLoading: Boolean = false,
) {
    val isSubmitEnabled: Boolean get() = otpCode.length == 6 && !isLoading
    val isTimerExpired: Boolean get() = timerSeconds == 0

    val displayPhone: String
        get() {
            if (phone.length != 10) return "+90 $phone"
            return "+90 ${phone.substring(0, 3)} ${phone.substring(3, 6)} " +
                    "${phone.substring(6, 8)} ${phone.substring(8, 10)}"
        }
}

sealed interface OtpIntent {
    data class OtpCodeChanged(val value: String) : OtpIntent
    data object Submit : OtpIntent
    data object ResendCode : OtpIntent
    data object ChangePhone : OtpIntent
}

sealed interface OtpEffect {
    data class NavigateToHome(val role: String) : OtpEffect
    data object NavigateBack : OtpEffect
    data class ShowError(val message: String) : OtpEffect
    data class ShowSuccess(val message: String) : OtpEffect
}
