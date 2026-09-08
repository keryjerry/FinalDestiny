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
import com.devil.finaldestiny.ui.components.NotificationBellButton
import com.devil.finaldestiny.ui.components.ProfileAvatarView
import com.devil.finaldestiny.ui.theme.*

@Composable
fun SecondaryDashboardScreen(
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
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    var showCreatePostDialog by remember { mutableStateOf(false) }
    var isReelUploadMode by remember { mutableStateOf(false) }
    var showCommentsSheetForPost by remember { mutableStateOf<MomentPost?>(null) }
    var activeStoryView by remember { mutableStateOf<StoryItem?>(null) }

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

    // Launcher for adding a Story
    val storyMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onAddStory(it.toString())
            Toast.makeText(context, "✨ 24h Story Posted!", Toast.LENGTH_SHORT).show()
        }
    }

    val storyRingGradient = Brush.linearGradient(
        colors = listOf(BrightCyanAccent, SkyBluePrimary, VerifiedBlue)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlueBgLight)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // TOP FEED HEADER & REEL UPLOAD BAR
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SkyBlueCardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SkyBlueBorder, RoundedCornerShape(16.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(SkyBlueHeader)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = NavyTextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "📸 DESTINY LIVE MOMENTS & REELS",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyTextPrimary,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text("Reels, Collabs & Monetized Moments", fontSize = 10.sp, color = SlateTextSecondary)
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                NotificationBellButton(
                                    notifications = notifications,
                                    onClick = onOpenNotifications
                                )

                                IconButton(
                                    onClick = {
                                        isReelUploadMode = false
                                        showCreatePostDialog = true
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(SkyBluePrimary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Create", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        // REEL UPLOAD ACTION BUTTON
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    isReelUploadMode = true
                                    showCreatePostDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrightCyanAccent),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload Reel / Video 🎬", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    isReelUploadMode = false
                                    showCreatePostDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SkyBlueHeader),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Post Photo 📸", color = NavyTextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // DESTINY STORIES CAROUSEL
            item {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "✨ 24h Destiny Stories", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyTextPrimary)
                        Text(text = "Tap to View", fontSize = 11.sp, color = SkyBluePrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // My Story Add Button
                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .background(SkyBlueHeader)
                                        .border(2.dp, storyRingGradient, CircleShape)
                                        .clickable { storyMediaLauncher.launch("image/*") }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Story", tint = SkyBluePrimary, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Your Story", fontSize = 10.sp, color = NavyTextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Creator Stories List
                        items(storyTrays) { story ->
                            val storyImageBitmap = rememberLoadedImage(context, story.mediaUri)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { activeStoryView = story }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(62.dp)
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
                                        Text(text = story.authorName.take(1).uppercase(), fontSize = 22.sp, color = SkyBluePrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(story.authorName, fontSize = 10.sp, color = NavyTextPrimary, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // DYNAMIC MOMENT & REEL POSTS FEED
            items(momentPosts) { post ->
                val localBitmap = rememberLoadedImage(context, post.mediaUri)
                val isReel = post.mediaType == MediaType.REEL_VIDEO

                Card(
                    colors = CardDefaults.cardColors(containerColor = SkyBlueCardBg),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SkyBlueBorder, RoundedCornerShape(20.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        // AUTHOR HEADER WITH SPONSORED TAG & FOLLOW BUTTON
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ProfileAvatarView(
                                    name = post.authorName,
                                    profilePictureUri = post.authorAvatar,
                                    size = 40.dp,
                                    showBorder = true,
                                    borderColor = SkyBluePrimary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(post.authorName, fontWeight = FontWeight.Bold, color = NavyTextPrimary, fontSize = 14.sp)
                                        Text(" ✓", fontSize = 10.sp, color = VerifiedBlue, fontWeight = FontWeight.Bold)
                                    }
                                    if (post.isSponsored) {
                                        Text("✨ ${post.sponsorName ?: "Brand Partnership"}", fontSize = 10.sp, color = SkyBluePrimary, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("${post.authorHandle} • ${post.timestamp}", fontSize = 10.sp, color = SlateTextSecondary)
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // DYNAMIC ASPECT RATIO CONTAINER (4:5 PHOTO vs 9:16 REEL VIDEO)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(if (isReel) 9f / 16f else 4f / 5f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(NavyTextPrimary)
                                .border(1.dp, SkyBlueBorder, RoundedCornerShape(14.dp))
                        ) {
                            if (localBitmap != null) {
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

                            // OVERLAY BADGES & CONTROLS FOR REELS
                            if (isReel) {
                                // Reel Play Button Center Overlay
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

                                // Reel Top Badges (Duration & Views)
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // CAPTION
                        Text(text = post.caption, fontSize = 13.sp, color = NavyTextPrimary, fontWeight = FontWeight.Medium)

                        // SPONSORED BRAND COLLAB CTA BUTTON
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // INTERACTIVE ENGAGEMENT BAR
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
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
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (post.isLiked) HeartRed else SlateTextSecondary
                                    )
                                }
                                Text("${post.likesCount}", fontSize = 12.sp, color = NavyTextPrimary, fontWeight = FontWeight.Bold)

                                Spacer(modifier = Modifier.width(14.dp))

                                IconButton(onClick = { showCommentsSheetForPost = post }) {
                                    Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = "Comments", tint = SlateTextSecondary, modifier = Modifier.size(20.dp))
                                }
                                Text("${post.commentsCount}", fontSize = 12.sp, color = NavyTextPrimary, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onTipPost(post) },
                                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tip 💎 ${post.giftTipsTotal}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // FLOATING ACTION BUTTON
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

    // CREATE POST OR REEL DIALOG
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
                            if (isReelUploadMode) {
                                onPublishReel(captionInput.ifBlank { "New Reel Video! 🎬" }, selectedMediaUri)
                            } else {
                                onPublishPost(captionInput.ifBlank { "Shared a new moment!" }, selectedMediaUri)
                            }
                            captionInput = ""
                            selectedMediaUri = null
                            showCreatePostDialog = false
                            Toast.makeText(context, if (isReelUploadMode) "✨ Reel Video Published to Feed!" else "✨ Moment Published to Feed!", Toast.LENGTH_SHORT).show()
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
