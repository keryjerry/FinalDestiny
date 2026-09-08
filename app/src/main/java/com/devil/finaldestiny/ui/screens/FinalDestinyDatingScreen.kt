package com.devil.finaldestiny.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.model.SwipeCard
import com.devil.finaldestiny.model.UserProfile
import com.devil.finaldestiny.ui.components.PaymentQrModalDialog
import com.devil.finaldestiny.ui.components.ProfileAvatarView
import com.devil.finaldestiny.ui.theme.*

data class DatingMessage(
    val senderName: String,
    val text: String,
    val timestamp: String,
    val isFromMe: Boolean
)

@Composable
fun FinalDestinyDatingScreen(
    user: UserProfile,
    cards: List<SwipeCard>,
    matchedCard: SwipeCard?,
    onSwipeRight: (String) -> Unit,
    onSwipeLeft: (String) -> Unit,
    onSwipeUpSuperLike: (String) -> Unit,
    onDismissMatchModal: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Swipe Deck, 1: Dating Inbox, 2: Who Liked / Visited Me
    var activeChatTarget by remember { mutableStateOf<UserProfile?>(null) }
    var datingChatInput by remember { mutableStateOf("") }

    var isWhoLikedUnlocked by remember { mutableStateOf(false) }
    var isVisitorsUnlocked by remember { mutableStateOf(false) }
    var showPaymentQrModal by remember { mutableStateOf(false) }
    var pendingPaymentItem by remember { mutableStateOf("") }
    var pendingPaymentPrice by remember { mutableStateOf(49) }

    // Mock Dating Messages for Active Matches
    val datingChats = remember {
        mutableStateMapOf(
            "u201" to mutableStateListOf(
                DatingMessage("Ananya Roy", "Hey! Loved your profile bio 😊", "10:30 AM", isFromMe = false),
                DatingMessage("Me", "Hey Ananya! Glad we matched! 💖", "10:32 AM", isFromMe = true)
            ),
            "u203" to mutableStateListOf(
                DatingMessage("Simran Kaur", "Hi there! Are you also into architecture & karaoke?", "9:15 AM", isFromMe = false)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGradient)
            .padding(14.dp)
    ) {
        // TOP HEADER BAR WITH PROFILE INFO
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MetallicGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MetallicGold, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("💖 FINAL DESTINY DATING", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MetallicGold, letterSpacing = 0.5.sp)
                        Text("Active as ${user.name} (${user.age} yrs)", fontSize = 10.sp, color = LightGold.copy(0.7f))
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(WineRedMedium)
                        .border(1.dp, MetallicGold, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("💎 ${user.diamonds}", fontSize = 11.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // DATING CATEGORY TABS
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CardBackground,
            contentColor = MetallicGold,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).border(1.dp, CrimsonVelvet, RoundedCornerShape(14.dp))
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("🔥 Discover Cards", fontSize = 11.sp, modifier = Modifier.padding(10.dp), color = if (selectedTab == 0) MetallicGold else LightGold.copy(0.6f), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("💬 Dating Chat", fontSize = 11.sp, modifier = Modifier.padding(10.dp), color = if (selectedTab == 1) MetallicGold else LightGold.copy(0.6f), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("💖 Likes & Visitors", fontSize = 11.sp, modifier = Modifier.padding(10.dp), color = if (selectedTab == 2) MetallicGold else LightGold.copy(0.6f), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // TAB 0: TINDER-STYLE SWIPE CARDS DECK
        if (selectedTab == 0) {
            if (cards.isNotEmpty()) {
                val currentCard = cards.first()
                val profile = currentCard.profile

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(2.dp, GoldGradient, RoundedCornerShape(24.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ProfileAvatarView(
                                            name = profile.name,
                                            profilePictureUri = profile.profilePictureUri ?: profile.photos.firstOrNull(),
                                            gender = profile.gender,
                                            size = 48.dp,
                                            showBorder = true,
                                            borderColor = MetallicGold
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(profile.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("${profile.age}", fontSize = 15.sp, color = LightGold)
                                                if (profile.verifiedStatus) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("✓", fontSize = 12.sp, color = VerifiedBlue, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Text(profile.handle, fontSize = 11.sp, color = LightGold.copy(0.7f))
                                        }
                                    }
                                    Text("📍 ${currentCard.distanceKm} km away", fontSize = 11.sp, color = LightGold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(profile.bio, fontSize = 13.sp, color = LightGold, modifier = Modifier.padding(vertical = 4.dp))

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    profile.lifestyleTags.forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(WineRedMedium)
                                                .border(1.dp, MetallicGold.copy(0.4f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(tag, fontSize = 10.sp, color = LightGold)
                                        }
                                    }
                                }
                            }

                            // TINDER-STYLE ACTION BUTTONS BAR (PASS, SUPER LIKE, LIKE, HEART)
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                            ) {
                                // ❌ Pass (Swipe Left)
                                IconButton(
                                    onClick = { onSwipeLeft(currentCard.id) },
                                    modifier = Modifier.size(54.dp).clip(CircleShape).background(WineRedMedium).border(2.dp, Color.Red, CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Pass", tint = Color.Red, modifier = Modifier.size(28.dp))
                                }

                                // ⭐ Super Like (Swipe Up)
                                IconButton(
                                    onClick = { onSwipeUpSuperLike(currentCard.id) },
                                    modifier = Modifier.size(50.dp).clip(CircleShape).background(WineRedMedium).border(2.dp, MetallicGold, CircleShape)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = "Super Like", tint = MetallicGold, modifier = Modifier.size(26.dp))
                                }

                                // ❤️ Like (Swipe Right)
                                IconButton(
                                    onClick = { onSwipeRight(currentCard.id) },
                                    modifier = Modifier.size(54.dp).clip(CircleShape).background(MetallicGold).border(2.dp, WineRedDark, CircleShape)
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = "Like", tint = HeartRed, modifier = Modifier.size(28.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("✨ All Match Cards Explored!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Check back soon for new local dating profiles!", fontSize = 12.sp, color = LightGold)
                    }
                }
            }
        } else if (selectedTab == 1) {
            // TAB 1: DEDICATED DATING CHAT INBOX
            if (activeChatTarget == null) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                    val matchedProfiles = cards.map { it.profile }.take(4)
                    items(matchedProfiles) { matchedUser ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CrimsonVelvet, RoundedCornerShape(16.dp))
                                .clickable { activeChatTarget = matchedUser }
                                .padding(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(46.dp).clip(CircleShape).background(WineRedDark).border(1.5.dp, MetallicGold, CircleShape)
                                    ) {
                                        Text(matchedUser.name.take(1).uppercase(), fontSize = 20.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(matchedUser.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LightGold)
                                        Text("Matched on Final Destiny Dating 💖", fontSize = 10.sp, color = LiveIndicatorGreen)
                                    }
                                }

                                Button(
                                    onClick = { activeChatTarget = matchedUser },
                                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Chat 💬", color = WineRedDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                // CHATBOX VIEW WITH MATCHED DATING USER
                val chatUser = activeChatTarget!!
                val messages = datingChats.getOrPut(chatUser.id) { mutableStateListOf() }

                Column(modifier = Modifier.fillMaxSize()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💬 Chatting with ${chatUser.name}", color = MetallicGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            IconButton(onClick = { activeChatTarget = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Chat", tint = LightGold)
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { msg ->
                            Row(
                                horizontalArrangement = if (msg.isFromMe) Arrangement.End else Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = if (msg.isFromMe) MetallicGold else WineRedMedium),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.widthIn(max = 240.dp).padding(4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(msg.text, fontSize = 12.sp, color = if (msg.isFromMe) WineRedDark else LightGold)
                                        Text(msg.timestamp, fontSize = 9.sp, color = if (msg.isFromMe) WineRedDark.copy(0.7f) else LightGold.copy(0.6f), modifier = Modifier.align(Alignment.End))
                                    }
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        OutlinedTextField(
                            value = datingChatInput,
                            onValueChange = { datingChatInput = it },
                            placeholder = { Text("Type a private message...", fontSize = 11.sp, color = LightGold.copy(0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = DarkGold),
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(46.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (datingChatInput.isNotBlank()) {
                                    messages.add(DatingMessage(user.name, datingChatInput, "Just now", isFromMe = true))
                                    datingChatInput = ""
                                }
                            },
                            modifier = Modifier.size(42.dp).clip(CircleShape).background(MetallicGold)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = WineRedDark)
                        }
                    }
                }
            }
        } else if (selectedTab == 2) {
            // TAB 2: WHO LIKED ME & WHO VISITED ME (PAID PAYTM UNLOCK SECTIONS)
            Column(modifier = Modifier.fillMaxSize()) {
                // Who Liked Me Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, MetallicGold, RoundedCornerShape(16.dp)).padding(12.dp)
                ) {
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💖 Who Liked Me", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(HeartRed).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("14 Singles", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (!isWhoLikedUnlocked) {
                                Button(
                                    onClick = {
                                        pendingPaymentItem = "Unlock Who Liked Me Feature"
                                        pendingPaymentPrice = 49
                                        showPaymentQrModal = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = WineRedDark, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Unlock ₹49", fontSize = 10.sp, color = WineRedDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isWhoLikedUnlocked) {
                            Text("🔓 Unlocked! 14 users liked your profile.", fontSize = 11.sp, color = LiveIndicatorGreen, fontWeight = FontWeight.Bold)
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(12.dp)).background(WineRedMedium)
                            ) {
                                Text("🔒 14 Singles Liked Your Profile!\nTap Unlock to view photos & chat.", fontSize = 11.sp, color = LightGold, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Who Visited Me Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, MetallicGold, RoundedCornerShape(16.dp)).padding(12.dp)
                ) {
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("👀 Who Visited Me", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(WineRedMedium).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("32 Visitors", fontSize = 9.sp, color = LightGold, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (!isVisitorsUnlocked) {
                                Button(
                                    onClick = {
                                        pendingPaymentItem = "Unlock Who Visited Me Feature"
                                        pendingPaymentPrice = 49
                                        showPaymentQrModal = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = WineRedDark, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Unlock ₹49", fontSize = 10.sp, color = WineRedDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isVisitorsUnlocked) {
                            Text("🔓 Unlocked! 32 profile visitors revealed.", fontSize = 11.sp, color = LiveIndicatorGreen, fontWeight = FontWeight.Bold)
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(12.dp)).background(WineRedMedium)
                            ) {
                                Text("🔒 32 Visitors Viewed Your Profile Today!\nTap Unlock to reveal visitor list.", fontSize = 11.sp, color = LightGold, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // MATCH CELEBRATION MODAL
    if (matchedCard != null) {
        AlertDialog(
            onDismissRequest = onDismissMatchModal,
            containerColor = CardBackground,
            title = { Text("💖 IT'S A DATING MATCH! 💖", color = MetallicGold, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("You and ${matchedCard.profile.name} liked each other!", fontSize = 13.sp, color = LightGold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onDismissMatchModal()
                            selectedTab = 1
                            activeChatTarget = matchedCard.profile
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
                    ) {
                        Text("Send Message Now 💬", color = WineRedDark, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissMatchModal) {
                    Text("Keep Swiping", color = LightGold)
                }
            }
        )
    }

    // OFFICIAL PAYTM UPI PAYMENT QR MODAL
    if (showPaymentQrModal) {
        PaymentQrModalDialog(
            amountInr = pendingPaymentPrice,
            itemDescription = pendingPaymentItem,
            onPaymentSuccess = {
                if (pendingPaymentItem.contains("Liked")) {
                    isWhoLikedUnlocked = true
                } else {
                    isVisitorsUnlocked = true
                }
                showPaymentQrModal = false
            },
            onDismiss = { showPaymentQrModal = false }
        )
    }
}
