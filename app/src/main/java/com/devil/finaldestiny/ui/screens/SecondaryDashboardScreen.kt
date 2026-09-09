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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.model.AppNotification
import com.devil.finaldestiny.model.MediaType
import com.devil.finaldestiny.model.MomentComment
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
    onPublishPost: (String, String?) -> Unit,
    onPublishReel: (String, String?) -> Unit = { _, _ -> },
    onTipPost: (MomentPost) -> Unit,
    onAddStory: (String) -> Unit = {},
    onAddComment: (String, String) -> Unit = { _, _ -> },
    onToggleFollowAuthor: (String) -> Unit = {},
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
    var activeStoryView by remember { mutableStateOf<StoryItem?>(null) }

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
                                showCreatePostDialog = true
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

            // FULL-BLEED EDGE-TO-EDGE FEED POSTS
            items(momentPosts) { post ->
                val localBitmap = rememberLoadedImage(context, post.mediaUri)
                val isReel = post.mediaType == MediaType.REEL_VIDEO
                val postRelativeTime = TimeUtils.formatTimestamp(post.timestamp, post.createdAtEpochMs)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    // AUTHOR HEADER (INSTAGRAM STYLE WITH HORIZONTAL PADDING)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ProfileAvatarView(
                                name = post.authorName,
                                profilePictureUri = post.authorAvatar,
                                size = 38.dp,
                                showBorder = true,
                                borderColor = SkyBluePrimary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(post.authorName, fontWeight = FontWeight.Bold, color = NavyTextPrimary, fontSize = 13.sp)
                                    Text(" ✓", fontSize = 10.sp, color = VerifiedBlue, fontWeight = FontWeight.Bold)
                                }
                                if (post.isSponsored) {
                                    Text("✨ ${post.sponsorName ?: "Brand Partnership"}", fontSize = 10.sp, color = SkyBluePrimary, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("${post.authorHandle} • $postRelativeTime", fontSize = 10.sp, color = SlateTextSecondary)
                                }
                            }
                        }

                        // Follow Button
                        Button(
                            onClick = {
                                onToggleFollowAuthor(post.id)
                                Toast.makeText(
                                    context,
                                    if (post.isFollowingAuthor) "Unfollowed ${post.authorName}" else "❤️ Following ${post.authorName}!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (post.isFollowingAuthor) SkyBlueHeader else SkyBluePrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = if (post.isFollowingAuthor) "✓ Following" else "+ Follow",
                                fontSize = 11.sp,
                                color = if (post.isFollowingAuthor) NavyTextPrimary else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // FULL-BLEED MEDIA CONTAINER (0DP SIDE MARGINS TOUCHING SCREEN BORDERS)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(if (isReel) 9f / 16f else 4f / 5f)
                            .background(NavyTextPrimary)
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
                                Text(
                                    text = "Monetized Creator Media Feed",
                                    color = Color.White.copy(0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // OVERLAY BADGES FOR REELS
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

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                                    .padding(10.dp)
                            ) {
                                Surface(
                                    color = Color.Black.copy(0.6f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "🎬 REEL • ${post.videoDuration ?: "0:30"}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    color = Color.Black.copy(0.6f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "👁️ ${post.viewsCount} views",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // INTERACTIVE ENGAGEMENT BAR (PADDING 14.DP)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    onLikePost(post.id)
                                    Toast.makeText(
                                        context,
                                        if (post.isLiked) "Unliked Post" else "❤️ Loved & Liked Moment!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (post.isLiked) HeartRed else NavyTextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${post.likesCount}", fontSize = 12.sp, color = NavyTextPrimary, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.width(16.dp))

                            IconButton(
                                onClick = { showCommentsSheetForPost = post },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = "Comments", tint = NavyTextPrimary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${post.commentsCount}", fontSize = 12.sp, color = NavyTextPrimary, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onTipPost(post) },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tip 💎 ${post.giftTipsTotal}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // CAPTION & SPONSORED BRAND CTA (PADDING 14.DP)
                    Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                        Text(text = post.caption, fontSize = 13.sp, color = NavyTextPrimary, fontWeight = FontWeight.Normal)

                        if (post.isSponsored) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    Toast.makeText(context, "🔗 Opening Brand Deal: ${post.ctaUrl}", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrightCyanAccent),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(post.ctaText ?: "Visit Brand Partner 🛍️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    HorizontalDivider(
                        color = SkyBlueBorder,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                }
            }
        }

        // FLOATING ACTION BUTTON FOR QUICK POSTING
        FloatingActionButton(
            onClick = {
                isReelUploadMode = false
                showCreatePostDialog = true
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

    // 1. LIVE STREAM NOT ELIGIBLE DIALOG (< 500 FOLLOWERS)
    if (showNotEligibleDialog) {
        AlertDialog(
            onDismissRequest = { showNotEligibleDialog = false },
            containerColor = SkyBlueCardBg,
            title = {
                Text(
                    text = "Live Stream Not Eligible",
                    color = NavyTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "You need at least 500 followers to go live on Final Destiny. Keep creating content and growing your community!",
                    color = SlateTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showNotEligibleDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
                ) {
                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 2. COMMUNITY GUIDELINES & MODERATION WARNING DIALOG (>= 500 FOLLOWERS)
    if (showCommunityGuidelinesDialog) {
        AlertDialog(
            onDismissRequest = { showCommunityGuidelinesDialog = false },
            containerColor = SkyBlueCardBg,
            title = {
                Text(
                    text = "Community Guidelines Warning",
                    color = NavyTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "No vulgarity, nudity, or hate speech allowed. Violating streams will result in immediate termination and permanent account suspension/ban.",
                    color = SlateTextSecondary,
                    fontSize = 13.sp
                )
            },
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

    // CREATE POST OR REEL DIALOG (TRIGGERS PROGRESS TRACKING FOR BOTH PHOTOS & VIDEOS)
    if (showCreatePostDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePostDialog = false },
            containerColor = SkyBlueCardBg,
            title = {
                Text(
                    text = if (isReelUploadMode) "🎬 Upload Reel / Short Video" else "📸 Share Photo Moment",
                    color = NavyTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isReelUploadMode) "Select a 9:16 vertical video from your storage to publish on the Reel feed:" else "Select an image from your device storage to post on the feed:",
                        fontSize = 12.sp,
                        color = SlateTextSecondary
                    )

                    OutlinedTextField(
                        value = captionInput,
                        onValueChange = { captionInput = it },
                        label = { Text(if (isReelUploadMode) "Reel Title & Hashtags" else "Caption & Hashtags", color = SlateTextSecondary, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = SkyBlueBorder,
                            focusedTextColor = NavyTextPrimary,
                            unfocusedTextColor = NavyTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { postMediaLauncher.launch(if (isReelUploadMode) "video/*" else "image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlueHeader),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (isReelUploadMode) Icons.Default.Videocam else Icons.Default.PhotoLibrary, contentDescription = null, tint = SkyBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedMediaUri != null) "✓ Media Selected (Tap to Change)" else if (isReelUploadMode) "📁 Select Video from Storage" else "📁 Select Image from Storage",
                            color = NavyTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (selectedMediaUri != null) {
                        val previewBitmap = rememberLoadedImage(context, selectedMediaUri)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SkyBlueBgLight)
                                .border(1.dp, SkyBlueBorder, RoundedCornerShape(12.dp))
                        ) {
                            if (previewBitmap != null) {
                                Image(
                                    bitmap = previewBitmap,
                                    contentDescription = "Preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Videocam, contentDescription = null, tint = SkyBluePrimary)
                                    Text("🎬 Video Selected & Ready to Upload", color = NavyTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (captionInput.isNotBlank() || selectedMediaUri != null) {
                            val caption = captionInput.ifBlank { if (isReelUploadMode) "New Reel Video! 🎬" else "Shared a new moment!" }
                            val mediaUri = selectedMediaUri
                            val isReel = isReelUploadMode

                            captionInput = ""
                            selectedMediaUri = null
                            showCreatePostDialog = false

                            coroutineScope.launch {
                                isUploadingMedia = true
                                uploadStatusText = if (isReel) "Uploading Reel Video..." else "Uploading Photo..."
                                for (p in 1..10) {
                                    uploadProgressPercentage = p / 10f
                                    kotlinx.coroutines.delay(140)
                                }
                                if (isReel) {
                                    onPublishReel(caption, mediaUri)
                                } else {
                                    onPublishPost(caption, mediaUri)
                                }
                                isUploadingMedia = false
                                uploadProgressPercentage = 0f
                                Toast.makeText(context, if (isReel) "✨ Reel Video Published to Feed!" else "✨ Moment Published to Feed!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
                ) {
                    Text(if (isReelUploadMode) "Publish Reel 🎬" else "Publish Moment 📸", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePostDialog = false }) {
                    Text("Cancel", color = SlateTextSecondary)
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
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SkyBluePrimary,
                                unfocusedBorderColor = SkyBlueBorder,
                                focusedTextColor = NavyTextPrimary,
                                unfocusedTextColor = NavyTextPrimary
                            ),
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
private fun rememberLoadedImage(context: Context, uriString: String?): ImageBitmap? {
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
private fun ExoVideoPlayerView(
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
