package com.example.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.constants.AppIcons
import com.example.core.theme.Dimensions
import com.example.core.theme.Spacing
import com.example.shared.buttons.IconButtonWidget
import com.example.shared.chips.NotificationBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommercialAppBar(
    businessName: String,
    currentDateText: String,
    notificationCount: Int = 3,
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    testTag: String = "commercial_app_bar",
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Store Avatar Icon
                Surface(
                    shape = RoundedCornerShape(Dimensions.radius12),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = businessName.take(1).uppercase().ifEmpty { "S" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(Spacing.m))
                Column {
                    Text(
                        text = businessName.ifEmpty { "SmartBiz Store" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentDateText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            // Search Button Action
            IconButtonWidget(
                icon = AppIcons.Search,
                contentDescription = "Search",
                onClick = onSearchClick,
                testTag = "app_bar_search_button"
            )

            // Notifications Action with Badge Overlay
            Box(contentAlignment = Alignment.TopEnd) {
                IconButtonWidget(
                    icon = AppIcons.Notification,
                    contentDescription = "Notifications",
                    onClick = onNotificationClick,
                    testTag = "app_bar_notification_button"
                )
                if (notificationCount > 0) {
                    NotificationBadge(
                        count = notificationCount,
                        modifier = Modifier.padding(top = 4.dp, end = 4.dp)
                    )
                }
            }

            // Profile Action Button
            IconButtonWidget(
                icon = AppIcons.Business,
                contentDescription = "Profile",
                onClick = onProfileClick,
                testTag = "app_bar_profile_button"
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.testTag(testTag)
    )
}
