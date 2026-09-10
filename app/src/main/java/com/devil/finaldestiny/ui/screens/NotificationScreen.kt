package com.devil.finaldestiny.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.devil.finaldestiny.model.AppNotification
import com.devil.finaldestiny.model.UserProfile
import com.devil.finaldestiny.ui.components.ProfileAvatarView
import com.devil.finaldestiny.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    user: UserProfile = UserProfile(),
    notificationsList: List<AppNotification> = emptyList(),
    onRefresh: suspend () -> Unit = {},
    onNavigateToFeed: () -> Unit = {},
    onSelectNotification: (AppNotification) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlueBgLight)
    ) {
        // TOP HEADER BAR WITH UNIVERSAL BACK ARROW ('<') & HOME FEED ICON
        Surface(
            color = SkyBlueCardBg,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SkyBlueHeader)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NavyTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Notifications",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyTextPrimary
                        )
                        Text(
                            text = user.handle,
                            fontSize = 11.sp,
                            color = SlateTextSecondary
                        )
                    }
                }

                // Right Action Icon: Home / Feed Shortcut
                IconButton(
                    onClick = onNavigateToFeed,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SkyBlueHeader)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home Feed",
                        tint = NavyTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    try {
                        onRefresh()
                    } finally {
                        isRefreshing = false
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            if (notificationsList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notifications yet ✨",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notificationsList) { notification ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SkyBlueCardBg),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SkyBlueBorder, RoundedCornerShape(16.dp))
                                .clickable { onSelectNotification(notification) }
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (!notification.senderAvatarUrl.isNullOrBlank()) {
                                        ProfileAvatarView(
                                            name = notification.title,
                                            profilePictureUri = notification.senderAvatarUrl,
                                            size = 40.dp
                                        )
                                    } else {
                                        Text(
                                            text = notification.iconSymbol.ifBlank { "🔔" },
                                            fontSize = 24.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notification.title,
                                            fontSize = 13.sp,
                                            color = NavyTextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = notification.message,
                                            fontSize = 11.sp,
                                            color = NavyTextPrimary.copy(0.85f)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = notification.timestamp,
                                            fontSize = 10.sp,
                                            color = SlateTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
