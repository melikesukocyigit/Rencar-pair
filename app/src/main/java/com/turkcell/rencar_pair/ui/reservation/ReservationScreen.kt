package com.turkcell.rencar_pair.ui.reservation

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.ui.theme.BackgroundDark
import com.turkcell.rencar_pair.ui.theme.BorderSubtleDark
import com.turkcell.rencar_pair.ui.theme.Primary
import com.turkcell.rencar_pair.ui.theme.SuccessBackgroundDark
import com.turkcell.rencar_pair.ui.theme.SuccessStrongDark
import com.turkcell.rencar_pair.ui.theme.SurfaceDark
import com.turkcell.rencar_pair.ui.theme.SurfaceElevatedDark
import com.turkcell.rencar_pair.ui.theme.TextHintDark
import com.turkcell.rencar_pair.ui.theme.TextOnPrimary
import com.turkcell.rencar_pair.ui.theme.TextPrimaryDark
import com.turkcell.rencar_pair.ui.theme.TextSecondaryDark
import com.turkcell.rencar_pair.ui.theme.TextTertiaryDark
import com.turkcell.rencar_pair.ui.theme.bodyS
import com.turkcell.rencar_pair.ui.theme.headingXL
import com.turkcell.rencar_pair.ui.theme.labelS
import com.turkcell.rencar_pair.ui.theme.labelXS
import com.turkcell.rencar_pair.ui.theme.titleL
import com.turkcell.rencar_pair.ui.theme.titleXS

// VehicleDetailBottomSheet ile ayni gerekce: gercek API (VehicleResponseDto) yakit
// yuzdesi, vites tipi ve koltuk sayisi saglamiyor; ayni onaylanmis placeholder degerler
// burada da kullaniliyor (docs/decisions.md - "Arac Detay Bottom Sheet" karari).
private const val PLACEHOLDER_FUEL_PERCENT = 72
private const val PLACEHOLDER_TRANSMISSION = "Manuel"
private const val PLACEHOLDER_SEAT_COUNT = 5

@Composable
fun ReservationRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReservationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReservationEffect.ShowSuccessAndNavigateBack -> {
                    snackbarHostState.showSnackbar(effect.message)
                    onBack()
                }
                is ReservationEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    ReservationScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun ReservationScreen(
    state: ReservationUiState,
    onIntent: (ReservationIntent) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevatedDark)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = TextPrimaryDark,
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(text = "Rezervasyon Onayı", style = headingXL, color = TextPrimaryDark)
            }

            VehicleSummaryCard(
                brand = state.brand,
                model = state.model,
                plate = state.plate,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Kiralama planı", style = titleL, color = TextPrimaryDark)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PlanOptionCard(
                    label = "Dakikalık",
                    priceLabel = "₺${"%.2f".format(state.pricePerMinute).replace('.', ',')}/dk",
                    selected = state.selectedPlan == RentalPlan.DAKIKALIK,
                    onClick = { onIntent(ReservationIntent.PlanSelected(RentalPlan.DAKIKALIK)) },
                    modifier = Modifier.weight(1f),
                )
                PlanOptionCard(
                    label = "Saatlik",
                    priceLabel = "₺${state.pricePerHour.toInt()}/sa",
                    selected = state.selectedPlan == RentalPlan.SAATLIK,
                    onClick = { onIntent(ReservationIntent.PlanSelected(RentalPlan.SAATLIK)) },
                    modifier = Modifier.weight(1f),
                )
                PlanOptionCard(
                    label = "Günlük",
                    priceLabel = "₺${state.pricePerDay.toInt()}",
                    selected = state.selectedPlan == RentalPlan.GUNLUK,
                    onClick = { onIntent(ReservationIntent.PlanSelected(RentalPlan.GUNLUK)) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Tahmini ücret (${state.estimatedDurationLabel})",
                    style = bodyS,
                    color = TextTertiaryDark,
                )
                Text(
                    text = "~₺${state.estimatedCost.toInt()}",
                    style = titleL,
                    color = TextPrimaryDark,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.clickable {
                    onIntent(ReservationIntent.TermsToggled(!state.termsAccepted))
                },
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (state.termsAccepted) Primary else SurfaceElevatedDark),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.termsAccepted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = TextOnPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Kullanım şartlarını ve kasko/sigorta koşullarını okudum, onaylıyorum.",
                    style = bodyS,
                    color = TextSecondaryDark,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onIntent(ReservationIntent.ConfirmClicked) },
                enabled = state.isConfirmEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = TextOnPrimary),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        color = TextOnPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Text(text = "Rezervasyonu Tamamla", style = titleL, color = TextOnPrimary)
                }
            }
        }
    }
}

@Composable
private fun VehicleSummaryCard(
    brand: String,
    model: String,
    plate: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val resId = remember(brand, model) {
        val resourceName = "car_${brand}_${model}".lowercase().replace(" ", "_")
        context.resources.getIdentifier(resourceName, "drawable", context.packageName)
            .takeIf { it != 0 }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceDark)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceElevatedDark),
            contentAlignment = Alignment.Center,
        ) {
            if (resId != null) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "$brand $model",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = TextTertiaryDark,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "$brand $model", style = titleL, color = TextPrimaryDark)
            Text(
                text = "$plate · $PLACEHOLDER_TRANSMISSION · $PLACEHOLDER_SEAT_COUNT kişi",
                style = bodyS,
                color = TextTertiaryDark,
                modifier = Modifier.padding(top = 2.dp),
            )
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(SuccessBackgroundDark)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(text = "Yakıt %$PLACEHOLDER_FUEL_PERCENT", style = labelXS, color = SuccessStrongDark)
            }
        }
    }
}

@Composable
private fun PlanOptionCard(
    label: String,
    priceLabel: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) Primary else BorderSubtleDark
    val textColor = if (selected) Primary else TextSecondaryDark

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .border(width = 1.7.dp, color = borderColor, shape = RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = label, style = labelS, color = if (selected) TextPrimaryDark else TextHintDark)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = priceLabel, style = titleXS, color = textColor)
    }
}
