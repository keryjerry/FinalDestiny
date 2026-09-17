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
import com.devil.finaldestiny.data.DirectChatMessage
import com.devil.finaldestiny.data.SupabaseAuthClient
import com.devil.finaldestiny.model.DirectMessageConversation
import com.devil.finaldestiny.ui.components.ProfileAvatarView
import com.devil.finaldestiny.ui.theme.*
import kotlinx.coroutines.launch

data class ActiveDirectChatTarget(
    val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    conversationsList: List<DirectMessageConversation> = emptyList(),
    initialTargetUserId: String? = null,
    initialTargetUsername: String? = null,
    initialTargetAvatarUrl: String? = null,
    onOpenConversation: (DirectMessageConversation) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchInput by remember { mutableStateOf("") }

    var activeChatTarget by remember(initialTargetUserId) {
        mutableStateOf<ActiveDirectChatTarget?>(
            if (!initialTargetUserId.isNullOrBlank()) {
                val name = initialTargetUsername.takeIf { !it.isNullOrBlank() } ?: "User"
                ActiveDirectChatTarget(
                    id = initialTargetUserId,
                    name = name,
                    handle = if (name.startsWith("@")) name else "@$name",
                    avatarUrl = initialTargetAvatarUrl
                )
            } else null
        )
    }

    val currentUserId = remember { SupabaseAuthClient.getCurrentUserId() ?: "usr_me" }
    var liveConversations by remember { mutableStateOf<List<DirectMessageConversation>>(conversationsList) }

    // Fetch live conversations with resilient profile resolution
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val fetched = SupabaseAuthClient.fetchConversationsFromSupabase(currentUserId)
                    if (fetched.isNotEmpty()) {
                        liveConversations = fetched
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val displayConversations = if (conversationsList.isNotEmpty()) conversationsList else liveConversations

    if (activeChatTarget != null) {
        val target = activeChatTarget!!
        var chatInput by remember { mutableStateOf("") }
        val messagesList = remember { mutableStateListOf<DirectChatMessage>() }
        var isLoadingMessages by remember { mutableStateOf(true) }

        // Resolve Target Profile Details asynchronously if generic or fallback
        LaunchedEffect(target.id) {
            if (target.name == "User" || target.name.startsWith("User_") || target.avatarUrl.isNullOrBlank()) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val prof = SupabaseAuthClient.fetchSingleUserProfileFromSupabase(target.id)
                        if (prof != null) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                activeChatTarget = ActiveDirectChatTarget(
                                    id = prof.id,
                                    name = prof.name,
                                    handle = prof.handle,
                                    avatarUrl = prof.profilePictureUri ?: target.avatarUrl
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        LaunchedEffect(target.id) {
            isLoadingMessages = true
            try {
                val fetched = SupabaseAuthClient.fetchDirectMessagesFromSupabase(
                    currentUserId = currentUserId,
                    targetUserId = target.id
                )
                messagesList.clear()
                messagesList.addAll(fetched)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingMessages = false
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SkyBlueBgLight)
        ) {
            // DIRECT CHAT TOP HEADER BAR
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
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (!initialTargetUserId.isNullOrBlank()) {
                                    onBack()
                                } else {
                                    activeChatTarget = null
                                }
                            },
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

                        ProfileAvatarView(
                            name = target.name,
                            profilePictureUri = target.avatarUrl,
                            size = 40.dp,
                            showBorder = true,
                            borderColor = SkyBluePrimary
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = target.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyTextPrimary
                            )
                            Text(
                                text = "🟢 Active now",
                                fontSize = 11.sp,
                                color = LiveIndicatorGreen
                            )
                        }
                    }
                }
            }

            // CHAT MESSAGES BODY
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isLoadingMessages) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = SkyBluePrimary
                    )
                } else if (messagesList.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Text("💬", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No messages yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Send a direct message to start chatting with ${target.name}!",
                            fontSize = 12.sp,
                            color = SlateTextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(messagesList) { msg ->
                            Row(
                                horizontalArrangement = if (msg.isFromMe) Arrangement.End else Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (msg.isFromMe) SkyBluePrimary else SkyBlueCardBg
                                    ),
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (msg.isFromMe) 16.dp else 4.dp,
                                        bottomEnd = if (msg.isFromMe) 4.dp else 16.dp
                                    ),
                                    modifier = Modifier
                                        .widthIn(max = 260.dp)
                                        .border(
                                            width = if (msg.isFromMe) 0.dp else 1.dp,
                                            color = SkyBlueBorder,
                                            shape = RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (msg.isFromMe) 16.dp else 4.dp,
                                                bottomEnd = if (msg.isFromMe) 4.dp else 16.dp
                                            )
                                        )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = msg.content,
                                            fontSize = 14.sp,
                                            color = if (msg.isFromMe) Color.White else NavyTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // CHAT INPUT BAR AT BOTTOM
            Surface(
                color = SkyBlueCardBg,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Type a message...", fontSize = 13.sp, color = SlateTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = SkyBlueBorder,
                            focusedContainerColor = SkyBlueBgLight,
                            unfocusedContainerColor = SkyBlueBgLight,
                            focusedTextColor = NavyTextPrimary,
                            unfocusedTextColor = NavyTextPrimary
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            val text = chatInput.trim()
                            if (text.isNotBlank()) {
                                val newMsg = DirectChatMessage(
                                    id = "msg_${System.currentTimeMillis()}",
                                    senderId = currentUserId,
                                    receiverId = target.id,
                                    content = text,
                                    createdAt = "Just now",
                                    isFromMe = true
                                )
                                messagesList.add(newMsg)
                                chatInput = ""

                                coroutineScope.launch {
                                    val (success, err) = SupabaseAuthClient.sendDirectMessageToSupabase(
                                        context = context,
                                        currentUserId = currentUserId,
                                        targetUserId = target.id,
                                        content = text
                                    )
                                    if (!success) {
                                        Toast.makeText(context, "❌ Message failed to send: ${err ?: "Network error"}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SkyBluePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        return
    }

    // MAIN CONVERSATIONS LIST VIEW
    val filteredList = displayConversations.filter {
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
                                activeChatTarget = ActiveDirectChatTarget(
                                    id = chat.id,
                                    name = chat.userName,
                                    handle = chat.userHandle,
                                    avatarUrl = chat.userAvatar
                                )
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
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
