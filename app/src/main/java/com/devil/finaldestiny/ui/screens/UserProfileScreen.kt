package com.devil.finaldestiny.ui.screens

import android.content.Context
import android.graphics.Bitmap
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Share
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
import com.devil.finaldestiny.data.SupabaseAuthClient
import com.devil.finaldestiny.model.Gender
import com.devil.finaldestiny.model.UserProfile
import com.devil.finaldestiny.ui.components.VipBadge
import com.devil.finaldestiny.ui.theme.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class MenuItemData(
    val id: String,
    val title: String,
    val icon: String,
    val badgeDot: Boolean = false,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    user: UserProfile,
    onSaveProfile: (UserProfile) -> Unit,
    onNavigateToStore: () -> Unit = {},
    onNavigateToSecondaryFeed: () -> Unit = {},
    onNavigateToMonetization: () -> Unit = {},
    onNavigateToDating: () -> Unit = {},
    onNavigateToAboutUs: () -> Unit = {},
    onLogOut: () -> Unit,
    onRefresh: suspend () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    var isEditing by remember { mutableStateOf(false) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    var showFollowersDialog by remember { mutableStateOf(false) }
    var showComplaintModal by remember { mutableStateOf(false) }
    var showPrivacyPolicyModal by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("Followers List") }

    var menuPageIndex by remember { mutableStateOf(0) } // 0, 1, 2

    var editName by remember(user) { mutableStateOf(user.name) }
    var editBio by remember(user) { mutableStateOf(user.bio) }
    var editAge by remember(user) { mutableStateOf(user.age.toString()) }
    var editGender by remember(user) { mutableStateOf(user.gender) }
    var editRelationshipIntent by remember(user) { mutableStateOf(user.relationshipIntent) }

    // Complaint Form State
    var complaintCategory by remember { mutableStateOf("Billing & Payment") }
    var complaintDescription by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val updated = user.copy(profilePictureUri = it.toString(), verifiedStatus = true)
            onSaveProfile(updated)
            Toast.makeText(context, "📸 Profile Photo Uploaded & Account Verified! 🛡️", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val tempUri = saveBitmapToCacheUri(context, it)
            tempUri?.let { uri ->
                val updated = user.copy(profilePictureUri = uri.toString(), verifiedStatus = true)
                onSaveProfile(updated)
                Toast.makeText(context, "📷 Live Selfie Verified & Account Verified! 🛡️", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val customAvatarBitmap = rememberLoadedImage(context, user.profilePictureUri)

    // 3 PAGES OF MENU ITEMS BASED ON USER'S ATTACHED SCREENSHOTS
    val page1Items = listOf(
        MenuItemData("m1", "Get Rewards", "🎁", badgeDot = true) { Toast.makeText(context, "🎁 Claimed Daily Reward (+50 Coins)!", Toast.LENGTH_SHORT).show() },
        MenuItemData("m2", "Task", "📋", badgeDot = true) { Toast.makeText(context, "📋 3 Daily Tasks Active!", Toast.LENGTH_SHORT).show() },
        MenuItemData("m3", "Monthly card", "💳") { onNavigateToStore() },
        MenuItemData("m4", "Store", "🏪") { onNavigateToStore() },
        MenuItemData("m5", "Relationship", "💑") { onNavigateToDating() },
        MenuItemData("m6", "Wallet", "👛") { onNavigateToStore() },
        MenuItemData("m7", "Noble", "👑") { onNavigateToStore() },
        MenuItemData("m8", "VIP", "⭐", badgeDot = true) { onNavigateToStore() }
    )

    val page2Items = listOf(
        MenuItemData("m9", "Coupon", "🎟️") { Toast.makeText(context, "🎟️ 2 Discount Coupons Available!", Toast.LENGTH_SHORT).show() },
        MenuItemData("m10", "Honor Level", "🏆") { Toast.makeText(context, "🏆 Current Honor Rank: Gold III", Toast.LENGTH_SHORT).show() },
        MenuItemData("m11", "Family", "👨‍👩‍👧") { Toast.makeText(context, "👨‍👩‍👧 Destiny Family Guild Level 5", Toast.LENGTH_SHORT).show() },
        MenuItemData("m12", "Matchmaker", "💘", badgeDot = true) { onNavigateToDating() },
        MenuItemData("m13", "Backpack", "🎒") { Toast.makeText(context, "🎒 Inventory: 12 Gifts & 3 Cars", Toast.LENGTH_SHORT).show() },
        MenuItemData("m14", "Room premium", "🏠", badgeDot = true) { onNavigateToStore() },
        MenuItemData("m15", "Destiny Live", "📸") { onNavigateToSecondaryFeed() },
        MenuItemData("m16", "Level", "📊", badgeDot = true) { Toast.makeText(context, "📊 User Level 13 (80% to Lv.14)", Toast.LENGTH_SHORT).show() }
    )

    val page3Items = listOf(
        MenuItemData("m17", "Share", "🔗") { Toast.makeText(context, "🔗 Referral Link Copied!", Toast.LENGTH_SHORT).show() },
        MenuItemData("m18", "Help", "🎧", badgeDot = true) { showComplaintModal = true },
        MenuItemData("m19", "Badge", "🏅", badgeDot = true) { Toast.makeText(context, "🏅 5 Badges Unlocked", Toast.LENGTH_SHORT).show() },
        MenuItemData("m20", "Room Badge", "🏰", badgeDot = true) { Toast.makeText(context, "🏰 Host Room Badge: VIP Host", Toast.LENGTH_SHORT).show() },
        MenuItemData("m21", "Room title", "🏷️") { onNavigateToStore() },
        MenuItemData("m22", "Feedback", "📝") { showComplaintModal = true },
        MenuItemData("m23", "Facebook", "📘") { Toast.makeText(context, "🌐 Facebook Page Connected", Toast.LENGTH_SHORT).show() },
        MenuItemData("m24", "About & Legal", "📜") { showPrivacyPolicyModal = true }
    )

    val currentMenuGrid = when (menuPageIndex) {
        0 -> page1Items
        1 -> page2Items
        else -> page3Items
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile & Services", color = MetallicGold, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MetallicGold)
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = {
                            val newAge = editAge.toIntOrNull() ?: user.age
                            val updated = user.copy(
                                name = editName,
                                bio = editBio,
                                age = newAge,
                                gender = editGender,
                                relationshipIntent = editRelationshipIntent
                            )
                            onSaveProfile(updated)
                            isEditing = false
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = MetallicGold)
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MetallicGold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBackground)
            )
        },
        containerColor = WineRedDark
    ) { innerPadding ->
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
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(PrimaryGradient)
                    .verticalScroll(scrollState)
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // Profile Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, GoldGradient, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(90.dp)
                            .clickable { showPhotoOptionsDialog = true }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(86.dp)
                                .clip(CircleShape)
                                .background(WineRedLight)
                                .border(3.dp, MetallicGold, CircleShape)
                        ) {
                            if (customAvatarBitmap != null) {
                                Image(
                                    bitmap = customAvatarBitmap,
                                    contentDescription = "User Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = user.name.take(1).uppercase(),
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MetallicGold
                                )
                            }
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(30.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(MetallicGold)
                                .border(1.5.dp, WineRedDark, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Upload Photo",
                                tint = WineRedDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(onClick = { showPhotoOptionsDialog = true }) {
                        Text("📷 Upload Photo / Take Selfie", color = MetallicGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = user.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LightGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        if (user.verifiedStatus) {
                            Text("🛡️", fontSize = 14.sp)
                        }
                    }

                    Text(text = user.handle, fontSize = 13.sp, color = LightGold.copy(0.7f))

                    Spacer(modifier = Modifier.height(6.dp))
                    VipBadge(vipLevel = user.vipLevel)

                    Spacer(modifier = Modifier.height(12.dp))

                    // User Stats Bar
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.clickable {
                            dialogTitle = "Followers (${user.followerCount})"
                            showFollowersDialog = true
                        }) {
                            ProfileStatItem(label = "Followers 👥", value = "${user.followerCount}")
                        }

                        Box(modifier = Modifier.clickable {
                            dialogTitle = "Following (${user.followingCount})"
                            showFollowersDialog = true
                        }) {
                            ProfileStatItem(label = "Following ✨", value = "${user.followingCount}")
                        }

                        ProfileStatItem(label = "Coins 🪙", value = "${user.coins}")
                        ProfileStatItem(label = "Diamonds 💎", value = "${user.diamonds}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-PAGE INTERACTIVE MENU GRID CARD (MATCHING REFERENCE USER SCREENSHOTS)
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MetallicGold.copy(0.5f), RoundedCornerShape(20.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Menu", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MetallicGold)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (menuPageIndex > 0) menuPageIndex-- },
                                enabled = menuPageIndex > 0,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Page", tint = if (menuPageIndex > 0) MetallicGold else LightGold.copy(0.3f))
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                (0..2).forEach { pageIdx ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (menuPageIndex == pageIdx) 8.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(if (menuPageIndex == pageIdx) MetallicGold else LightGold.copy(0.4f))
                                    )
                                }
                            }

                            IconButton(
                                onClick = { if (menuPageIndex < 2) menuPageIndex++ },
                                enabled = menuPageIndex < 2,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Page", tint = if (menuPageIndex < 2) MetallicGold else LightGold.copy(0.3f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4 x 2 Grid of 8 Menu Items
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val row1 = currentMenuGrid.take(4)
                        val row2 = currentMenuGrid.drop(4).take(4)

                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            row1.forEach { item ->
                                MenuItemIconButton(item = item)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            row2.forEach { item ->
                                MenuItemIconButton(item = item)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!isEditing) {
                // Profile Information Summary Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CrimsonVelvet, RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "PROFILE DETAILS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MetallicGold,
                            letterSpacing = 1.sp
                        )
                        HorizontalDivider(color = CrimsonVelvet)

                        ProfileDetailRow(label = "Registered Email 📧", value = SupabaseAuthClient.getUserEmail() ?: "Not connected")
                        ProfileDetailRow(label = "Supabase Auth User ID 🆔", value = user.id)
                        ProfileDetailRow(label = "About Me", value = user.bio.ifBlank { "No bio added yet." })
                        ProfileDetailRow(label = "Age & Gender", value = "${user.age} yrs • ${user.gender.name}")
                        ProfileDetailRow(label = "Dating Goal", value = user.relationshipIntent)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { isEditing = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = WineRedDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Profile", color = WineRedDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showComplaintModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = WineRedMedium),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Icon(Icons.Default.Feedback, contentDescription = null, tint = MetallicGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Help & Complaint", color = LightGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            } else {
                // Edit Mode Form
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MetallicGold, RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "EDIT PROFILE DETAILS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MetallicGold
                        )

                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Display Name", color = LightGold) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = CrimsonVelvet),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editBio,
                            onValueChange = { editBio = it },
                            label = { Text("Bio", color = LightGold) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = CrimsonVelvet),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editAge,
                            onValueChange = { editAge = it },
                            label = { Text("Age", color = LightGold) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = CrimsonVelvet),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { isEditing = false },
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Text("Cancel", color = LightGold)
                            }

                            Button(
                                onClick = {
                                    val newAge = editAge.toIntOrNull() ?: user.age
                                    val updated = user.copy(
                                        name = editName,
                                        bio = editBio,
                                        age = newAge,
                                        gender = editGender,
                                        relationshipIntent = editRelationshipIntent
                                    )
                                    onSaveProfile(updated)
                                    isEditing = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Text("Save", color = WineRedDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Log Out Button
            Button(
                onClick = onLogOut,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.85f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out from Account", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

    // FEEDBACK & COMPLAINT BOX DIALOG
    if (showComplaintModal) {
        AlertDialog(
            onDismissRequest = { showComplaintModal = false },
            containerColor = CardBackground,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Feedback, contentDescription = null, tint = MetallicGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📝 Complaint Box & Support", color = MetallicGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Complaint Category:", fontSize = 11.sp, color = LightGold)
                    val categories = listOf("Billing & Payment", "User Behavior / Report", "App Bug / Technical", "Host Monetization")
                    categories.forEach { cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (complaintCategory == cat) WineRedMedium else Color.Transparent)
                                .clickable { complaintCategory = cat }
                                .padding(6.dp)
                        ) {
                            RadioButton(
                                selected = (complaintCategory == cat),
                                onClick = { complaintCategory = cat },
                                colors = RadioButtonDefaults.colors(selectedColor = MetallicGold)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(cat, fontSize = 12.sp, color = LightGold)
                        }
                    }

                    OutlinedTextField(
                        value = complaintDescription,
                        onValueChange = { complaintDescription = it },
                        placeholder = { Text("Describe your issue or report details...", fontSize = 11.sp, color = LightGold.copy(0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = DarkGold),
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (complaintDescription.isNotBlank()) {
                            showComplaintModal = false
                            complaintDescription = ""
                            Toast.makeText(context, "✅ Support Ticket Submitted! Ticket #DST-${(1000..9999).random()} Created.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Please enter issue details.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
                ) {
                    Text("Submit Ticket 📩", color = WineRedDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showComplaintModal = false }) {
                    Text("Close", color = LightGold)
                }
            }
        )
    }

    // PRIVACY POLICY & ABOUT APP DIALOG
    if (showPrivacyPolicyModal) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyModal = false },
            containerColor = CardBackground,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Policy, contentDescription = null, tint = MetallicGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📜 Privacy Policy & App Info", color = MetallicGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("App Name: Destiny Live", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                    Text("Founder & Creator: DarkDevil", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LightGold)
                    Text("Version: 2.4.0 (Official Build)", fontSize = 11.sp, color = LightGold.copy(0.7f))
                    HorizontalDivider(color = CrimsonVelvet)

                    Text("1. Data Protection & Privacy", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                    Text("Destiny Live is committed to safeguarding user personal information. All media uploaded to Destiny Live Moments or live rooms are protected and encrypted.", fontSize = 10.sp, color = LightGold.copy(0.8f))

                    Text("2. Community Guidelines & Safety", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                    Text("Zero tolerance for harassment, hate speech, or explicit content. Users or hosts violating guidelines will face immediate room bans or account suspension.", fontSize = 10.sp, color = LightGold.copy(0.8f))

                    Text("3. Purchases & Refund Terms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                    Text("All financial recharges (Diamonds, Coins, VIP Passes) are processed securely. Unspent diamond balances are non-transferable.", fontSize = 10.sp, color = LightGold.copy(0.8f))
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyPolicyModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
                ) {
                    Text("I Understand & Agree", color = WineRedDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // PHOTO / SELFIE UPLOADER MODAL DIALOG
    if (showPhotoOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoOptionsDialog = false },
            containerColor = CardBackground,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MetallicGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📷 Profile Photo & Selfie Verification", color = MetallicGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Upload a profile photo or take a live selfie to verify your profile automatically 🛡️:",
                        fontSize = 12.sp,
                        color = LightGold
                    )

                    Button(
                        onClick = {
                            showPhotoOptionsDialog = false
                            try {
                                cameraLauncher.launch(null)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Camera launch error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WineRedMedium),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MetallicGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take Live Selfie 📷 (Auto-Verify)", color = LightGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            showPhotoOptionsDialog = false
                            try {
                                galleryLauncher.launch("image/*")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gallery launch error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = WineRedDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose from Gallery 🖼️", color = WineRedDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoOptionsDialog = false }) {
                    Text("Cancel", color = LightGold)
                }
            }
        )
    }

    // FOLLOWERS / FOLLOWING LIST MODAL DIALOG
    if (showFollowersDialog) {
        AlertDialog(
            onDismissRequest = { showFollowersDialog = false },
            containerColor = CardBackground,
            title = { Text(dialogTitle, color = MetallicGold, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val mockList = listOf(
                        "Ananya Roy (@Ananya_Roy)" to "VIP 4",
                        "Aarav Sharma (@Aarav_Sharma)" to "VIP 5",
                        "Simran Kaur (@Simran_Vibes)" to "VIP 3",
                        "Vikram Malhotra (@Vikram_M)" to "VIP 6"
                    )
                    mockList.forEach { (nameHandle, vip) ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(WineRedMedium)
                                .padding(8.dp)
                        ) {
                            Text(nameHandle, fontSize = 12.sp, color = LightGold, fontWeight = FontWeight.SemiBold)
                            Text(vip, fontSize = 10.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFollowersDialog = false }) {
                    Text("Close", color = LightGold)
                }
            }
        )
    }
}

@Composable
fun MenuItemIconButton(item: MenuItemData) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(70.dp)
            .clickable { item.onClick() }
            .padding(vertical = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(WineRedMedium)
                .border(1.dp, MetallicGold.copy(0.4f), RoundedCornerShape(14.dp))
        ) {
            Text(item.icon, fontSize = 20.sp)

            if (item.badgeDot) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(HeartRed)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.title,
            fontSize = 10.sp,
            color = LightGold,
            maxLines = 1
        )
    }
}

@Composable
fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
        Text(text = label, fontSize = 10.sp, color = LightGold.copy(0.7f))
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = MetallicGold)
        Text(text = value, fontSize = 12.sp, color = LightGold, fontWeight = FontWeight.SemiBold)
    }
}

private fun saveBitmapToCacheUri(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val file = File(context.cacheBufferDir(), "selfie_${System.currentTimeMillis()}.jpg")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        stream.flush()
        stream.close()
        Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun Context.cacheBufferDir(): File {
    val dir = File(cacheDir, "profile_photos")
    if (!dir.exists()) dir.mkdirs()
    return dir
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
