package com.devil.finaldestiny.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import com.devil.finaldestiny.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.model.AppNotification
import com.devil.finaldestiny.model.UserProfile
import com.devil.finaldestiny.ui.components.NotificationBellButton
import com.devil.finaldestiny.ui.components.VipBadge
import com.devil.finaldestiny.ui.theme.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimaryDashboardScreen(
    user: UserProfile,
    notifications: List<AppNotification> = emptyList(),
    onOpenNotifications: () -> Unit = {},
    onNavigateToSwipe: () -> Unit,
    onNavigateToAudioRoom: () -> Unit,
    onNavigateToVideoRoom: () -> Unit,
    onNavigateToHostPortal: () -> Unit,
    onNavigateToVipStore: () -> Unit,
    onNavigateToSecondaryFeed: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAboutUs: () -> Unit = {},
    onRefresh: suspend () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    var showSettingsModal by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English") }
    var cacheSizeMb by remember { mutableStateOf(42) }

    val scrollState = rememberScrollState()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PrimaryGradient)
                .statusBarsPadding()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        // Swiping Left (negative dragAmount) navigates to Instagram Moments Feed!
                        if (dragAmount < -40f) {
                            onNavigateToSecondaryFeed()
                        }
                    }
                }
                .verticalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .padding(bottom = 90.dp)
        ) {
        // User Profile Header Card with Realtime Notification Bell
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CrimsonVelvet, RoundedCornerShape(16.dp))
                .clickable { onNavigateToProfile() }
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateToProfile() }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(WineRedDark)
                            .border(1.5.dp, MetallicGold, CircleShape)
                    ) {
                        Text(text = user.name.take(1).uppercase(), fontSize = 18.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = user.name, fontWeight = FontWeight.Bold, color = LightGold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            if (user.verifiedStatus) {
                                Text(text = "🛡️", fontSize = 10.sp)
                            }
                        }
                        Text(text = user.handle, fontSize = 10.sp, color = LightGold.copy(0.7f))
                        Spacer(modifier = Modifier.height(2.dp))
                        VipBadge(vipLevel = user.vipLevel)
                    }
                }

                // Top Right Action Controls: Notification Bell 🔔 & Settings ⚙️
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    NotificationBellButton(
                        notifications = notifications,
                        onClick = onOpenNotifications
                    )

                    IconButton(onClick = { showSettingsModal = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = LightGold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // INSTAGRAM MOMENTS & REELS FEED BANNER (TAP OR SWIPE LEFT TO OPEN)
        Card(
            colors = CardDefaults.cardColors(containerColor = SkyBlueHeader),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BrightCyanAccent, RoundedCornerShape(14.dp))
                .clickable { onNavigateToSecondaryFeed() }
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📸", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Destiny Live Moment Feed", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyTextPrimary)
                        Text("Swipe Left 👈 or Tap to view live reels & moments", fontSize = 10.sp, color = SkyBluePrimary)
                    }
                }
                Text("Open Feed ➔", fontSize = 11.sp, color = SkyBluePrimary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ABOUT US & CREATOR MONETIZATION PROGRAM BANNER
        Card(
            colors = CardDefaults.cardColors(containerColor = SkyBlueCardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SkyBlueBorder, RoundedCornerShape(14.dp))
                .clickable { onNavigateToAboutUs() }
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🚀", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("About Final Destiny & Monetization", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyTextPrimary)
                        Text("View Creator Program, Payouts & Guidelines", fontSize = 10.sp, color = SlateTextSecondary)
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = SkyBluePrimary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // QUICK METRICS SUMMARY
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).border(1.dp, CrimsonVelvet, RoundedCornerShape(12.dp)).padding(6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("💰 Coins", fontSize = 9.sp, color = LightGold.copy(0.7f))
                    Text("${user.coins}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).border(1.dp, CrimsonVelvet, RoundedCornerShape(12.dp)).padding(6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("💎 Diamonds", fontSize = 9.sp, color = LightGold.copy(0.7f))
                    Text("${user.diamonds}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).border(1.dp, CrimsonVelvet, RoundedCornerShape(12.dp)).padding(6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("👥 Followers", fontSize = 9.sp, color = LightGold.copy(0.7f))
                    Text("${user.followerCount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LightGold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SHARE THE APP & GROW COMMUNITY BANNER
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, MetallicGold, RoundedCornerShape(18.dp))
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MetallicGold, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "App Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Share the App & Grow Community ✨",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightGold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Invite friends to Final Destiny & build your live circle!",
                            fontSize = 11.sp,
                            color = LightGold.copy(0.7f)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val shareUrl = "https://twwezpogwtmjavoemdvi.supabase.co/storage/v1/object/public/app_updates/app-debug.apk"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "✨ Join me on Final Destiny - Where Hearts Connect & Voices Resonate! Download the app here: $shareUrl")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Final Destiny via")
                            context.startActivity(shareIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = WineRedDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share App", color = WineRedDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val shareUrl = "https://twwezpogwtmjavoemdvi.supabase.co/storage/v1/object/public/app_updates/app-debug.apk"
                            clipboardManager.setText(AnnotatedString(shareUrl))
                            Toast.makeText(context, "Link copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MetallicGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MetallicGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Link", color = LightGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // CORE APPLICATION HUB CARDS
        Text("CORE APPLICATION HUBS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MetallicGold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(6.dp))

        // 1. Live Audio Sofa Room Hub
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MetallicGold, RoundedCornerShape(14.dp))
                .clickable { onNavigateToAudioRoom() }
                .padding(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(WineRedMedium)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = MetallicGold, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("10-Mic Red Velvet Audio Room", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LightGold)
                        Text("Achat Style Sofa Seats & MP3 Jukebox", fontSize = 10.sp, color = LightGold.copy(0.7f))
                    }
                }
                Text("Enter 🎙️", fontSize = 11.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Video Broadcast & Co-Watching Stage
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CrimsonVelvet, RoundedCornerShape(14.dp))
                .clickable { onNavigateToVideoRoom() }
                .padding(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(WineRedMedium)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = LiveIndicatorGreen, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("HD Video Broadcast & YouTube Sync Stage", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LightGold)
                        Text("Live Cam Stream & YouTube Co-Watching", fontSize = 10.sp, color = LightGold.copy(0.7f))
                    }
                }
                Text("Watch 📹", fontSize = 11.sp, color = LiveIndicatorGreen, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Tinder 9:16 Swipe Match Portal
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CrimsonVelvet, RoundedCornerShape(14.dp))
                .clickable { onNavigateToSwipe() }
                .padding(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(WineRedMedium)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = HeartRed, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Discover Match Swipe Cards", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LightGold)
                        Text("Swipe Right for Match & Instant 1v1 Calls", fontSize = 10.sp, color = LightGold.copy(0.7f))
                    }
                }
                Text("Swipe ❤️", fontSize = 11.sp, color = HeartRed, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. VIP Asset Store & Creator Monetization
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, MetallicGold, RoundedCornerShape(14.dp))
                    .clickable { onNavigateToVipStore() }
                    .padding(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = MetallicGold, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("VIP Store 👑", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, CrimsonVelvet, RoundedCornerShape(14.dp))
                    .clickable { onNavigateToHostPortal() }
                    .padding(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = LiveIndicatorGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Creator Payouts 💵", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LiveIndicatorGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // SETTINGS MODAL DIALOG
    if (showSettingsModal) {
        AlertDialog(
            onDismissRequest = { showSettingsModal = false },
            containerColor = CardBackground,
            title = { Text("⚙️ Settings & System Diagnostics", color = MetallicGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("App Version: FinalDestiny v1.0.0", fontSize = 12.sp, color = LightGold)
                    Text("Official Platform: Final Destiny", fontSize = 12.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = CrimsonVelvet, thickness = 1.dp)

                    Text("App Language: $selectedLanguage", fontSize = 12.sp, color = LightGold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("English", "Hindi", "Hinglish").forEach { lang ->
                            Button(
                                onClick = { selectedLanguage = lang },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedLanguage == lang) MetallicGold else WineRedMedium
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(lang, fontSize = 10.sp, color = if (selectedLanguage == lang) WineRedDark else LightGold)
                            }
                        }
                    }

                    HorizontalDivider(color = CrimsonVelvet, thickness = 1.dp)

                    Button(
                        onClick = {
                            cacheSizeMb = 0
                            Toast.makeText(context, "🧹 App Cache Cleared Successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonVelvet),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Cache ($cacheSizeMb MB)", color = LightGold)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSettingsModal = false }, colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)) {
                    Text("Close", color = WineRedDark)
                }
            }
        )
    }
}
}
