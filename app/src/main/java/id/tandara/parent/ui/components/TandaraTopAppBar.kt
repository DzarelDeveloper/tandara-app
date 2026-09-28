package id.tandara.parent.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tandara.parent.core.designsystem.AppBackground
import id.tandara.parent.core.designsystem.BorderColor
import id.tandara.parent.core.designsystem.DividerColor
import id.tandara.parent.core.designsystem.ElevatedSurface
import id.tandara.parent.core.designsystem.ErrorRed
import id.tandara.parent.core.designsystem.PrimarySurface
import id.tandara.parent.core.designsystem.PrimaryText
import id.tandara.parent.core.designsystem.SecondarySurface
import id.tandara.parent.core.designsystem.SecondaryText

/**
 * Dark Navy Top App Bar for Tandara Parent App.
 * Displays Tandara logo, brand name, tagline "Pantau Langkah, Dukung Masa Depan", and notification bell.
 */
@Composable
fun TandaraBrandTopAppBar(
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
    tagline: String = "Pantau Langkah, Dukung Masa Depan",
    hasUnreadNotifications: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppBackground)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TandaraLogo(size = 36.dp)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tandara",
                    color = PrimaryText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 23.sp
                )
                Text(
                    text = tagline,
                    color = SecondaryText,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Notification Bell with 48dp Touch Target and unread indicator dot
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SecondarySurface)
                    .border(1.dp, BorderColor, CircleShape)
                    .clickable(onClick = onNotificationClick)
                    .testTag("home_notification_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifikasi",
                    tint = PrimaryText,
                    modifier = Modifier.size(22.dp)
                )
                if (hasUnreadNotifications) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .align(Alignment.TopEnd)
                            .padding(top = 10.dp, end = 10.dp)
                            .background(ErrorRed, CircleShape)
                            .border(1.dp, SecondarySurface, CircleShape)
                    )
                }
            }
        }

        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}

/**
 * Top App Bar for secondary screens with back navigation in dark theme.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TandaraTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppBackground)
            .statusBarsPadding()
    ) {
        TopAppBar(
            title = {
                if (subtitle != null) {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    )
                }
            },
            navigationIcon = {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("top_bar_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = PrimaryText
                        )
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppBackground,
                titleContentColor = PrimaryText,
                navigationIconContentColor = PrimaryText
            )
        )
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}
