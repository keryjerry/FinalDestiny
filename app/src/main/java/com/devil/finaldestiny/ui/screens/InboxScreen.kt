package com.devil.finaldestiny.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Search
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
import com.devil.finaldestiny.model.DirectMessageConversation
import com.devil.finaldestiny.ui.components.ProfileAvatarView
import com.devil.finaldestiny.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    conversationsList: List<DirectMessageConversation> = emptyList(),
    onOpenConversation: (DirectMessageConversation) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var searchInput by remember { mutableStateOf("") }

    val filteredList = conversationsList.filter {
        it.userName.contains(searchInput, ignoreCase = true) ||
        it.userHandle.contains(searchInput, ignoreCase = true) ||
        it.lastMessage.contains(searchInput, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlueBgLight)
    ) {
        // TOP HEADER BAR WITH UNIVERSAL BACK ARROW ('<') & PAGE TITLE "Messages"
        Surface(
            color = SkyBlueCardBg,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
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

                    Column {
                        Text(
                            text = "Messages",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyTextPrimary
                        )
                        Text(
                            text = "1-on-1 Direct Chats",
                            fontSize = 11.sp,
                            color = SlateTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = { Toast.makeText(context, "💬 New Chat Started", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SkyBluePrimary)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "New Message",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // SEARCH BAR FOR CONVERSATIONS
        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                placeholder = { Text("Search conversations...", fontSize = 12.sp, color = SlateTextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlateTextSecondary, modifier = Modifier.size(18.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkyBluePrimary,
                    unfocusedBorderColor = SkyBlueBorder,
                    focusedContainerColor = SkyBlueCardBg,
                    unfocusedContainerColor = SkyBlueCardBg,
                    focusedTextColor = NavyTextPrimary,
                    unfocusedTextColor = NavyTextPrimary
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("💬", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No direct messages yet 💬",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Start a 1-on-1 chat with creators from Discover People!",
                        fontSize = 12.sp,
                        color = SlateTextSecondary
                    )
                }
            }
        } else {
            // CONVERSATIONS DIRECT MESSAGE LIST
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
            items(filteredList) { chat ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SkyBlueCardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SkyBlueBorder, RoundedCornerShape(16.dp))
                        .clickable {
                            onOpenConversation(chat)
                            Toast.makeText(context, "Opening Chat with ${chat.userName}", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                ProfileAvatarView(
                                    name = chat.userName,
                                    profilePictureUri = chat.userAvatar,
                                    size = 48.dp,
                                    showBorder = true,
                                    borderColor = SkyBluePrimary
                                )
                                if (chat.isOnline) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(LiveIndicatorGreen)
                                            .border(1.5.dp, Color.White, CircleShape)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = chat.userName,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyTextPrimary,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = chat.timestamp,
                                        fontSize = 10.sp,
                                        color = SlateTextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = chat.lastMessage,
                                    fontSize = 12.sp,
                                    color = if (chat.unreadCount > 0) NavyTextPrimary else SlateTextSecondary,
                                    fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }

                        if (chat.unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(SkyBluePrimary)
                            ) {
                                Text(
                                    text = "${chat.unreadCount}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}
