package com.turkcell.rencar_pair.ui.auth.license

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.turkcell.rencar_pair.ui.theme.RencarpairTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LicenseScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        state: LicenseUiState,
        onIntent: (LicenseIntent) -> Unit,
        onBack: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            RencarpairTheme {
                LicenseScreen(
                    state = state,
                    onIntent = onIntent,
                    onBack = onBack,
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }

    @Test
    fun ehliyetAdimindaGorsellerEksikkenSubmitButonuPasiftir() {
        setContent(
            state = LicenseUiState(currentStep = LicenseStep.EHLIYET, isSubmitEnabled = false),
            onIntent = {},
        )

        composeTestRule.onNodeWithTag("license_submit_button").assertIsNotEnabled()
    }

    @Test
    fun ehliyetAdimindaGorsellerTamamlaninceSubmitAktifOlurVeTiklaninceNextStepTetiklenir() {
        val intents = mutableListOf<LicenseIntent>()
        setContent(
            state = LicenseUiState(currentStep = LicenseStep.EHLIYET, isSubmitEnabled = true),
            onIntent = { intents += it },
        )

        composeTestRule.onNodeWithTag("license_submit_button").assertIsEnabled()
        composeTestRule.onNodeWithTag("license_submit_button").performClick()

        assertTrue(intents.contains(LicenseIntent.NextStepClicked))
    }

    @Test
    fun ehliyetAdimindaGeriButonuOnBackCagirir() {
        var backCalled = false
        setContent(
            state = LicenseUiState(currentStep = LicenseStep.EHLIYET),
            onIntent = {},
            onBack = { backCalled = true },
        )

        composeTestRule.onNodeWithTag("license_back_button").performClick()

        assertTrue(backCalled)
    }

    @Test
    fun onayAdimindaCikisYapTiklaninceLogoutTetiklenir() {
        val intents = mutableListOf<LicenseIntent>()
        setContent(
            state = LicenseUiState(currentStep = LicenseStep.ONAY, status = "UNDER_REVIEW"),
            onIntent = { intents += it },
        )

        composeTestRule.onNodeWithTag("license_logout_button").performClick()

        assertTrue(intents.contains(LicenseIntent.Logout))
    }
}
