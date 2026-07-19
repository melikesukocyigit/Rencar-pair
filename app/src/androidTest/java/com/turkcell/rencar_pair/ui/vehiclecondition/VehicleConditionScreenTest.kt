package com.turkcell.rencar_pair.ui.vehiclecondition

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
class VehicleConditionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        state: VehicleConditionUiState,
        onIntent: (VehicleConditionIntent) -> Unit,
    ) {
        composeTestRule.setContent {
            RencarpairTheme {
                VehicleConditionScreen(
                    state = state,
                    onIntent = onIntent,
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }

    @Test
    fun fotolarEksikkenConfirmButonuPasiftir() {
        setContent(state = VehicleConditionUiState(checkedSides = emptySet()), onIntent = {})

        composeTestRule.onNodeWithTag("vehicle_condition_confirm_button").assertIsNotEnabled()
    }

    @Test
    fun dortYonDeCekilinceConfirmAktifOlurVeTiklaninceConfirmClickedTetiklenir() {
        val intents = mutableListOf<VehicleConditionIntent>()
        setContent(
            state = VehicleConditionUiState(checkedSides = VehicleSide.entries.toSet()),
            onIntent = { intents += it },
        )

        composeTestRule.onNodeWithTag("vehicle_condition_confirm_button").assertIsEnabled()
        composeTestRule.onNodeWithTag("vehicle_condition_confirm_button").performClick()

        assertTrue(intents.contains(VehicleConditionIntent.ConfirmClicked))
    }

    @Test
    fun geriButonuTiklaninceBackClickedTetiklenir() {
        val intents = mutableListOf<VehicleConditionIntent>()
        setContent(state = VehicleConditionUiState(), onIntent = { intents += it })

        composeTestRule.onNodeWithTag("vehicle_condition_back_button").performClick()

        assertTrue(intents.contains(VehicleConditionIntent.BackClicked))
    }
}
