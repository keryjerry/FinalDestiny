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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.model.AppNotification
import com.devil.finaldestiny.model.UserProfile
import com.devil.finaldestiny.ui.components.ProfileAvatarView
import com.devil.finaldestiny.ui.theme.*

data class ActivityNotificationItem(
    val id: String,
    val senderName: String,
    val senderAvatar: String?,
    val actionText: String,
    val relativeTime: String,
    val timeGroup: String, // "New", "Last 7 days", "Last 30 days"
    val actionType: String, // "FOLLOW", "MESSAGE", "MEDIA_THUMBNAIL"
    val mediaThumbnailUrl: String? = null,
    var isFollowingBack: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    user: UserProfile = UserProfile(),
    notificationsList: List<AppNotification> = emptyList(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    val sampleNotifications = remember {
        listOf(
            ActivityNotificationItem("n1", "Aria Rose", null, "liked your reel video 🎬", "2h ago", "New", "MEDIA_THUMBNAIL", "https://picsum.photos/100/100?random=301"),
            ActivityNotificationItem("n2", "Farman Ali", null, "started following you", "5h ago", "New", "FOLLOW"),
            ActivityNotificationItem("n3", "Simran Kaur", null, "commented: \"Amazing room vibes! 🔥\"", "1d ago", "Last 7 days", "MEDIA_THUMBNAIL", "https://picsum.photos/100/100?random=302"),
            ActivityNotificationItem("n4", "Aarav Sharma", null, "tipped 100 Diamonds 💎 in Live Room", "3d ago", "Last 7 days", "MESSAGE"),
            ActivityNotificationItem("n5", "Riya Kapoor", null, "started following you", "12d ago", "Last 30 days", "FOLLOW"),
            ActivityNotificationItem("n6", "Vikram Malhotra", null, "liked your photo post 📸", "18d ago", "Last 30 days", "MEDIA_THUMBNAIL", "https://picsum.photos/100/100?random=303")
        )
    }

    val groupedMap = sampleNotifications.groupBy { it.timeGroup }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlueBgLight)
    ) {
        // TOP HEADER BAR WITH UNIVERSAL BACK ARROW ('<') & PAGE TITLE "Notifications"
        Surface(
            color = SkyBlueCardBg,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
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
        }

        // GROUPED ACTIVITY LIST ("New", "Last 7 days", "Last 30 days")
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            listOf("New", "Last 7 days", "Last 30 days").forEach { groupKey ->
                val itemsInGroup = groupedMap[groupKey] ?: emptyList()
                if (itemsInGroup.isNotEmpty()) {
                    item {
                        Text(
                            text = groupKey,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(itemsInGroup) { item ->
                        var isFollowing by remember { mutableStateOf(item.isFollowingBack) }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SkyBlueCardBg),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SkyBlueBorder, RoundedCornerShape(16.dp))
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
                                    ProfileAvatarView(
                                        name = item.senderName,
                                        profilePictureUri = item.senderAvatar,
                                        size = 44.dp,
                                        showBorder = true,
                                        borderColor = SkyBluePrimary
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = buildString {
                                                append(item.senderName)
                                                append(" ")
                                                append(item.actionText)
                                            },
                                            fontSize = 12.sp,
                                            color = NavyTextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.relativeTime,
                                            fontSize = 10.sp,
                                            color = SlateTextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Action Button / Media Thumbnail Preview on Far Right
                                when (item.actionType) {
                                    "FOLLOW" -> {
                                        Button(
                                            onClick = {
                                                isFollowing = !isFollowing
                                                Toast.makeText(context, if (isFollowing) "Followed ${item.senderName}" else "Unfollowed ${item.senderName}", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isFollowing) SkyBlueHeader else SkyBluePrimary
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text(
                                                text = if (isFollowing) "✓ Following" else "Follow Back",
                                                fontSize = 10.sp,
                                                color = if (isFollowing) NavyTextPrimary else Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    "MESSAGE" -> {
                                        Button(
                                            onClick = {
                                                Toast.makeText(context, "Opening Chat with ${item.senderName}", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlueHeader),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text(
                                                text = "Message 💬",
                                                fontSize = 10.sp,
                                                color = NavyTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    "MEDIA_THUMBNAIL" -> {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(NavyTextPrimary)
                                                .border(1.dp, SkyBlueBorder, RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = "Media Preview",
                                                tint = HeartRed,
                                                modifier = Modifier.size(18.dp)
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
}
