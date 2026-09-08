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
import androidx.compose.material.icons.filled.PhotoLibrary
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
    onTipPost: (MomentPost) -> Unit,
    onAddStory: (String) -> Unit = {},
    onAddComment: (String, String) -> Unit = { _, _ -> },
    onToggleFollowAuthor: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showCommentsSheetForPost by remember { mutableStateOf<MomentPost?>(null) }
    var activeStoryView by remember { mutableStateOf<StoryItem?>(null) }

    var captionInput by remember { mutableStateOf("") }
    var selectedMediaUri by remember { mutableStateOf<String?>(null) }
    var commentInputText by remember { mutableStateOf("") }

    // Launcher for selecting Moment Media (Image/Video) from Local Storage
    val postMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedMediaUri = it.toString()
            Toast.makeText(context, "📸 Media Selected from Phone Storage!", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher for adding a Story from Local Device Storage
    val storyMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onAddStory(it.toString())
            Toast.makeText(context, "✨ 24h Story Posted from Storage!", Toast.LENGTH_SHORT).show()
        }
    }

    val instagramRingGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFF9CE34), Color(0xFFEE2A7B), Color(0xFF6228D7))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGradient)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // TOP INSTAGRAM HEADER WITH BACK ARROW & SHARE BUTTON
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MetallicGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(10.dp)
                ) {
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
                                    .background(WineRedMedium)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MetallicGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "📸 DESTINY LIVE MOMENTS & REELS FEED",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MetallicGold,
                                    letterSpacing = 0.5.sp
                                )
                                Text("Real Local Storage Media, Likes & Comments", fontSize = 10.sp, color = LightGold.copy(0.7f))
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
                                onClick = { showCreatePostDialog = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MetallicGold)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Create", tint = WineRedDark, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            // DESTINY LIVE MOMENT 24-HOUR STORIES CAROUSEL
            item {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "✨ 24h Destiny Stories", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LightGold)
                        Text(text = "Tap to View", fontSize = 10.sp, color = MetallicGold)
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
                                        .background(WineRedMedium)
                                        .border(2.dp, instagramRingGradient, CircleShape)
                                        .clickable { storyMediaLauncher.launch("image/*") }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Story", tint = MetallicGold, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Your Story", fontSize = 10.sp, color = LightGold, fontWeight = FontWeight.SemiBold)
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
                                        .background(WineRedDark)
                                        .border(
                                            width = 2.5.dp,
                                            brush = if (story.isViewed) Brush.linearGradient(listOf(Color.Gray, Color.DarkGray)) else instagramRingGradient,
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
                                        Text(text = story.authorName.take(1).uppercase(), fontSize = 22.sp, color = LightGold, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(story.authorName, fontSize = 10.sp, color = LightGold, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // CHRONOLOGICAL INSTAGRAM MOMENTS POSTS FEED
            items(momentPosts) { post ->
                val localBitmap = rememberLoadedImage(context, post.mediaUri)

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CrimsonVelvet, RoundedCornerShape(20.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        // INSTAGRAM AUTHOR HEADER WITH WORKING + FOLLOW BUTTON
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
                                    borderColor = DarkGold
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(post.authorName, fontWeight = FontWeight.Bold, color = LightGold, fontSize = 14.sp)
                                        Text(" ✓", fontSize = 10.sp, color = VerifiedBlue, fontWeight = FontWeight.Bold)
                                    }
                                    Text("${post.authorHandle} • ${post.timestamp}", fontSize = 10.sp, color = LightGold.copy(0.7f))
                                }
                            }

                            // Working Follow Button
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
                                    containerColor = if (post.isFollowingAuthor) WineRedMedium else MetallicGold
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = if (post.isFollowingAuthor) "✓ Following" else "+ Follow",
                                    fontSize = 11.sp,
                                    color = if (post.isFollowingAuthor) LightGold else WineRedDark,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // INSTAGRAM MEDIA POST DISPLAY (LOCAL STORAGE IMAGE OR DEFAULT PREVIEW)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(WineRedDark)
                                .border(1.dp, CrimsonVelvet, RoundedCornerShape(14.dp))
                        ) {
                            if (localBitmap != null) {
                                Image(
                                    bitmap = localBitmap,
                                    contentDescription = "Uploaded Media",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = LightGold, modifier = Modifier.size(42.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "📸 Local Storage Moment Uploaded", color = LightGold.copy(alpha = 0.85f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Real User Image & Video Feed", color = LightGold.copy(alpha = 0.5f), fontSize = 10.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // CAPTION
                        Text(text = post.caption, fontSize = 13.sp, color = LightGold, fontWeight = FontWeight.Medium)

                        Spacer(modifier = Modifier.height(10.dp))

                        // INSTAGRAM INTERACTIVE ENGAGEMENT BAR (LIKE, COMMENT, TIP)
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Working Like / Love Button
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
                                        tint = if (post.isLiked) HeartRed else LightGold
                                    )
                                }
                                Text("${post.likesCount}", fontSize = 12.sp, color = LightGold, fontWeight = FontWeight.Bold)

                                Spacer(modifier = Modifier.width(14.dp))

                                // Working Comment Button (Opens Comments Sheet)
                                IconButton(onClick = { showCommentsSheetForPost = post }) {
                                    Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = "Comments", tint = LightGold, modifier = Modifier.size(20.dp))
                                }
                                Text("${post.commentsCount}", fontSize = 12.sp, color = LightGold, fontWeight = FontWeight.Bold)
                            }

                            // Tip Diamonds Button
                            Button(
                                onClick = { onTipPost(post) },
                                colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = WineRedDark, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tip 💎 ${post.giftTipsTotal}", fontSize = 11.sp, color = WineRedDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // FLOATING INSTAGRAM CREATE MOMENT BUTTON
        FloatingActionButton(
            onClick = { showCreatePostDialog = true },
            containerColor = MetallicGold,
            contentColor = WineRedDark,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Post Moment")
        }
    }

    // CREATE INSTAGRAM MOMENT DIALOG WITH LOCAL STORAGE SELECTOR
    if (showCreatePostDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePostDialog = false },
            containerColor = CardBackground,
            title = { Text("📸 Share Instagram Moment / Reel", color = MetallicGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select an image or video from your device storage to post on the feed:", fontSize = 11.sp, color = LightGold)

                    OutlinedTextField(
                        value = captionInput,
                        onValueChange = { captionInput = it },
                        label = { Text("Caption & Hashtags", color = LightGold, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MetallicGold,
                            unfocusedBorderColor = DarkGold,
                            focusedTextColor = LightGold,
                            unfocusedTextColor = LightGold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { postMediaLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = WineRedMedium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MetallicGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedMediaUri != null) "✓ Media Selected (Tap to Change)" else "📁 Select Image/Video from Storage",
                            color = MetallicGold,
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
                                .background(WineRedDark)
                                .border(1.dp, MetallicGold, RoundedCornerShape(12.dp))
                        ) {
                            if (previewBitmap != null) {
                                Image(
                                    bitmap = previewBitmap,
                                    contentDescription = "Preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text("📸 Image Ready to Upload", color = LightGold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (captionInput.isNotBlank() || selectedMediaUri != null) {
                            onPublishPost(captionInput.ifBlank { "Shared a new moment!" }, selectedMediaUri)
                            captionInput = ""
                            selectedMediaUri = null
                            showCreatePostDialog = false
                            Toast.makeText(context, "✨ Moment Published to Feed!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
                ) {
                    Text("Publish to Feed", color = WineRedDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePostDialog = false }) {
                    Text("Cancel", color = LightGold)
                }
            }
        )
    }

    // INSTAGRAM REAL COMMENTS SHEET MODAL
    if (showCommentsSheetForPost != null) {
        val post = showCommentsSheetForPost!!
        AlertDialog(
            onDismissRequest = { showCommentsSheetForPost = null },
            containerColor = CardBackground,
            title = { Text("💬 Comments (${post.comments.size})", color = MetallicGold, fontSize = 16.sp) },
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
                                    Text(comment.senderName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MetallicGold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(comment.timestamp, fontSize = 9.sp, color = LightGold.copy(0.6f))
                                }
                                Text(comment.text, fontSize = 12.sp, color = LightGold)
                                HorizontalDivider(color = WineRedMedium, thickness = 0.5.dp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }

                    // Write Comment Row
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = commentInputText,
                            onValueChange = { commentInputText = it },
                            placeholder = { Text("Add a comment...", color = LightGold.copy(0.5f), fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MetallicGold,
                                unfocusedBorderColor = DarkGold,
                                focusedTextColor = LightGold,
                                unfocusedTextColor = LightGold
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
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(MetallicGold)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = WineRedDark, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCommentsSheetForPost = null }) {
                    Text("Close", color = LightGold)
                }
            }
        )
    }

    // INSTAGRAM STORY FULLSCREEN VIEWER MODAL
    if (activeStoryView != null) {
        val story = activeStoryView!!
        val storyBitmap = rememberLoadedImage(context, story.mediaUri)

        AlertDialog(
            onDismissRequest = { activeStoryView = null },
            containerColor = Color.Black,
            title = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✨ ${story.authorName}'s 24h Story", color = LightGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(story.timestamp, fontSize = 10.sp, color = LightGold.copy(0.6f))
                    }
                    IconButton(onClick = { activeStoryView = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = LightGold)
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
                        .background(WineRedDark)
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
                            Text("${story.authorName}'s 24-Hour Story Update", color = LightGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { activeStoryView = null }, colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)) {
                    Text("Close Story", color = WineRedDark)
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
