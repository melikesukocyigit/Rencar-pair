package com.turkcell.rencar_pair.ui.onboarding

data class OnboardingUiState(
    val currentPage: Int = 0,
    val totalPages: Int = 3,
)

sealed interface OnboardingIntent {
    data object StartRegister : OnboardingIntent
    data object GoToLogin     : OnboardingIntent
}

sealed interface OnboardingEffect {
    data object NavigateToRegister : OnboardingEffect
    data object NavigateToLogin    : OnboardingEffect
}
