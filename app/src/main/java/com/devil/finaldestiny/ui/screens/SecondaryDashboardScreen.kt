package com.devil.finaldestiny.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.model.MomentPost
import com.devil.finaldestiny.model.StoryItem
import com.devil.finaldestiny.ui.theme.*

@Composable
fun SecondaryDashboardScreen(
    storyTrays: List<StoryItem>,
    momentPosts: List<MomentPost>,
    onLikePost: (String) -> Unit,
    onPublishPost: (String) -> Unit,
    onTipPost: (MomentPost) -> Unit
) {
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var captionInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGradient)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Title
            item {
                Text(
                    text = "SHARE MOMENTS SOCIAL FEED",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MetallicGold,
                    letterSpacing = 1.sp
                )
            }

            // 24-Hour Disappearing Story Trays Carousel Header (PRD Section 3.2)
            item {
                Column {
                    Text(text = "24h Status Trays", fontSize = 11.sp, color = LightGold.copy(0.8f))
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
                                        .border(2.dp, MetallicGold, CircleShape)
                                        .clickable { showCreatePostDialog = true }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Story", tint = MetallicGold, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Your Story", fontSize = 10.sp, color = LightGold)
                            }
                        }

                        // Creator Story Trays
                        items(storyTrays) { story ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .background(WineRedDark)
                                        .border(
                                            width = 2.dp,
                                            color = if (story.isViewed) LightGold.copy(0.4f) else CrimsonVelvet,
                                            shape = CircleShape
                                        )
                                ) {
                                    Text(text = story.authorName.take(1), fontSize = 22.sp, color = LightGold, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(story.authorName, fontSize = 10.sp, color = LightGold, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // Chronological Social Feed Stream
            items(momentPosts) { post ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CrimsonVelvet, RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        // Author Header & 1-Click Follow
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(WineRedDark)
                                        .border(1.5.dp, DarkGold, CircleShape)
                                ) {
                                    Text(text = post.authorName.take(1), fontSize = 16.sp, color = LightGold, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(post.authorName, fontWeight = FontWeight.Bold, color = LightGold, fontSize = 14.sp)
                                        Text(" 🛡️", fontSize = 10.sp)
                                    }
                                    Text(post.timestamp, fontSize = 10.sp, color = LightGold.copy(0.7f))
                                }
                            }

                            Button(
                                onClick = { /* Follow */ },
                                colors = ButtonDefaults.buttonColors(containerColor = WineRedMedium),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("+ Follow", fontSize = 11.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Full-Width Media Post Placeholder
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(WineRedDark)
                                .border(1.dp, CrimsonVelvet, RoundedCornerShape(14.dp))
                        ) {
                            Text(text = "📸 Full-Width Moment Media", color = LightGold.copy(alpha = 0.7f), fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(text = post.caption, fontSize = 13.sp, color = LightGold)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Social Engagement Bar: Like, Comment, Direct Tipping
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onLikePost(post.id) }) {
                                    Icon(
                                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (post.isLiked) HeartRed else LightGold
                                    )
                                }
                                Text("${post.likesCount}", fontSize = 12.sp, color = LightGold)

                                Spacer(modifier = Modifier.width(16.dp))

                                Icon(Icons.Default.Comment, contentDescription = "Comments", tint = LightGold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${post.commentsCount}", fontSize = 12.sp, color = LightGold)
                            }

                            // Direct Tipping / Gifting on Posts
                            Button(
                                onClick = { onTipPost(post) },
                                colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = WineRedDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tip Diamonds (💎 ${post.giftTipsTotal})", fontSize = 11.sp, color = WineRedDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Floating Camera Button to Post Moments
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

    // Create Moment Dialog
    if (showCreatePostDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePostDialog = false },
            containerColor = CardBackground,
            title = { Text("Publish Moment to Social Feed", color = MetallicGold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = captionInput,
                        onValueChange = { captionInput = it },
                        label = { Text("Caption & Lifestyle Tags", color = LightGold) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = DarkGold),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("🎵 Music Stickers & Filters Applied", fontSize = 11.sp, color = LiveIndicatorGreen)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (captionInput.isNotBlank()) {
                            onPublishPost(captionInput)
                            captionInput = ""
                            showCreatePostDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
                ) {
                    Text("Publish Post", color = WineRedDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePostDialog = false }) {
                    Text("Cancel", color = LightGold)
                }
            }
        )
    }
}
