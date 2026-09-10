package com.devil.finaldestiny.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.devil.finaldestiny.model.MomentPost
import com.devil.finaldestiny.ui.components.ProfileAvatarView
import com.devil.finaldestiny.ui.theme.HeartRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelViewerScreen(
    reels: List<MomentPost>,
    initialIndex: Int = 0,
    onLikePost: (String) -> Unit = {},
    onAddComment: (String, String) -> Unit = { _, _ -> },
    onToggleFollowAuthor: (String) -> Unit = {},
    onToggleSavePost: (String) -> Unit = {},
    onIncrementView: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    if (reels.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No Reels Available", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))) {
                    Text("Go Back", color = Color.White)
                }
            }
        }
        return
    }

    val safeInitialIndex = initialIndex.coerceIn(0, reels.size - 1)
    val pagerState = rememberPagerState(initialPage = safeInitialIndex) { reels.size }
    var activeCommentSheetReel by remember { mutableStateOf<MomentPost?>(null) }
    var commentInputText by remember { mutableStateOf("") }

    // Trigger dynamic view count atomically via Supabase RPC on reel start
    LaunchedEffect(pagerState.currentPage) {
        val currentReel = reels.getOrNull(pagerState.currentPage)
        currentReel?.let {
            onIncrementView(it.id)
        }
    }

    // Full edge-to-edge dark container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // FULL-SCREEN VERTICAL PAGER
        VerticalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val reel = reels[page]
            val distanceFromCurrentPage = kotlin.math.abs(page - pagerState.currentPage)
            val isCurrentPage = distanceFromCurrentPage == 0
            var isCaptionExpanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(reel.id) {
                        detectTapGestures(
                            onDoubleTap = {
                                onLikePost(reel.id)
                                Toast.makeText(context, "❤️ Loved!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
            ) {
                // EDGE-TO-EDGE EXOPLAYER INSTANCE
                ReelExoPlayerView(
                    videoUri = reel.mediaUrl.ifBlank { reel.mediaUri ?: "" },
                    distanceFromCurrentPage = distanceFromCurrentPage,
                    modifier = Modifier.fillMaxSize()
                )

                // BOTTOM & SIDE SHADOW GRADIENT OVERLAY
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                // 3. FLOATING RIGHT-SIDE ACTION OVERLAYS (INSTAGRAM STYLE)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 12.dp, bottom = 32.dp)
                ) {
                    // Like Action (Heart icon with dynamic count)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { onLikePost(reel.id) },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.35f))
                        ) {
                            Icon(
                                imageVector = if (reel.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like Reel",
                                tint = if (reel.isLiked) HeartRed else Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCount(reel.likesCount),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Comment Action (Chat bubble with count; opens sliding sheet)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { activeCommentSheetReel = reel },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.35f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Comment,
                                contentDescription = "Comments",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCount(reel.commentsCount),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Share Action (Paper plane icon)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "🚀 Reel Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.35f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Share Reel",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCount(reel.sharesCount),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Save / Bookmark Action
                    IconButton(
                        onClick = {
                            onToggleSavePost(reel.id)
                            Toast.makeText(context, if (reel.isSaved) "Unsaved" else "Saved to Bookmarks 🔖", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                    ) {
                        Icon(
                            imageVector = if (reel.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save Reel",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Rotating Audio Vinyl / Disc Icon in Bottom-Right Corner
                    RotatingAudioVinylDisc(
                        albumCoverUrl = reel.authorAvatar,
                        isPlaying = isCurrentPage
                    )
                }

                // 3. FLOATING LEFT-SIDE CREATOR INFO OVERLAY
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .navigationBarsPadding()
                        .padding(start = 14.dp, bottom = 32.dp, end = 80.dp)
                ) {
                    // Creator Info: Avatar + @username + "Follow" Pill
                    val creatorTitle = when {
                        !reel.profile?.fullName.isNullOrBlank() -> reel.profile!!.fullName!!
                        !reel.profile?.username.isNullOrBlank() -> if (reel.profile!!.username!!.startsWith("@")) reel.profile!!.username!! else "@${reel.profile!!.username!!}"
                        !reel.authorName.isNullOrBlank() && reel.authorName.trim().lowercase() != "null" && !reel.authorName.startsWith("User_") -> reel.authorName.trim()
                        !reel.authorHandle.isNullOrBlank() && reel.authorHandle.trim().lowercase() != "null" -> reel.authorHandle.trim()
                        !reel.userId.isNullOrBlank() && reel.userId.trim().lowercase() != "null" -> "User_${reel.userId.take(5)}"
                        else -> "Creator"
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileAvatarView(
                            name = creatorTitle,
                            profilePictureUri = reel.authorAvatar,
                            size = 38.dp,
                            showBorder = true,
                            borderColor = Color.White
                        )

                        Text(
                            text = creatorTitle,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Follow Pill Button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (reel.isFollowingAuthor) Color.White.copy(alpha = 0.25f) else Color(0xFF2563EB)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (reel.isFollowingAuthor) Color.White.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { onToggleFollowAuthor(reel.id) }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (reel.isFollowingAuthor) "Following" else "Follow",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Caption Text with Expand / Collapse Toggle
                    if (reel.caption.isNotBlank()) {
                        Text(
                            text = reel.caption,
                            color = Color.White,
                            fontSize = 13.sp,
                            maxLines = if (isCaptionExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { isCaptionExpanded = !isCaptionExpanded }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Audio Track Marquee Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Audio Track",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val displayAudioTitle = when {
                            !reel.audioTitle.isNullOrBlank() && reel.audioTitle.trim().lowercase() != "null" -> reel.audioTitle.trim()
                            else -> "Original Audio • $creatorTitle"
                        }
                        Text(
                            text = displayAudioTitle,
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 4. TOP APP BAR OVERLAY & DISMISS (Floating Translucent Back Arrow)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Dismiss Reels",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = "Reels",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Box(modifier = Modifier.size(38.dp)) // Spacer for alignment
        }

        // SLIDING TRANSLUCENT COMMENT SHEET
        val activeSheetReel = activeCommentSheetReel
        if (activeSheetReel != null) {
            ModalBottomSheet(
                onDismissRequest = { activeCommentSheetReel = null },
                containerColor = Color(0xFF121212),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Comments (${activeSheetReel.comments.size})",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { activeCommentSheetReel = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(activeSheetReel.comments) { comment ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ProfileAvatarView(
                                    name = comment.senderName,
                                    profilePictureUri = null,
                                    size = 32.dp
                                )
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(comment.senderName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(comment.timestamp, fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(comment.text, fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
                                }
                            }
                        }
                    }

                    // Add Comment Input Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(bottom = 12.dp, top = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = commentInputText,
                            onValueChange = { commentInputText = it },
                            placeholder = { Text("Add a comment...", color = Color.Gray, fontSize = 13.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color.Gray
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (commentInputText.isNotBlank()) {
                                    onAddComment(activeSheetReel.id, commentInputText.trim())
                                    commentInputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Post Comment", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun ReelExoPlayerView(
    videoUri: String,
    distanceFromCurrentPage: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isValidVideo = videoUri.startsWith("http") || videoUri.startsWith("content") || videoUri.endsWith(".mp4") || videoUri.contains("video")

    if (!isValidVideo || distanceFromCurrentPage > 1) {
        Box(
            modifier = modifier.background(Color.Black)
        )
        return
    }

    val cacheDataSourceFactory = remember(context) {
        com.devil.finaldestiny.utils.ReelVideoCache.createCacheDataSourceFactory(context)
    }

    val exoPlayer = remember(videoUri) {
        val mediaSourceFactory = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
            .setDataSourceFactory(cacheDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                setMediaItem(MediaItem.fromUri(Uri.parse(videoUri)))
                repeatMode = Player.REPEAT_MODE_ONE
                prepare()
            }
    }

    val isCurrentPage = distanceFromCurrentPage == 0

    LaunchedEffect(isCurrentPage) {
        if (isCurrentPage) {
            exoPlayer.playWhenReady = true
            exoPlayer.play()
        } else {
            exoPlayer.playWhenReady = false
            exoPlayer.pause()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = modifier
    )
}

@Composable
fun RotatingAudioVinylDisc(
    albumCoverUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VinylRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DiscRotation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Color(0xFF181818))
            .border(2.dp, Color(0xFF444444), CircleShape)
            .graphicsLayer {
                if (isPlaying) {
                    rotationZ = rotationAngle
                }
            }
    ) {
        // Inner Grooves
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF282828))
                .border(1.dp, Color(0xFF555555), CircleShape)
        )
        // Center Avatar / Cover
        ProfileAvatarView(
            name = "Audio",
            profilePictureUri = albumCoverUrl,
            size = 20.dp,
            showBorder = false
        )
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000f)
        count >= 1_000 -> String.format("%.1fK", count / 1_000f)
        else -> count.toString()
    }
}
