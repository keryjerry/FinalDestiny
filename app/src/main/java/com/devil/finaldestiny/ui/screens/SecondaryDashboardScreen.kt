package com.devil.finaldestiny.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.data.SupabaseAuthClient
import com.devil.finaldestiny.engine.intelligence.CoreIntelligenceEngine
import com.devil.finaldestiny.model.AppNotification
import com.devil.finaldestiny.model.AudioTrack
import com.devil.finaldestiny.model.EventType
import com.devil.finaldestiny.model.MediaType
import com.devil.finaldestiny.model.MomentPost
import com.devil.finaldestiny.model.StoryItem
import com.devil.finaldestiny.model.UserProfile
import com.devil.finaldestiny.ui.components.ProfileAvatarView
import com.devil.finaldestiny.ui.theme.*
import com.devil.finaldestiny.utils.TimeUtils

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryDashboardScreen(
    user: UserProfile = UserProfile(),
    storyTrays: List<StoryItem>,
    momentPosts: List<MomentPost>,
    notifications: List<AppNotification> = emptyList(),
    onOpenNotifications: () -> Unit = {},
    onLikePost: (String) -> Unit,
    onPublishPost: (
        caption: String,
        mediaUri: String?,
        isAiGenerated: Boolean,
        commentsDisabled: Boolean,
        hideLikes: Boolean,
        hideShares: Boolean,
        scheduledAt: String?,
        altText: String?,
        appliedFilter: String?,
        overlayText: String?
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
    onPublishReel: (
        caption: String,
        mediaUri: String?,
        audioTitle: String?,
        audioArtist: String?,
        audioUrl: String?,
        isAiGenerated: Boolean,
        commentsDisabled: Boolean,
        hideLikes: Boolean,
        hideShares: Boolean,
        scheduledAt: String?,
        altText: String?,
        appliedFilter: String?,
        overlayText: String?
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onTipPost: (MomentPost) -> Unit,
    onAddStory: (String) -> Unit = {},
    onAddComment: (String, String) -> Unit = { _, _ -> },
    onToggleFollowAuthor: (String) -> Unit = {},
    onToggleSavePost: (String) -> Unit = {},
    onStartLiveStream: () -> Unit = {},
    onRefresh: suspend () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    var showCreatePostDialog by remember { mutableStateOf(false) }
    var isReelUploadMode by remember { mutableStateOf(false) }
    var showCommentsSheetForPost by remember { mutableStateOf<MomentPost?>(null) }
    var showMoreOptionsMenuForPost by remember { mutableStateOf<MomentPost?>(null) }
    var showAudioDetailSheetForPost by remember { mutableStateOf<MomentPost?>(null) }
    var showDirectShareSheetForPost by remember { mutableStateOf<MomentPost?>(null) }
    var activeStoryView by remember { mutableStateOf<StoryItem?>(null) }

    // Music Search Bottom Sheet State for Reel Upload
    var showMusicPickerSheet by remember { mutableStateOf(false) }
    var selectedAudioTrack by remember { mutableStateOf<AudioTrack?>(null) }
    var musicSearchQuery by remember { mutableStateOf("") }
    var musicTabState by remember { mutableIntStateOf(0) } // 0: Trending, 1: Saved
    var playingPreviewAudioId by remember { mutableStateOf<String?>(null) }

    // Sample Trending Audio Tracks
    val sampleAudioTracks = remember {
        listOf(
            AudioTrack("a1", "Ye Meera Deewanapan Hai ✨", "Susheela Raman", duration = "0:30"),
            AudioTrack("a2", "Destiny Acoustic Sunset 🎸", "Aarav Sharma", duration = "0:45"),
            AudioTrack("a3", "Midnight Synth Beats 🎹", "DJ Arjun", duration = "0:30"),
            AudioTrack("a4", "Bole Chudiyan (Remix) 💃", "Simran & Group", duration = "0:60"),
            AudioTrack("a5", "Lo-Fi Coffee Chill ☕", "LoFi Girl", duration = "0:30")
        )
    }

    // Eligibility & Warning Dialog States for Go Live
    var showNotEligibleDialog by remember { mutableStateOf(false) }
    var showCommunityGuidelinesDialog by remember { mutableStateOf(false) }

    var isUploadingMedia by remember { mutableStateOf(false) }
    var uploadProgressPercentage by remember { mutableFloatStateOf(0f) }
    var uploadStatusText by remember { mutableStateOf("Uploading media...") }

    var captionInput by remember { mutableStateOf("") }
    var selectedMediaUri by remember { mutableStateOf<String?>(null) }
    var commentInputText by remember { mutableStateOf("") }

    // Launcher for selecting Media (Photo or Reel Video)
    val postMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedMediaUri = it.toString()
            showCreatePostDialog = true
            Toast.makeText(context, if (isReelUploadMode) "🎬 Reel Video Selected!" else "📸 Photo Selected!", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher for adding a Story (supports BOTH Photos and Videos */*)
    val storyMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                isUploadingMedia = true
                uploadStatusText = "Uploading 24h Story..."
                for (p in 1..10) {
                    uploadProgressPercentage = p / 10f
                    kotlinx.coroutines.delay(120)
                }
                onAddStory(it.toString())
                isUploadingMedia = false
                uploadProgressPercentage = 0f
                Toast.makeText(context, "✨ 24h Story Posted!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val storyRingGradient = Brush.linearGradient(
        colors = listOf(BrightCyanAccent, SkyBluePrimary, VerifiedBlue)
    )

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                try {
                    onRefresh()
                } finally {
                    isRefreshing = false
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlueBgLight)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // UPLOAD PROGRESS SUMMARY BANNER (STICKY TOP BANNER FOR BOTH PHOTOS & VIDEOS)
            if (isUploadingMedia) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color(0xFF38BDF8),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(uploadStatusText, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                                Text("${(uploadProgressPercentage * 100).toInt()}%", color = Color(0xFFFACC15), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { uploadProgressPercentage },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF38BDF8),
                                trackColor = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }

            // TOP BAR HEADER: UNIVERSAL BACK ARROW ('<'), INSTAGRAM BRAND LOGO & HEART NOTIFICATION BELL
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SkyBlueHeader)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NavyTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Final Destiny",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NavyTextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    // Top-Right Heart Icon for Notifications
                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SkyBlueHeader)
                        ) {
                            Icon(
                                imageVector = if (notifications.any { !it.isRead }) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Social Notifications",
                                tint = HeartRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        if (notifications.any { !it.isRead }) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(HeartRed)
                                    .border(1.5.dp, Color.White, CircleShape)
                            )
                        }
                    }
                }
            }

            // INSTAGRAM-STYLE STORIES TRAY
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Current User Profile Story Item with '+' Badge
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { storyMediaLauncher.launch("*/*") }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(64.dp)
                            ) {
                                ProfileAvatarView(
                                    name = user.name.ifBlank { "You" },
                                    profilePictureUri = user.profilePictureUri,
                                    size = 60.dp,
                                    showBorder = true,
                                    borderColor = SkyBluePrimary
                                )

                                // '+' Badge Overlay at Bottom-Right
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2563EB))
                                        .border(1.5.dp, Color.White, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Media Story",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Your story", fontSize = 10.sp, color = NavyTextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Active Followers / Friends Stories List
                    items(storyTrays) { story ->
                        val storyImageBitmap = rememberLoadedImage(context, story.mediaUri)
                        val relativeTime = TimeUtils.formatTimestamp(story.timestamp, story.createdAtEpochMs)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { activeStoryView = story }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(SkyBlueBgLight)
                                    .border(
                                        width = 2.5.dp,
                                        brush = if (story.isViewed) Brush.linearGradient(listOf(Color.LightGray, Color.Gray)) else storyRingGradient,
                                        shape = CircleShape
                                    )
                            ) {
                                if (storyImageBitmap != null) {
                                    Image(
                                        bitmap = storyImageBitmap,
                                        contentDescription = story.authorName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(text = story.authorName.take(1).uppercase(), fontSize = 20.sp, color = SkyBluePrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(story.authorName, fontSize = 10.sp, color = NavyTextPrimary, maxLines = 1, fontWeight = FontWeight.SemiBold)
                            Text(relativeTime, fontSize = 9.sp, color = SlateTextSecondary, maxLines = 1)
                        }
                    }
                }
            }

            // ACTION BUTTONS (DIRECTLY BELOW STORIES TRAY: 🎬 REEL & 🔴 GO LIVE)
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    // 🎬 Reel Button: Gradient Blue #2563EB to Purple #7C3AED
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                                )
                            )
                            .clickable {
                                isReelUploadMode = true
                                postMediaLauncher.launch("*/*")
                            }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎬", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // 🔴 Go Live Button: Gradient Red #E11D48 to Orange #F97316 (With 500 Follower Check & Guidelines)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFE11D48), Color(0xFFF97316))
                                )
                            )
                            .clickable {
                                if (user.followerCount < 500) {
                                    showNotEligibleDialog = true
                                } else {
                                    showCommunityGuidelinesDialog = true
                                }
                            }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔴", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Go Live", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // FULL-BLEED EDGE-TO-EDGE FEED POSTS WITH FULL INSTAGRAM OVERLAYS
            items(momentPosts) { post ->
                val localBitmap = rememberLoadedImage(context, post.mediaUri)
                val isReel = post.mediaType == MediaType.REEL_VIDEO
                var isExpandedCaption by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    // FULL-BLEED MEDIA CONTAINER WITH TOP 4 & BOTTOM 6 INSTAGRAM OVERLAYS
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(if (isReel) 9f / 16f else 4f / 5f)
                            .background(NavyTextPrimary)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        onLikePost(post.id)
                                        Toast.makeText(context, "❤️ Loved!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                    ) {
                        if (isReel && !post.mediaUri.isNullOrEmpty()) {
                            ExoVideoPlayerView(
                                videoUri = post.mediaUri,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (localBitmap != null) {
                            Image(
                                bitmap = localBitmap,
                                contentDescription = "Uploaded Media",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    if (isReel) Icons.Default.Videocam else Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = BrightCyanAccent,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isReel) "🎬 9:16 Vertical Reel Video" else "📸 4:5 Photo Post",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Top Gradient Scrim for Header Overlay Visibility
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .align(Alignment.TopCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Black.copy(0.65f), Color.Transparent)
                                    )
                                )
                        )

                        // ----------------------------------------------------
                        // TOP 4 ACTIONS OVERLAY (MATCHING ANNOTATED SCREENSHOT)
                        // ----------------------------------------------------
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                // ARROW 1: Profile Avatar & Co-Author Header
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ProfileAvatarView(
                                        name = post.authorName,
                                        profilePictureUri = post.authorAvatar,
                                        size = 32.dp,
                                        showBorder = true,
                                        borderColor = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = post.authorHandle.removePrefix("@"),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                    Text(" ✓", fontSize = 10.sp, color = VerifiedBlue, fontWeight = FontWeight.Bold)

                                    if (!post.collaboratorName.isNullOrBlank()) {
                                        Text(
                                            text = " and ",
                                            color = Color.White.copy(0.85f),
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "${post.collaboratorName}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Text(" ✓", fontSize = 10.sp, color = VerifiedBlue, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // ARROW 2: Tilted Arrow Music Pill (Susheela Raman • Ye Meera Deewanapan...)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { showAudioDetailSheetForPost = post }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NorthEast,
                                        contentDescription = "Audio Track",
                                        tint = Color.White.copy(0.9f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = "Music",
                                        tint = Color.White.copy(0.9f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = post.audioTitle ?: "Susheela Raman • Ye Meera Deewanapan...",
                                        color = Color.White.copy(0.9f),
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // ARROW 3: High-End Translucent Follow Pill Button
                                Surface(
                                    onClick = {
                                        onToggleFollowAuthor(post.id)
                                        Toast.makeText(context, if (post.isFollowingAuthor) "Unfollowed" else "Following!", Toast.LENGTH_SHORT).show()
                                    },
                                    color = Color(0x33FFFFFF),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.6f)),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (post.isFollowingAuthor) "Following" else "Follow",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // ARROW 4: More Options Three-Dots Menu Icon
                                IconButton(
                                    onClick = { showMoreOptionsMenuForPost = post },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More Options",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        // Reel Center Play Button Overlay
                        if (isReel) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(0.45f))
                                    .border(1.dp, Color.White.copy(0.6f), CircleShape)
                                    .clickable {
                                        Toast.makeText(context, "▶️ Playing Reel Video...", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }
                    }

                    // --------------------------------------------------------
                    // BOTTOM 6 ENGAGEMENT OVERLAY (MATCHING REFERENCE IMAGE)
                    // --------------------------------------------------------
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        // ENGAGEMENT ICONS BAR (Heart, Comment, Repost, Send DM, Bookmark)
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // ARROW 1: Outlined / Filled Heart Icon with Counter
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            onLikePost(post.id)
                                            CoreIntelligenceEngine.instance.recordInteraction("usr_me", post, EventType.LIKE)
                                            Toast.makeText(context, if (post.isLiked) "Unliked" else "❤️ Loved!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Like",
                                            tint = if (post.isLiked) HeartRed else NavyTextPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                // ARROW 2: Comment Speech Bubble Icon (1,969)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        showCommentsSheetForPost = post
                                        CoreIntelligenceEngine.instance.recordInteraction("usr_me", post, EventType.COMMENT)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Comment,
                                        contentDescription = "Comments",
                                        tint = NavyTextPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${post.commentsCount}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyTextPrimary
                                    )
                                }

                                // ARROW 3: Share / Repost Counter (2,327)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        CoreIntelligenceEngine.instance.recordInteraction("usr_me", post, EventType.SHARE)
                                        Toast.makeText(context, "🔁 Reposted Reel to your feed!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Repeat,
                                        contentDescription = "Repost",
                                        tint = NavyTextPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${post.repostsCount}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyTextPrimary
                                    )
                                }

                                // ARROW 4: Direct Message / Send Paper Plane Icon
                                IconButton(
                                    onClick = {
                                        showDirectShareSheetForPost = post
                                        CoreIntelligenceEngine.instance.recordInteraction("usr_me", post, EventType.SHARE)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Share DM",
                                        tint = NavyTextPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // ARROW 6: Bookmark Ribbon Icon (Save Button)
                            IconButton(
                                onClick = {
                                    onToggleSavePost(post.id)
                                    CoreIntelligenceEngine.instance.recordInteraction("usr_me", post, EventType.SAVE_BOOKMARK)
                                    Toast.makeText(context, if (post.isSaved) "Unsaved" else "📌 Saved to Profile!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Save Post",
                                    tint = if (post.isSaved) SkyBluePrimary else NavyTextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Liked by Avatars & Text Summary
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Liked by ",
                                fontSize = 12.sp,
                                color = SlateTextSecondary
                            )
                            Text(
                                text = "tithismily ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyTextPrimary
                            )
                            Text(
                                text = "and ",
                                fontSize = 12.sp,
                                color = SlateTextSecondary
                            )
                            Text(
                                text = "others",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // ARROW 5: Author Caption, Mentions & Inline "...more" Toggle
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = buildString {
                                    append(post.authorHandle.removePrefix("@"))
                                    append(" ")
                                    append(if (isExpandedCaption) post.caption else post.caption.take(45))
                                },
                                fontSize = 13.sp,
                                color = NavyTextPrimary,
                                maxLines = if (isExpandedCaption) Int.MAX_VALUE else 1
                            )

                            if (post.caption.length > 45 && !isExpandedCaption) {
                                Text(
                                    text = " ...more",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextSecondary,
                                    modifier = Modifier.clickable { isExpandedCaption = true }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = post.timestamp,
                            fontSize = 10.sp,
                            color = SlateTextSecondary
                        )
                    }

                    HorizontalDivider(
                        color = SkyBlueBorder,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        }

        // FLOATING ACTION BUTTON FOR QUICK POSTING
        FloatingActionButton(
            onClick = {
                isReelUploadMode = false
                postMediaLauncher.launch("image/*")
            },
            containerColor = SkyBluePrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Post Moment")
        }
    }

    // 1. MUSIC SEARCH & ATTACH BOTTOM SHEET DIALOG FOR REELS
    if (showMusicPickerSheet) {
        AlertDialog(
            onDismissRequest = { showMusicPickerSheet = false },
            containerColor = SkyBlueCardBg,
            title = {
                Column {
                    Text("🎵 Select Audio Track for Reel", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Search Bar
                    OutlinedTextField(
                        value = musicSearchQuery,
                        onValueChange = { musicSearchQuery = it },
                        placeholder = { Text("Search music, tracks, artists...", fontSize = 12.sp, color = SlateTextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlateTextSecondary, modifier = Modifier.size(18.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = SkyBlueBorder,
                            focusedTextColor = NavyTextPrimary,
                            unfocusedTextColor = NavyTextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    TabRow(selectedTabIndex = musicTabState) {
                        Tab(selected = musicTabState == 0, onClick = { musicTabState = 0 }) {
                            Text("Trending Tracks", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                        }
                        Tab(selected = musicTabState == 1, onClick = { musicTabState = 1 }) {
                            Text("Saved Audio", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            },
            text = {
                val tracks = sampleAudioTracks.filter {
                    it.title.contains(musicSearchQuery, ignoreCase = true) ||
                    it.artist.contains(musicSearchQuery, ignoreCase = true)
                }

                LazyColumn(modifier = Modifier.height(240.dp)) {
                    items(tracks) { track ->
                        val isPlaying = playingPreviewAudioId == track.id
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        playingPreviewAudioId = if (isPlaying) null else track.id
                                    },
                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(SkyBlueHeader)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Preview",
                                        tint = SkyBluePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(track.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyTextPrimary)
                                    Text("${track.artist} • ${track.duration}", fontSize = 10.sp, color = SlateTextSecondary)
                                }
                            }

                            Button(
                                onClick = {
                                    selectedAudioTrack = track
                                    showMusicPickerSheet = false
                                    Toast.makeText(context, "🎵 Attached Track: ${track.title}", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Add", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMusicPickerSheet = false }) {
                    Text("Close", color = SlateTextSecondary)
                }
            }
        )
    }

    // 2. MORE OPTIONS BOTTOM SHEET DIALOG (ARROW 4 OVERLAY)
    if (showMoreOptionsMenuForPost != null) {
        val post = showMoreOptionsMenuForPost!!
        AlertDialog(
            onDismissRequest = { showMoreOptionsMenuForPost = null },
            containerColor = SkyBlueCardBg,
            title = { Text("Options", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            showMoreOptionsMenuForPost = null
                            Toast.makeText(context, "🔗 Link Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔗 Copy Link", color = NavyTextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                    }

                    TextButton(
                        onClick = {
                            showMoreOptionsMenuForPost = null
                            Toast.makeText(context, "📲 Sharing post to external apps...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📲 Share to...", color = NavyTextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                    }

                    TextButton(
                        onClick = {
                            showMoreOptionsMenuForPost = null
                            Toast.makeText(context, "🚫 Marked as Not Interested", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🚫 Not Interested", color = SlateTextSecondary, modifier = Modifier.fillMaxWidth())
                    }

                    TextButton(
                        onClick = {
                            showMoreOptionsMenuForPost = null
                            Toast.makeText(context, "🚩 Reported post to moderators", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🚩 Report", color = HeartRed, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMoreOptionsMenuForPost = null }) {
                    Text("Cancel", color = SlateTextSecondary)
                }
            }
        )
    }

    // 3. AUDIO DETAILS BOTTOM SHEET DIALOG (ARROW 2 OVERLAY)
    if (showAudioDetailSheetForPost != null) {
        val post = showAudioDetailSheetForPost!!
        AlertDialog(
            onDismissRequest = { showAudioDetailSheetForPost = null },
            containerColor = SkyBlueCardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = SkyBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(post.audioTitle ?: "Original Audio", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyTextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🎵 Audio Track: ${post.audioArtist ?: "Artist"}", fontSize = 12.sp, color = SlateTextSecondary)
                    Text("🔥 Used in 4,280 Reels on Final Destiny", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SkyBluePrimary)

                    Button(
                        onClick = {
                            showAudioDetailSheetForPost = null
                            isReelUploadMode = true
                            postMediaLauncher.launch("video/*")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🎬 Use this Audio for your Reel", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAudioDetailSheetForPost = null }) {
                    Text("Close", color = SlateTextSecondary)
                }
            }
        )
    }

    // 4. DIRECT SHARE FRIENDS LIST SHEET (BOTTOM 6 ARROW 4 DM SHARE)
    if (showDirectShareSheetForPost != null) {
        val post = showDirectShareSheetForPost!!
        AlertDialog(
            onDismissRequest = { showDirectShareSheetForPost = null },
            containerColor = SkyBlueCardBg,
            title = { Text("✈️ Send Post via Direct Message", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val friends = listOf("Ananya Roy", "Aarav Sharma", "Simran Kaur", "Vikram Malhotra", "Riya Kapoor")
                    friends.forEach { friendName ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(friendName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyTextPrimary)
                            Button(
                                onClick = {
                                    showDirectShareSheetForPost = null
                                    Toast.makeText(context, "✈️ Sent post to $friendName!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Send", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDirectShareSheetForPost = null }) {
                    Text("Close", color = SlateTextSecondary)
                }
            }
        )
    }

    // 5. LIVE STREAM NOT ELIGIBLE DIALOG (< 500 FOLLOWERS)
    if (showNotEligibleDialog) {
        AlertDialog(
            onDismissRequest = { showNotEligibleDialog = false },
            containerColor = SkyBlueCardBg,
            title = { Text("Live Stream Not Eligible", color = NavyTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("You need at least 500 followers to go live on Final Destiny. Keep creating content and growing your community!", color = SlateTextSecondary, fontSize = 13.sp) },
            confirmButton = {
                Button(onClick = { showNotEligibleDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)) {
                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 6. COMMUNITY GUIDELINES & MODERATION WARNING DIALOG (>= 500 FOLLOWERS)
    if (showCommunityGuidelinesDialog) {
        AlertDialog(
            onDismissRequest = { showCommunityGuidelinesDialog = false },
            containerColor = SkyBlueCardBg,
            title = { Text("Community Guidelines Warning", color = NavyTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("No vulgarity, nudity, or hate speech allowed. Violating streams will result in immediate termination and permanent account suspension/ban.", color = SlateTextSecondary, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showCommunityGuidelinesDialog = false
                        Toast.makeText(context, "🔴 Camera & Audio Permissions Granted - Starting Stream...", Toast.LENGTH_SHORT).show()
                        onStartLiveStream()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("Agree & Proceed", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCommunityGuidelinesDialog = false }) {
                    Text("Cancel", color = SlateTextSecondary)
                }
            }
        )
    }

    // FULL-BLEED INSTAGRAM NEW POST COMPOSER SCREEN OVERLAY
    if (showCreatePostDialog) {
        InstagramNewPostScreen(
            mediaUri = selectedMediaUri,
            isReel = isReelUploadMode,
            onBack = {
                showCreatePostDialog = false
                selectedMediaUri = null
            },
            onPublish = { caption, mediaUri, audioTitle, audioArtist, audioUrl, isAiGenerated, commentsDisabled, hideLikes, hideShares, scheduledAt, altText, appliedFilter, overlayText ->
                showCreatePostDialog = false
                selectedMediaUri = null

                coroutineScope.launch {
                    isUploadingMedia = true
                    uploadStatusText = if (isReelUploadMode) "Uploading Reel Video..." else "Uploading Moment Photo..."
                    for (p in 1..10) {
                        uploadProgressPercentage = p / 10f
                        kotlinx.coroutines.delay(120)
                    }

                    if (isReelUploadMode) {
                        onPublishReel(caption, mediaUri, audioTitle, audioArtist, audioUrl, isAiGenerated, commentsDisabled, hideLikes, hideShares, scheduledAt, altText, appliedFilter, overlayText)
                    } else {
                        onPublishPost(caption, mediaUri, isAiGenerated, commentsDisabled, hideLikes, hideShares, scheduledAt, altText, appliedFilter, overlayText)
                    }

                    isUploadingMedia = false
                    uploadProgressPercentage = 0f
                    Toast.makeText(context, "✨ Published to Feed!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // COMMENTS SHEET MODAL
    if (showCommentsSheetForPost != null) {
        val post = showCommentsSheetForPost!!
        AlertDialog(
            onDismissRequest = { showCommentsSheetForPost = null },
            containerColor = SkyBlueCardBg,
            title = { Text("💬 Comments (${post.comments.size})", color = NavyTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(post.comments) { comment ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(comment.senderName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyTextPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(comment.timestamp, fontSize = 9.sp, color = SlateTextSecondary)
                                }
                                Text(comment.text, fontSize = 12.sp, color = NavyTextPrimary)
                                HorizontalDivider(color = SkyBlueBorder, thickness = 0.5.dp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = commentInputText,
                            onValueChange = { commentInputText = it },
                            placeholder = { Text("Add a comment...", color = SlateTextSecondary, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SkyBluePrimary, unfocusedBorderColor = SkyBlueBorder, focusedTextColor = NavyTextPrimary, unfocusedTextColor = NavyTextPrimary),
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(44.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (commentInputText.isNotBlank()) {
                                    onAddComment(post.id, commentInputText)
                                    commentInputText = ""
                                    Toast.makeText(context, "💬 Comment posted!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(SkyBluePrimary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCommentsSheetForPost = null }) {
                    Text("Close", color = SlateTextSecondary)
                }
            }
        )
    }

    // STORY FULLSCREEN VIEWER MODAL
    if (activeStoryView != null) {
        val story = activeStoryView!!
        val storyBitmap = rememberLoadedImage(context, story.mediaUri)

        AlertDialog(
            onDismissRequest = { activeStoryView = null },
            containerColor = NavyTextPrimary,
            title = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✨ ${story.authorName}'s Story", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(story.timestamp, fontSize = 10.sp, color = Color.White.copy(0.6f))
                    }
                    IconButton(onClick = { activeStoryView = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            },
            text = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                ) {
                    if (storyBitmap != null) {
                        Image(
                            bitmap = storyBitmap,
                            contentDescription = "Story Media",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✨", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${story.authorName}'s 24-Hour Story Update", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { activeStoryView = null }, colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)) {
                    Text("Close Story", color = Color.White)
                }
            }
        )
    }
}

@Composable
internal fun rememberLoadedImage(context: Context, uriString: String?): ImageBitmap? {
    return remember(uriString) {
        if (uriString.isNullOrBlank()) null
        else {
            try {
                val uri = Uri.parse(uriString)
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                bitmap?.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
internal fun ExoVideoPlayerView(
    videoUri: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember(videoUri) {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
            val mediaItem = androidx.media3.common.MediaItem.fromUri(Uri.parse(videoUri))
            setMediaItem(mediaItem)
            repeatMode = androidx.media3.common.Player.REPEAT_MODE_ONE
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    androidx.compose.ui.viewinterop.AndroidView(
        factory = { ctx ->
            androidx.media3.ui.PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = modifier
    )
}
