package com.turkcell.rencar_pair.ui.onboarding

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.turkcell.rencar_pair.ui.theme.RencarpairTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(onIntent: (OnboardingIntent) -> Unit) {
        composeTestRule.setContent {
            RencarpairTheme {
                OnboardingScreen(
                    state = OnboardingUiState(),
                    onIntent = onIntent,
                )
            }
        }
    }

    @Test
    fun hemenBaslaTiklaninceStartRegisterTetiklenir() {
        val intents = mutableListOf<OnboardingIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("onboarding_start_button").performClick()

        assertTrue(intents.contains(OnboardingIntent.StartRegister))
    }

    @Test
    fun girisYapLinkineTiklaninceGoToLoginTetiklenir() {
        val intents = mutableListOf<OnboardingIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("onboarding_login_link").performTouchInput {
            click(Offset(width * 0.9f, height / 2f))
        }

        assertTrue(intents.contains(OnboardingIntent.GoToLogin))
    }
}
