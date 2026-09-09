package com.devil.finaldestiny.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

    // PERSONAL MIC MUTE & APP MASTER VOLUME CONTROLS
    var isPersonalMicMuted by remember { mutableStateOf(false) }
    var isMasterAppAudioMuted by remember { mutableStateOf(false) }

    // Dynamic Host Profile State (Name & Avatar settings)
    var hostDisplayName by remember { mutableStateOf(room.hostUser.name) }
    var customHostAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var customHostSelfieBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Follow Host State
    var isFollowingHost by remember { mutableStateOf(false) }
    var hostFollowersCount by remember { mutableStateOf(12500) }

    // Background Theme State (15 dynamic themes)
    var selectedTheme by remember { mutableStateOf(AvailableRoomThemes.first()) }

    // Jukebox State & Local Device Audio Picker
    var isJukeboxPlaying by remember { mutableStateOf(false) }
    var currentSongName by remember { mutableStateOf("Romantic_Acoustic_Track_01.mp3") }
    var customAudioUri by remember { mutableStateOf<Uri?>(null) }

    // Launchers for Host Avatar Upload (Gallery & Camera Selfie)
    val galleryAvatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            customHostAvatarUri = uri
            customHostSelfieBitmap = null
            Toast.makeText(context, "📸 Host Profile Picture Saved & Updated!", Toast.LENGTH_SHORT).show()
        }
    }

    val selfieAvatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            customHostSelfieBitmap = bitmap
            customHostAvatarUri = null
            Toast.makeText(context, "📷 Host Selfie Profile Saved & Updated!", Toast.LENGTH_SHORT).show()
        }
    }

    // Audio Picker Launcher for Jukebox
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            customAudioUri = uri
            val fileName = uri.lastPathSegment ?: "Custom_Device_Track.mp3"
            currentSongName = fileName
            isJukeboxPlaying = true
            Toast.makeText(context, "🎵 Loaded & Playing: $currentSongName", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(selectedTheme.brush)
            .statusBarsPadding()
            .padding(10.dp)
    ) {
        // CLEAN ACHAT-STYLE HOST HEADER BAR WITH BACK / EXIT & AVATAR SETTINGS
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MetallicGold.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left Side: Back Arrow Button & Host Avatar (Clickable to change profile pic)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showLeaveRoomDialog = true },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(WineRedMedium)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back / Exit Room",
                            tint = MetallicGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Host Avatar (Clickable for Profile Picture Settings)
                    ProfileAvatarView(
                        name = room.hostUser.name,
                        profilePictureUri = customHostAvatarUri?.toString() ?: room.hostUser.profilePictureUri,
                        gender = room.hostUser.gender,
                        size = 44.dp,
                        showBorder = true,
                        borderColor = MetallicGold,
                        modifier = Modifier.clickable { showHostProfileSettings = true }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Host Name & Live Stats
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = hostDisplayName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightGold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("✓", fontSize = 11.sp, color = VerifiedBlue, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "👥 $hostFollowersCount Followers",
                                fontSize = 9.sp,
                                color = LightGold.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(LiveIndicatorGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "👁️ ${room.viewerCount}",
                                    fontSize = 9.sp,
                                    color = LiveIndicatorGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Right Side: Action Buttons (+ Follow, Theme Picker, Exit Icon)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = {
                            isFollowingHost = !isFollowingHost
                            if (isFollowingHost) {
                                hostFollowersCount += 1
                                Toast.makeText(context, "❤️ You followed $hostDisplayName!", Toast.LENGTH_SHORT).show()
                            } else {
                                hostFollowersCount -= 1
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowingHost) WineRedMedium else MetallicGold
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = if (isFollowingHost) "✓ Following" else "+ Follow",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFollowingHost) LightGold else WineRedDark
                        )
                    }

                    IconButton(
                        onClick = { showThemePicker = true },
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(WineRedMedium)
                            .border(1.dp, MetallicGold, CircleShape)
                    ) {
                        Text("🎨", fontSize = 13.sp)
                    }

                    IconButton(
                        onClick = { showLeaveRoomDialog = true },
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(CrimsonVelvet)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit Room", tint = LightGold, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // LOCAL STORAGE AUDIO JUKEBOX BANNER
        Card(
            colors = CardDefaults.cardColors(containerColor = WineRedMedium),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showJukeboxPlayer = true }
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MetallicGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isJukeboxPlaying) "🎵 Playing: $currentSongName" else "📁 Audio Hub (Pick MP3 / Play Music)",
                        fontSize = 11.sp,
                        color = LightGold,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = {
                        isJukeboxPlaying = !isJukeboxPlaying
                        Toast.makeText(
                            context,
                            if (isJukeboxPlaying) "▶️ Audio Playback Started" else "⏸️ Audio Paused",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(
                        text = if (isJukeboxPlaying) "Pause ⏸️" else "Play ▶️",
                        fontSize = 10.sp,
                        color = WineRedDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 10-MIC RED VELVET SOFA MATRIX (5x2 Grid)
        Text(
            text = "RED VELVET 10-MIC SOFA MATRIX (TAP SEAT FOR MIC CONTROLS)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MetallicGold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            items(room.seats) { seat ->
                SofaSeatComposable(
                    seat = seat,
                    onClick = { selectedSeatForAdmin = seat }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // PERSONAL MIC MUTE & APP MASTER VOLUME CONTROL BAR
        Card(
            colors = CardDefaults.cardColors(containerColor = WineRedMedium),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MetallicGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Personal Mic Mute/Unmute
                Button(
                    onClick = {
                        isPersonalMicMuted = !isPersonalMicMuted
                        Toast.makeText(
                            context,
                            if (isPersonalMicMuted) "🔇 Your Mic Muted" else "🎙️ Your Mic Active",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPersonalMicMuted) HeartRed else LiveIndicatorGreen
                    ),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(
                        imageVector = if (isPersonalMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null,
                        tint = WineRedDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPersonalMicMuted) "My Mic MUTED 🔇" else "My Mic ACTIVE 🎙️",
                        color = WineRedDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Master App Volume Toggle
                Button(
                    onClick = {
                        isMasterAppAudioMuted = !isMasterAppAudioMuted
                        Toast.makeText(
                            context,
                            if (isMasterAppAudioMuted) "🔇 ALL ROOM AUDIO MUTED" else "🔊 Room Audio Unmuted",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMasterAppAudioMuted) HeartRed else MetallicGold
                    ),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(
                        imageVector = if (isMasterAppAudioMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = WineRedDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMasterAppAudioMuted) "Audio MUTED 🔇" else "Audio ON 🔊",
                        color = WineRedDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Moderation Alert Banner
        if (moderationAlerts.isNotEmpty()) {
            val latestAlert = moderationAlerts.first()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(WineRedDark)
                    .border(1.dp, HeartRed, RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Text(text = "⚠️ ${latestAlert.message}", fontSize = 10.sp, color = LightGold)
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // LIVE ROOM CHAT STREAM
        Text("LIVE ROOM CHAT STREAM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            reverseLayout = true,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .heightIn(max = 140.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x33000000))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
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

        // BOTTOM CHAT & GIFT ACTION BAR ANCHORED ABOVE SYSTEM NAV WITH IME KEYBOARD LIFT
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 4.dp)
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = { Text("Say something in live chat...", color = LightGold.copy(0.5f), fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MetallicGold,
                    unfocusedBorderColor = DarkGold,
                    focusedTextColor = LightGold,
                    unfocusedTextColor = LightGold
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {
                    if (chatInput.isNotBlank()) {
                        onSendChatMessage(chatInput)
                        chatInput = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MetallicGold)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = WineRedDark)
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = { showGiftSheet = true },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CrimsonVelvet)
            ) {
                Icon(Icons.Default.CardGiftcard, contentDescription = "Gift Store", tint = MetallicGold)
            }
        }
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
                        label = { Text("Host Display Name", color = LightGold, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = DarkGold),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                showHostProfileSettings = false
                                selfieAvatarLauncher.launch()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WineRedMedium),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MetallicGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Take Selfie 📷", fontSize = 10.sp, color = LightGold, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                showHostProfileSettings = false
                                galleryAvatarLauncher.launch("image/*")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = WineRedDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pick Gallery 🖼️", fontSize = 10.sp, color = WineRedDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showHostProfileSettings = false
                        Toast.makeText(context, "Host Profile Saved & Updated!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
                ) {
                    Text("Save Settings", color = WineRedDark)
                }
            }
        )
    }

    // ROOM THEME SELECTION MODAL
    if (showThemePicker) {
        AlertDialog(
            onDismissRequest = { showThemePicker = false },
            containerColor = CardBackground,
            title = { Text("🎨 Select Room Background Theme", color = MetallicGold, fontSize = 16.sp) },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(AvailableRoomThemes) { theme ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTheme.id == theme.id) WineRedMedium else Color.Transparent)
                                .clickable {
                                    selectedTheme = theme
                                    showThemePicker = false
                                    Toast.makeText(context, "Applied Theme: ${theme.name}", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(theme.brush)
                                        .border(1.dp, MetallicGold, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("${theme.emoji} ${theme.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LightGold)
                                    Text(theme.description, fontSize = 10.sp, color = LightGold.copy(0.6f))
                                }
                            }
                            if (selectedTheme.id == theme.id) {
                                Text("Active ✓", fontSize = 11.sp, color = LiveIndicatorGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showThemePicker = false }, colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)) {
                    Text("Done", color = WineRedDark)
                }
            }
        )
    }

    // JUKEBOX MP3 LOCAL FILE PICKER MODAL
    if (showJukeboxPlayer) {
        AlertDialog(
            onDismissRequest = { showJukeboxPlayer = false },
            containerColor = CardBackground,
            title = { Text("🎵 Local Storage MP3 Jukebox", color = MetallicGold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Stream music directly from your phone storage to all guests:", fontSize = 11.sp, color = LightGold)

                    Button(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = WineRedDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📂 Select Audio from Phone Storage", color = WineRedDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    if (customAudioUri != null) {
                        Text("Selected File: $currentSongName", fontSize = 11.sp, color = LiveIndicatorGreen, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = CrimsonVelvet, thickness = 1.dp)

                    Text("Or choose built-in romantic tracks:", fontSize = 11.sp, color = LightGold.copy(0.8f))

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
                                    Toast.makeText(context, "Playing: $song", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 6.dp)
                        ) {
                            Text(song, fontSize = 12.sp, color = LightGold)
                            Text("Play ▶️", fontSize = 11.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showJukeboxPlayer = false }, colors = ButtonDefaults.buttonColors(containerColor = WineRedMedium)) {
                    Text("Close", color = LightGold)
                }
            }
        )
    }

    // SEAT ADMIN & MIC ACCESS CONTROL MODAL
    if (selectedSeatForAdmin != null) {
        val seat = selectedSeatForAdmin!!
        AlertDialog(
            onDismissRequest = { selectedSeatForAdmin = null },
            containerColor = CardBackground,
            title = { Text("🎙️ Sofa Seat #${seat.seatIndex} Mic Controls", color = MetallicGold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Role: ${seat.role.name}", fontSize = 12.sp, color = LightGold)
                    if (seat.userProfile != null) {
                        Text("Guest: ${seat.userProfile.name} (VIP ${seat.userProfile.vipLevel})", fontSize = 12.sp, color = MetallicGold)
                    }

                    Button(
                        onClick = {
                            onToggleSeatMute(seat.seatIndex)
                            selectedSeatForAdmin = null
                            Toast.makeText(context, if (seat.isMuted) "Mic Access Granted 🎙️" else "Mic Muted 🔇", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (seat.isMuted) LiveIndicatorGreen else WineRedMedium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (seat.isMuted) "Unmute & Enable Mic 🎙️" else "Mute Mic 🔇", color = if (seat.isMuted) WineRedDark else LightGold, fontWeight = FontWeight.Bold)
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
                            Text("Kick User From Seat", color = LightGold)
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

    // GIFT STORE SELECTOR MODAL SHEET
    if (showGiftSheet) {
        AlertDialog(
            onDismissRequest = { showGiftSheet = false },
            containerColor = CardBackground,
            title = { Text("🎁 Send Luxury Gift (25% Host Share)", color = MetallicGold) },
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
