package com.devil.finaldestiny

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
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
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.data.AppRepository
import com.devil.finaldestiny.engine.AppInstallerEngine
import com.devil.finaldestiny.engine.UpdateReleaseInfo
import com.devil.finaldestiny.model.GiftItem
import com.devil.finaldestiny.model.PaymentMethodType
import com.devil.finaldestiny.ui.components.FloatingGalaxyNavPill
import com.devil.finaldestiny.ui.components.MediaPickerBottomSheet
import com.devil.finaldestiny.ui.components.MonetizationAnnouncementModal
import com.devil.finaldestiny.ui.components.NotificationCenterModal
import com.devil.finaldestiny.ui.components.PermissionModalDialog
import com.devil.finaldestiny.ui.components.UpdateInstallerModalDialog
import com.devil.finaldestiny.ui.screens.*
import com.devil.finaldestiny.ui.settings.*
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
    SEARCH_EXPLORE,
    INBOX,
    NOTIFICATION,
    DISCOVER_SWIPE,
    LIVE_AUDIO_ROOM,
    LIVE_VIDEO_ROOM,
    VIP_STORE,
    CREATOR_MONETIZATION,
    USER_PROFILE,
    CREATOR_HUB,
    CREATOR_TOOLS,
    SETTINGS_ACTIVITY,
    FINAL_DESTINY_DATING,
    ABOUT_US,
    NEW_POST,
    REEL_VIEWER
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

        lifecycleScope.launch {
            try {
                val info = AppInstallerEngine.checkSupabaseAppVersion()
                if (info != null && info.isUpdateAvailable) {
                    globalUpdateInfo.value = info
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

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
        var globalUpdateInfo = androidx.compose.runtime.mutableStateOf<UpdateReleaseInfo?>(null)
    }
}

@Composable
fun FinalDestinyApp(repository: AppRepository) {
    val context = LocalContext.current
    LaunchedEffect(context) {
        com.devil.finaldestiny.ui.theme.ThemeManager.init(context)
    }

    val initialScreen = remember(context) {
        if (com.devil.finaldestiny.data.SupabaseAuthClient.hasValidSession(context)) {
            Screen.PRIMARY_DASHBOARD
        } else {
            Screen.AUTH_SPLASH
        }
    }
    var currentScreen by remember { mutableStateOf(initialScreen) }
    var showPermissionModal by remember { mutableStateOf(false) }
    var showMonetizationPopup by remember { mutableStateOf(true) }

    // Background Kept Room Sessions State
    var isAudioRoomKeptInBackground by remember { mutableStateOf(false) }
    var isVideoRoomKeptInBackground by remember { mutableStateOf(false) }

    val user by repository.currentUser.collectAsState()
    val userSettingsState by repository.userSettingsState.collectAsState()
    val savedAccounts by repository.savedAccounts.collectAsState()
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

    var showMediaPickerSheet by remember { mutableStateOf(false) }
    var pickedMediaUri by remember { mutableStateOf<String?>(null) }
    var isPickedMediaReel by remember { mutableStateOf(false) }
    var selectedReelInitialIndex by remember { mutableIntStateOf(0) }

    // Auto-Update Checker State (Supabase Remote Config)
    var updateInfoState by remember { mutableStateOf<UpdateReleaseInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }
    var updateDownloadProgress by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repository.refreshNotifications()
        var hasException = false
        try {
            android.widget.Toast.makeText(context, "Checking for updates...", android.widget.Toast.LENGTH_SHORT).show()
            val localVerCode = com.devil.finaldestiny.BuildConfig.VERSION_CODE
            val info = AppInstallerEngine.checkSupabaseAppVersion(context = context, currentVersionCode = localVerCode)
            if (info != null) {
                android.widget.Toast.makeText(context, "Supabase: v${info.versionCode} (Local:$localVerCode)", android.widget.Toast.LENGTH_LONG).show()
                updateInfoState = info
                if (info.isUpdateAvailable) {
                    showUpdateDialog = true
                }
            } else {
                hasException = true
                android.widget.Toast.makeText(context, "Update check returned NULL", android.widget.Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            hasException = true
            android.widget.Toast.makeText(context, "Update check error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
            when (currentScreen) {
                Screen.AUTH_SPLASH -> SplashScreen(
                    repository = repository,
                    onLoginSuccess = { currentScreen = Screen.PRIMARY_DASHBOARD }
                )

                Screen.LIVENESS_CHECK -> LivenessVerificationScreen(
                    onVerificationCompleted = { currentScreen = Screen.PRIMARY_DASHBOARD }
                )

                Screen.PRIMARY_DASHBOARD -> PrimaryDashboardScreen(
                    user = user,
                    notifications = notifications,
                    onOpenNotifications = { currentScreen = Screen.NOTIFICATION },
                    onNavigateToHostPortal = { currentScreen = Screen.CREATOR_MONETIZATION },
                    onNavigateToVipStore = { currentScreen = Screen.VIP_STORE },
                    onNavigateToSecondaryFeed = { currentScreen = Screen.SECONDARY_FEED },
                    onNavigateToProfile = { currentScreen = Screen.USER_PROFILE },
                    onNavigateToAboutUs = { currentScreen = Screen.ABOUT_US },
                    onRefresh = { repository.refreshDashboardData() }
                )

                Screen.USER_PROFILE -> UserProfileScreen(
                    user = user,
                    userPosts = momentPosts.filter { it.userId == user.id || it.authorHandle == user.handle || it.authorName == user.name },
                    savedAccounts = savedAccounts,
                    onSaveProfile = { updated -> repository.updateUserProfile(updated, context) },
                    onNavigateToStore = { currentScreen = Screen.VIP_STORE },
                    onNavigateToSecondaryFeed = { currentScreen = Screen.SECONDARY_FEED },
                    onNavigateToMonetization = { currentScreen = Screen.CREATOR_MONETIZATION },
                    onNavigateToCreatorHub = { currentScreen = Screen.CREATOR_HUB },
                    onNavigateToCreatorTools = { currentScreen = Screen.CREATOR_TOOLS },
                    onNavigateToSettings = { currentScreen = Screen.SETTINGS_ACTIVITY },
                    onNavigateToAboutUs = { currentScreen = Screen.ABOUT_US },
                    onLogOut = {
                        com.devil.finaldestiny.data.SupabaseAuthClient.signOut(context)
                        currentScreen = Screen.AUTH_SPLASH
                    },
                    onRefresh = { repository.refreshUserProfile() },
                    onToggleFollowCandidate = { targetId, isFollowing -> repository.toggleFollowUser(targetId, isFollowing) },
                    onSwitchAccount = { targetId -> repository.switchAccount(targetId, context) },
                    onAddAccount = { email, name -> repository.addAccount(email, name, context) },
                    onRemoveAccount = { targetId -> repository.removeAccount(targetId, context) },
                    onOpenMediaPicker = { showMediaPickerSheet = true },
                    onBack = { currentScreen = Screen.PRIMARY_DASHBOARD }
                )

                Screen.CREATOR_HUB -> CreatorHubScreen(
                    user = user,
                    onSaveUser = { updated -> repository.updateUserProfile(updated, context) },
                    onNavigateToEditProfile = { currentScreen = Screen.USER_PROFILE },
                    onNavigateToTools = { currentScreen = Screen.CREATOR_TOOLS },
                    onBack = { currentScreen = Screen.USER_PROFILE }
                )

                Screen.CREATOR_TOOLS -> CreatorToolsScreen(
                    user = user,
                    onSaveUser = { updated -> repository.updateUserProfile(updated, context) },
                    onBack = { currentScreen = Screen.USER_PROFILE }
                )

                Screen.SETTINGS_ACTIVITY -> SettingsActivityScreen(
                    userSettings = userSettingsState,
                    onUpdateSettings = { updated -> repository.updateSettingsState(updated) },
                    onLogOutAllSessions = {
                        com.devil.finaldestiny.data.SupabaseAuthClient.signOut(context)
                        currentScreen = Screen.AUTH_SPLASH
                    },
                    onClearSearchHistory = { repository.clearSearchHistory() },
                    onRequestDataExport = {
                        Toast.makeText(context, "📩 Asynchronous data export requested! Download link will be emailed to ${userSettingsState.email}.", Toast.LENGTH_LONG).show()
                    },
                    onAddKeywordToBlacklist = { keyword -> repository.addKeywordToBlacklist(keyword) },
                    onUnblockUser = { targetId -> repository.unblockUser(targetId) },
                    onResetFeedInterests = { repository.resetFeedInterests() },
                    onNavigateToCreatorTools = { currentScreen = Screen.CREATOR_TOOLS },
                    onBack = { currentScreen = Screen.USER_PROFILE }
                )

                Screen.SECONDARY_FEED -> SecondaryDashboardScreen(
                    user = user,
                    storyTrays = storyTrays,
                    momentPosts = momentPosts,
                    notifications = notifications,
                    onOpenNotifications = { currentScreen = Screen.NOTIFICATION },
                    onLikePost = { postId -> repository.toggleLikePost(postId) },
                    onPublishPost = { caption, mediaUri, isAiGenerated, commentsDisabled, hideLikes, hideShares, scheduledAt, altText, appliedFilter, overlayText, ctaLink, ctaLabel, isPaidPartnership, promotionStatus, promotionBudget ->
                        repository.postMoment(context, caption, mediaUri, isAiGenerated, commentsDisabled, hideLikes, hideShares, scheduledAt, altText, appliedFilter, overlayText, ctaLink, ctaLabel, isPaidPartnership, promotionStatus, promotionBudget)
                    },
                    onPublishReel = { caption, mediaUri, audioTitle, audioArtist, audioUrl, isAiGenerated, commentsDisabled, hideLikes, hideShares, scheduledAt, altText, appliedFilter, overlayText, ctaLink, ctaLabel, isPaidPartnership, promotionStatus, promotionBudget ->
                        repository.postReelVideo(context, caption, mediaUri, audioTitle, audioArtist, audioUrl, isAiGenerated, commentsDisabled, hideLikes, hideShares, scheduledAt, altText, appliedFilter, overlayText, ctaLink, ctaLabel, isPaidPartnership, promotionStatus, promotionBudget)
                    },
                    onTipPost = { currentScreen = Screen.VIP_STORE },
                    onAddStory = { mediaUri -> repository.addStory(mediaUri) },
                    onAddComment = { postId, text -> repository.addCommentToPost(postId, text) },
                    onToggleFollowAuthor = { postId -> repository.toggleFollowPostAuthor(postId) },
                    onToggleSavePost = { postId -> repository.toggleSavePost(postId) },
                    onIncrementView = { postId -> repository.incrementPostView(postId) },
                    onRefresh = { repository.refreshMomentsAndReels() },
                    onOpenMediaPicker = { showMediaPickerSheet = true },
                    onBack = { currentScreen = Screen.PRIMARY_DASHBOARD }
                )

                Screen.SEARCH_EXPLORE -> SearchExploreScreen(
                    explorePosts = momentPosts,
                    onSelectPost = { currentScreen = Screen.SECONDARY_FEED },
                    onBack = { currentScreen = Screen.SECONDARY_FEED }
                )

                Screen.INBOX -> InboxScreen(
                    onBack = { currentScreen = Screen.SECONDARY_FEED }
                )

                Screen.NOTIFICATION -> NotificationScreen(
                    user = user,
                    notificationsList = notifications,
                    onRefresh = { repository.refreshNotifications() },
                    onNavigateToFeed = { currentScreen = Screen.SECONDARY_FEED },
                    onBack = { currentScreen = Screen.SECONDARY_FEED }
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
                    onRefresh = { repository.refreshCreatorAnalytics() },
                    onBack = { currentScreen = Screen.PRIMARY_DASHBOARD }
                )

                Screen.ABOUT_US -> AboutUsScreen(
                    onBack = { currentScreen = Screen.PRIMARY_DASHBOARD }
                )

                Screen.NEW_POST -> InstagramNewPostScreen(
                    mediaUri = pickedMediaUri,
                    isReel = isPickedMediaReel,
                    user = user,
                    onBack = { currentScreen = Screen.PRIMARY_DASHBOARD },
                    onPublish = { caption, mediaUri, audioTitle, audioArtist, audioUrl, isAiGenerated, commentsDisabled, hideLikes, hideShares, scheduledAt, altText, appliedFilter, overlayText, ctaLink, ctaLabel, isPaidPartnership, promotionStatus, promotionBudget ->
                        coroutineScope.launch {
                            if (isPickedMediaReel) {
                                repository.postReelVideo(
                                    context, caption, mediaUri, audioTitle, audioArtist, audioUrl,
                                    isAiGenerated, commentsDisabled, hideLikes, hideShares, scheduledAt,
                                    altText, appliedFilter, overlayText, ctaLink, ctaLabel,
                                    isPaidPartnership, promotionStatus, promotionBudget
                                )
                            } else {
                                repository.postMoment(
                                    context, caption, mediaUri, isAiGenerated, commentsDisabled,
                                    hideLikes, hideShares, scheduledAt, altText, appliedFilter,
                                    overlayText, ctaLink, ctaLabel, isPaidPartnership,
                                    promotionStatus, promotionBudget
                                )
                            }
                        }
                        currentScreen = Screen.PRIMARY_DASHBOARD
                    }
                )

                else -> {}
            }
        }

        // EDGE-TO-EDGE FLOATING GALAXY NAVIGATION PILL CONTAINER (iOS DYNAMIC ISLAND STYLE)
        if (currentScreen != Screen.AUTH_SPLASH && currentScreen != Screen.LIVENESS_CHECK && currentScreen != Screen.ABOUT_US) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                    .zIndex(99f)
            ) {
                if (currentScreen == Screen.PRIMARY_DASHBOARD || currentScreen == Screen.SECONDARY_FEED) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FloatingGalaxyNavPill(
                            currentScreen = currentScreen,
                            user = user,
                            unreadNotificationCount = notifications.count { !it.isRead },
                            onNavigate = { destination -> currentScreen = destination },
                            modifier = Modifier.weight(1f)
                        )
                        FloatingActionButton(
                            onClick = { showMediaPickerSheet = true },
                            shape = CircleShape,
                            containerColor = Color(0xFF1E88E5),
                            contentColor = Color.White,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Post",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
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

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        val micGranted = permissions[android.Manifest.permission.RECORD_AUDIO] ?: false
        if (cameraGranted || micGranted) {
            Toast.makeText(context, "✅ Camera & Audio OS Permissions Granted!", Toast.LENGTH_SHORT).show()
        }
    }

    if (showPermissionModal) {
        PermissionModalDialog(
            onAcceptPermissions = {
                showPermissionModal = false
                try {
                    val perms = mutableListOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.RECORD_AUDIO
                    )
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        perms.add(android.Manifest.permission.READ_MEDIA_IMAGES)
                        perms.add(android.Manifest.permission.READ_MEDIA_VIDEO)
                    } else {
                        perms.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    permissionsLauncher.launch(perms.toTypedArray())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            onDismiss = { showPermissionModal = false }
        )
    }

    if (showMediaPickerSheet) {
        MediaPickerBottomSheet(
            onDismiss = { showMediaPickerSheet = false },
            onMediaSelected = { uri, isVideo ->
                pickedMediaUri = uri.toString()
                isPickedMediaReel = isVideo
                showMediaPickerSheet = false
                currentScreen = Screen.NEW_POST
            }
        )
    }

    if (showNotificationModal) {
        NotificationCenterModal(
            notifications = notifications,
            onDismiss = { showNotificationModal = false },
            onMarkAllRead = { repository.markAllNotificationsAsRead() },
            onClearAll = { repository.clearAllNotifications() },
            onNavigateToFeed = { currentScreen = Screen.SECONDARY_FEED },
            onNotificationClick = { notification ->
                repository.markNotificationAsRead(notification.id)
                showNotificationModal = false
                when (notification.actionTargetScreen) {
                    "SECONDARY_FEED" -> currentScreen = Screen.SECONDARY_FEED
                    "CREATOR_MONETIZATION" -> currentScreen = Screen.CREATOR_MONETIZATION
                    else -> {}
                }
            }
        )
    }

    val currentUpdate = updateInfoState ?: MainActivity.globalUpdateInfo.value
    if ((showUpdateDialog || currentUpdate != null) && currentUpdate != null && currentUpdate.isUpdateAvailable) {
        android.util.Log.d("UPDATE_FLOW", "Rendering dialog on screen now: isUpdateAvailable=true")
        UpdateInstallerModalDialog(
            updateInfo = currentUpdate,
            downloadProgress = updateDownloadProgress,
            isDownloading = isDownloadingUpdate,
            onStartDownload = {
                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    isDownloadingUpdate = true
                    updateDownloadProgress = 0
                    val downloadId = AppInstallerEngine.startDownload(context, currentUpdate.apkDownloadUrl)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        if (downloadId != -1L) {
                            Toast.makeText(context, "⬇️ Downloading update in notification bar...", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Opening download in browser...", Toast.LENGTH_SHORT).show()
                        }
                        isDownloadingUpdate = false
                    }
                }
            },
            onDismiss = {
                showUpdateDialog = false
                updateInfoState = null
                MainActivity.globalUpdateInfo.value = null
            }
        )
    }
}