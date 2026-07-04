package com.turkcell.rencar_pair.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.data.wallet.PaymentCard
import com.turkcell.rencar_pair.data.wallet.WalletTransaction
import com.turkcell.rencar_pair.ui.navigation.NavigationTab
import com.turkcell.rencar_pair.ui.navigation.RencarBottomNavigation
import com.turkcell.rencar_pair.ui.theme.Primary
import com.turkcell.rencar_pair.ui.theme.SuccessDefault
import com.turkcell.rencar_pair.ui.theme.bodyM
import com.turkcell.rencar_pair.ui.theme.bodyS
import com.turkcell.rencar_pair.ui.theme.headingL
import com.turkcell.rencar_pair.ui.theme.headingXL
import com.turkcell.rencar_pair.ui.theme.labelM
import com.turkcell.rencar_pair.ui.theme.labelS
import com.turkcell.rencar_pair.ui.theme.titleL
import com.turkcell.rencar_pair.ui.theme.titleM
import com.turkcell.rencar_pair.ui.theme.titleS

@Composable
fun WalletRoute(
    onBack: () -> Unit,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WalletEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                is WalletEffect.NavigateBack -> onBack()
            }
        }
    }

    WalletScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onTabSelected = onTabSelected,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    state: WalletUiState,
    onIntent: (WalletIntent) -> Unit,
    onBack: () -> Unit,
    onTabSelected: (NavigationTab) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cüzdan",
                        style = headingXL,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            RencarBottomNavigation(
                selectedTab = NavigationTab.CUZDAN,
                onTabSelected = onTabSelected
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Rencar Balance Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1E7FE0), Color(0xFF0B6BCB))
                        )
                    )
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Rencar bakiyesi",
                        style = bodyS,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("₺%.2f", state.balance),
                        style = headingXL.copy(fontSize = 34.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .clickable { onIntent(WalletIntent.ToggleLoadBalanceDialog) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Bakiye Yükle",
                                style = titleS,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Registered Cards Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Kayıtlı kartlar",
                    style = titleM,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "+ Ekle",
                    style = titleS,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    modifier = Modifier.clickable { onIntent(WalletIntent.ToggleAddCardDialog) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(state.cards) { card ->
                    CardItem(card = card)
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Son işlemler",
                        style = titleM,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(11.dp))
                }

                if (state.transactions.isEmpty()) {
                    item {
                        Text(
                            text = "Henüz bir işlem bulunmuyor.",
                            style = bodyM,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(state.transactions) { transaction ->
                        TransactionItem(transaction = transaction)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 11.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // Card add dialog/sheet
        if (state.showAddCardDialog) {
            ModalBottomSheet(
                onDismissRequest = { onIntent(WalletIntent.ToggleAddCardDialog) },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Yeni Kart Ekle",
                        style = titleM,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.cardNoInput,
                        onValueChange = { onIntent(WalletIntent.CardNoChanged(it)) },
                        label = { Text("Kart Numarası") },
                        placeholder = { Text("4000 1234 5678 9010") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.cardExpiryInput,
                        onValueChange = { onIntent(WalletIntent.CardExpiryChanged(it)) },
                        label = { Text("Son Kullanma Tarihi") },
                        placeholder = { Text("MM/YY") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onIntent(WalletIntent.SubmitAddCard) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Kartı Kaydet", style = titleL)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Balance load dialog/sheet
        if (state.showLoadBalanceDialog) {
            ModalBottomSheet(
                onDismissRequest = { onIntent(WalletIntent.ToggleLoadBalanceDialog) },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Bakiye Yükle",
                        style = titleM,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.loadAmountInput,
                        onValueChange = { onIntent(WalletIntent.LoadAmountChanged(it)) },
                        label = { Text("Yüklenecek Tutar (TL)") },
                        placeholder = { Text("200") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Kullanılacak Kart",
                        style = labelM,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    state.cards.forEach { card ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (state.selectedCardId == card.id) Primary.copy(alpha = 0.1f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .clickable { onIntent(WalletIntent.SelectCardForLoad(card.id)) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CardIcon(type = card.type)
                                Text(
                                    text = card.number,
                                    style = titleS,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            RadioButton(
                                selected = state.selectedCardId == card.id,
                                onClick = { onIntent(WalletIntent.SelectCardForLoad(card.id)) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onIntent(WalletIntent.SubmitLoadBalance) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Yüklemeyi Onayla", style = titleL)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CardItem(card: PaymentCard) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
            .padding(13.dp, 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CardIcon(type = card.type)
            Column {
                Text(
                    text = card.number,
                    style = titleM,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Son kullanma ${card.expiryDate}",
                    style = bodyS,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (card.isDefault) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE7F4EC))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Varsayılan",
                    color = Color(0xFF1A9E63),
                    style = labelS,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun CardIcon(type: String) {
    val gradient = if (type == "VISA") {
        Brush.linearGradient(colors = listOf(Color(0xFF1A1F71), Color(0xFF0B6BCB)))
    } else {
        Brush.linearGradient(colors = listOf(Color(0xFFEB001B), Color(0xFFF79E1B)))
    }

    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (type == "VISA") "VISA" else "MC",
            color = Color.White,
            style = labelS.copy(fontSize = 9.sp),
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun TransactionItem(transaction: WalletTransaction) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        if (transaction.isIncome) Color(0xFFE7F4EC) else Color(0xFFFBEDED)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.isIncome) Icons.Default.Add else Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = if (transaction.isIncome) Color(0xFF1A9E63) else Color(0xFFE5484D),
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = transaction.title,
                    style = bodyM,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = transaction.date,
                    style = bodyS,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "${if (transaction.isIncome) "+" else "-"}₺${String.format("%.2f", transaction.amount)}",
            style = labelM,
            fontWeight = FontWeight.ExtraBold,
            color = if (transaction.isIncome) Color(0xFF1A9E63) else MaterialTheme.colorScheme.onBackground
        )
    }
}
