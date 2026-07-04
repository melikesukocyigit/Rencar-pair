package com.turkcell.rencar_pair.ui.profile

// ─── UiState ──────────────────────────────────────────────────────────────────

data class ProfileUiState(
    val userName: String        = "",
    val userPhone: String       = "",
    // License info — in future replace with a real API call
    val isLicenseApproved: Boolean = true,
    val licenseClass: String    = "B sınıfı · geçerli",
)

// ─── Intent ───────────────────────────────────────────────────────────────────

sealed interface ProfileIntent {
    data object Logout      : ProfileIntent
    data object EditProfile : ProfileIntent
}

// ─── Effect ───────────────────────────────────────────────────────────────────

sealed interface ProfileEffect {
    data object NavigateToOnboarding : ProfileEffect
}
