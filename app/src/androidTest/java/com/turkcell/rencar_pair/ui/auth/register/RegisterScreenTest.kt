package com.turkcell.rencar_pair.ui.auth.register

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.turkcell.rencar_pair.ui.theme.RencarpairTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(onIntent: (RegisterIntent) -> Unit) {
        composeTestRule.setContent {
            var state by remember { mutableStateOf(RegisterUiState()) }
            RencarpairTheme {
                RegisterScreen(
                    state = state,
                    onIntent = { intent ->
                        state = when (intent) {
                            is RegisterIntent.FullNameChanged -> state.copy(fullName = intent.value)
                            is RegisterIntent.EmailChanged -> state.copy(email = intent.value)
                            is RegisterIntent.PhoneChanged -> state.copy(phone = intent.value)
                            is RegisterIntent.PasswordChanged -> state.copy(password = intent.value)
                            RegisterIntent.TogglePasswordVisibility ->
                                state.copy(isPasswordVisible = !state.isPasswordVisible)
                            else -> state
                        }
                        onIntent(intent)
                    },
                    onBack = {},
                    onNavigateToLogin = {},
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }

    @Test
    fun formBoskenSubmitButonuPasiftir() {
        setContent(onIntent = {})

        composeTestRule.onNodeWithTag("register_submit_button").assertIsNotEnabled()
    }

    @Test
    fun gecerliFormDolduruluncaSubmitAktifOlurVeTiklaninceSubmitIntentiTetiklenir() {
        val intents = mutableListOf<RegisterIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("register_fullname_input").performTextInput("Ahmet Yılmaz")
        composeTestRule.onNodeWithTag("register_email_input").performTextInput("ahmet@test.com")
        composeTestRule.onNodeWithTag("register_phone_input").performTextInput("5320000000")
        composeTestRule.onNodeWithTag("register_password_input").performTextInput("123456")

        composeTestRule.onNodeWithTag("register_submit_button").assertIsEnabled()
        composeTestRule.onNodeWithTag("register_submit_button").performClick()

        assertTrue(intents.contains(RegisterIntent.Submit))
    }

    @Test
    fun sifreGosterGizleTiklaninceToggleIntentiTetiklenir() {
        val intents = mutableListOf<RegisterIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("register_password_toggle").performClick()

        assertTrue(intents.contains(RegisterIntent.TogglePasswordVisibility))
    }

    @Test
    fun girisYapLinkineTiklaninceNavigasyonTetiklenir() {
        var navigated = false
        composeTestRule.setContent {
            RencarpairTheme {
                RegisterScreen(
                    state = RegisterUiState(),
                    onIntent = {},
                    onBack = {},
                    onNavigateToLogin = { navigated = true },
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }

        composeTestRule.onNodeWithTag("register_login_link").performTouchInput {
            click(Offset(width * 0.9f, height / 2f))
        }

        assertTrue(navigated)
    }
}
