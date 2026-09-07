package com.devil.finaldestiny

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.devil.finaldestiny.data.AppRepository
import com.devil.finaldestiny.model.GiftItem
import com.devil.finaldestiny.model.PaymentMethodType
import com.devil.finaldestiny.ui.components.PermissionModalDialog
import com.devil.finaldestiny.ui.screens.*
import com.devil.finaldestiny.ui.theme.CardBackground
import com.devil.finaldestiny.ui.theme.CrimsonVelvet
import com.devil.finaldestiny.ui.theme.FinalDestinyTheme
import com.devil.finaldestiny.ui.theme.LightGold
import com.devil.finaldestiny.ui.theme.MetallicGold
import com.devil.finaldestiny.ui.theme.WineRedDark
import com.devil.finaldestiny.ui.theme.WineRedMedium

enum class Screen {
    AUTH_SPLASH,
    LIVENESS_CHECK,
    PRIMARY_DASHBOARD,
    SECONDARY_FEED,
    DISCOVER_SWIPE,
    LIVE_AUDIO_ROOM,
    LIVE_VIDEO_ROOM,
    VIP_STORE,
    CREATOR_MONETIZATION
}

class MainActivity : ComponentActivity() {
    private val repository = AppRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinalDestinyTheme {
                FinalDestinyApp(repository = repository)
            }
        }
    }
}

@Composable
fun FinalDestinyApp(repository: AppRepository) {
    var currentScreen by remember { mutableStateOf(Screen.AUTH_SPLASH) }
    var showPermissionModal by remember { mutableStateOf(false) }

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

    Scaffold(
        bottomBar = {
            if (currentScreen != Screen.AUTH_SPLASH && currentScreen != Screen.LIVENESS_CHECK) {
                NavigationBar(
                    containerColor = CardBackground,
                    contentColor = MetallicGold,
                    modifier = Modifier
                        .height(72.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .border(1.dp, CrimsonVelvet, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.PRIMARY_DASHBOARD,
                        onClick = { currentScreen = Screen.PRIMARY_DASHBOARD },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard", tint = if (currentScreen == Screen.PRIMARY_DASHBOARD) MetallicGold else LightGold.copy(0.6f)) },
                        label = { Text("Dashboard", fontSize = 10.sp, color = if (currentScreen == Screen.PRIMARY_DASHBOARD) MetallicGold else LightGold.copy(0.6f)) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.SECONDARY_FEED,
                        onClick = { currentScreen = Screen.SECONDARY_FEED },
                        icon = { Icon(Icons.Default.DynamicFeed, contentDescription = "Moments", tint = if (currentScreen == Screen.SECONDARY_FEED) MetallicGold else LightGold.copy(0.6f)) },
                        label = { Text("Moments", fontSize = 10.sp, color = if (currentScreen == Screen.SECONDARY_FEED) MetallicGold else LightGold.copy(0.6f)) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.DISCOVER_SWIPE,
                        onClick = { currentScreen = Screen.DISCOVER_SWIPE },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Discover", tint = if (currentScreen == Screen.DISCOVER_SWIPE) MetallicGold else LightGold.copy(0.6f)) },
                        label = { Text("Discover", fontSize = 10.sp, color = if (currentScreen == Screen.DISCOVER_SWIPE) MetallicGold else LightGold.copy(0.6f)) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.LIVE_AUDIO_ROOM,
                        onClick = { currentScreen = Screen.LIVE_AUDIO_ROOM },
                        icon = { Icon(Icons.Default.Mic, contentDescription = "Audio Live", tint = if (currentScreen == Screen.LIVE_AUDIO_ROOM) MetallicGold else LightGold.copy(0.6f)) },
                        label = { Text("10-Mic Audio", fontSize = 10.sp, color = if (currentScreen == Screen.LIVE_AUDIO_ROOM) MetallicGold else LightGold.copy(0.6f)) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.LIVE_VIDEO_ROOM,
                        onClick = { currentScreen = Screen.LIVE_VIDEO_ROOM },
                        icon = { Icon(Icons.Default.Videocam, contentDescription = "Video Live", tint = if (currentScreen == Screen.LIVE_VIDEO_ROOM) MetallicGold else LightGold.copy(0.6f)) },
                        label = { Text("Video Sync", fontSize = 10.sp, color = if (currentScreen == Screen.LIVE_VIDEO_ROOM) MetallicGold else LightGold.copy(0.6f)) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.VIP_STORE,
                        onClick = { currentScreen = Screen.VIP_STORE },
                        icon = { Icon(Icons.Default.Storefront, contentDescription = "VIP Store", tint = if (currentScreen == Screen.VIP_STORE) MetallicGold else LightGold.copy(0.6f)) },
                        label = { Text("VIP Store", fontSize = 10.sp, color = if (currentScreen == Screen.VIP_STORE) MetallicGold else LightGold.copy(0.6f)) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.CREATOR_MONETIZATION,
                        onClick = { currentScreen = Screen.CREATOR_MONETIZATION },
                        icon = { Icon(Icons.Default.MonetizationOn, contentDescription = "Payouts", tint = if (currentScreen == Screen.CREATOR_MONETIZATION) MetallicGold else LightGold.copy(0.6f)) },
                        label = { Text("25% Payouts", fontSize = 10.sp, color = if (currentScreen == Screen.CREATOR_MONETIZATION) MetallicGold else LightGold.copy(0.6f)) }
                    )
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
                    onNavigateToSwipe = { currentScreen = Screen.DISCOVER_SWIPE },
                    onNavigateToAudioRoom = { currentScreen = Screen.LIVE_AUDIO_ROOM },
                    onNavigateToVideoRoom = { currentScreen = Screen.LIVE_VIDEO_ROOM },
                    onNavigateToHostPortal = { currentScreen = Screen.CREATOR_MONETIZATION },
                    onNavigateToVipStore = { currentScreen = Screen.VIP_STORE },
                    onNavigateToSecondaryFeed = { currentScreen = Screen.SECONDARY_FEED }
                )

                Screen.SECONDARY_FEED -> SecondaryDashboardScreen(
                    storyTrays = storyTrays,
                    momentPosts = momentPosts,
                    onLikePost = { postId -> repository.toggleLikePost(postId) },
                    onPublishPost = { caption -> repository.postMoment(caption) },
                    onTipPost = { post -> currentScreen = Screen.VIP_STORE }
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
                    onKickUser = { seatIndex -> repository.kickUserFromRoom(seatIndex) }
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
                    onSendChatMessage = { text -> repository.sendChatMessage(text) }
                )

                Screen.VIP_STORE -> VipStoreScreen(
                    vipTiers = repository.vipTiers,
                    giftStoreItems = repository.giftStoreItems,
                    userVipLevel = user.vipLevel,
                    userDiamonds = user.diamonds,
                    onPurchaseAsset = { gift -> repository.sendGiftInRoom(gift) }
                )

                Screen.CREATOR_MONETIZATION -> CreatorMonetizationScreen(
                    kycData = kycData,
                    hostEarnings = hostEarnings,
                    userFollowers = user.followerCount,
                    onSubmitKyc = { aadhaar, pan, legalName -> repository.submitKycForm(aadhaar, pan, legalName) },
                    onRequestPayout = { amt, method, upiOrAccount -> repository.requestBankPayout(amt, method, upiOrAccount) }
                )
            }
        }
    }

    if (showPermissionModal) {
        PermissionModalDialog(
            onAcceptPermissions = { showPermissionModal = false },
            onDismiss = { showPermissionModal = false }
        )
    }
}