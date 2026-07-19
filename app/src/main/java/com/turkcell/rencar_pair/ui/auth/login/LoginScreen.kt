package com.turkcell.rencar_pair.ui.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.ui.theme.Primary
import com.turkcell.rencar_pair.ui.theme.RencarpairTheme
import com.turkcell.rencar_pair.ui.theme.TextOnPrimary
import com.turkcell.rencar_pair.ui.theme.bodyM
import com.turkcell.rencar_pair.ui.theme.bodyS
import com.turkcell.rencar_pair.ui.theme.headingXL
import com.turkcell.rencar_pair.ui.theme.labelM
import com.turkcell.rencar_pair.ui.theme.titleL
import com.turkcell.rencar_pair.ui.theme.titleS

@Composable
fun LoginRoute(
    onNavigateToOtp: (phone: String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToOtp -> onNavigateToOtp(effect.phone)
                is LoginEffect.ShowError     -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    LoginScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onNavigateToRegister = onNavigateToRegister,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onIntent: (LoginIntent) -> Unit,
    onBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            BackButton(onClick = onBack)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Tekrar hoş geldin",
                style = headingXL,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Telefon numarını gir, SMS ile doğrulama kodu gönderelim.",
                style = bodyM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Telefon numarası",
                style = labelM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            PhoneInputRow(
                phoneNumber = state.phoneNumber,
                onPhoneNumberChange = { onIntent(LoginIntent.PhoneNumberChanged(it)) },
                onDone = { onIntent(LoginIntent.Submit); keyboardController?.hide() },
            )

            Spacer(modifier = Modifier.height(8.dp))

            InfoRow()

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onIntent(LoginIntent.Submit); keyboardController?.hide() },
                enabled = state.isSubmitEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("login_submit_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = TextOnPrimary,
                    disabledContainerColor = Primary.copy(alpha = 0.4f),
                    disabledContentColor = TextOnPrimary.copy(alpha = 0.6f),
                ),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = TextOnPrimary,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(text = "Kod Gönder", style = titleL, color = TextOnPrimary)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            RegisterLinkRow(
                onRegisterClick = onNavigateToRegister,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .testTag("login_back_button"),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Geri",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun PhoneInputRow(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeBorder = Primary
    val idleBorder = MaterialTheme.colorScheme.outline

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .height(54.dp)
                .width(90.dp)
                .border(1.dp, idleBorder, RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "TR  +90",
                style = titleS,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        val fieldBorder = if (phoneNumber.isNotEmpty()) activeBorder else idleBorder

        BasicTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .border(1.dp, fieldBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp)
                .testTag("login_phone_input"),
            textStyle = bodyM.copy(color = MaterialTheme.colorScheme.onSurface),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (phoneNumber.isEmpty()) {
                        Text(
                            text = "532 000 00 00",
                            style = bodyM,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun InfoRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp).padding(top = 1.dp),
        )
        Text(
            text = "6 haneli kodu bu numaraya göndereceğiz. SMS ücreti operatörüne bağlıdır.",
            style = bodyS,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RegisterLinkRow(
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val annotatedText = buildAnnotatedString {
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
            append("Hesabın yok mu? ")
        }
        withStyle(SpanStyle(color = Primary)) {
            append("Kayıt ol")
        }
    }

    androidx.compose.foundation.text.ClickableText(
        text = annotatedText,
        style = bodyM.copy(textAlign = TextAlign.Center),
        modifier = modifier.testTag("login_register_link"),
        onClick = { offset ->
            val registerStart = annotatedText.indexOf("Kayıt ol")
            if (offset >= registerStart) onRegisterClick()
        },
    )
}

@Preview(showBackground = true, name = "Login - Light")
@Composable
private fun LoginScreenLightPreview() {
    RencarpairTheme(darkTheme = false) {
        LoginScreen(
            state = LoginUiState(phoneNumber = "5320000000"),
            onIntent = {},
            onBack = {},
            onNavigateToRegister = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0F14, name = "Login - Dark")
@Composable
private fun LoginScreenDarkPreview() {
    RencarpairTheme(darkTheme = true) {
        LoginScreen(
            state = LoginUiState(),
            onIntent = {},
            onBack = {},
            onNavigateToRegister = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
