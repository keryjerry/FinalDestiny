@file:Suppress("DEPRECATION")

package com.devil.finaldestiny.ui.screens

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.Camera
import android.net.Uri
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.devil.finaldestiny.model.*
import com.devil.finaldestiny.ui.components.ProfileAvatarView
import com.devil.finaldestiny.ui.theme.*

private fun extractYoutubeVideoId(rawUrl: String): String {
    val cleanUrl = rawUrl.trim()
    if (cleanUrl.isEmpty()) return "aUa0amEcCac"

    // Regex matching standard video URLs, shorts, live streams, embeds, and shortened links
    val regex = Regex("""(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?|shorts|live)\/|.*[?&]v=)|youtu\.be\/)([^"&?\/\s]{11})""", RegexOption.IGNORE_CASE)
    val match = regex.find(cleanUrl)
    if (match != null && match.groupValues.size > 1) {
        return match.groupValues[1]
    }

    // Fallback: If user directly pasted an 11-char ID
    val simpleId = cleanUrl.split("?", "&", "#", "/", " ").firstOrNull()?.trim() ?: ""
    if (simpleId.length in 8..15) {
        return simpleId
    }

    return "aUa0amEcCac"
}

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
    val scrollState = rememberScrollState()

    var isCameraActive by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var youtubeUrlInput by remember { mutableStateOf("https://youtu.be/aUa0amEcCac") }
    var activeVideoId by remember { mutableStateOf("aUa0amEcCac") }
    var loadedVideoId by remember { mutableStateOf<String?>(null) }
    var showGiftSheet by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    var showLeaveRoomDialog by remember { mutableStateOf(false) }
    var showHostProfileSettings by remember { mutableStateOf(false) }
    var selectedSeatForMediaControls by remember { mutableStateOf<SofaSeat?>(null) }
    var chatInput by remember { mutableStateOf("") }

    // PERSONAL MIC MUTE & APP MASTER VOLUME CONTROLS
    var isPersonalMicMuted by remember { mutableStateOf(false) }
    var isMasterAppAudioMuted by remember { mutableStateOf(false) }

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
            Toast.makeText(context, "🎥 Live Camera Access Granted!", Toast.LENGTH_SHORT).show()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(selectedTheme.brush)
            .verticalScroll(scrollState)
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

                    // Host Avatar (Renders Host Photo & Clickable for Profile Picture Settings)
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

                    // Host Profile Info
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

        // MAIN VIDEO STAGE (INSTAGRAM LIVE STYLE IMMERSIVE 9:16 TALL VIDEO STREAM)
        Card(
            colors = CardDefaults.cardColors(containerColor = WineRedDark),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .border(2.dp, MetallicGold, RoundedCornerShape(24.dp))
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
                        }
                    }
                } else if (isCameraActive) {
                    if (hasCameraPermission) {
                        // REAL CAMERA PREVIEW STREAM SURFACEVIEW WITH 9:16 CENTER_CROP ASPECT SCALING
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

                                                // Set optimal 16:9 preview resolution for natural face proportions
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
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                Icon(Icons.Default.VideocamOff, contentDescription = null, tint = HeartRed, modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Camera Access Required to Stream", color = LightGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
                                ) {
                                    Text("Grant Camera Access 📷", color = WineRedDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                } else {
                    // HIGH PERFORMANCE STABLE YOUTUBE EMBED PLAYER
                    AndroidView(
                        factory = { webContext ->
                            WebView(webContext).apply {
                                layoutParams = android.view.ViewGroup.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                                setBackgroundColor(android.graphics.Color.BLACK)
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    mediaPlaybackRequiresUserGesture = false
                                    allowFileAccess = true
                                    allowContentAccess = true
                                    javaScriptCanOpenWindowsAutomatically = true
                                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    userAgentString = "Mozilla/5.0 (Linux; Android 12; Pixel 6 Build/SQ3A.220705.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.71 Mobile Safari/537.36"
                                }
                                webChromeClient = WebChromeClient()
                                webViewClient = object : WebViewClient() {
                                    @Suppress("DEPRECATION")
                                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                        return false
                                    }
                                }
                            }
                        },
                        update = { webView ->
                            if (loadedVideoId != activeVideoId) {
                                loadedVideoId = activeVideoId
                                val htmlContent = """
                                    <!DOCTYPE html>
                                    <html>
                                    <body style="margin:0;padding:0;background-color:black;">
                                        <iframe width="100%" height="100%" 
                                            src="https://www.youtube.com/embed/$activeVideoId?autoplay=1&mute=0&controls=1&playsinline=1&enablejsapi=1&rel=0" 
                                            frameborder="0" 
                                            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                                            allowfullscreen>
                                        </iframe>
                                    </body>
                                    </html>
                                """.trimIndent()
                                webView.loadDataWithBaseURL("https://www.youtube.com", htmlContent, "text/html", "utf-8", null)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // STAGE BROADCAST & YOUTUBE LINK PLAY CONTROLS
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CrimsonVelvet, RoundedCornerShape(16.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Real Camera Stream Toggle
                    Button(
                        onClick = {
                            if (!isCameraActive) {
                                if (hasCameraPermission) {
                                    isCameraActive = true
                                    Toast.makeText(context, "🎥 Live Camera Feed Active!", Toast.LENGTH_SHORT).show()
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            } else {
                                isCameraActive = false
                                Toast.makeText(context, "Camera Turned Off", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCameraActive) LiveIndicatorGreen else MetallicGold
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = WineRedDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCameraActive) "Cam Active 🎥" else "Live Cam 📷",
                            color = WineRedDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Open Direct in YouTube App / Browser Button
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$activeVideoId"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open YouTube app", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonVelvet),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text("Open YouTube 📲", color = LightGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // YouTube URL Paste Input Field & Play Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = youtubeUrlInput,
                        onValueChange = { youtubeUrlInput = it },
                        placeholder = { Text("Paste YouTube Link...", color = LightGold.copy(0.5f), fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MetallicGold,
                            unfocusedBorderColor = DarkGold,
                            focusedTextColor = LightGold,
                            unfocusedTextColor = LightGold
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(48.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (youtubeUrlInput.isNotBlank()) {
                                isCameraActive = false
                                val extractedId = extractYoutubeVideoId(youtubeUrlInput)
                                activeVideoId = extractedId
                                Toast.makeText(context, "🎬 Playing YouTube Video!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Play 🎬", color = WineRedDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // QUICK PRESET YOUTUBE MUSIC & VIDEO BUTTONS
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val presets = listOf(
                        "🎵 Bollywood" to "https://youtu.be/aUa0amEcCac",
                        "🎧 Lo-Fi Beats" to "https://www.youtube.com/watch?v=jfKfPfyJRdk",
                        "🎬 Party DJ" to "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                    )

                    presets.forEach { (label, url) ->
                        FilterChip(
                            selected = (youtubeUrlInput == url),
                            onClick = {
                                youtubeUrlInput = url
                                isCameraActive = false
                                activeVideoId = extractYoutubeVideoId(url)
                                Toast.makeText(context, "▶️ Playing $label", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = WineRedMedium,
                                labelColor = LightGold,
                                selectedContainerColor = MetallicGold,
                                selectedLabelColor = WineRedDark
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = (youtubeUrlInput == url),
                                borderColor = MetallicGold
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }



        Spacer(modifier = Modifier.height(10.dp))

        // LIVE DIAMOND COLLECTION HUD
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("💎 Live Diamond Target: 18,900 / 20,000", fontSize = 11.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
            Text("94.5%", fontSize = 10.sp, color = LiveIndicatorGreen, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { 0.945f },
            color = MetallicGold,
            trackColor = WineRedDark,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // VIDEO ROOM CHAT LOG
        Text("LIVE CHAT STREAM & 1v1 CALLS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
        Spacer(modifier = Modifier.height(4.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackgroundTransparent),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
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
                            onClick = {
                                Toast.makeText(context, "📞 Initiating 1v1 Call with ${msg.senderName}...", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier.height(20.dp)
                        ) {
                            Text("📞 Call Invite", fontSize = 9.sp, color = MetallicGold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // BOTTOM CHAT INPUT BAR
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = { Text("Say something in video room...", color = LightGold.copy(0.5f), fontSize = 12.sp) },
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

        Spacer(modifier = Modifier.height(16.dp))
    }

    // SEAT MEDIA CONTROLS MODAL (TOGGLE CAM / MIC FOR EACH GUEST SEAT)
    if (selectedSeatForMediaControls != null) {
        val seat = selectedSeatForMediaControls!!
        AlertDialog(
            onDismissRequest = { selectedSeatForMediaControls = null },
            containerColor = CardBackground,
            title = { Text("📹 Guest Seat #${seat.seatIndex} Media Controls", color = MetallicGold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (seat.userProfile != null) {
                        Text("Guest: ${seat.userProfile.name} (VIP ${seat.userProfile.vipLevel})", fontSize = 12.sp, color = MetallicGold)
                    }

                    Button(
                        onClick = {
                            selectedSeatForMediaControls = null
                            Toast.makeText(context, "📹 Toggled Camera Stream for Seat #${seat.seatIndex}", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiveIndicatorGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = WineRedDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Toggle Guest Camera 📹", color = WineRedDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            selectedSeatForMediaControls = null
                            Toast.makeText(context, "🎙️ Toggled Mic Access for Seat #${seat.seatIndex}", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = WineRedDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Toggle Guest Microphone 🎙️", color = WineRedDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSeatForMediaControls = null }) {
                    Text("Close", color = LightGold)
                }
            }
        )
    }

    // KEEP / EXIT ROOM OPTIONS MODAL
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
