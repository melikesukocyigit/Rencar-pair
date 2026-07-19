package com.turkcell.rencar_pair.ui.reservation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
class ReservationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(onIntent: (ReservationIntent) -> Unit) {
        composeTestRule.setContent {
            var state by remember {
                mutableStateOf(
                    ReservationUiState(
                        vehicleId = "v1",
                        brand = "Renault",
                        model = "Clio",
                        plate = "34 AB 123",
                        pricePerDay = 720.0,
                    ),
                )
            }
            RencarpairTheme {
                ReservationScreen(
                    state = state,
                    onIntent = { intent ->
                        state = when (intent) {
                            is ReservationIntent.PlanSelected -> state.copy(selectedPlan = intent.plan)
                            is ReservationIntent.TermsToggled -> state.copy(termsAccepted = intent.accepted)
                            else -> state
                        }
                        onIntent(intent)
                    },
                    onBack = {},
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }

    @Test
    fun sartlarOnaylanmadanConfirmButonuPasiftir() {
        setContent(onIntent = {})

        composeTestRule.onNodeWithTag("reservation_confirm_button").assertIsNotEnabled()
    }

    @Test
    fun sartlarOnaylaninceConfirmAktifOlurVeTiklaninceConfirmClickedTetiklenir() {
        val intents = mutableListOf<ReservationIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("reservation_terms_checkbox").performClick()
        composeTestRule.onNodeWithTag("reservation_confirm_button").assertIsEnabled()
        composeTestRule.onNodeWithTag("reservation_confirm_button").performClick()

        assertTrue(intents.contains(ReservationIntent.ConfirmClicked))
    }

    @Test
    fun planSecildiginteSelectedIntentiTetiklenir() {
        val intents = mutableListOf<ReservationIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("reservation_plan_gunluk").performClick()

        assertTrue(intents.contains(ReservationIntent.PlanSelected(RentalPlan.GUNLUK)))
    }
}
