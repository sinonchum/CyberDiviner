package com.cyberdiviner.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.CyberWhite
import com.cyberdiviner.ui.theme.GrayCaption
import com.cyberdiviner.ui.theme.HuiwenFontFamily

/**
 * Bottom navigation item definition.
 */
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
) {
    data object Oracle : BottomNavItem(
        route = Routes.ORACLE,
        label = "叩问天机",
        icon = {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "叩问天机"
            )
        }
    )

    data object Rituals : BottomNavItem(
        route = Routes.RITUALS,
        label = "术数推演",
        icon = {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "术数推演"
            )
        }
    )

    data object Archive : BottomNavItem(
        route = Routes.ARCHIVE,
        label = "因果命簿",
        icon = {
            Icon(
                imageVector = Icons.Default.Archive,
                contentDescription = "因果命簿"
            )
        }
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Oracle,
    BottomNavItem.Rituals,
    BottomNavItem.Archive
)

/**
 * Persistent bottom navigation bar — pure B&W, HuiwenFontFamily labels.
 */
@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = CyberBlack,
        contentColor = CyberWhite,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                icon = item.icon,
                label = {
                    Text(
                        text = item.label,
                        fontFamily = HuiwenFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                },
                selected = selected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CyberWhite,
                    unselectedIconColor = GrayCaption,
                    selectedTextColor = CyberWhite,
                    unselectedTextColor = GrayCaption,
                    indicatorColor = Color(0xFF111111)
                )
            )
        }
    }
}
