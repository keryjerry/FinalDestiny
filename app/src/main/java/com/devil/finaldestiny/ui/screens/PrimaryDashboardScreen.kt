package com.devil.finaldestiny.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.model.UserProfile
import com.devil.finaldestiny.ui.components.VipBadge
import com.devil.finaldestiny.ui.theme.*

@Composable
fun PrimaryDashboardScreen(
    user: UserProfile,
    onNavigateToSwipe: () -> Unit,
    onNavigateToAudioRoom: () -> Unit,
    onNavigateToVideoRoom: () -> Unit,
    onNavigateToHostPortal: () -> Unit,
    onNavigateToVipStore: () -> Unit,
    onNavigateToSecondaryFeed: () -> Unit
) {
    var showSettingsModal by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English") }
    var cacheSizeMb by remember { mutableStateOf(42) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGradient)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // User Profile Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CrimsonVelvet, RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(WineRedDark)
                            .border(2.dp, MetallicGold, CircleShape)
                    ) {
                        Text(text = user.name.take(1), fontSize = 22.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = user.name, fontWeight = FontWeight.Bold, color = LightGold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            if (user.verifiedStatus) {
                                Text(text = "🛡️", fontSize = 12.sp)
                            }
                        }
                        Text(text = user.handle, fontSize = 12.sp, color = LightGold.copy(0.7f))
                        Spacer(modifier = Modifier.height(4.dp))
                        VipBadge(vipLevel = user.vipLevel)
                    }
                }

                IconButton(onClick = { showSettingsModal = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MetallicGold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "DASHBOARD CONTROL MATRIX",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MetallicGold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Grid Row 1: Find Match & Who Liked
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                DashboardGridTile(
                    title = "FIND YOUR MATCH",
                    subtitle = "Swipe dating deck",
                    icon = "🔥",
                    badge = "NEW",
                    gradient = Brush.linearGradient(listOf(WineRedMedium, CrimsonVelvet)),
                    onClick = onNavigateToSwipe
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .border(1.dp, CrimsonVelvet, RoundedCornerShape(18.dp))
                        .clickable { onNavigateToSwipe() }
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("WHO LIKED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                            Text("14 Views", fontSize = 10.sp, color = LightGold.copy(0.7f))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("P1", "P2", "P3").forEach { _ ->
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .blur(4.dp)
                                        .clip(CircleShape)
                                        .background(WineRedLight)
                                ) {
                                    Text("👤", fontSize = 14.sp)
                                }
                            }
                        }

                        Text("Tap to reveal profiles 🔒", fontSize = 10.sp, color = LightGold.copy(0.8f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid Row 2: Audio Room & Video Live Hub
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                DashboardGridTile(
                    title = "AUDIO PARTY ROOM",
                    subtitle = "10-Mic Red Sofa Live",
                    icon = "🛋️",
                    badge = "LIVE",
                    gradient = Brush.linearGradient(listOf(WineRedMedium, SeatVelvetBg)),
                    onClick = onNavigateToAudioRoom
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                DashboardGridTile(
                    title = "VIDEO LIVE HUB",
                    subtitle = "YouTube Player Sync",
                    icon = "🎬",
                    badge = "SYNC",
                    gradient = Brush.linearGradient(listOf(CrimsonVelvet, WineRedDark)),
                    onClick = onNavigateToVideoRoom
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid Row 3: Wallet Hub & Become a Host
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .border(1.dp, MetallicGold.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .clickable { onNavigateToVipStore() }
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("WALLET HUB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                            Text("💎 Store", fontSize = 10.sp, color = MetallicGold)
                        }

                        Column {
                            Text("🪙 ${user.coins} Coins", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LightGold)
                            Text("💎 ${user.diamonds} Diamonds", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LightGold)
                        }

                        Button(
                            onClick = onNavigateToVipStore,
                            colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Recharge Gateway", fontSize = 10.sp, color = WineRedDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .border(1.dp, CrimsonVelvet, RoundedCornerShape(18.dp))
                        .clickable { onNavigateToHostPortal() }
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("BECOME A HOST", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                            Text("25% Share", fontSize = 10.sp, color = LiveIndicatorGreen, fontWeight = FontWeight.Bold)
                        }

                        Column {
                            Text("${user.followerCount} / 100 Followers", fontSize = 11.sp, color = LightGold)
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (user.followerCount.toFloat() / 100f).coerceAtMost(1.0f) },
                                color = LiveIndicatorGreen,
                                trackColor = WineRedDark,
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                            )
                        }

                        Text("Aadhaar/PAN Payouts ->", fontSize = 10.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Settings Modal
    if (showSettingsModal) {
        AlertDialog(
            onDismissRequest = { showSettingsModal = false },
            containerColor = CardBackground,
            title = { Text("User Settings & Security", color = MetallicGold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Language: $selectedLanguage", color = LightGold, fontSize = 13.sp)
                    Row {
                        listOf("English", "Hindi", "Punjabi", "Tamil").forEach { lang ->
                            TextButton(onClick = { selectedLanguage = lang }) {
                                Text(lang, color = if (selectedLanguage == lang) MetallicGold else LightGold.copy(0.7f), fontSize = 11.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = CrimsonVelvet)

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Storage Cache Cleaner", color = LightGold, fontSize = 13.sp)
                        Text("${cacheSizeMb}MB", color = MetallicGold, fontSize = 13.sp)
                    }
                    Button(
                        onClick = { cacheSizeMb = 0 },
                        colors = ButtonDefaults.buttonColors(containerColor = WineRedLight)
                    ) {
                        Text("Clear Cache", color = LightGold, fontSize = 11.sp)
                    }

                    HorizontalDivider(color = CrimsonVelvet)

                    Text("Blocked Users & Privacy Disclaimers", color = LightGold, fontSize = 12.sp)
                    Text("🔒 1v1 Calls strictly require recipient acceptance.", color = MetallicGold, fontSize = 11.sp)
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

@Composable
private fun DashboardGridTile(
    title: String,
    subtitle: String,
    icon: String,
    badge: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(gradient)
            .border(1.dp, CrimsonVelvet, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(icon, fontSize = 22.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MetallicGold)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = WineRedDark)
                }
            }

            Column {
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                Text(subtitle, fontSize = 10.sp, color = LightGold.copy(0.85f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
