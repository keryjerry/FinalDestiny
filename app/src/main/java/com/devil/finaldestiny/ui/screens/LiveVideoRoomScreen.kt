package com.devil.finaldestiny.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.model.*
import com.devil.finaldestiny.ui.theme.*

@Composable
fun LiveVideoRoomScreen(
    room: LiveRoom,
    chatMessages: List<ChatMessage>,
    giftStoreItems: List<GiftItem>,
    isVisionSentinelActive: Boolean,
    isVisionBlackoutTriggered: Boolean,
    onTriggerSentinelTest: () -> Unit,
    onRestoreSentinel: () -> Unit,
    onSendGift: (GiftItem) -> Unit,
    onSendChatMessage: (String) -> Unit
) {
    var activeVideoTitle by remember { mutableStateOf("Co-Watching: Trending Music Video (Synced)") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGradient)
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        // AI Vision Sentinel Status Banner
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isVisionBlackoutTriggered) HeartRed else LiveIndicatorGreen)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isVisionBlackoutTriggered) "🚨 3S BLACKOUT" else "🛡️ SENTINEL ACTIVE (2.5s)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = WineRedDark
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(room.title, fontSize = 11.sp, color = LightGold, maxLines = 1)
            }

            Button(
                onClick = {
                    if (isVisionBlackoutTriggered) onRestoreSentinel() else onTriggerSentinelTest()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WineRedMedium),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text(if (isVisionBlackoutTriggered) "Restore Stream" else "Test AI Sentinel", fontSize = 10.sp, color = MetallicGold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Upper Stage: Host Video Stream & Synchronized YouTube Player Engine
        Card(
            colors = CardDefaults.cardColors(containerColor = WineRedDark),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .border(1.5.dp, MetallicGold, RoundedCornerShape(20.dp))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isVisionBlackoutTriggered) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🚫 STREAM BLACKOUT TRIGGERED", color = HeartRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("AI Vision Sentinel sampled unauthorized broadcast element.", color = LightGold, fontSize = 10.sp)
                            Text("Room suspended for 3 seconds.", color = DarkGold, fontSize = 10.sp)
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = MetallicGold, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(activeVideoTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LightGold)
                        Text("Synchronized Frame-Accurate YouTube Sync Player", fontSize = 10.sp, color = LiveIndicatorGreen)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 10-Guest Video Sofa Grid (Rounded Square Glassmorphic Tiles)
        Text("10-GUEST VIDEO SOFA TILES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MetallicGold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            items(room.seats) { seat ->
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(68.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x992A0510))
                        .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (seat.userProfile != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📹", fontSize = 14.sp)
                            Text(seat.userProfile.name, fontSize = 9.sp, color = LightGold, maxLines = 1)
                        }
                    } else {
                        Icon(Icons.Default.Add, contentDescription = "Empty", tint = LightGold.copy(0.4f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Gifting HUD Progress Bar
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("💎 Live Diamond Collection: 18,900", fontSize = 11.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
            Text("Target: 20,000", fontSize = 10.sp, color = LightGold.copy(0.8f))
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { 0.94f },
            color = MetallicGold,
            trackColor = WineRedDark,
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Video Room Chat Log Stream
        Text("LIVE ROOM CHAT & 1v1 CALLS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
        Spacer(modifier = Modifier.height(6.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackgroundTransparent),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .border(1.dp, CrimsonVelvet, RoundedCornerShape(14.dp))
                .padding(8.dp)
        ) {
            LazyColumn(
                reverseLayout = true,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(chatMessages.reversed()) { msg ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${msg.senderName}: ${msg.text}",
                            fontSize = 10.sp,
                            color = LightGold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { /* Invite Call */ },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier.height(20.dp)
                        ) {
                            Text("📞 Call Invite", fontSize = 9.sp, color = MetallicGold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
