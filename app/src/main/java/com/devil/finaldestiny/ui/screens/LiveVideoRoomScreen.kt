@file:Suppress("DEPRECATION")

package com.devil.finaldestiny.ui.screens

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.Camera
import android.net.Uri
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.devil.finaldestiny.model.*
import com.devil.finaldestiny.ui.components.ProfileAvatarView
import com.devil.finaldestiny.ui.theme.*

@Suppress("DEPRECATION")
private fun getFrontCameraId(): Int {
    try {
        val cameraInfo = Camera.CameraInfo()
        for (i in 0 until Camera.getNumberOfCameras()) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return i
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return 0
}

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
    onSendChatMessage: (String) -> Unit,
    onExitRoom: () -> Unit = {},
    onKeepRoom: () -> Unit = {}
) {
    val context = LocalContext.current

    var isCameraActive by remember { mutableStateOf(true) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var showGiftSheet by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    var showLeaveRoomDialog by remember { mutableStateOf(false) }
    var showHostProfileSettings by remember { mutableStateOf(false) }
    var chatInput by remember { mutableStateOf("") }

    // PERSONAL MIC MUTE CONTROL
    var isPersonalMicMuted by remember { mutableStateOf(false) }

    // Dynamic Host Profile State
    var hostDisplayName by remember { mutableStateOf(room.hostUser.name) }
    var customHostAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var customHostSelfieBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Follow Host State
    var isFollowingHost by remember { mutableStateOf(false) }
    var hostFollowersCount by remember { mutableStateOf(18200) }

    // Background Theme State (15 dynamic themes)
    var selectedTheme by remember { mutableStateOf(AvailableRoomThemes.first()) }

    // Runtime Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            isCameraActive = true
            Toast.makeText(context, "🎥 Live Camera Feed Active!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "⚠️ Camera Permission Required to Stream", Toast.LENGTH_SHORT).show()
        }
    }

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

    // Auto-request permission on launch if camera is active
    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(selectedTheme.brush)
    ) {
        // 1. FULL SCREEN NATIVE 9:16 CAMERA STREAM OR PLACEHOLDER
        Box(modifier = Modifier.fillMaxSize()) {
            if (isVisionBlackoutTriggered) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🚫 STREAM BLACKOUT TRIGGERED", color = HeartRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("AI Vision Sentinel sampled unauthorized broadcast element.", color = LightGold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onRestoreSentinel,
                            colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
                        ) {
                            Text("Restore Stream 🔄", color = WineRedDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (isCameraActive && hasCameraPermission) {
                // REAL CAMERA PREVIEW STREAM SURFACEVIEW WITH 9:16 ASPECT FILL
                AndroidView(
                    factory = { surfaceContext ->
                        SurfaceView(surfaceContext).apply {
                            holder.addCallback(object : SurfaceHolder.Callback {
                                var camera: Camera? = null

                                override fun surfaceCreated(holder: SurfaceHolder) {
                                    try {
                                        val camId = getFrontCameraId()
                                        camera = Camera.open(camId)
                                        camera?.setDisplayOrientation(90)

                                        val params = camera?.parameters
                                        val sizes = params?.supportedPreviewSizes
                                        if (!sizes.isNullOrEmpty()) {
                                            val optimal = sizes.minByOrNull {
                                                val r = it.width.toFloat() / it.height.toFloat()
                                                kotlin.math.abs(r - (16f / 9f))
                                            }
                                            if (optimal != null) {
                                                params?.setPreviewSize(optimal.width, optimal.height)
                                                camera?.parameters = params
                                            }
                                        }

                                        camera?.setPreviewDisplay(holder)
                                        camera?.startPreview()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

                                override fun surfaceDestroyed(holder: SurfaceHolder) {
                                    try {
                                        camera?.stopPreview()
                                        camera?.release()
                                        camera = null
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            })
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // CAMERA OFF / PERMISSION REQUIRED PLACEHOLDER
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0B1017)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = WineRedMedium,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (!hasCameraPermission) Icons.Default.VideocamOff else Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = MetallicGold,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (!hasCameraPermission) "Camera Access Required to Stream" else "Live Camera is Paused",
                            color = LightGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap below to activate your live video feed",
                            color = LightGold.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (!hasCameraPermission) {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                } else {
                                    isCameraActive = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
                        ) {
                            Text("Turn On Camera 📷", color = WineRedDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // 2. FLOATING OVERLAY CONTENT (TOP BAR, FLOATING CONTROLS, CHAT & INPUT)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP HEADER BAR (HOST INFO, FOLLOW, THEME, EXIT)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xCC0F172A)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MetallicGold.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    // Left: Back Arrow & Host Info
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
                                contentDescription = "Exit Room",
                                tint = MetallicGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        ProfileAvatarView(
                            name = room.hostUser.name,
                            profilePictureUri = customHostAvatarUri?.toString() ?: room.hostUser.profilePictureUri,
                            gender = room.hostUser.gender,
                            size = 42.dp,
                            showBorder = true,
                            borderColor = MetallicGold,
                            modifier = Modifier.clickable { showHostProfileSettings = true }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = hostDisplayName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("✓", fontSize = 11.sp, color = VerifiedBlue, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "👥 $hostFollowersCount",
                                    fontSize = 10.sp,
                                    color = LightGold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(LiveIndicatorGreen.copy(alpha = 0.3f))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "🔴 LIVE ${room.viewerCount}",
                                        fontSize = 9.sp,
                                        color = LiveIndicatorGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Right: Actions (+ Follow, Theme, Exit)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xAA1E293B))
                                .border(1.dp, MetallicGold.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Text("🎨", fontSize = 13.sp)
                        }

                        IconButton(
                            onClick = { showLeaveRoomDialog = true },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CrimsonVelvet)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit Room", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // FLOATING ACTION CONTROLS ON RIGHT SIDE (CAMERA TOGGLE, MIC MUTE, SENTINEL TEST)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Cam Toggle Floating Button
                    IconButton(
                        onClick = {
                            if (!isCameraActive) {
                                if (hasCameraPermission) {
                                    isCameraActive = true
                                    Toast.makeText(context, "🎥 Camera Feed Active!", Toast.LENGTH_SHORT).show()
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            } else {
                                isCameraActive = false
                                Toast.makeText(context, "Camera Off", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isCameraActive) LiveIndicatorGreen else Color(0xAA1E293B))
                            .border(1.dp, MetallicGold, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isCameraActive) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            contentDescription = "Camera Toggle",
                            tint = if (isCameraActive) WineRedDark else Color.White
                        )
                    }

                    // Mic Mute Floating Button
                    IconButton(
                        onClick = {
                            isPersonalMicMuted = !isPersonalMicMuted
                            Toast.makeText(context, if (isPersonalMicMuted) "🎙️ Mic Muted" else "🎙️ Mic Unmuted", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isPersonalMicMuted) HeartRed else Color(0xAA1E293B))
                            .border(1.dp, MetallicGold, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPersonalMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mic Toggle",
                            tint = Color.White
                        )
                    }

                    // AI Vision Sentinel Floating Indicator
                    IconButton(
                        onClick = onTriggerSentinelTest,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isVisionSentinelActive) WineRedMedium else Color(0xAA1E293B))
                            .border(1.dp, MetallicGold, CircleShape)
                    ) {
                        Text("🛡️", fontSize = 18.sp)
                    }
                }
            }

            // BOTTOM SECTION: DIAMOND TARGET HUD, FLOATING LIVE CHAT & CHAT INPUT BAR
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // DIAMOND TARGET HUD BAR
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC0F172A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("💎 Diamond Target: 18,900 / 20,000", fontSize = 11.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                            Text("94.5%", fontSize = 10.sp, color = LiveIndicatorGreen, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { 0.945f },
                            color = MetallicGold,
                            trackColor = WineRedDark,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape)
                        )
                    }
                }

                // FLOATING CHAT STREAM CONTAINER (INSTAGRAM LIVE STYLE OVERLAY)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xAA000000)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        reverseLayout = true,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(chatMessages.reversed()) { msg ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x44000000))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = msg.senderName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MetallicGold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = msg.text,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // BOTTOM CHAT INPUT BAR WITH EXPLICIT BRIGHT WHITE TEXT & CONTRAST
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Say something in video room...", color = Color.LightGray, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedPlaceholderColor = Color.LightGray,
                            unfocusedPlaceholderColor = Color.LightGray,
                            focusedContainerColor = Color(0xDD1F2937),
                            unfocusedContainerColor = Color(0xDD1F2937),
                            focusedBorderColor = MetallicGold,
                            unfocusedBorderColor = Color(0x88D4AF37),
                            cursorColor = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
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
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MetallicGold)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = WineRedDark)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { showGiftSheet = true },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(CrimsonVelvet)
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = "Gift Store", tint = MetallicGold)
                    }
                }
            }
        }
    }

    // MODALS: EXIT ROOM DIALOG, HOST PROFILE SETTINGS, THEME PICKER, GIFT SHEET
    if (showLeaveRoomDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveRoomDialog = false },
            containerColor = CardBackground,
            title = { Text("🚪 Leave Video Room Options", color = MetallicGold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Choose how you want to exit the Video Room:", fontSize = 12.sp, color = LightGold)

                    Button(
                        onClick = {
                            showLeaveRoomDialog = false
                            onKeepRoom()
                            Toast.makeText(context, "📌 Video Room running in background (Keep Mode)", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(context, "🚪 Left Video Room completely", Toast.LENGTH_SHORT).show()
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MetallicGold,
                            unfocusedBorderColor = DarkGold
                        ),
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
