package com.turkcell.rencar_pair.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.turkcell.rencar_pair.ui.theme.Primary

enum class NavigationTab {
    HARITA,
    GECMIS,
    CUZDAN,
    PROFIL
}

@Composable
fun RencarBottomNavigation(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = modifier
    ) {
        NavigationBarItem(
            selected = selectedTab == NavigationTab.HARITA,
            onClick = { onTabSelected(NavigationTab.HARITA) },
            icon = { 
                Icon(
                    imageVector = Icons.Default.LocationOn, 
                    contentDescription = "Harita"
                ) 
            },
            label = { Text("Harita") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary,
                selectedTextColor = Primary,
                unselectedIconColor = Color(0xFF8A929E),
                unselectedTextColor = Color(0xFF8A929E),
                indicatorColor = MaterialTheme.colorScheme.surface
            )
        )
        NavigationBarItem(
            selected = selectedTab == NavigationTab.GECMIS,
            onClick = { onTabSelected(NavigationTab.GECMIS) },
            icon = { 
                Icon(
                    imageVector = Icons.Default.History, 
                    contentDescription = "Geçmiş"
                ) 
            },
            label = { Text("Geçmiş") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary,
                selectedTextColor = Primary,
                unselectedIconColor = Color(0xFF8A929E),
                unselectedTextColor = Color(0xFF8A929E),
                indicatorColor = MaterialTheme.colorScheme.surface
            )
        )
        NavigationBarItem(
            selected = selectedTab == NavigationTab.CUZDAN,
            onClick = { onTabSelected(NavigationTab.CUZDAN) },
            icon = { 
                Icon(
                    imageVector = Icons.Default.CreditCard, 
                    contentDescription = "Cüzdan"
                ) 
            },
            label = { Text("Cüzdan") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary,
                selectedTextColor = Primary,
                unselectedIconColor = Color(0xFF8A929E),
                unselectedTextColor = Color(0xFF8A929E),
                indicatorColor = MaterialTheme.colorScheme.surface
            )
        )
        NavigationBarItem(
            selected = selectedTab == NavigationTab.PROFIL,
            onClick = { onTabSelected(NavigationTab.PROFIL) },
            icon = { 
                Icon(
                    imageVector = Icons.Default.Person, 
                    contentDescription = "Profil"
                ) 
            },
            label = { Text("Profil") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary,
                selectedTextColor = Primary,
                unselectedIconColor = Color(0xFF8A929E),
                unselectedTextColor = Color(0xFF8A929E),
                indicatorColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}
