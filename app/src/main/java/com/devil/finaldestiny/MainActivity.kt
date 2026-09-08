package com.devil.finaldestiny

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.data.AppRepository
import com.devil.finaldestiny.engine.AppInstallerEngine
import com.devil.finaldestiny.engine.UpdateReleaseInfo
import com.devil.finaldestiny.model.GiftItem
import com.devil.finaldestiny.model.PaymentMethodType
import com.devil.finaldestiny.ui.components.MonetizationAnnouncementModal
import com.devil.finaldestiny.ui.components.NotificationCenterModal
import com.devil.finaldestiny.ui.components.PermissionModalDialog
import com.devil.finaldestiny.ui.components.UpdateInstallerModalDialog
import com.devil.finaldestiny.ui.screens.*
import com.devil.finaldestiny.ui.theme.*
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import kotlinx.coroutines.launch

enum class Screen {
    AUTH_SPLASH,
    LIVENESS_CHECK,
    PRIMARY_DASHBOARD,
    SECONDARY_FEED,
    DISCOVER_SWIPE,
    LIVE_AUDIO_ROOM,
    LIVE_VIDEO_ROOM,
    VIP_STORE,
    CREATOR_MONETIZATION,
    USER_PROFILE,
    FINAL_DESTINY_DATING,
    ABOUT_US
}

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    private val repository = AppRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Checkout.preload(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        repository.initializeUserSession(this)
        enableEdgeToEdge()
        setContent {
            FinalDestinyTheme {
                FinalDestinyApp(repository = repository)
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        val id = razorpayPaymentId ?: paymentData?.paymentId ?: "pay_test_success"
        paymentResultListener?.invoke(true, id)
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        val err = response ?: "Payment Cancelled/Failed (Code $code)"
        paymentResultListener?.invoke(false, err)
    }

    companion object {
        var paymentResultListener: ((Boolean, String) -> Unit)? = null
    }
}

@Composable
fun FinalDestinyApp(repository: AppRepository) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(Screen.AUTH_SPLASH) }
    var showPermissionModal by remember { mutableStateOf(false) }
    var showMonetizationPopup by remember { mutableStateOf(true) }

    // Background Kept Room Sessions State
    var isAudioRoomKeptInBackground by remember { mutableStateOf(false) }
    var isVideoRoomKeptInBackground by remember { mutableStateOf(false) }

    val user by repository.currentUser.collectAsState()
    val swipeCards by repository.swipeCards.collectAsState()
    val matchedCard by repository.matchedCard.collectAsState()
    val storyTrays by repository.storyTrays.collectAsState()
    val momentPosts by repository.momentPosts.collectAsState()
    val audioRoom by repository.currentAudioRoom.collectAsState()
    val videoRoom by repository.currentVideoRoom.collectAsState()
    val chatMessages by repository.chatMessages.collectAsState()
    val kycData by repository.kycData.collectAsState()
    val hostEarnings by repository.hostEarnings.collectAsState()
    val moderationAlerts by repository.moderationAlerts.collectAsState()
    val isVisionSentinelActive by repository.isVisionSentinelActive.collectAsState()
    val isVisionBlackoutTriggered by repository.isVisionBlackoutTriggered.collectAsState()
    val notifications by repository.notifications.collectAsState()
    val creatorAnalytics by repository.creatorAnalytics.collectAsState()
    var showNotificationModal by remember { mutableStateOf(false) }

    // Auto-Update Checker State (Supabase Remote Config)
    var updateInfoState by remember { mutableStateOf<UpdateReleaseInfo?>(null) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }
    var updateDownloadProgress by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val releaseInfo = AppInstallerEngine.checkSupabaseAppVersion(currentVersionCode = 1)
        if (releaseInfo != null) {
            updateInfoState = releaseInfo
        }
    }

    Scaffold(
        bottomBar = {
            if (currentScreen != Screen.AUTH_SPLASH && currentScreen != Screen.LIVENESS_CHECK && currentScreen != Screen.ABOUT_US) {
                Column {
                    // FLOATING PIP MINI PLAYER BANNER WHEN A ROOM IS KEPT IN BACKGROUND
                    if (isAudioRoomKeptInBackground && currentScreen != Screen.LIVE_AUDIO_ROOM) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SkyBlueHeader),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .border(1.dp, BrightCyanAccent, RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { currentScreen = Screen.LIVE_AUDIO_ROOM }
                                ) {
                                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = LiveIndicatorGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("🔴 Audio Room Active (Keep Mode)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyTextPrimary)
                                        Text("Host: ${audioRoom.hostUser.name} | Tap to Re-enter ↩", fontSize = 9.sp, color = SkyBluePrimary)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = { currentScreen = Screen.LIVE_AUDIO_ROOM },
                                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("Open ↩", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            isAudioRoomKeptInBackground = false
                                            Toast.makeText(context, "Audio Room Closed", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = NavyTextPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    } else if (isVideoRoomKeptInBackground && currentScreen != Screen.LIVE_VIDEO_ROOM) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SkyBlueHeader),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .border(1.dp, BrightCyanAccent, RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { currentScreen = Screen.LIVE_VIDEO_ROOM }
                                ) {
                                    Icon(Icons.Default.Videocam, contentDescription = null, tint = LiveIndicatorGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("🔴 Video Stream Active (Keep Mode)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyTextPrimary)
                                        Text("Host: ${videoRoom.hostUser.name} | Tap to Re-enter ↩", fontSize = 9.sp, color = SkyBluePrimary)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = { currentScreen = Screen.LIVE_VIDEO_ROOM },
                                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("Open ↩", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            isVideoRoomKeptInBackground = false
                                            Toast.makeText(context, "Video Room Closed", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = NavyTextPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // BOTTOM NAVIGATION BAR
                    NavigationBar(
                        containerColor = SkyBlueCardBg,
                        contentColor = SkyBluePrimary,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .fillMaxWidth()
                            .height(68.dp)
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            .border(1.dp, SkyBlueBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == Screen.PRIMARY_DASHBOARD,
                            onClick = { currentScreen = Screen.PRIMARY_DASHBOARD },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard", tint = if (currentScreen == Screen.PRIMARY_DASHBOARD) SkyBluePrimary else SlateTextSecondary) },
                            label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (currentScreen == Screen.PRIMARY_DASHBOARD) SkyBluePrimary else SlateTextSecondary) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.DISCOVER_SWIPE,
                            onClick = { currentScreen = Screen.DISCOVER_SWIPE },
                            icon = { Icon(Icons.Default.Favorite, contentDescription = "Discover", tint = if (currentScreen == Screen.DISCOVER_SWIPE) SkyBluePrimary else SlateTextSecondary) },
                            label = { Text("Discover", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (currentScreen == Screen.DISCOVER_SWIPE) SkyBluePrimary else SlateTextSecondary) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.LIVE_AUDIO_ROOM,
                            onClick = {
                                isAudioRoomKeptInBackground = false
                                currentScreen = Screen.LIVE_AUDIO_ROOM
                            },
                            icon = { Icon(Icons.Default.Mic, contentDescription = "Audio Live", tint = if (currentScreen == Screen.LIVE_AUDIO_ROOM) SkyBluePrimary else SlateTextSecondary) },
                            label = { Text("Audio Room", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (currentScreen == Screen.LIVE_AUDIO_ROOM) SkyBluePrimary else SlateTextSecondary) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.LIVE_VIDEO_ROOM,
                            onClick = {
                                isVideoRoomKeptInBackground = false
                                currentScreen = Screen.LIVE_VIDEO_ROOM
                            },
                            icon = { Icon(Icons.Default.Videocam, contentDescription = "Video Live", tint = if (currentScreen == Screen.LIVE_VIDEO_ROOM) SkyBluePrimary else SlateTextSecondary) },
                            label = { Text("Video Live", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (currentScreen == Screen.LIVE_VIDEO_ROOM) SkyBluePrimary else SlateTextSecondary) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.VIP_STORE,
                            onClick = { currentScreen = Screen.VIP_STORE },
                            icon = { Icon(Icons.Default.Storefront, contentDescription = "VIP Store", tint = if (currentScreen == Screen.VIP_STORE) SkyBluePrimary else SlateTextSecondary) },
                            label = { Text("VIP Store", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (currentScreen == Screen.VIP_STORE) SkyBluePrimary else SlateTextSecondary) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.USER_PROFILE,
                            onClick = { currentScreen = Screen.USER_PROFILE },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile", tint = if (currentScreen == Screen.USER_PROFILE) SkyBluePrimary else SlateTextSecondary) },
                            label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (currentScreen == Screen.USER_PROFILE) SkyBluePrimary else SlateTextSecondary) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.AUTH_SPLASH -> SplashScreen(
                    onLoginSuccess = { currentScreen = Screen.PRIMARY_DASHBOARD }
                )

                Screen.LIVENESS_CHECK -> LivenessVerificationScreen(
                    onVerificationCompleted = { currentScreen = Screen.PRIMARY_DASHBOARD }
                )

                Screen.PRIMARY_DASHBOARD -> PrimaryDashboardScreen(
                    user = user,
                    notifications = notifications,
                    onOpenNotifications = { showNotificationModal = true },
                    onNavigateToSwipe = { currentScreen = Screen.FINAL_DESTINY_DATING },
                    onNavigateToAudioRoom = {
                        isAudioRoomKeptInBackground = false
                        currentScreen = Screen.LIVE_AUDIO_ROOM
                    },
                    onNavigateToVideoRoom = {
                        isVideoRoomKeptInBackground = false
                        currentScreen = Screen.LIVE_VIDEO_ROOM
                    },
                    onNavigateToHostPortal = { currentScreen = Screen.CREATOR_MONETIZATION },
                    onNavigateToVipStore = { currentScreen = Screen.VIP_STORE },
                    onNavigateToSecondaryFeed = { currentScreen = Screen.SECONDARY_FEED },
                    onNavigateToProfile = { currentScreen = Screen.USER_PROFILE },
                    onNavigateToAboutUs = { currentScreen = Screen.ABOUT_US },
                    onRefresh = { repository.refreshDashboardData() }
                )

                Screen.USER_PROFILE -> UserProfileScreen(
                    user = user,
                    onSaveProfile = { updated -> repository.updateUserProfile(updated) },
                    onNavigateToStore = { currentScreen = Screen.VIP_STORE },
                    onNavigateToSecondaryFeed = { currentScreen = Screen.SECONDARY_FEED },
                    onNavigateToMonetization = { currentScreen = Screen.CREATOR_MONETIZATION },
                    onNavigateToDating = { currentScreen = Screen.FINAL_DESTINY_DATING },
                    onNavigateToAboutUs = { currentScreen = Screen.ABOUT_US },
                    onLogOut = { currentScreen = Screen.AUTH_SPLASH },
                    onRefresh = { repository.refreshUserProfile() },
                    onBack = { currentScreen = Screen.PRIMARY_DASHBOARD }
                )

                Screen.SECONDARY_FEED -> SecondaryDashboardScreen(
                    storyTrays = storyTrays,
                    momentPosts = momentPosts,
                    notifications = notifications,
                    onOpenNotifications = { showNotificationModal = true },
                    onLikePost = { postId -> repository.toggleLikePost(postId) },
                    onPublishPost = { caption, mediaUri -> repository.postMoment(caption, mediaUri) },
                    onPublishReel = { caption, mediaUri -> repository.postReelVideo(caption, mediaUri) },
                    onTipPost = { currentScreen = Screen.VIP_STORE },
                    onAddStory = { mediaUri -> repository.addStory(mediaUri) },
                    onAddComment = { postId, text -> repository.addCommentToPost(postId, text) },
                    onToggleFollowAuthor = { postId -> repository.toggleFollowPostAuthor(postId) },
                    onRefresh = { repository.refreshMomentsAndReels() },
                    onBack = { currentScreen = Screen.PRIMARY_DASHBOARD }
                )

                Screen.DISCOVER_SWIPE -> DiscoverSwipeScreen(
                    cards = swipeCards,
                    matchedCard = matchedCard,
                    onSwipeRight = { cardId -> repository.performSwipe(cardId, isLike = true, isSuperLike = false) },
                    onSwipeLeft = { cardId -> repository.performSwipe(cardId, isLike = false, isSuperLike = false) },
                    onSwipeUpSuperLike = { cardId -> repository.performSwipe(cardId, isLike = false, isSuperLike = true) },
                    onDismissMatchModal = { repository.dismissMatchModal() },
                    onStartCallInvitation = { currentScreen = Screen.LIVE_AUDIO_ROOM }
                )

                Screen.LIVE_AUDIO_ROOM -> LiveAudioRoomScreen(
                    room = audioRoom,
                    chatMessages = chatMessages,
                    giftStoreItems = repository.giftStoreItems,
                    moderationAlerts = moderationAlerts,
                    onSendChatMessage = { text -> repository.sendChatMessage(text) },
                    onSendGift = { gift -> repository.sendGiftInRoom(gift) },
                    onToggleSeatMute = { seatIndex -> repository.toggleSeatMute(seatIndex) },
                    onKickUser = { seatIndex -> repository.kickUserFromRoom(seatIndex) },
                    onExitRoom = {
                        isAudioRoomKeptInBackground = false
                        currentScreen = Screen.PRIMARY_DASHBOARD
                    },
                    onKeepRoom = {
                        isAudioRoomKeptInBackground = true
                        currentScreen = Screen.PRIMARY_DASHBOARD
                    }
                )

                Screen.LIVE_VIDEO_ROOM -> LiveVideoRoomScreen(
                    room = videoRoom,
                    chatMessages = chatMessages,
                    giftStoreItems = repository.giftStoreItems,
                    isVisionSentinelActive = isVisionSentinelActive,
                    isVisionBlackoutTriggered = isVisionBlackoutTriggered,
                    onTriggerSentinelTest = { repository.triggerVisionSentinelTest() },
                    onRestoreSentinel = { repository.restoreVisionSentinel() },
                    onSendGift = { gift -> repository.sendGiftInRoom(gift) },
                    onSendChatMessage = { text -> repository.sendChatMessage(text) },
                    onExitRoom = {
                        isVideoRoomKeptInBackground = false
                        currentScreen = Screen.PRIMARY_DASHBOARD
                    },
                    onKeepRoom = {
                        isVideoRoomKeptInBackground = true
                        currentScreen = Screen.PRIMARY_DASHBOARD
                    }
                )

                Screen.VIP_STORE -> VipStoreScreen(
                    vipTiers = repository.vipTiers,
                    giftStoreItems = repository.giftStoreItems,
                    userVipLevel = user.vipLevel,
                    userDiamonds = user.diamonds,
                    isMonetizedHost = kycData.isApproved,
                    hostEarnings = hostEarnings,
                    onPurchaseAsset = { gift -> repository.sendGiftInRoom(gift) },
                    onTopUpDiamonds = { amt -> repository.topUpDiamonds(amt) },
                    onRefresh = { repository.refreshVipStore() },
                    onBack = { currentScreen = Screen.PRIMARY_DASHBOARD }
                )

                Screen.CREATOR_MONETIZATION -> CreatorMonetizationScreen(
                    kycData = kycData,
                    hostEarnings = hostEarnings,
                    userFollowers = user.followerCount,
                    analytics = creatorAnalytics,
                    onSubmitKyc = { aadhaar, pan, legalName -> repository.submitKycForm(aadhaar, pan, legalName) },
                    onRequestPayout = { amt, method, upiOrAccount -> repository.requestBankPayout(amt, method, upiOrAccount) },
                    onRefresh = { repository.refreshCreatorAnalytics() }
                )

                Screen.FINAL_DESTINY_DATING -> FinalDestinyDatingScreen(
                    user = user,
                    cards = swipeCards,
                    matchedCard = matchedCard,
                    onSwipeRight = { cardId -> repository.performSwipe(cardId, isLike = true, isSuperLike = false) },
                    onSwipeLeft = { cardId -> repository.performSwipe(cardId, isLike = false, isSuperLike = false) },
                    onSwipeUpSuperLike = { cardId -> repository.performSwipe(cardId, isLike = false, isSuperLike = true) },
                    onDismissMatchModal = { repository.dismissMatchModal() },
                    onBack = { currentScreen = Screen.PRIMARY_DASHBOARD }
                )

                Screen.ABOUT_US -> AboutUsScreen(
                    onBack = { currentScreen = Screen.PRIMARY_DASHBOARD }
                )
            }
        }
    }

    if (showMonetizationPopup && (currentScreen == Screen.PRIMARY_DASHBOARD || currentScreen == Screen.SECONDARY_FEED)) {
        MonetizationAnnouncementModal(
            onDismiss = { showMonetizationPopup = false },
            onStartCreating = {
                showMonetizationPopup = false
                currentScreen = Screen.SECONDARY_FEED
            }
        )
    }

    if (showPermissionModal) {
        PermissionModalDialog(
            onAcceptPermissions = { showPermissionModal = false },
            onDismiss = { showPermissionModal = false }
        )
    }

    if (showNotificationModal) {
        NotificationCenterModal(
            notifications = notifications,
            onDismiss = { showNotificationModal = false },
            onMarkAllRead = { repository.markAllNotificationsAsRead() },
            onClearAll = { repository.clearAllNotifications() },
            onNotificationClick = { notification ->
                repository.markNotificationAsRead(notification.id)
                showNotificationModal = false
                when (notification.actionTargetScreen) {
                    "LIVE_AUDIO_ROOM" -> {
                        isAudioRoomKeptInBackground = false
                        currentScreen = Screen.LIVE_AUDIO_ROOM
                    }
                    "LIVE_VIDEO_ROOM" -> {
                        isVideoRoomKeptInBackground = false
                        currentScreen = Screen.LIVE_VIDEO_ROOM
                    }
                    "DISCOVER_SWIPE" -> currentScreen = Screen.DISCOVER_SWIPE
                    "SECONDARY_FEED" -> currentScreen = Screen.SECONDARY_FEED
                    "CREATOR_MONETIZATION" -> currentScreen = Screen.CREATOR_MONETIZATION
                    else -> {}
                }
            }
        )
    }

    val currentUpdate = updateInfoState
    if (currentUpdate != null) {
        UpdateInstallerModalDialog(
            updateInfo = currentUpdate,
            downloadProgress = updateDownloadProgress,
            isDownloading = isDownloadingUpdate,
            onStartDownload = {
                coroutineScope.launch {
                    isDownloadingUpdate = true
                    updateDownloadProgress = 0
                    val downloadedFile = AppInstallerEngine.downloadApkFile(
                        context = context,
                        downloadUrl = currentUpdate.apkDownloadUrl
                    ) { progress ->
                        updateDownloadProgress = progress
                    }
                    isDownloadingUpdate = false
                    if (downloadedFile != null) {
                        val installPromptSuccess = AppInstallerEngine.promptPackageInstall(context, downloadedFile)
                        if (!installPromptSuccess) {
                            Toast.makeText(context, "Please allow 'Install Unknown Apps' permission to complete update.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Download failed. Please check network connection.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = {
                updateInfoState = null
            }
        )
    }
}