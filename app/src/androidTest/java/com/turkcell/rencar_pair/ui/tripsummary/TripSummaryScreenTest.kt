package com.turkcell.rencar_pair.ui.tripsummary

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
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
class TripSummaryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        state: TripSummaryUiState,
        onIntent: (TripSummaryIntent) -> Unit,
    ) {
        composeTestRule.setContent {
            RencarpairTheme {
                TripSummaryScreen(
                    state = state,
                    onIntent = onIntent,
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }

    @Test
    fun odemeYontemiSeciliyorVePaymentMethodSelectedTetiklenir() {
        val intents = mutableListOf<TripSummaryIntent>()
        setContent(state = TripSummaryUiState(totalPrice = 150.0), onIntent = { intents += it })

        composeTestRule.onNodeWithTag("trip_summary_payment_card").performClick()

        assertTrue(intents.contains(TripSummaryIntent.PaymentMethodSelected(PaymentMethod.CARD)))
    }

    @Test
    fun odeButonuTiklaninceyaPayClickedTetiklenir() {
        val intents = mutableListOf<TripSummaryIntent>()
        setContent(state = TripSummaryUiState(totalPrice = 150.0), onIntent = { intents += it })

        composeTestRule.onNodeWithTag("trip_summary_pay_button").performClick()

        assertTrue(intents.contains(TripSummaryIntent.PayClicked))
    }

    @Test
    fun odemeOnaylandiktanSonraAnaSayfayaDonTiklaninceDoneClickedTetiklenir() {
        val intents = mutableListOf<TripSummaryIntent>()
        setContent(state = TripSummaryUiState(totalPrice = 150.0, isPaid = true), onIntent = { intents += it })

        composeTestRule.onNodeWithTag("trip_summary_done_button").performClick()

        assertTrue(intents.contains(TripSummaryIntent.DoneClicked))
    }
}
