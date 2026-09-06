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
import com.devil.finaldestiny.ui.components.GiftAnimationOverlay
import com.devil.finaldestiny.ui.components.SofaSeatComposable
import com.devil.finaldestiny.ui.theme.*

@Composable
fun LiveAudioRoomScreen(
    room: LiveRoom,
    chatMessages: List<ChatMessage>,
    giftStoreItems: List<GiftItem>,
    moderationAlerts: List<ModerationAlert>,
    onSendChatMessage: (String) -> Unit,
    onSendGift: (GiftItem) -> Unit,
    onToggleSeatMute: (Int) -> Unit,
    onKickUser: (Int) -> Unit
) {
    var chatInput by remember { mutableStateOf("") }
    var showGiftSheet by remember { mutableStateOf(false) }
    var showJukeboxPlayer by remember { mutableStateOf(false) }
    var selectedSeatForAdmin by remember { mutableStateOf<SofaSeat?>(null) }

    var isJukeboxPlaying by remember { mutableStateOf(false) }
    var currentSongName by remember { mutableStateOf("Romantic_Acoustic_Track_01.mp3") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGradient)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Broadcast Bar: Room Host, Viewer Count, Follow & Moderation Menu
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CrimsonVelvet, RoundedCornerShape(16.dp))
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(WineRedDark)
                                .border(1.5.dp, MetallicGold, CircleShape)
                        ) {
                            Text(room.hostUser.name.take(1), fontSize = 16.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(room.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LightGold, maxLines = 1)
                            Text("👑 Host: ${room.hostUser.name} | 👥 ${room.viewerCount} Viewers", fontSize = 10.sp, color = LightGold.copy(0.7f))
                        }
                    }

                    // Top Gifter Golden Crown Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MetallicGold)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("👑 ${room.topGifterName}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = WineRedDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Built-in Local Storage Audio Jukebox Banner (PRD Section 4.1)
            Card(
                colors = CardDefaults.cardColors(containerColor = WineRedMedium),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showJukeboxPlayer = !showJukeboxPlayer }
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = MetallicGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isJukeboxPlaying) "🎵 Playing: $currentSongName" else "📁 Local Storage Audio Hub (Jukebox)",
                            fontSize = 11.sp,
                            color = LightGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(if (isJukeboxPlaying) "Pause ⏸️" else "Open 🎵", fontSize = 10.sp, color = MetallicGold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 10-Mic Red Velvet Sofa Grid Matrix (2 rows of 5 sofa seats)
            Text("RED VELVET 10-MIC SOFA MATRIX", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
            Spacer(modifier = Modifier.height(6.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(205.dp)
            ) {
                items(room.seats) { seat ->
                    SofaSeatComposable(
                        seat = seat,
                        onClick = { selectedSeatForAdmin = seat }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Moderation Alert Logs Banner
            if (moderationAlerts.isNotEmpty()) {
                val latestAlert = moderationAlerts.first()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(WineRedDark)
                        .border(1.dp, HeartRed, RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    Text(text = "⚠️ ${latestAlert.message}", fontSize = 10.sp, color = LightGold)
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Real-Time Floating Moderation & Chat Log (PRD Section 4.3)
            Text("LIVE ROOM CHAT STREAM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                reverseLayout = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackgroundTransparent)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(chatMessages.reversed()) { msg ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (msg.isSystemAlert) {
                            Text(text = "🛡️ ${msg.text}", fontSize = 11.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                        } else {
                            Text(text = "VIP ${msg.senderVipLevel} ", fontSize = 9.sp, color = DarkGold, fontWeight = FontWeight.Bold)
                            Text(text = "${msg.senderName}: ", fontSize = 11.sp, color = LightGold, fontWeight = FontWeight.Bold)
                            Text(
                                text = msg.text,
                                fontSize = 11.sp,
                                color = if (msg.giftSentName != null) MetallicGold else LightGold.copy(0.9f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Action Bar: Chat Input, Gift Box, Top-up Shortcut
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { Text("Say something...", color = LightGold.copy(0.5f), fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = DarkGold),
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(46.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (chatInput.isNotBlank()) {
                            onSendChatMessage(chatInput)
                            chatInput = ""
                        }
                    },
                    modifier = Modifier.size(46.dp).clip(CircleShape).background(MetallicGold)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = WineRedDark)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Floating Gift Box Launcher
                IconButton(
                    onClick = { showGiftSheet = true },
                    modifier = Modifier.size(46.dp).clip(CircleShape).background(CrimsonVelvet)
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = "Gift Store", tint = MetallicGold)
                }
            }
        }

        // Jukebox Local Storage MP3 Sheet
        if (showJukeboxPlayer) {
            AlertDialog(
                onDismissRequest = { showJukeboxPlayer = false },
                containerColor = CardBackground,
                title = { Text("🎵 Local Storage MP3 Jukebox", color = MetallicGold) },
                text = {
                    Column {
                        Text("Stream audio tracks directly from phone storage to room guests:", fontSize = 11.sp, color = LightGold)
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf("Romantic_Acoustic_Track_01.mp3", "LoFi_Chill_Beats_02.mp3", "Guitar_Solo_Session.mp3").forEach { song ->
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentSongName = song
                                        isJukeboxPlaying = true
                                        showJukeboxPlayer = false
                                    }
                                    .padding(vertical = 6.dp)
                            ) {
                                Text(song, fontSize = 12.sp, color = LightGold)
                                Text("Play ▶️", fontSize = 11.sp, color = MetallicGold)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showJukeboxPlayer = false }, colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)) {
                        Text("Close", color = WineRedDark)
                    }
                }
            )
        }

        // Seat Admin Action Modal
        if (selectedSeatForAdmin != null) {
            val seat = selectedSeatForAdmin!!
            AlertDialog(
                onDismissRequest = { selectedSeatForAdmin = null },
                containerColor = CardBackground,
                title = { Text("Sofa Seat #${seat.seatIndex} Controls", color = MetallicGold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Role: ${seat.role.name}", fontSize = 12.sp, color = LightGold)
                        if (seat.userProfile != null) {
                            Text("User: ${seat.userProfile.name} (VIP Level ${seat.userProfile.vipLevel})", fontSize = 12.sp, color = MetallicGold)
                        }

                        Button(
                            onClick = {
                                onToggleSeatMute(seat.seatIndex)
                                selectedSeatForAdmin = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WineRedMedium),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (seat.isMuted) "Unmute Mic" else "Mute Mic", color = LightGold)
                        }

                        if (seat.userProfile != null) {
                            Button(
                                onClick = {
                                    onKickUser(seat.seatIndex)
                                    selectedSeatForAdmin = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonVelvet),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Kick User (VIP Immunity Check)", color = LightGold)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedSeatForAdmin = null }) {
                        Text("Cancel", color = LightGold)
                    }
                }
            )
        }

        // Live Gift Store Selector Modal Sheet
        if (showGiftSheet) {
            AlertDialog(
                onDismissRequest = { showGiftSheet = false },
                containerColor = CardBackground,
                title = { Text("🎁 Send Luxury Gift (25% Host Revenue)", color = MetallicGold) },
                text = {
                    Column {
                        LazyColumn(modifier = Modifier.height(240.dp)) {
                            items(giftStoreItems) { gift ->
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSendGift(gift)
                                            showGiftSheet = false
                                        }
                                        .padding(vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(gift.iconSymbol, fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(gift.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LightGold)
                                            Text(gift.category.name, fontSize = 10.sp, color = LightGold.copy(0.6f))
                                        }
                                    }
                                    Text("💎 ${gift.diamondPrice}", fontSize = 12.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGiftSheet = false }) {
                        Text("Close", color = LightGold)
                    }
                }
            )
        }
    }
}
