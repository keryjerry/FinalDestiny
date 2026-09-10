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
import androidx.compose.material.icons.filled.Settings
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
import coil.request.videoFrameMillis
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
    userPosts: List<com.devil.finaldestiny.model.MomentPost> = emptyList(),
    savedAccounts: List<UserProfile> = emptyList(),
    onSaveProfile: (UserProfile) -> Unit,
    onNavigateToStore: () -> Unit = {},
    onNavigateToSecondaryFeed: () -> Unit = {},
    onNavigateToMonetization: () -> Unit = {},
    onNavigateToCreatorHub: () -> Unit = {},
    onNavigateToCreatorTools: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToDating: () -> Unit = {},
    onNavigateToAboutUs: () -> Unit = {},
    onLogOut: () -> Unit,
    onRefresh: suspend () -> Unit = {},
    onToggleFollowCandidate: (String, Boolean) -> Unit = { _, _ -> },
    onSwitchAccount: (String) -> Unit = {},
    onAddAccount: (String, String) -> Unit = { _, _ -> },
    onRemoveAccount: (String) -> Unit = {},
    onOpenMediaPicker: (() -> Unit)? = null,
    onNavigateToReelViewer: (Int) -> Unit = {},
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
    var showAccountSwitcherBottomSheet by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var newAccEmail by remember { mutableStateOf("") }
    var newAccName by remember { mutableStateOf("") }
    var showDiscoverPeople by remember { mutableStateOf(true) }
    var dialogTitle by remember { mutableStateOf("Followers List") }

    var showCreateTravelPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistNameInput by remember { mutableStateOf("") }
    val travelPlaylists = remember {
        mutableStateListOf(
            StoryHighlightItem("h1", "Darjeeling 🏔️", "https://picsum.photos/200/200?random=50"),
            StoryHighlightItem("h2", "Kalimpong 🌿", "https://picsum.photos/200/200?random=51"),
            StoryHighlightItem("h3", "Mumbai 🏙️", "https://picsum.photos/200/200?random=52")
        )
    }

    var activeTabState by remember { mutableIntStateOf(0) } // 0: Grid, 1: Reels, 2: Tagged/Saved

    var editName by remember(user) { mutableStateOf(user.name.ifBlank { "Dilshad_The mountain lover" }) }
    var editBio by remember(user) { mutableStateOf(user.bio.ifBlank { "I am traveling buddy .i love to travel 🧳😊" }) }
    var editCategory by remember(user) { mutableStateOf(user.creatorCategory) }
    var editDisplayCategory by remember(user) { mutableStateOf(user.displayCategoryOnProfile) }
    var editAccountType by remember(user) { mutableStateOf(user.accountType) }

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

    val gridPosts = remember(userPosts) {
        userPosts.filter { it.mediaType != com.devil.finaldestiny.model.MediaType.REEL_VIDEO }
    }
    val reelPosts = remember(userPosts) {
        userPosts.filter { it.mediaType == com.devil.finaldestiny.model.MediaType.REEL_VIDEO }
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
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp)
        ) {
            // 1. TOP APP BAR (INSTAGRAM OFFICIAL LIGHT THEME)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Left: Back Arrow (Pop back to Primary Dashboard) & '+' Create Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (onOpenMediaPicker != null) {
                                onOpenMediaPicker()
                            } else {
                                try {
                                    visualMediaLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                } catch (e: Exception) {
                                    galleryLauncher.launch("image/*")
                                }
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create", tint = Color.Black, modifier = Modifier.size(28.dp))
                    }
                }

                // Center/Left: Username with Chevron and Red Dot (Opens Multi-Account Switcher)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        showAccountSwitcherBottomSheet = true
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
                            .clickable {
                                try {
                                    visualMediaLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                } catch (e: Exception) {
                                    galleryLauncher.launch("image/*")
                                }
                            }
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
                            .clickable {
                                try {
                                    visualMediaLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                } catch (e: Exception) {
                                    galleryLauncher.launch("image/*")
                                }
                            }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Avatar", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                // Right: 3 Stat Columns (Posts, Followers, Following)
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.weight(1f)
                ) {
                    InstagramStatItem(count = "${userPosts.size}", label = "posts")

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
                if (user.displayCategoryOnProfile && user.creatorCategory.isNotBlank()) {
                    Text(
                        text = user.creatorCategory,
                        fontSize = 13.sp,
                        color = Color(0xFF8E8E93)
                    )
                }

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
                // Professional Dashboard vs Personal Account Banner
                if (user.accountType == "Creator" || user.accountType == "Professional") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F7)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToCreatorHub() }
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text("Professional dashboard", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("207 views in the last 30 days.", fontSize = 12.sp, color = Color(0xFF8E8E93))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = { onNavigateToCreatorTools() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Tools", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val updated = user.copy(accountType = "Creator")
                                onSaveProfile(updated)
                                Toast.makeText(context, "✨ Switched to Professional Creator Account!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text("Switch to Professional Account", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                Text("Get insights, category badge, and creator tools.", fontSize = 11.sp, color = Color(0xFF15803D))
                            }
                            Text("Switch >", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                        }
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

                        // Category Selector
                        Text("Category", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val categories = listOf("Digital creator", "AI Creator", "Digital Marketer", "Video Creator", "Gamer", "Entrepreneur")
                            items(categories) { cat ->
                                FilterChip(
                                    selected = (editCategory == cat),
                                    onClick = { editCategory = if (editCategory == cat) "" else cat },
                                    label = { Text(cat, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF3897F0),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Display Category Toggle
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Display category on profile", fontSize = 13.sp, color = Color.Black)
                            Switch(
                                checked = editDisplayCategory,
                                onCheckedChange = { editDisplayCategory = it }
                            )
                        }

                        // Account Type Switcher
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Account Type", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Current: $editAccountType", fontSize = 12.sp, color = Color.Gray)
                            }
                            OutlinedButton(
                                onClick = {
                                    editAccountType = if (editAccountType == "Personal") "Creator" else "Personal"
                                }
                            ) {
                                Text(if (editAccountType == "Personal") "Switch to Creator" else "Switch to Personal", fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                val updated = user.copy(
                                    name = editName,
                                    bio = editBio,
                                    creatorCategory = editCategory,
                                    displayCategoryOnProfile = editDisplayCategory,
                                    accountType = editAccountType
                                )
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
                                                val nextState = !candidate.isFollowing
                                                candidate.isFollowing = nextState
                                                onToggleFollowCandidate(candidate.id, nextState)
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

            // 6. STORY HIGHLIGHTS TRAY (TRAVEL PLAYLISTS)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // First item: '+' New Highlight / Travel Playlist Launcher
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color(0xFFC7C7CC), CircleShape)
                                .clickable { showCreateTravelPlaylistDialog = true }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "New Playlist", tint = Color.Black, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("New", fontSize = 11.sp, color = Color.Black)
                    }
                }

                items(travelPlaylists) { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "Opening ${item.title} Playlist ✈️", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color(0xFFE5E5EA), CircleShape)
                        ) {
                            val bitmap = rememberProfileLoadedImage(context, item.coverUrl)
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize().background(Color(0xFFF1F5F9))
                                ) {
                                    Text(item.title.take(1), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.title, fontSize = 11.sp, color = Color.Black, maxLines = 1)
                    }
                }
            }

            if (showCreateTravelPlaylistDialog) {
                AlertDialog(
                    onDismissRequest = { showCreateTravelPlaylistDialog = false },
                    title = { Text("Create Travel Playlist ✈️", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Name your trip or travel collection (e.g. Darjeeling, Kalimpong, Mumbai):", fontSize = 13.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = newPlaylistNameInput,
                                onValueChange = { newPlaylistNameInput = it },
                                placeholder = { Text("Enter trip title...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newPlaylistNameInput.isNotBlank()) {
                                    travelPlaylists.add(
                                        StoryHighlightItem(
                                            id = "hl_${System.currentTimeMillis()}",
                                            title = newPlaylistNameInput.trim(),
                                            coverUrl = "https://picsum.photos/200/200?random=${(100..999).random()}"
                                        )
                                    )
                                    newPlaylistNameInput = ""
                                    showCreateTravelPlaylistDialog = false
                                    Toast.makeText(context, "✈️ Travel Playlist Created!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0))
                        ) {
                            Text("Save", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCreateTravelPlaylistDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7. MEDIA TABS & DYNAMIC GRID / EMPTY STATES
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

            // Tab Content Body: Dynamic Media Grid or Clean Instagram Empty State
            when (activeTabState) {
                0 -> {
                    if (gridPosts.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(1.dp),
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 600.dp)
                        ) {
                            items(gridPosts) { post ->
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .background(Color(0xFFE5E5EA))
                                        .clickable { onNavigateToSecondaryFeed() }
                                ) {
                                    val bitmap = rememberProfileLoadedImage(context, post.mediaUrl)
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = post.caption,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.PhotoLibrary,
                                            contentDescription = "Media Item",
                                            tint = Color(0xFF8E8E93),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp, horizontal = 24.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.Black, CircleShape)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No Posts Yet", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "When you share photos, they will appear on your profile.",
                                fontSize = 13.sp,
                                color = Color(0xFF8E8E93),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                1 -> {
                    if (reelPosts.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(1.dp),
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 600.dp)
                        ) {
                            items(reelPosts) { reel ->
                                val bitmap = rememberProfileLoadedImage(context, reel.mediaUrl)
                                val videoFrameBitmap = rememberVideoFrameBitmap(context, reel.mediaUrl)
                                val realViews = reel.viewsCount.toLong()
                                val displayViews = when {
                                    realViews >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", realViews / 1_000_000.0)
                                    realViews >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", realViews / 1_000.0)
                                    else -> realViews.toString()
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .background(Color(0xFF1C1C1E))
                                        .clickable {
                                            val index = reelPosts.indexOfFirst { r -> r.id == reel.id }.coerceAtLeast(0)
                                            onNavigateToReelViewer(index)
                                        }
                                ) {
                                    if (!reel.mediaUrl.isNullOrBlank()) {
                                        coil.compose.AsyncImage(
                                            model = coil.request.ImageRequest.Builder(LocalContext.current)
                                                .data(reel.mediaUrl)
                                                .decoderFactory(coil.decode.VideoFrameDecoder.Factory())
                                                .videoFrameMillis(1000)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Video Thumbnail",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = reel.caption,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else if (videoFrameBitmap != null) {
                                        Image(
                                            bitmap = videoFrameBitmap,
                                            contentDescription = reel.caption,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Movie,
                                            contentDescription = "Reel Item",
                                            tint = Color(0xFF8E8E93),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    // Dark bottom gradient overlay for view count contrast
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp)
                                            .align(Alignment.BottomCenter)
                                            .background(
                                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(0.75f))
                                                )
                                            )
                                    )

                                    // Bottom-Left Overlay: White Play Icon (▶) + Dynamic Database Views Count
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(start = 6.dp, bottom = 6.dp)
                                    ) {
                                        Text(
                                            text = "▶",
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = displayViews,
                                            fontSize = 11.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp, horizontal = 24.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.Black, CircleShape)
                            ) {
                                Icon(Icons.Default.Movie, contentDescription = null, tint = Color.Black, modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No Reels Yet", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Share your creative videos with the world.",
                                fontSize = 13.sp,
                                color = Color(0xFF8E8E93),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                2 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp, horizontal = 24.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Black, CircleShape)
                        ) {
                            Icon(Icons.Default.PersonPin, contentDescription = null, tint = Color.Black, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Photos and videos of you", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "When people tag you in photos and videos, they'll appear here.",
                            fontSize = 13.sp,
                            color = Color(0xFF8E8E93),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // MULTI-ACCOUNT SWITCHER BOTTOM SHEET
    if (showAccountSwitcherBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAccountSwitcherBottomSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Switch accounts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val displayAccounts = if (savedAccounts.isEmpty()) listOf(user) else savedAccounts

                displayAccounts.forEach { acc ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                showAccountSwitcherBottomSheet = false
                                if (acc.id != user.id) {
                                    onSwitchAccount(acc.id)
                                }
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF006494))
                            ) {
                                Text(
                                    acc.name.take(1).uppercase(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = acc.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = acc.handle,
                                    fontSize = 12.sp,
                                    color = Color(0xFF8E8E93)
                                )
                            }
                        }

                        if (acc.id == user.id) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active Account",
                                tint = Color(0xFF3897F0),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE5E5EA))

                // Add Account Action Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showAccountSwitcherBottomSheet = false
                            showAddAccountDialog = true
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFE5E5EA), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add account",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Add Instagram account",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3897F0)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // ADD ACCOUNT DIALOG
    if (showAddAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAddAccountDialog = false },
            containerColor = Color.White,
            title = {
                Text("➕ Add Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter new account email & name:", fontSize = 12.sp, color = Color.Gray)

                    OutlinedTextField(
                        value = newAccEmail,
                        onValueChange = { newAccEmail = it },
                        label = { Text("Account Email", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF3897F0), unfocusedBorderColor = Color.LightGray),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newAccName,
                        onValueChange = { newAccName = it },
                        label = { Text("Display Name (Optional)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF3897F0), unfocusedBorderColor = Color.LightGray),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAccEmail.isNotBlank()) {
                            onAddAccount(newAccEmail, newAccName)
                            showAddAccountDialog = false
                            newAccEmail = ""
                            newAccName = ""
                            Toast.makeText(context, "Account added successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0))
                ) {
                    Text("Log In / Add", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAccountDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
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
                            onNavigateToSettings()
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF3897F0))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Settings and activity", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

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
    var imageBitmap by remember(uriString) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(uriString) {
        if (uriString.isNullOrBlank()) {
            imageBitmap = null
            return@LaunchedEffect
        }
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
                    val url = java.net.URL(uriString)
                    val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
                        connectTimeout = 5000
                        readTimeout = 5000
                    }
                    val inputStream = connection.inputStream
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imageBitmap = bitmap?.asImageBitmap()
                } else {
                    val uri = Uri.parse(uriString)
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imageBitmap = bitmap?.asImageBitmap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    return imageBitmap
}

@Composable
fun rememberVideoFrameBitmap(context: Context, videoUrl: String?): ImageBitmap? {
    var frameBitmap by remember(videoUrl) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(videoUrl) {
        if (!videoUrl.isNullOrBlank() && (videoUrl.contains("video", ignoreCase = true) || videoUrl.endsWith(".mp4", ignoreCase = true))) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val retriever = android.media.MediaMetadataRetriever()
                    retriever.setDataSource(videoUrl, HashMap<String, String>())
                    val bitmap = retriever.getFrameAtTime(1000000, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    retriever.release()
                    bitmap?.let {
                        frameBitmap = it.asImageBitmap()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    return frameBitmap
}
