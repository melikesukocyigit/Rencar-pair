package com.turkcell.rencar_pair.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.ui.navigation.NavigationTab
import com.turkcell.rencar_pair.ui.navigation.RencarBottomNavigation
import com.turkcell.rencar_pair.ui.theme.*

// ─── Route ────────────────────────────────────────────────────────────────────

@Composable
fun ProfileRoute(
    onNavigateToOnboarding: () -> Unit,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.NavigateToOnboarding -> onNavigateToOnboarding()
            }
        }
    }

    ProfileScreen(
        state             = uiState,
        onIntent          = viewModel::onIntent,
        onTabSelected     = onTabSelected,
        snackbarHostState = snackbarHostState,
        modifier          = modifier,
    )
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    onTabSelected: (NavigationTab) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        snackbarHost    = { SnackbarHost(snackbarHostState) },
        containerColor  = BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Profil",
                        style = headingXL,
                        color = TextPrimaryLight,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight,
                ),
            )
        },
        bottomBar = {
            RencarBottomNavigation(
                selectedTab   = NavigationTab.PROFIL,
                onTabSelected = onTabSelected,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── User header card ──────────────────────────────────────────────
            UserHeaderCard(
                name    = state.userName,
                phone   = state.userPhone,
                onEdit  = { onIntent(ProfileIntent.EditProfile) },
            )

            // ── License status card ───────────────────────────────────────────
            LicenseCard(
                isApproved   = state.isLicenseApproved,
                licenseClass = state.licenseClass,
            )

            // ── Menu items card ───────────────────────────────────────────────
            MenuCard(onTabSelected = onTabSelected)

            // ── Logout card ───────────────────────────────────────────────────
            LogoutCard(onLogout = { onIntent(ProfileIntent.Logout) })

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─── User Header Card ─────────────────────────────────────────────────────────

@Composable
private fun UserHeaderCard(
    name: String,
    phone: String,
    onEdit: () -> Unit,
) {
    val initials = name
        .trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Avatar with gradient + initials
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF5C7BF0), Color(0xFF3B5CE6))
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text  = initials,
                style = titleL.copy(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold),
                color = Color.White,
            )
        }

        // Name + phone
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text     = name.ifBlank { "Kullanıcı" },
                style    = headingM.copy(fontSize = 18.sp),
                color    = TextPrimaryLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text  = phone.ifBlank { "-" },
                style = bodyS,
                color = TextHintLight,
            )
        }

        // Edit button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, BorderDefaultLight, RoundedCornerShape(10.dp))
                .clickable(onClick = onEdit),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Outlined.Edit,
                contentDescription = "Profili düzenle",
                tint               = TextTertiaryLight,
                modifier           = Modifier.size(18.dp),
            )
        }
    }
}

// ─── License Card ─────────────────────────────────────────────────────────────

@Composable
private fun LicenseCard(isApproved: Boolean, licenseClass: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Shield icon bubble
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SuccessBackgroundLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Outlined.Shield,
                contentDescription = null,
                tint               = SuccessStrong,
                modifier           = Modifier.size(22.dp),
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text  = "Ehliyet doğrulandı",
                style = titleS.copy(fontWeight = FontWeight.Bold),
                color = TextPrimaryLight,
            )
            Text(
                text  = licenseClass,
                style = labelS,
                color = TextHintLight,
            )
        }

        // Badge
        if (isApproved) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SuccessBackgroundLight)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(
                    text  = "Onaylı",
                    style = labelMicro.copy(fontWeight = FontWeight.ExtraBold),
                    color = SuccessStrong,
                )
            }
        }
    }
}

// ─── Menu Card ────────────────────────────────────────────────────────────────

private data class MenuItem(
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val label: String,
)

@Composable
private fun MenuCard(onTabSelected: (NavigationTab) -> Unit) {
    val items = listOf(
        MenuItem(Icons.Outlined.CreditCard, Primary, Primary.copy(alpha = 0.10f), "Ödeme yöntemleri"),
        MenuItem(Icons.Outlined.Settings,   TextTertiaryLight, Color(0xFFF0F2F6), "Ayarlar"),
        MenuItem(Icons.AutoMirrored.Outlined.HelpOutline, TextTertiaryLight, Color(0xFFF0F2F6), "Yardım & destek"),
        MenuItem(Icons.Outlined.CardGiftcard, TextTertiaryLight, Color(0xFFF0F2F6), "Davet et · ₺50 kazan"),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White),
    ) {
        items.forEachIndexed { index, item ->
            MenuRow(item = item, onClick = {
                // Ödeme yöntemleri → cüzdan sekmesi
                if (index == 0) onTabSelected(NavigationTab.CUZDAN)
            })
            if (index < items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 58.dp),
                    color    = DividerLight,
                )
            }
        }
    }
}

@Composable
private fun MenuRow(item: MenuItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Icon bubble
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(item.iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = item.icon,
                contentDescription = null,
                tint               = item.iconTint,
                modifier           = Modifier.size(19.dp),
            )
        }

        Text(
            text     = item.label,
            style    = bodyM.copy(fontWeight = FontWeight.Medium),
            color    = TextPrimaryLight,
            modifier = Modifier.weight(1f),
        )

        Icon(
            imageVector        = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint               = TextHintLight,
            modifier           = Modifier.size(20.dp),
        )
    }
}

// ─── Logout Card ──────────────────────────────────────────────────────────────

@Composable
private fun LogoutCard(onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .clickable(onClick = onLogout)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector        = Icons.AutoMirrored.Outlined.Logout,
            contentDescription = null,
            tint               = ErrorDefault,
            modifier           = Modifier.size(20.dp),
        )
        Text(
            text  = "Çıkış yap",
            style = bodyM.copy(fontWeight = FontWeight.Bold),
            color = ErrorDefault,
        )
    }
}
