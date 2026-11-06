package com.mzantsi.table.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mzantsi.table.data.AppNotification
import com.mzantsi.table.data.NotificationType
import com.mzantsi.table.ui.theme.MzantsiCream
import com.mzantsi.table.ui.theme.MzantsiGold
import com.mzantsi.table.ui.theme.MzantsiGreen
import com.mzantsi.table.ui.theme.MzantsiGreenDark
import com.mzantsi.table.ui.theme.MzantsiOrange

@Composable
fun NotificationsScreen(
    notifications: List<AppNotification>,
    notificationsEnabled: Boolean,
    onNotificationClick: (AppNotification) -> Unit,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit
) {
    val unread = notifications.count { !it.read }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().background(MzantsiGreenDark).padding(horizontal = 8.dp, vertical = 8.dp)) {
            TextButton(onClick = onBack) { Text("← Back", color = Color.White) }
            Column(Modifier.padding(horizontal = 8.dp)) {
                Text("Notifications", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Text(
                    if (unread == 0) "You're all caught up" else "$unread new",
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (notifications.isNotEmpty()) {
                Row {
                    if (unread > 0) TextButton(onClick = onMarkAllRead) { Text("Mark all as read", color = Color.White) }
                    TextButton(onClick = onClearAll) { Text("Clear all", color = Color.White.copy(alpha = 0.8f)) }
                }
            } else {
                Spacer(Modifier.height(8.dp))
            }
        }

        if (!notificationsEnabled) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MzantsiCream)
                    .clickable(onClick = onOpenSettings)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.NotificationsOff, contentDescription = null, tint = MzantsiOrange)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Pop-up notifications are off. Tap to turn them on in your Profile.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (notifications.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(12.dp))
                Text("No notifications yet", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "You'll hear from us here when someone rates, cheers or comments on your attempts, " +
                        "reviews your recipes, or when a cook you follow adds something new.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(notifications, key = { it.id }) { n ->
                    NotificationRow(n) { onNotificationClick(n) }
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(n: AppNotification, onClick: () -> Unit) {
    val (icon, color) = iconFor(n.type)
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (n.read) Color.Transparent else MzantsiGreen.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                n.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (n.read) FontWeight.Normal else FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(n.body, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            Spacer(Modifier.height(4.dp))
            Text(notificationTimeAgo(n.createdAtMillis), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        if (!n.read) {
            Spacer(Modifier.width(8.dp))
            Box(Modifier.padding(top = 6.dp).size(10.dp).clip(CircleShape).background(MzantsiOrange))
        }
    }
}

private fun iconFor(type: NotificationType): Pair<ImageVector, Color> = when (type) {
    NotificationType.POST_RATED -> Icons.Filled.Star to MzantsiGold
    NotificationType.POST_CHEERED -> Icons.Filled.Celebration to MzantsiOrange
    NotificationType.POST_COMMENT -> Icons.Filled.ChatBubble to MzantsiGreen
    NotificationType.RECIPE_REVIEW -> Icons.Filled.RateReview to MzantsiGold
    NotificationType.NEW_RECIPE -> Icons.Filled.MenuBook to MzantsiGreen
    NotificationType.NEW_POST -> Icons.Filled.Groups to MzantsiGreenDark
}

private fun notificationTimeAgo(millis: Long): String {
    val minutes = ((System.currentTimeMillis() - millis) / 60_000L).coerceAtLeast(0)
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 24 * 60 -> "${minutes / 60} h ago"
        else -> "${minutes / (24 * 60)} d ago"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBell(unreadCount: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge { Text(if (unreadCount > 9) "9+" else unreadCount.toString()) }
                }
            }
        ) {
            Icon(
                Icons.Filled.Notifications,
                contentDescription = if (unreadCount > 0) "Notifications, $unreadCount new" else "Notifications",
                tint = Color.White
            )
        }
    }
}
