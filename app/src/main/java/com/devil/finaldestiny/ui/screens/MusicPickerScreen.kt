package com.devil.finaldestiny.ui.screens

import android.net.Uri
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.devil.finaldestiny.data.GlobalMusicRepository
import com.devil.finaldestiny.model.AudioTrack
import com.devil.finaldestiny.ui.components.ProfileAvatarView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPickerScreen(
    onSelectTrack: (AudioTrack) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var tracksList by remember { mutableStateOf<List<AudioTrack>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("Trending 🔥") }
    var playingTrackId by remember { mutableStateOf<String?>(null) }

    // Lightweight ExoPlayer for audio preview playback
    val previewPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(previewPlayer) {
        onDispose {
            previewPlayer.stop()
            previewPlayer.release()
        }
    }

    // Search / Filter Trigger
    LaunchedEffect(searchQuery, selectedCategory) {
        isLoading = true
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val queryToUse = if (searchQuery.isNotBlank()) searchQuery else selectedCategory.removeSuffix(" 🔥").removeSuffix(" 🎬").removeSuffix(" 🎵").removeSuffix(" 🎧").removeSuffix(" ☕").removeSuffix(" ❤️")
            val results = GlobalMusicRepository.searchGlobalMusic(queryToUse)
            tracksList = results
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🎵 Global Music Library",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        previewPlayer.stop()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E2638))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Bollywood, Punjabi, Singer, Artist...", color = Color.Gray, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedBorderColor = Color(0xFF3897F0),
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips Row
            val categories = listOf("Trending 🔥", "Bollywood 🎬", "Punjabi 🎵", "Hollywood 🎧", "Lo-Fi ☕", "Romance ❤️")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = (selectedCategory == cat && searchQuery.isBlank())
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            searchQuery = ""
                            selectedCategory = cat
                        },
                        label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF3897F0),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color.LightGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (searchQuery.isNotBlank()) "RESULTS FOR \"$searchQuery\"" else "TOP HITS & TRENDING AUDIO",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Track List / Loading Indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF3897F0))
                }
            } else if (tracksList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tracks found", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(tracksList, key = { it.id }) { track ->
                        val isPlayingThis = (playingTrackId == track.id)

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    ProfileAvatarView(
                                        name = track.title,
                                        profilePictureUri = track.albumCoverUrl,
                                        size = 46.dp,
                                        showBorder = false
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = track.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${track.artist} • ${track.duration}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF94A3B8),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Live Audio Preview Play/Pause Toggle
                                    IconButton(
                                        onClick = {
                                            if (isPlayingThis) {
                                                previewPlayer.stop()
                                                playingTrackId = null
                                            } else {
                                                if (!track.audioUrl.isNullOrBlank()) {
                                                    previewPlayer.stop()
                                                    val mediaItem = MediaItem.fromUri(Uri.parse(track.audioUrl))
                                                    previewPlayer.setMediaItem(mediaItem)
                                                    previewPlayer.prepare()
                                                    previewPlayer.playWhenReady = true
                                                    playingTrackId = track.id
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF334155))
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Preview Audio",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Use / Select Track Button
                                    Button(
                                        onClick = {
                                            previewPlayer.stop()
                                            onSelectTrack(track)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0)),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Use", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
