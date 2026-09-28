package id.tandara.parent.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tandara.parent.core.designsystem.AccentBlue
import id.tandara.parent.core.designsystem.DividerColor
import id.tandara.parent.core.designsystem.MutedText
import id.tandara.parent.core.designsystem.PrimaryBlue
import id.tandara.parent.core.designsystem.PrimarySurface
import id.tandara.parent.core.designsystem.SecondarySurface
import id.tandara.parent.core.designsystem.TandaraTheme
import id.tandara.parent.core.navigation.Screen

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val testTag: String
) {
    data object Home : BottomNavItem(
        route = Screen.Home.route,
        title = "Beranda",
        testTag = "nav_tab_beranda"
    )

    data object Reports : BottomNavItem(
        route = Screen.Reports.route,
        title = "Laporan",
        testTag = "nav_tab_laporan"
    )

    data object Permission : BottomNavItem(
        route = Screen.Permission.route,
        title = "Izin",
        testTag = "nav_tab_izin"
    )

    data object Settings : BottomNavItem(
        route = Screen.Settings.route,
        title = "Pengaturan",
        testTag = "nav_tab_pengaturan"
    )

    companion object {
        val items = listOf(Home, Reports, Permission, Settings)
    }
}

/**
 * Dark fixed bottom navigation for Tandara Parent App.
 * Clean, structured design without giant pills.
 */
@Composable
fun TandaraBottomNavigation(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PrimarySurface)
            .navigationBarsPadding()
            .testTag("tandara_bottom_nav")
    ) {
        HorizontalDivider(color = DividerColor, thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.items.forEach { item ->
                val isSelected = currentRoute == item.route
                val activeColor = TandaraTheme.colors.primary
                val inactiveColor = TandaraTheme.colors.textMuted

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (!isSelected) {
                                onNavigateToRoute(item.route)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag(item.testTag)
                ) {
                    when (item) {
                        BottomNavItem.Home -> {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = item.title,
                                tint = if (isSelected) activeColor else inactiveColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        BottomNavItem.Reports -> {
                            Icon(
                                imageVector = Icons.Outlined.BarChart,
                                contentDescription = item.title,
                                tint = if (isSelected) activeColor else inactiveColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        BottomNavItem.Permission -> {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.Assignment else Icons.Outlined.Description,
                                contentDescription = item.title,
                                tint = if (isSelected) activeColor else inactiveColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        BottomNavItem.Settings -> {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = item.title,
                                tint = if (isSelected) activeColor else inactiveColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.title,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) activeColor else inactiveColor
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // Subtle active indicator dot instead of giant pill
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                color = if (isSelected) activeColor else Color.Transparent,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }
    }
}
