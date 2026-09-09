package com.devil.finaldestiny.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyAndSocialControlsScreen(
    userSettings: UserSettingsState,
    onUpdateSettings: (UserSettingsState) -> Unit,
    onAddKeywordToBlacklist: (String) -> Unit,
    onUnblockUser: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Privacy, 1: Close Friends, 2: Comments & Moderation, 3: Blocked Users
    val tabs = listOf("Account Privacy", "Close Friends", "Moderation", "Blocked & Muted")

    var newKeywordInput by remember { mutableStateOf("") }
    var showCloseFriendsDialog by remember { mutableStateOf(false) }
    var newListNameInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Privacy & Social Controls",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF3897F0),
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = (selectedTab == index),
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: ACCOUNT PRIVACY & COUNTS
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Account Privacy Settings", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                        // Private Account Toggle
                        ListItem(
                            headlineContent = { Text("Private Account", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Only approved followers can see your photos, reels, and stories.", fontSize = 11.sp, color = Color.Gray) },
                            leadingContent = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Black) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.isPrivateAccount,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(isPrivateAccount = it)) }
                                )
                            }
                        )

                        // Hide Like and Share Counts
                        ListItem(
                            headlineContent = { Text("Hide Like & Share Counts", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("On posts from other accounts, the total number of likes and shares will be hidden.", fontSize = 11.sp, color = Color.Gray) },
                            leadingContent = { Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = Color.Black) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.hideLikeShareCounts,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(hideLikeShareCounts = it)) }
                                )
                            }
                        )

                        // Hide Follower and Following Lists
                        ListItem(
                            headlineContent = { Text("Hide Follower & Following Lists", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Prevent non-followers from viewing your follower/following list.", fontSize = 11.sp, color = Color.Gray) },
                            leadingContent = { Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = Color.Black) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.hideFollowerFollowingList,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(hideFollowerFollowingList = it)) }
                                )
                            }
                        )

                        // Read Receipts
                        ListItem(
                            headlineContent = { Text("Read Receipts", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Others can see when you've read their direct messages.", fontSize = 11.sp, color = Color.Gray) },
                            leadingContent = { Icon(Icons.Default.MarkChatRead, contentDescription = null, tint = Color.Black) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.readReceiptsEnabled,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(readReceiptsEnabled = it)) }
                                )
                            }
                        )
                    }
                }

                1 -> {
                    // TAB 1: CLOSE FRIENDS MANAGER
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close Friends Lists", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Button(
                                onClick = { showCloseFriendsDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("+ New List", fontSize = 11.sp, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        userSettings.closeFriendsLists.forEach { list ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(list.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF166534))
                                        Text("${list.memberCount} members ⭐", fontSize = 11.sp, color = Color(0xFF15803D))
                                    }
                                    Text("Members: ${list.memberNames.joinToString(", ")}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: INTERACTION CONTROLS & KEYWORD BLACKLIST
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Comment & Interaction Controls", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                        // Manual Tag Approval
                        ListItem(
                            headlineContent = { Text("Manual Tag & Mention Approval", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Review photos and videos before tags appear on your profile.", fontSize = 11.sp, color = Color.Gray) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.tagApprovalRequired,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(tagApprovalRequired = it)) }
                                )
                            }
                        )

                        // Offensive Comment Filter
                        ListItem(
                            headlineContent = { Text("Offensive Comment Filter", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Automatically hide comments that may be offensive or abusive.", fontSize = 11.sp, color = Color.Gray) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.offensiveCommentFilterEnabled,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(offensiveCommentFilterEnabled = it)) }
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Keyword Blacklist Input
                        Text("Custom Keyword Blacklist", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = newKeywordInput,
                                onValueChange = { newKeywordInput = it },
                                label = { Text("Add blocked word") },
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (newKeywordInput.isNotBlank()) {
                                        onAddKeywordToBlacklist(newKeywordInput.trim())
                                        val updatedList = userSettings.keywordBlacklist + newKeywordInput.trim()
                                        onUpdateSettings(userSettings.copy(keywordBlacklist = updatedList))
                                        newKeywordInput = ""
                                        Toast.makeText(context, "Added word to moderation blacklist", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                Text("Add")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Active Blacklisted Words: ${userSettings.keywordBlacklist.joinToString(", ")}", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                3 -> {
                    // TAB 3: BLOCKED & MUTED LISTS
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Blocked & Muted Accounts", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Manage blocked users across Posts, Stories, Reels, and Messages.", fontSize = 11.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(10.dp))

                        userSettings.blockedMutedUsers.forEach { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                        Text("${item.handle} • ${if (item.isBlocked) "Blocked" else "Muted"} (${item.category})", fontSize = 11.sp, color = Color(0xFFDC2626))
                                    }
                                    Button(
                                        onClick = {
                                            onUnblockUser(item.id)
                                            val updated = userSettings.blockedMutedUsers.filter { it.id != item.id }
                                            onUpdateSettings(userSettings.copy(blockedMutedUsers = updated))
                                            Toast.makeText(context, "Unblocked ${item.handle}", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Unblock", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // CREATE CLOSE FRIENDS LIST DIALOG
    if (showCloseFriendsDialog) {
        AlertDialog(
            onDismissRequest = { showCloseFriendsDialog = false },
            title = { Text("Create Close Friends List", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newListNameInput,
                    onValueChange = { newListNameInput = it },
                    label = { Text("List Name (e.g., VIP Circle)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newListNameInput.isNotBlank()) {
                            val newList = CloseFriendsList(
                                id = "cf_${System.currentTimeMillis()}",
                                name = newListNameInput.trim(),
                                memberCount = 1,
                                memberNames = listOf("@User_Me")
                            )
                            onUpdateSettings(userSettings.copy(closeFriendsLists = userSettings.closeFriendsLists + newList))
                            newListNameInput = ""
                            showCloseFriendsDialog = false
                            Toast.makeText(context, "✅ Created Close Friends List!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseFriendsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
