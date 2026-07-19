package com.turkcell.rencar_pair.ui.auth.otp

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.outlined.Timer
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.ui.theme.Primary
import com.turkcell.rencar_pair.ui.theme.RencarpairTheme
import com.turkcell.rencar_pair.ui.theme.TextOnPrimary
import com.turkcell.rencar_pair.ui.theme.bodyM
import com.turkcell.rencar_pair.ui.theme.bodyS
import com.turkcell.rencar_pair.ui.theme.headingL
import com.turkcell.rencar_pair.ui.theme.titleL
import com.turkcell.rencar_pair.ui.theme.titleM

@Composable
fun OtpRoute(
    onNavigateToHome: (role: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OtpViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OtpEffect.ShowSuccess    -> snackbarHostState.showSnackbar(effect.message)
                is OtpEffect.NavigateToHome -> onNavigateToHome(effect.role)
                is OtpEffect.NavigateBack   -> onNavigateBack()
                is OtpEffect.ShowError      -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    OtpScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun OtpScreen(
    state: OtpUiState,
    onIntent: (OtpIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try { focusRequester.requestFocus() } catch (_: Exception) { }
    }

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

            BackButton(onClick = { onIntent(OtpIntent.ChangePhone) })

            Spacer(modifier = Modifier.height(24.dp))

            PhoneIconBox()

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Telefonunu doğrula",
                style = headingL,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${state.displayPhone} numarasına gönderdiğimiz 6 haneli kodu gir.",
                style = bodyM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            OtpCodeInput(
                otpCode = state.otpCode,
                onOtpCodeChange = { onIntent(OtpIntent.OtpCodeChanged(it)) },
                focusRequester = focusRequester,
            )

            Spacer(modifier = Modifier.height(16.dp))

            ResendRow(
                timerSeconds = state.timerSeconds,
                isTimerExpired = state.isTimerExpired,
                onResendClick = { onIntent(OtpIntent.ResendCode) },
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onIntent(OtpIntent.Submit); keyboardController?.hide() },
                enabled = state.isSubmitEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("otp_submit_button"),
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
                    Text(text = "Doğrula ve Devam Et", style = titleL, color = TextOnPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ChangePhoneRow(
                onChangeClick = { onIntent(OtpIntent.ChangePhone) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.weight(1f))
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
            .testTag("otp_back_button"),
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
private fun PhoneIconBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.PhoneAndroid,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(26.dp),
        )
    }
}

@Composable
private fun OtpCodeInput(
    otpCode: String,
    onOtpCodeChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        BasicTextField(
            value = otpCode,
            onValueChange = onOtpCodeChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester)
                .testTag("otp_code_input"),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(6) { index ->
                val digit = otpCode.getOrNull(index)?.toString() ?: ""
                val isActive = index == otpCode.length
                val isFilled = digit.isNotEmpty()

                val borderColor = when {
                    isActive -> Primary
                    isFilled -> MaterialTheme.colorScheme.outline
                    else     -> MaterialTheme.colorScheme.outline
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .border(
                            width = if (isActive) 1.5.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { focusRequester.requestFocus() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isActive && digit.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .height(20.dp)
                                .background(Primary),
                        )
                    } else if (digit.isNotEmpty()) {
                        Text(
                            text = digit,
                            style = titleM.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResendRow(
    timerSeconds: Int,
    isTimerExpired: Boolean,
    onResendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val minutes = timerSeconds / 60
    val seconds = timerSeconds % 60
    val timerText = "%d:%02d".format(minutes, seconds)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Timer,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
        if (isTimerExpired) {
            Text(
                text = "Kodu tekrar gönder",
                style = bodyS.copy(color = Primary),
                modifier = Modifier
                    .clickable(onClick = onResendClick)
                    .testTag("otp_resend_button"),
            )
        } else {
            Text(
                text = "Kodu tekrar gönder · $timerText",
                style = bodyS,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("otp_resend_timer"),
            )
        }
    }
}

@Composable
private fun ChangePhoneRow(
    onChangeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val annotatedText = buildAnnotatedString {
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
            append("Numara yanlıs mı? ")
        }
        withStyle(SpanStyle(color = Primary)) {
            append("Değiştir")
        }
    }

    androidx.compose.foundation.text.ClickableText(
        text = annotatedText,
        style = bodyM.copy(textAlign = TextAlign.Center),
        modifier = modifier.testTag("otp_change_phone_button"),
        onClick = { offset ->
            val start = annotatedText.indexOf("Değiştir")
            if (offset >= start) onChangeClick()
        },
    )
}

@Preview(showBackground = true, name = "OTP - Light")
@Composable
private fun OtpScreenLightPreview() {
    RencarpairTheme(darkTheme = false) {
        OtpScreen(
            state = OtpUiState(phone = "5320000000", otpCode = "482", timerSeconds = 42),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0F14, name = "OTP - Dark")
@Composable
private fun OtpScreenDarkPreview() {
    RencarpairTheme(darkTheme = true) {
        OtpScreen(
            state = OtpUiState(phone = "5320000000", otpCode = "", timerSeconds = 60),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
