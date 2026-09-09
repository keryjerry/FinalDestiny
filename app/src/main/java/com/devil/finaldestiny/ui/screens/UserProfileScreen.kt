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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.model.UserProfile
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class DiscoverUserItem(
    val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String,
    val mutualsLabel: String,
    val isFollowBack: Boolean = false,
    var isFollowing: Boolean = false
)

data class StoryHighlightItem(
    val id: String,
    val title: String,
    val coverUrl: String
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
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    var showDiscoverPeople by remember { mutableStateOf(true) }
    var dialogTitle by remember { mutableStateOf("Followers List") }

    var activeTabState by remember { mutableIntStateOf(0) } // 0: Grid, 1: Reels, 2: Tagged/Saved

    var editName by remember(user) { mutableStateOf(user.name.ifBlank { "Dilshad_The mountain lover" }) }
    var editBio by remember(user) { mutableStateOf(user.bio.ifBlank { "I am traveling buddy .i love to travel 🧳😊" }) }

    var complaintCategory by remember { mutableStateOf("Billing & Payment") }
    var complaintDescription by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    val visualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val updated = user.copy(profilePictureUri = it.toString(), verifiedStatus = true)
            onSaveProfile(updated)
            Toast.makeText(context, "📸 Media uploaded to Profile!", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val updated = user.copy(profilePictureUri = it.toString(), verifiedStatus = true)
            onSaveProfile(updated)
            Toast.makeText(context, "📸 Profile Photo Updated!", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, "📷 Live Selfie Verified!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val customAvatarBitmap = rememberProfileLoadedImage(context, user.profilePictureUri)

    val sampleDiscoverUsers = remember {
        mutableStateListOf(
            DiscoverUserItem("d1", "Anurag Ray", "@anurag_ray", "https://picsum.photos/200/200?random=101", "2 mutuals", isFollowBack = true),
            DiscoverUserItem("d2", "Sahil Khan", "@sahil_khan", "https://picsum.photos/200/200?random=102", "1 mutual", isFollowBack = false),
            DiscoverUserItem("d3", "Sagarika", "@sagarika_s", "https://picsum.photos/200/200?random=103", "1 mutual", isFollowBack = false)
        )
    }

    val storyHighlights = remember {
        listOf(
            StoryHighlightItem("h1", "Siliguri", "https://picsum.photos/200/200?random=201"),
            StoryHighlightItem("h2", "Kalimpong", "https://picsum.photos/200/200?random=202"),
            StoryHighlightItem("h3", "Darjeeling", "https://picsum.photos/200/200?random=203")
        )
    }

    val postsThumbnails = remember {
        listOf(
            "https://picsum.photos/400/400?random=301",
            "https://picsum.photos/400/400?random=302",
            "https://picsum.photos/400/400?random=303",
            "https://picsum.photos/400/400?random=304",
            "https://picsum.photos/400/400?random=305",
            "https://picsum.photos/400/400?random=306"
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                onRefresh()
                isRefreshing = false
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 1. TOP APP BAR (INSTAGRAM OFFICIAL LIGHT THEME)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Left: '+' Icon
                IconButton(
                    onClick = {
                        try {
                            visualMediaLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } catch (e: Exception) {
                            galleryLauncher.launch("image/*")
                        }
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create", tint = Color.Black, modifier = Modifier.size(28.dp))
                }

                // Center/Left: Username with Chevron and Red Dot
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        Toast.makeText(context, "Switching accounts...", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(
                        text = user.handle.removePrefix("@").ifBlank { "dilshadwarsi42" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                }

                // Right: Threads (@) Icon & Hamburger Menu (≡)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "@",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "Opening Threads profile...", Toast.LENGTH_SHORT).show()
                        }
                    )

                    IconButton(
                        onClick = { showSettingsBottomSheet = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black, modifier = Modifier.size(28.dp))
                    }
                }
            }

            // 2. HEADER SECTION (AVATAR & COUNTERS)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Left: Large Avatar (86dp) with Note bubble & + overlay badge
                Box(modifier = Modifier.size(90.dp)) {
                    // Floating Note Status Bubble Above Avatar
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-2).dp, y = (-8).dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(14.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("Ready for...", fontSize = 10.sp, color = Color(0xFF6E6E73))
                    }

                    // Circular User Avatar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFE5E5EA), CircleShape)
                            .clickable { showPhotoOptionsDialog = true }
                    ) {
                        if (customAvatarBitmap != null) {
                            Image(
                                bitmap = customAvatarBitmap,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF006494))
                            ) {
                                Text(
                                    text = user.name.take(1).uppercase(),
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Bottom-Right Black '+' Badge Overlay
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = 0.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { showPhotoOptionsDialog = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Avatar", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                // Right: 3 Stat Columns (Posts, Followers, Following)
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.weight(1f)
                ) {
                    InstagramStatItem(count = "265", label = "posts")

                    Box(modifier = Modifier.clickable {
                        dialogTitle = "Followers (${user.followerCount})"
                        showFollowersDialog = true
                    }) {
                        InstagramStatItem(count = "${user.followerCount}", label = "followers")
                    }

                    Box(modifier = Modifier.clickable {
                        dialogTitle = "Following (${user.followingCount})"
                        showFollowersDialog = true
                    }) {
                        InstagramStatItem(count = "${user.followingCount}", label = "following")
                    }
                }
            }

            // 3. USER BIO & EXTERNAL LINK PILLS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                // Display Name + Verified Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.name.ifBlank { "Dilshad_The mountain lover" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Category Tag
                Text(
                    text = "Digital creator",
                    fontSize = 13.sp,
                    color = Color(0xFF8E8E93)
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Bio Text
                Text(
                    text = user.bio.ifBlank { "I am traveling buddy .i love to travel 🧳😊" },
                    fontSize = 13.sp,
                    color = Color.Black,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Clickable Oval Link Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Link Pill 1: Threads handle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF2F2F7))
                            .clickable { Toast.makeText(context, "Opening Threads profile...", Toast.LENGTH_SHORT).show() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("@", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(user.handle.removePrefix("@").ifBlank { "dilshadwarsi42" }, fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Link Pill 2: Channel
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF2F2F7))
                            .clickable { Toast.makeText(context, "Opening Samay Par Dhiyan Do...", Toast.LENGTH_SHORT).show() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("▷", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Samay Par Dhiyan Do", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Link Pill 3: Facebook / Page
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF2F2F7))
                            .clickable { Toast.makeText(context, "Opening Travelling Keeda...", Toast.LENGTH_SHORT).show() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Facebook", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1877F2))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Travelling Keeda", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Link Pill 4: + Add link
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF2F2F7))
                            .clickable { Toast.makeText(context, "Add new social link...", Toast.LENGTH_SHORT).show() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = "Add Link", tint = Color(0xFF8E8E93), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Add", fontSize = 12.sp, color = Color(0xFF8E8E93), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. ACTION BUTTONS & PROFESSIONAL DASHBOARD BANNER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Professional Dashboard Light Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F7)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToMonetization() }
                        .padding(bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text("Professional dashboard", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("207 views in the last 30 days.", fontSize = 12.sp, color = Color(0xFF8E8E93))
                    }
                }

                // Two Equal-Width Sleek Buttons Side-by-Side
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { isEditing = !isEditing },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFEFEF)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Text(if (isEditing) "Close edit" else "Edit profile", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val shareUrl = "https://finaldestiny.app/user/${user.handle.removePrefix("@")}"
                            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                            val clipData = android.content.ClipData.newPlainText("Profile Link", shareUrl)
                            clipboardManager?.setPrimaryClip(clipData)

                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, "Check out ${user.name}'s profile on Final Destiny: $shareUrl")
                                type = "text/plain"
                            }
                            val shareIntent = android.content.Intent.createChooser(sendIntent, "Share Profile via")
                            context.startActivity(shareIntent)
                            Toast.makeText(context, "🔗 Profile link copied & Share Sheet launched!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFEFEF)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Text("Share profile", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            if (isEditing) {
                // EDIT PROFILE FORM INLINE CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F7)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text("Edit Profile Details", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Display Name", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0095F6), unfocusedBorderColor = Color.LightGray),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editBio,
                            onValueChange = { editBio = it },
                            label = { Text("Bio", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0095F6), unfocusedBorderColor = Color.LightGray),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val updated = user.copy(name = editName, bio = editBio)
                                onSaveProfile(updated)
                                isEditing = false
                                Toast.makeText(context, "✅ Profile Changes Saved!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. "DISCOVER PEOPLE" SUGGESTION CAROUSEL
            if (showDiscoverPeople) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text("Discover people", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close Discover",
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { showDiscoverPeople = false }
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(sampleDiscoverUsers) { candidate ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .width(135.dp)
                                    .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(8.dp))
                            ) {
                                Box(modifier = Modifier.padding(10.dp)) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color(0xFF8E8E93),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(14.dp)
                                            .clickable { sampleDiscoverUsers.remove(candidate) }
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE5E5EA))
                                        ) {
                                            Text(candidate.name.take(1), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(candidate.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 1)
                                        Text(candidate.mutualsLabel, fontSize = 11.sp, color = Color(0xFF8E8E93), maxLines = 1, overflow = TextOverflow.Ellipsis)

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Button(
                                            onClick = {
                                                candidate.isFollowing = !candidate.isFollowing
                                                Toast.makeText(context, if (candidate.isFollowing) "Followed ${candidate.name}" else "Unfollowed", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (candidate.isFollowing) Color(0xFFEFEFEF) else Color(0xFF3897F0)),
                                            contentPadding = PaddingValues(0.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(30.dp)
                                        ) {
                                            Text(
                                                if (candidate.isFollowing) "Following" else if (candidate.isFollowBack) "Follow back" else "Follow",
                                                color = if (candidate.isFollowing) Color.Black else Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // 6. STORY HIGHLIGHTS TRAY
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // First item: '+' New Highlight Circle
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color(0xFFC7C7CC), CircleShape)
                                .clickable {
                                    try {
                                        visualMediaLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    } catch (e: Exception) {
                                        galleryLauncher.launch("image/*")
                                    }
                                }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "New Highlight", tint = Color.Black, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("New", fontSize = 11.sp, color = Color.Black)
                    }
                }

                items(storyHighlights) { highlight ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color(0xFFC7C7CC), CircleShape)
                                .background(Color(0xFFF2F2F7))
                                .clickable {
                                    Toast.makeText(context, "Opening ${highlight.title}...", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Text(highlight.title.take(1), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(highlight.title, fontSize = 11.sp, color = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7. MEDIA TABS & 3x3 GRID
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = Color(0xFFE5E5EA))
            ) {
                // Tab 0: 3x3 Grid (Posts)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTabState = 0 }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.GridOn,
                        contentDescription = "Grid Posts",
                        tint = if (activeTabState == 0) Color.Black else Color(0xFF8E8E93),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(2.dp)
                            .background(if (activeTabState == 0) Color.Black else Color.Transparent)
                    )
                }

                // Tab 1: Reels Play Icon
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTabState = 1 }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.PlayCircleOutline,
                        contentDescription = "Reels",
                        tint = if (activeTabState == 1) Color.Black else Color(0xFF8E8E93),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(2.dp)
                            .background(if (activeTabState == 1) Color.Black else Color.Transparent)
                    )
                }

                // Tab 2: Tagged / Profile Contact Icon
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTabState = 2 }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.PersonPin,
                        contentDescription = "Tagged Posts",
                        tint = if (activeTabState == 2) Color.Black else Color(0xFF8E8E93),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(2.dp)
                            .background(if (activeTabState == 2) Color.Black else Color.Transparent)
                    )
                }
            }

            // 3x3 Edge-to-Edge Media Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            ) {
                items(postsThumbnails) { url ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(Color(0xFFE5E5EA))
                            .clickable { onNavigateToSecondaryFeed() }
                    ) {
                        Icon(
                            imageVector = when (activeTabState) {
                                1 -> Icons.Default.Movie
                                2 -> Icons.Default.Bookmark
                                else -> Icons.Default.PhotoLibrary
                            },
                            contentDescription = "Media Item",
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

    // SETTINGS & PRIVACY BOTTOM SHEET / MODAL
    if (showSettingsBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsBottomSheet = false },
            containerColor = Color.White
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Settings & Privacy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                HorizontalDivider(color = Color(0xFFE5E5EA))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showSettingsBottomSheet = false
                            showComplaintModal = true
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Feedback, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Help & Complaint Support", color = Color.Black, fontSize = 14.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showSettingsBottomSheet = false
                            showPrivacyPolicyModal = true
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Policy, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Privacy Policy & Terms", color = Color.Black, fontSize = 14.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showSettingsBottomSheet = false
                            onLogOut()
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Log Out from Account", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }

    // FEEDBACK & COMPLAINT BOX DIALOG
    if (showComplaintModal) {
        AlertDialog(
            onDismissRequest = { showComplaintModal = false },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Feedback, contentDescription = null, tint = Color(0xFF3897F0))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📝 Complaint Box & Support", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Complaint Category:", fontSize = 11.sp, color = Color.Gray)
                    val categories = listOf("Billing & Payment", "User Behavior / Report", "App Bug / Technical", "Host Monetization")
                    categories.forEach { cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (complaintCategory == cat) Color(0xFFF2F2F7) else Color.Transparent)
                                .clickable { complaintCategory = cat }
                                .padding(6.dp)
                        ) {
                            RadioButton(
                                selected = (complaintCategory == cat),
                                onClick = { complaintCategory = cat },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3897F0))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(cat, fontSize = 12.sp, color = Color.Black)
                        }
                    }

                    OutlinedTextField(
                        value = complaintDescription,
                        onValueChange = { complaintDescription = it },
                        placeholder = { Text("Describe your issue or report details...", fontSize = 11.sp, color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF3897F0), unfocusedBorderColor = Color.LightGray),
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0))
                ) {
                    Text("Submit Ticket 📩", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showComplaintModal = false }) {
                    Text("Close", color = Color.Gray)
                }
            }
        )
    }

    // PRIVACY POLICY & ABOUT APP DIALOG
    if (showPrivacyPolicyModal) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyModal = false },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Policy, contentDescription = null, tint = Color(0xFF3897F0))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📜 Privacy Policy & App Info", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("App Name: Final Destiny", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Founder & Creator: DarkDevil", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("Version: 2.4.0 (Official Build)", fontSize = 11.sp, color = Color.Gray)
                    HorizontalDivider(color = Color(0xFFE5E5EA))

                    Text("1. Data Protection & Privacy", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Final Destiny is committed to safeguarding user personal information. All media uploaded are protected and encrypted.", fontSize = 10.sp, color = Color.Gray)

                    Text("2. Community Guidelines & Safety", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Zero tolerance for harassment, hate speech, or explicit content. Users violating guidelines face immediate account suspension.", fontSize = 10.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyPolicyModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0))
                ) {
                    Text("I Understand & Agree", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // PHOTO / SELFIE UPLOADER MODAL DIALOG
    if (showPhotoOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoOptionsDialog = false },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF3897F0))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📷 Profile Photo & Selfie Verification", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Upload a profile photo or take a live selfie to verify your profile automatically 🛡️:",
                        fontSize = 12.sp,
                        color = Color.Gray
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take Live Selfie 📷 (Auto-Verify)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose from Gallery 🖼️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoOptionsDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // FOLLOWERS / FOLLOWING LIST MODAL DIALOG
    if (showFollowersDialog) {
        AlertDialog(
            onDismissRequest = { showFollowersDialog = false },
            containerColor = Color.White,
            title = { Text(dialogTitle, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val mockList = listOf(
                        "Ananya Roy (@Ananya_Roy)" to "Following",
                        "Aarav Sharma (@Aarav_Sharma)" to "Following",
                        "Simran Kaur (@Simran_Vibes)" to "Following",
                        "Vikram Malhotra (@Vikram_M)" to "Following"
                    )
                    mockList.forEach { (nameHandle, status) ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF2F2F7))
                                .padding(8.dp)
                        ) {
                            Text(nameHandle, fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
                            Text(status, fontSize = 10.sp, color = Color(0xFF3897F0), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFollowersDialog = false }) {
                    Text("Close", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun InstagramStatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(text = label, fontSize = 12.sp, color = Color(0xFF6E6E73))
    }
}

fun saveBitmapToCacheUri(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val file = File(context.cacheBufferDir(), "selfie_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            stream.flush()
        }
        Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun Context.cacheBufferDir(): File {
    val dir = File(cacheDir, "profile_photos")
    if (!dir.exists()) dir.mkdirs()
    return dir
}

@Composable
fun rememberProfileLoadedImage(context: Context, uriString: String?): ImageBitmap? {
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
