package com.devil.finaldestiny.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.model.*
import com.devil.finaldestiny.ui.components.ProfileAvatarView
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
    onKickUser: (Int) -> Unit,
    onExitRoom: () -> Unit = {},
    onKeepRoom: () -> Unit = {}
) {
    val context = LocalContext.current
    var chatInput by remember { mutableStateOf("") }
    var showGiftSheet by remember { mutableStateOf(false) }
    var showJukeboxPlayer by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    var showLeaveRoomDialog by remember { mutableStateOf(false) }
    var showHostProfileSettings by remember { mutableStateOf(false) }
    var selectedSeatForAdmin by remember { mutableStateOf<SofaSeat?>(null) }

    // Reference Image Feature States
    var roomNoticeText by remember {
        mutableStateOf(
            "🚫 No abusive language\n" +
            "🚫 No personal attacks\n" +
            "🚫 No unnecessary arguments\n" +
            "🚫 No spam or disturbing messages\n" +
            "🌷 **Be Kind ❤️ Be Polite 😊 Be Respectful 💎**\n" +
            "✨ Let's spread **good vibes, positivity & happiness** throughout the chat! 🌈\n" +
            "🙏 Thank you everyone for your cooperation and understanding! 💕\n" +
            "🥰 **Happy Chatting!** 💬 ✨ 🌸"
        )
    }
    var showEditNoticeDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showTaskDialog by remember { mutableStateOf(false) }
    var showGamesDialog by remember { mutableStateOf(false) }
    var showChestDialog by remember { mutableStateOf(false) }
    var showGridMenuDialog by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    // PERSONAL MIC MUTE & APP MASTER VOLUME CONTROLS
    var isPersonalMicMuted by remember { mutableStateOf(false) }
    var isMasterAppAudioMuted by remember { mutableStateOf(false) }

    // Dynamic Host Profile State
    var hostDisplayName by remember { mutableStateOf("Mountain ⛰️ L") }
    var roomIdDisplay by remember { mutableStateOf("ID: 496787143") }
    var customHostAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var customHostSelfieBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Follow Host State
    var isFollowingHost by remember { mutableStateOf(false) }
    var hostFollowersCount by remember { mutableStateOf(12500) }

    // Background Theme State (15 dynamic themes)
    var selectedTheme by remember { mutableStateOf(AvailableRoomThemes.first()) }

    // Jukebox State
    var isJukeboxPlaying by remember { mutableStateOf(false) }
    var currentSongName by remember { mutableStateOf("Romantic_Acoustic_Track_01.mp3") }

    // Launchers for Host Avatar Upload
    val galleryAvatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            customHostAvatarUri = uri
            customHostSelfieBitmap = null
            Toast.makeText(context, "📸 Host Profile Picture Saved!", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(selectedTheme.brush)
            .statusBarsPadding()
    ) {
        // MAIN SCROLLABLE CONTENT COLUMN
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // 1. TOP HEADER BAR (MATCHING REFERENCE IMAGE)
            // 1. TOP HEADER BAR (With Back Arrow on Left & Home button on Right)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Top Left Back Arrow Icon Button
                IconButton(
                    onClick = { onExitRoom() },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back Arrow",
                        tint = LightGold,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Host Avatar in Treasure Box / Crown Frame & Room Name / ID
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { showHostProfileSettings = true }
                    ) {
                        // Glowing Treasure Box Frame Effect
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(MetallicGold, CrimsonVelvet)))
                                .border(2.dp, LightGold, CircleShape)
                        )
                        ProfileAvatarView(
                            name = hostDisplayName,
                            profilePictureUri = customHostAvatarUri?.toString() ?: room.hostUser.profilePictureUri,
                            gender = room.hostUser.gender,
                            size = 44.dp,
                            showBorder = false,
                            modifier = Modifier.clip(CircleShape)
                        )
                        // Tiny Crown Badge on top left of avatar
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.TopStart)
                                .clip(CircleShape)
                                .background(DarkGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👑", fontSize = 9.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = hostDisplayName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightGold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            // VIP Badge & Ticket Icon
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MetallicGold)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("VIP 🌟", fontSize = 8.sp, color = WineRedDark, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = roomIdDisplay,
                                fontSize = 10.sp,
                                color = LightGold.copy(alpha = 0.8f),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Online Viewer Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(LiveIndicatorGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${room.viewerCount}",
                                    fontSize = 9.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Top Right Header Icons (Home `🏠`, Share `📤`, More `⋮`, Close `⏻`)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Home Button (Jumps directly to Feed / Reel Feed)
                    IconButton(
                        onClick = { onExitRoom() },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Home Feed", tint = LightGold, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = { showShareDialog = true },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Room", tint = LightGold, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = { showThemePicker = true },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = LightGold, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = { showLeaveRoomDialog = true },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(HeartRed.copy(alpha = 0.85f))
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = "Exit Room", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. 10-MIC CIRCULAR SOFA MATRIX (5x2 Grid matching reference image)
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
            ) {
                items(room.seats) { seat ->
                    SofaSeatComposable(
                        seat = seat,
                        onClick = { selectedSeatForAdmin = seat }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. ROOM NOTICE / RULES OVERLAY CARD (With Edit Button)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x661A0033)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MetallicGold.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(end = 45.dp)) {
                        Text(
                            text = roomNoticeText,
                            fontSize = 11.sp,
                            color = LightGold,
                            lineHeight = 15.sp
                        )
                    }

                    // Edit Button on top-right of notice card
                    Button(
                        onClick = { showEditNoticeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .height(28.dp)
                    ) {
                        Text("Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. "Share your room to others!" BANNER
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x44000000)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Share your room to others!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LightGold
                    )
                    Button(
                        onClick = {
                            val shareIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, "Join my Final Destiny Live Audio Room '$hostDisplayName'! ID: $roomIdDisplay 🎙️✨")
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Room via"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Share", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 5. ROOM TASK REWARD BANNER
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x992E1A0F)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTaskDialog = true }
                    .border(1.dp, MetallicGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text("🎁", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Complete the room task to get more rewards !",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightGold,
                            maxLines = 1
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MetallicGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // LIVE CHAT STREAM STREAM CONTAINER
            LazyColumn(
                reverseLayout = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .heightIn(max = 120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33000000))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(chatMessages.reversed()) { msg ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (msg.isSystemAlert) {
                            Text(text = "🛡️ ${msg.text}", fontSize = 10.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                        } else {
                            Text(text = "VIP ${msg.senderVipLevel} ", fontSize = 8.sp, color = DarkGold, fontWeight = FontWeight.Bold)
                            Text(text = "${msg.senderName}: ", fontSize = 10.sp, color = LightGold, fontWeight = FontWeight.Bold)
                            Text(
                                text = msg.text,
                                fontSize = 10.sp,
                                color = if (msg.giftSentName != null) MetallicGold else LightGold.copy(0.9f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6. BOTTOM TOOLBAR (Matching reference image footer icons exactly)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = 4.dp)
            ) {
                // Speaker Mute Toggle
                IconButton(
                    onClick = {
                        isMasterAppAudioMuted = !isMasterAppAudioMuted
                        Toast.makeText(context, if (isMasterAppAudioMuted) "🔇 Room Audio Muted" else "🔊 Room Audio Unmuted", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = if (isMasterAppAudioMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Audio Volume",
                        tint = LightGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Mic Mute Toggle
                IconButton(
                    onClick = {
                        isPersonalMicMuted = !isPersonalMicMuted
                        Toast.makeText(context, if (isPersonalMicMuted) "🔇 Mic Muted" else "🎙️ Mic Active", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isPersonalMicMuted) HeartRed else Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = if (isPersonalMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mic Mute",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Emoji / Reactions Button
                IconButton(
                    onClick = { showEmojiPicker = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Text("😊", fontSize = 18.sp)
                }

                // Chat Input Field Button / Trigger
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { Text("Say something...", color = LightGold.copy(0.5f), fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MetallicGold,
                        unfocusedBorderColor = DarkGold,
                        focusedTextColor = LightGold,
                        unfocusedTextColor = LightGold
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .padding(horizontal = 4.dp)
                )

                // Slot Machine Shortcut Icon
                IconButton(
                    onClick = { showGamesDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Text("🎰", fontSize = 20.sp)
                }

                // Agency Bonus Icon
                IconButton(
                    onClick = { Toast.makeText(context, "👑 Agency Bonus Active", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Text("👑", fontSize = 20.sp)
                }

                // Recharge Bonus Gift Box Icon (Glowing Pink Gift Box with "Recharge Bonus")
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))))
                        .clickable { showGiftSheet = true }
                        .border(1.5.dp, MetallicGold, CircleShape)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎁", fontSize = 14.sp)
                        Text("19s", fontSize = 7.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Grid Menu (9-dots)
                IconButton(
                    onClick = { showGridMenuDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.Apps, contentDescription = "More Apps", tint = LightGold, modifier = Modifier.size(20.dp))
                }
            }
        }

        // 7. RIGHT FLOATING ACTION BAR (Exact widgets from reference image: Slot machine, Game, Chat, Treasure Chest, Sofa)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp, top = 80.dp)
        ) {
            // Slot Machine Widget
            FloatingWidgetButton(emoji = "🎰", label = "Slot") { showGamesDialog = true }
            // Mini Game / Party Widget
            FloatingWidgetButton(emoji = "🎡", label = "Game") { showGamesDialog = true }
            // Chat Bubble Widget
            FloatingWidgetButton(emoji = "💬", label = "Chat") {
                Toast.makeText(context, "💬 Chat Stream Active", Toast.LENGTH_SHORT).show()
            }
            // Treasure Chest Widget
            FloatingWidgetButton(emoji = "💰", label = "Chest") { showChestDialog = true }
            // Gift Countdown Widget
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFF43F5E), Color(0xFFFB923C))))
                    .clickable { showGiftSheet = true }
                    .border(1.5.dp, MetallicGold, CircleShape)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎁", fontSize = 16.sp)
                    Text("19s", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            // Sofa Seating Widget
            FloatingWidgetButton(emoji = "🛋️", label = "Sofa") {
                Toast.makeText(context, "🛋️ 10-Mic Sofa Matrix Active", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- MODALS & DIALOGS ---

    // Seat Controls Dialog (Triggered on clicking any of the 10 mic seats)
    if (selectedSeatForAdmin != null) {
        val targetSeat = selectedSeatForAdmin!!
        AlertDialog(
            onDismissRequest = { selectedSeatForAdmin = null },
            containerColor = CardBackground,
            title = {
                Text(
                    text = "🎙️ Mic Seat #${targetSeat.seatIndex} Options",
                    color = MetallicGold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (targetSeat.userProfile != null) "User on Seat: ${targetSeat.userProfile.name}" else "Seat Status: Empty Mic Seat",
                        fontSize = 12.sp,
                        color = LightGold
                    )

                    // Toggle Mute Button
                    Button(
                        onClick = {
                            onToggleSeatMute(targetSeat.seatIndex)
                            Toast.makeText(context, if (targetSeat.isMuted) "🔊 Unmuted Seat #${targetSeat.seatIndex}" else "🔇 Muted Seat #${targetSeat.seatIndex}", Toast.LENGTH_SHORT).show()
                            selectedSeatForAdmin = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (targetSeat.isMuted) WineRedMedium else HeartRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (targetSeat.isMuted) Icons.Default.Mic else Icons.Default.MicOff, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (targetSeat.isMuted) "Unmute Seat Mic" else "Mute Seat Mic", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    if (targetSeat.userProfile != null && targetSeat.role != SeatRole.HOST) {
                        Button(
                            onClick = {
                                onKickUser(targetSeat.seatIndex)
                                Toast.makeText(context, "👢 User removed from Seat #${targetSeat.seatIndex}", Toast.LENGTH_SHORT).show()
                                selectedSeatForAdmin = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonVelvet),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Remove User from Mic", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSeatForAdmin = null }) {
                    Text("Close", color = LightGold)
                }
            }
        )
    }

    // Edit Notice Dialog
    if (showEditNoticeDialog) {
        var tempNotice by remember { mutableStateOf(roomNoticeText) }
        AlertDialog(
            onDismissRequest = { showEditNoticeDialog = false },
            containerColor = CardBackground,
            title = { Text("✏️ Edit Room Notice & Rules", color = MetallicGold, fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = tempNotice,
                    onValueChange = { tempNotice = it },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = DarkGold, focusedTextColor = LightGold, unfocusedTextColor = LightGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        roomNoticeText = tempNotice
                        showEditNoticeDialog = false
                        Toast.makeText(context, "✨ Room Notice Updated Successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
                ) {
                    Text("Save", color = WineRedDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNoticeDialog = false }) {
                    Text("Cancel", color = LightGold)
                }
            }
        )
    }

    // Share Room Dialog
    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            containerColor = CardBackground,
            title = { Text("📤 Share Room", color = MetallicGold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Invite friends to join $hostDisplayName's Live Party Room!", fontSize = 12.sp, color = LightGold)
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Room ID", "Final Destiny Room ID: 496787143")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "📋 Room ID copied to clipboard!", Toast.LENGTH_SHORT).show()
                            showShareDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WineRedMedium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📋 Copy Room ID", color = LightGold, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Close", color = LightGold)
                }
            }
        )
    }

    // Task Reward Dialog
    if (showTaskDialog) {
        AlertDialog(
            onDismissRequest = { showTaskDialog = false },
            containerColor = CardBackground,
            title = { Text("🎁 Room Daily Rewards & Tasks", color = MetallicGold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Stay in room for 15 mins: 500 Gems 💎", fontSize = 12.sp, color = LightGold)
                    Text("• Send 1 Gift to Host: Unlock VIP Badge 🌟", fontSize = 12.sp, color = LightGold)
                    Text("• Invite 3 Friends to Party: 2000 Diamonds 💎", fontSize = 12.sp, color = LightGold)
                }
            },
            confirmButton = {
                Button(onClick = {
                    showTaskDialog = false
                    Toast.makeText(context, "🎉 Task Reward Claimed Successfully!", Toast.LENGTH_SHORT).show()
                }, colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)) {
                    Text("Claim Reward 🎁", color = WineRedDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Games & Slot Machine Dialog
    if (showGamesDialog) {
        AlertDialog(
            onDismissRequest = { showGamesDialog = false },
            containerColor = CardBackground,
            title = { Text("🎰 Party Room Games & Lucky Spin", color = MetallicGold, fontSize = 16.sp) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Spin the wheel or play the slot machine to win huge diamond prizes!", fontSize = 12.sp, color = LightGold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(WineRedMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍒 7 💎", fontSize = 24.sp)
                    }
                    Button(onClick = {
                        Toast.makeText(context, "🎰 Jackpot! You won 10,000 Diamonds! 🎉", Toast.LENGTH_LONG).show()
                        showGamesDialog = false
                    }, colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)) {
                        Text("Spin Now (Cost: 100 💎)", color = WineRedDark, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGamesDialog = false }) {
                    Text("Close", color = LightGold)
                }
            }
        )
    }

    // Treasure Chest Dialog
    if (showChestDialog) {
        AlertDialog(
            onDismissRequest = { showChestDialog = false },
            containerColor = CardBackground,
            title = { Text("💰 Lucky Treasure Chest", color = MetallicGold, fontSize = 16.sp) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("A treasure chest filled with gold and diamonds has dropped in the room!", fontSize = 12.sp, color = LightGold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Text("🎁 50,000 Diamonds Pool", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LightGold)
                    Button(onClick = {
                        Toast.makeText(context, "🎉 Opened Treasure Chest! Claimed 500 Diamonds!", Toast.LENGTH_LONG).show()
                        showChestDialog = false
                    }, colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)) {
                        Text("Open Chest 🔓", color = WineRedDark, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChestDialog = false }) {
                    Text("Close", color = LightGold)
                }
            }
        )
    }

    // Grid Menu Dialog (9-dots)
    if (showGridMenuDialog) {
        AlertDialog(
            onDismissRequest = { showGridMenuDialog = false },
            containerColor = CardBackground,
            title = { Text("📱 Party Room Tools & Apps", color = MetallicGold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { showThemePicker = true; showGridMenuDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = WineRedMedium), modifier = Modifier.fillMaxWidth()) {
                        Text("🎨 Change Room Theme", color = LightGold)
                    }
                    Button(onClick = { showJukeboxPlayer = true; showGridMenuDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = WineRedMedium), modifier = Modifier.fillMaxWidth()) {
                        Text("🎵 Audio Jukebox & Music", color = LightGold)
                    }
                    Button(onClick = { showHostProfileSettings = true; showGridMenuDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = WineRedMedium), modifier = Modifier.fillMaxWidth()) {
                        Text("👑 Host Profile Settings", color = LightGold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGridMenuDialog = false }) {
                    Text("Close", color = LightGold)
                }
            }
        )
    }

    // Emoji / Reaction Picker Dialog
    if (showEmojiPicker) {
        AlertDialog(
            onDismissRequest = { showEmojiPicker = false },
            containerColor = CardBackground,
            title = { Text("💬 Send Room Reaction", color = MetallicGold, fontSize = 16.sp) },
            text = {
                Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                    listOf("❤️", "🔥", "🌹", "💎", "🎉", "✨").forEach { emoji ->
                        TextButton(onClick = {
                            onSendChatMessage(emoji)
                            showEmojiPicker = false
                            Toast.makeText(context, "Sent $emoji", Toast.LENGTH_SHORT).show()
                        }) {
                            Text(emoji, fontSize = 24.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEmojiPicker = false }) {
                    Text("Cancel", color = LightGold)
                }
            }
        )
    }

    // KEEP / EXIT ROOM OPTIONS MODAL
    if (showLeaveRoomDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveRoomDialog = false },
            containerColor = CardBackground,
            title = { Text("🚪 Leave Audio Room Options", color = MetallicGold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Choose how you want to exit the Audio Room:", fontSize = 12.sp, color = LightGold)

                    Button(
                        onClick = {
                            showLeaveRoomDialog = false
                            onKeepRoom()
                            Toast.makeText(context, "📌 Audio Room running in background (Keep Mode)", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WineRedMedium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PushPin, contentDescription = null, tint = MetallicGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📌 Keep Room in Background (PIP)", color = LightGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            showLeaveRoomDialog = false
                            onExitRoom()
                            Toast.makeText(context, "🚪 Left Audio Room completely", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonVelvet),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = LightGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🚪 Exit & Leave Room Completely", color = LightGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLeaveRoomDialog = false }) {
                    Text("Cancel", color = LightGold)
                }
            }
        )
    }

    // HOST LIVE PROFILE & AVATAR SETTINGS MODAL
    if (showHostProfileSettings) {
        AlertDialog(
            onDismissRequest = { showHostProfileSettings = false },
            containerColor = CardBackground,
            title = { Text("👑 Host Live Profile & Avatar Settings", color = MetallicGold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Customize your Live Room Host Avatar & Display Name:", fontSize = 11.sp, color = LightGold)

                    OutlinedTextField(
                        value = hostDisplayName,
                        onValueChange = { hostDisplayName = it },
                        label = { Text("Room / Host Title", color = LightGold, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = DarkGold),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { galleryAvatarLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🖼️ Choose Host Picture from Gallery", color = WineRedDark, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showHostProfileSettings = false }, colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)) {
                    Text("Done", color = WineRedDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // THEME PICKER MODAL
    if (showThemePicker) {
        AlertDialog(
            onDismissRequest = { showThemePicker = false },
            containerColor = CardBackground,
            title = { Text("🎨 Select Room Theme & Background", color = MetallicGold, fontSize = 16.sp) },
            text = {
                LazyColumn(modifier = Modifier.height(240.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AvailableRoomThemes) { theme ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = WineRedMedium),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTheme = theme
                                    showThemePicker = false
                                    Toast.makeText(context, "✨ Theme applied: ${theme.name}", Toast.LENGTH_SHORT).show()
                                }
                                .border(
                                    width = if (selectedTheme.name == theme.name) 2.dp else 0.dp,
                                    color = MetallicGold,
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(theme.brush)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(theme.name, color = LightGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemePicker = false }) {
                    Text("Close", color = LightGold)
                }
            }
        )
    }

    // GIFT STORE MODAL
    if (showGiftSheet) {
        AlertDialog(
            onDismissRequest = { showGiftSheet = false },
            containerColor = CardBackground,
            title = { Text("🎁 Send Live Room Gift", color = MetallicGold, fontSize = 16.sp) },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(260.dp)
                ) {
                    items(giftStoreItems) { gift ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = WineRedMedium),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clickable {
                                    onSendGift(gift)
                                    showGiftSheet = false
                                    Toast.makeText(context, "🎁 Sent ${gift.name}!", Toast.LENGTH_SHORT).show()
                                }
                                .border(1.dp, MetallicGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text(gift.iconSymbol, fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(gift.name, fontSize = 10.sp, color = LightGold, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("${gift.diamondPrice} 💎", fontSize = 9.sp, color = MetallicGold)
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

    // AUDIO JUKEBOX MODAL
    if (showJukeboxPlayer) {
        AlertDialog(
            onDismissRequest = { showJukeboxPlayer = false },
            containerColor = CardBackground,
            title = { Text("🎵 Room Audio Jukebox", color = MetallicGold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Current Track: $currentSongName", fontSize = 12.sp, color = LightGold)
                    Button(
                        onClick = {
                            isJukeboxPlaying = !isJukeboxPlaying
                            Toast.makeText(context, if (isJukeboxPlaying) "▶️ Playing Jukebox" else "⏸️ Paused Jukebox", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isJukeboxPlaying) "Pause Music ⏸️" else "Play Music ▶️", color = WineRedDark, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showJukeboxPlayer = false }) {
                    Text("Close", color = LightGold)
                }
            }
        )
    }
}

@Composable
private fun FloatingWidgetButton(emoji: String, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
                .border(1.dp, MetallicGold.copy(alpha = 0.6f), CircleShape)
        ) {
            Text(emoji, fontSize = 20.sp)
        }
    }
}
