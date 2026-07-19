package com.turkcell.rencar_pair.ui.splash

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.turkcell.rencar_pair.ui.theme.RencarpairTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun splashEkraniHazirDurumdaGoruntulenir() {
        composeTestRule.setContent {
            RencarpairTheme {
                SplashScreen(state = SplashUiState(isReady = true))
            }
        }

        composeTestRule.onNodeWithTag("splash_root").assertIsDisplayed()
    }
}
