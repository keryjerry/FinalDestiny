package com.devil.finaldestiny.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.data.SupabaseAuthClient
import com.devil.finaldestiny.model.MomentPost
import com.devil.finaldestiny.model.SearchResultUser
import com.devil.finaldestiny.ui.components.ProfileAvatarView
import com.devil.finaldestiny.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchExploreScreen(
    explorePosts: List<MomentPost> = emptyList(),
    onSelectPost: (MomentPost) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<SearchResultUser>>(emptyList()) }
    var isSearchLoading by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    // Backup / Seed Creators for Smart AI Fuzzy Search Fallback
    val localCreatorsDatabase = remember {
        listOf(
            SearchResultUser("u201", "Ananya Roy", "@Ananya_Roy", null, "Fashion • Mumbai", isVerified = true),
            SearchResultUser("u202", "Aarav Sharma", "@Aarav_Sharma", null, "Music • Delhi", isVerified = true),
            SearchResultUser("u203", "Simran Kaur", "@Simran_Vibes", null, "Vocalist • Chandigarh", isVerified = true),
            SearchResultUser("u204", "Vikram Malhotra", "@Vikram_M", null, "Fitness • Bengaluru", isVerified = true),
            SearchResultUser("u205", "Riya Kapoor", "@Riya_Kapoor", null, "Dance • Mumbai", isVerified = true),
            SearchResultUser("u206", "Kabir Verma", "@Kabir_V", null, "Startup • Pune", isVerified = true),
            SearchResultUser("u207", "Priya Singh", "@Priya_Singh", null, "Photography • Jaipur", isVerified = true),
            SearchResultUser("u208", "Rohan Mehta", "@Rohan_Mehta", null, "Films • Hyderabad", isVerified = true),
            SearchResultUser("u209", "Sneha Patel", "@Sneha_P", null, "Baking • Ahmedabad", isVerified = true),
            SearchResultUser("u210", "Devansh Singhania", "@Devansh_S", null, "Finance • Mumbai", isVerified = true)
        )
    }

    // 300ms Debounced AI Search Trigger
    LaunchedEffect(searchQuery) {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            searchResults = emptyList()
            isSearchLoading = false
        } else {
            isSearchLoading = true
            delay(300) // 300ms Debounce
            try {
                val jsonResponse = SupabaseAuthClient.executeSmartAiSearch(query)
                if (jsonResponse.isNotBlank() && jsonResponse.startsWith("[")) {
                    val parsedList = mutableListOf<SearchResultUser>()
                    val jsonArray = JSONArray(jsonResponse)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        parsedList.add(
                            SearchResultUser(
                                id = obj.optString("id", "u_$i"),
                                name = obj.optString("name", "Creator"),
                                handle = obj.optString("handle", "@creator"),
                                avatarUrl = obj.optString("avatar_url", null),
                                matchChip = obj.optString("match_chip", "Creator • India"),
                                isVerified = obj.optBoolean("is_verified", true)
                            )
                        )
                    }
                    searchResults = parsedList
                } else {
                    // Client-Side Smart Fuzzy Search Fallback across Name, Handle, Bio, City, Skill
                    val q = query.lowercase()
                    searchResults = localCreatorsDatabase.filter {
                        it.name.lowercase().contains(q) ||
                        it.handle.lowercase().contains(q) ||
                        it.matchChip.lowercase().contains(q)
                    }
                }
            } catch (e: Exception) {
                val q = query.lowercase()
                searchResults = localCreatorsDatabase.filter {
                    it.name.lowercase().contains(q) ||
                    it.handle.lowercase().contains(q) ||
                    it.matchChip.lowercase().contains(q)
                }
            } finally {
                isSearchLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlueBgLight)
    ) {
        // TOP HEADER BAR WITH UNIVERSAL BACK ARROW ('<') & GOOGLE-STYLE AI SEARCH PILL
        Surface(
            color = SkyBlueCardBg,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Top-Left Back Navigation Arrow ('<')
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

                Spacer(modifier = Modifier.width(8.dp))

                // Full-width Google-Style Expanding Search Pill
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(SkyBlueBgLight)
                        .border(
                            width = 1.5.dp,
                            brush = Brush.horizontalGradient(listOf(BrightCyanAccent, SkyBluePrimary)),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .padding(horizontal = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("✨", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                isSearching = it.isNotEmpty()
                            },
                            placeholder = {
                                Text(
                                    text = "✨ AI Search creators, skills, vibes...",
                                    color = SlateTextSecondary,
                                    fontSize = 12.sp
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = NavyTextPrimary,
                                unfocusedTextColor = NavyTextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search",
                                    tint = SlateTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // BODY CONTENT: LIVE AI SEARCH RESULTS vs EDGE-TO-EDGE MEDIA EXPLORE GRID
        if (searchQuery.isNotBlank()) {
            if (isSearchLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = SkyBluePrimary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("✨ AI Engine Scanning Creators & Vibes...", fontSize = 12.sp, color = SkyBluePrimary, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (searchResults.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "No creator profiles matching \"$searchQuery\"",
                        color = SlateTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = "✨ AI Matched Creators (${searchResults.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(searchResults) { user ->
                        var isFollowingUser by remember { mutableStateOf(user.isFollowing) }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SkyBlueCardBg),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SkyBlueBorder, RoundedCornerShape(14.dp))
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ProfileAvatarView(
                                        name = user.name,
                                        profilePictureUri = user.avatarUrl,
                                        size = 44.dp,
                                        showBorder = true,
                                        borderColor = SkyBluePrimary
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(user.name, fontWeight = FontWeight.Bold, color = NavyTextPrimary, fontSize = 14.sp)
                                            if (user.isVerified) {
                                                Text(" ✓", fontSize = 11.sp, color = VerifiedBlue, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text(user.handle, fontSize = 11.sp, color = SlateTextSecondary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Surface(
                                            color = SkyBlueHeader,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = user.matchChip,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SkyBluePrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        isFollowingUser = !isFollowingUser
                                        Toast.makeText(context, if (isFollowingUser) "❤️ Following ${user.name}" else "Unfollowed ${user.name}", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFollowingUser) SkyBlueHeader else SkyBluePrimary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = if (isFollowingUser) "✓ Following" else "+ Follow",
                                        fontSize = 11.sp,
                                        color = if (isFollowingUser) NavyTextPrimary else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // IDLE STATE: EDGE-TO-EDGE MEDIA EXPLORE GRID (THUMBNAILS OF POSTS / REELS)
            Column(modifier = Modifier.fillMaxSize()) {
                PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                Text(
                    text = "🔥 Trending Explore & Reels",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyTextPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(1.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(explorePosts) { post ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .background(NavyTextPrimary)
                                .clickable { onSelectPost(post) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Explore Reel Thumbnail",
                                tint = Color.White.copy(0.8f),
                                modifier = Modifier.size(36.dp)
                            )

                            Surface(
                                color = Color.Black.copy(0.5f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = "👁️ ${post.viewsCount}",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
