package com.turkcell.rencar_pair.ui.payment.checkout

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
class PaymentCheckoutScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun kapatButonuTiklaninceCloseClickedTetiklenir() {
        val intents = mutableListOf<PaymentCheckoutIntent>()
        composeTestRule.setContent {
            RencarpairTheme {
                PaymentCheckoutScreen(
                    state = PaymentCheckoutUiState(isLoading = false, paymentPageUrl = null),
                    onIntent = { intents += it },
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }

        composeTestRule.onNodeWithTag("payment_checkout_close_button").performClick()

        assertTrue(intents.contains(PaymentCheckoutIntent.CloseClicked))
    }
}
