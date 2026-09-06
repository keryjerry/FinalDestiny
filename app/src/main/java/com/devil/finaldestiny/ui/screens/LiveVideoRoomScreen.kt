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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    var youtubeUrlInput by remember { mutableStateOf("https://www.youtube.com/watch?v=dQw4w9WgXcQ") }
    var activeVideoTitle by remember { mutableStateOf("Co-Watching: Trending Music Video (Synced)") }
    var isYouTubePlayerActive by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGradient)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar & AI Vision Sentinel Status Banner (PRD Section 1.1)
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
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isVisionBlackoutTriggered) "🚨 3S BLACKOUT" else "🛡️ SENTINEL ACTIVE (2.5s)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = WineRedDark
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(room.title, fontSize = 11.sp, color = LightGold, maxLines = 1)
                }

                Button(
                    onClick = {
                        if (isVisionBlackoutTriggered) onRestoreSentinel() else onTriggerSentinelTest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WineRedMedium),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(if (isVisionBlackoutTriggered) "Restore Stream" else "Test AI Sentinel", fontSize = 9.sp, color = MetallicGold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Upper Stage: Host Video Stream & Synchronized YouTube Player Engine (PRD Section 4.2)
            Card(
                colors = CardDefaults.cardColors(containerColor = WineRedDark),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .border(1.5.dp, MetallicGold, RoundedCornerShape(18.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isVisionBlackoutTriggered) {
                        // AI Vision Sentinel 3-Second Blackout Simulation (PRD Section 1.1)
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
                        // Synchronized YouTube Video Stage Container
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = MetallicGold, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(activeVideoTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LightGold)
                            Text("Synchronized Frame-Accurate YouTube Sync Player", fontSize = 10.sp, color = LiveIndicatorGreen)

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { /* Change YT Link */ },
                                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("Change YouTube URL", fontSize = 10.sp, color = WineRedDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 10-Guest Video Sofa Grid below main stream
            Text("10-GUEST VIDEO SOFA TILES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
            Spacer(modifier = Modifier.height(4.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                items(room.seats) { seat ->
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(WineRedMedium)
                            .border(1.dp, CrimsonVelvet, RoundedCornerShape(12.dp)),
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

            Spacer(modifier = Modifier.height(6.dp))

            // Live Gifting HUD Progress Bar
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("💎 Live Diamond Collection: 18,900", fontSize = 11.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                Text("Target: 20,000", fontSize = 10.sp, color = LightGold.copy(0.8f))
            }
            Spacer(modifier = Modifier.height(2.dp))
            LinearProgressIndicator(
                progress = { 0.94f },
                color = MetallicGold,
                trackColor = WineRedDark,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Video Room Chat Log & 1v1 Call Request Buttons
            LazyColumn(
                reverseLayout = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackgroundTransparent)
                    .padding(8.dp)
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
                        // 1v1 Direct Call Action in Chat (PRD Section 4.2)
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
    }
}
