package com.devil.finaldestiny.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.data.GlobalMusicRepository
import com.devil.finaldestiny.model.AudioTrack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

data class PhotoFilterItem(
    val name: String,
    val colorMatrix: ColorMatrix?
)

fun generateAiColorMatrixFromPrompt(prompt: String): ColorMatrix {
    val lower = prompt.lowercase()
    var rMult = 1.0f
    var gMult = 1.0f
    var bMult = 1.0f
    var rOffset = 0f
    var gOffset = 0f
    var bOffset = 0f

    if (lower.contains("warm") || lower.contains("gold") || lower.contains("sunset") || lower.contains("amber") || lower.contains("sun")) {
        rMult += 0.22f; gMult += 0.10f; bMult -= 0.15f
        rOffset += 20f; gOffset += 10f; bOffset -= 12f
    }
    if (lower.contains("cold") || lower.contains("cool") || lower.contains("blue") || lower.contains("ice") || lower.contains("winter")) {
        rMult -= 0.15f; gMult += 0.05f; bMult += 0.28f
        rOffset -= 12f; bOffset += 22f
    }
    if (lower.contains("green") || lower.contains("emerald") || lower.contains("forest") || lower.contains("nature")) {
        gMult += 0.25f; rMult -= 0.10f; bMult -= 0.08f
        gOffset += 22f
    }
    if (lower.contains("neon") || lower.contains("cyber") || lower.contains("magenta") || lower.contains("pink") || lower.contains("purple")) {
        rMult += 0.28f; bMult += 0.32f; gMult -= 0.15f
        rOffset += 25f; bOffset += 28f; gOffset -= 12f
    }
    if (lower.contains("vintage") || lower.contains("retro") || lower.contains("film") || lower.contains("classic") || lower.contains("35mm")) {
        rMult *= 0.95f; gMult *= 0.90f; bMult *= 0.85f
        rOffset += 14f; gOffset += 12f; bOffset += 18f
    }
    if (lower.contains("bright") || lower.contains("glow") || lower.contains("pastel") || lower.contains("soft") || lower.contains("light")) {
        rMult += 0.10f; gMult += 0.10f; bMult += 0.10f
        rOffset += 18f; gOffset += 18f; bOffset += 18f
    }
    if (lower.contains("dark") || lower.contains("moody") || lower.contains("shadow") || lower.contains("night") || lower.contains("gothic")) {
        rMult *= 0.82f; gMult *= 0.82f; bMult *= 0.88f
        rOffset -= 12f; gOffset -= 12f; bOffset -= 6f
    }
    if (lower.contains("bw") || lower.contains("black") || lower.contains("monochrome") || lower.contains("noir")) {
        return ColorMatrix(floatArrayOf(
            0.30f, 0.59f, 0.11f, 0f, -5f,
            0.30f, 0.59f, 0.11f, 0f, -5f,
            0.30f, 0.59f, 0.11f, 0f, -5f,
            0.00f, 0.00f, 0.00f, 1f, 0f
        ))
    }

    if (rMult == 1.0f && gMult == 1.0f && bMult == 1.0f) {
        rMult = 1.15f; gMult = 1.08f; bMult = 1.05f
        rOffset = 12f; gOffset = 6f
    }

    return ColorMatrix(floatArrayOf(
        rMult, 0.00f, 0.00f, 0f, rOffset,
        0.00f, gMult, 0.00f, 0f, gOffset,
        0.00f, 0.00f, bMult, 0f, bOffset,
        0.00f, 0.00f, 0.00f, 1f, 0f
    ))
}

val samplePhotoFilters = listOf(
    PhotoFilterItem("Original", null),
    PhotoFilterItem("✨ Final Destiny AI", null),
    PhotoFilterItem("Royal Velvet", ColorMatrix(floatArrayOf(
        1.15f, 0.05f, 0.00f, 0f, 10f,
        0.05f, 1.05f, 0.00f, 0f, 5f,
        0.00f, 0.10f, 1.25f, 0f, 15f,
        0.00f, 0.00f, 0.00f, 1f, 0f
    ))),
    PhotoFilterItem("Golden Hour", ColorMatrix(floatArrayOf(
        1.25f, 0.10f, 0.00f, 0f, 20f,
        0.10f, 1.10f, 0.00f, 0f, 10f,
        0.00f, 0.00f, 0.80f, 0f, -10f,
        0.00f, 0.00f, 0.00f, 1f, 0f
    ))),
    PhotoFilterItem("Cinema Noir", ColorMatrix(floatArrayOf(
        0.30f, 0.59f, 0.11f, 0f, -5f,
        0.30f, 0.59f, 0.11f, 0f, -5f,
        0.30f, 0.59f, 0.11f, 0f, -5f,
        0.00f, 0.00f, 0.00f, 1f, 0f
    ))),
    PhotoFilterItem("Vintage Film", ColorMatrix(floatArrayOf(
        0.95f, 0.05f, 0.05f, 0f, 12f,
        0.05f, 0.90f, 0.05f, 0f, 8f,
        0.05f, 0.05f, 0.85f, 0f, 15f,
        0.00f, 0.00f, 0.00f, 1f, 0f
    ))),
    PhotoFilterItem("Cyberpunk 2077", ColorMatrix(floatArrayOf(
        1.20f, 0.00f, 0.25f, 0f, 20f,
        0.00f, 0.85f, 0.15f, 0f, -10f,
        0.20f, 0.00f, 1.40f, 0f, 30f,
        0.00f, 0.00f, 0.00f, 1f, 0f
    ))),
    PhotoFilterItem("Emerald Moody", ColorMatrix(floatArrayOf(
        0.85f, 0.10f, 0.05f, 0f, -5f,
        0.05f, 1.15f, 0.10f, 0f, 15f,
        0.10f, 0.10f, 0.90f, 0f, 5f,
        0.00f, 0.00f, 0.00f, 1f, 0f
    ))),
    PhotoFilterItem("Champagne Lux", ColorMatrix(floatArrayOf(
        1.08f, 0.02f, 0.02f, 0f, 18f,
        0.02f, 1.06f, 0.02f, 0f, 16f,
        0.02f, 0.02f, 1.04f, 0f, 20f,
        0.00f, 0.00f, 0.00f, 1f, 0f
    ))),
    PhotoFilterItem("Miami Sunset", ColorMatrix(floatArrayOf(
        1.30f, 0.10f, 0.05f, 0f, 25f,
        0.05f, 1.00f, 0.10f, 0f, 5f,
        0.05f, 0.05f, 1.15f, 0f, 15f,
        0.00f, 0.00f, 0.00f, 1f, 0f
    ))),
    PhotoFilterItem("Dark Knight", ColorMatrix(floatArrayOf(
        0.80f, 0.10f, 0.10f, 0f, -10f,
        0.10f, 0.85f, 0.10f, 0f, -10f,
        0.15f, 0.15f, 1.10f, 0f, 10f,
        0.00f, 0.00f, 0.00f, 1f, 0f
    ))),
    PhotoFilterItem("Monaco Sun", ColorMatrix(floatArrayOf(
        1.20f, 0.15f, 0.00f, 0f, 15f,
        0.05f, 1.10f, 0.05f, 0f, 10f,
        0.00f, 0.05f, 0.85f, 0f, -5f,
        0.00f, 0.00f, 0.00f, 1f, 0f
    ))),
    PhotoFilterItem("Soft Porcelain", ColorMatrix(floatArrayOf(
        1.05f, 0.05f, 0.02f, 0f, 12f,
        0.02f, 1.05f, 0.02f, 0f, 10f,
        0.02f, 0.02f, 1.05f, 0f, 12f,
        0.00f, 0.00f, 0.00f, 1f, 0f
    )))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstagramNewPostScreen(
    mediaUri: String?,
    isReel: Boolean,
    user: com.devil.finaldestiny.model.UserProfile = com.devil.finaldestiny.model.UserProfile(),
    onBack: () -> Unit,
    onPublish: (
        caption: String,
        mediaUri: String?,
        audioTitle: String?,
        audioArtist: String?,
        audioUrl: String?,
        isAiGenerated: Boolean,
        commentsDisabled: Boolean,
        hideLikes: Boolean,
        hideShares: Boolean,
        scheduledAt: String?,
        altText: String?,
        appliedFilter: String?,
        overlayText: String?,
        ctaLink: String?,
        ctaLabel: String?,
        isPaidPartnership: Boolean,
        promotionStatus: String,
        promotionBudget: Double
    ) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var captionText by remember { mutableStateOf("") }
    var selectedAudioTrack by remember { mutableStateOf<AudioTrack?>(null) }
    var selectedLocation by remember { mutableStateOf("") }
    var isAiLabelEnabled by remember { mutableStateOf(false) }
    var isOnlyPostToProfile by remember { mutableStateOf(false) }

    // Monetization & Paid Partnership State (500 Follower Gatekeeper)
    var isPaidPartnership by remember { mutableStateOf(false) }
    var ctaLinkInput by remember { mutableStateOf("") }
    var ctaLabelSelected by remember { mutableStateOf("Shop now") }
    var showPromotionCheckoutDialog by remember { mutableStateOf(false) }
    var promotionBudgetInput by remember { mutableStateOf("500") }
    var isPromotionActive by remember { mutableStateOf(false) }

    // Advanced Options State
    var showMoreOptionsScreen by remember { mutableStateOf(false) }
    var isScheduled by remember { mutableStateOf(false) }
    var scheduledDateTimeStr by remember { mutableStateOf<String?>(null) }
    var isCommentsDisabled by remember { mutableStateOf(false) }
    var isHideLikesEnabled by remember { mutableStateOf(false) }
    var isHideSharesEnabled by remember { mutableStateOf(false) }
    var isFacebookAutoShare by remember { mutableStateOf(false) }
    var altTextValue by remember { mutableStateOf("") }
    var showAltTextDialog by remember { mutableStateOf(false) }

    // Global Music Picker Screen State
    var showMusicPickerScreen by remember { mutableStateOf(false) }
    var showMusicPickerSheet by remember { mutableStateOf(false) }
    var musicSearchQuery by remember { mutableStateOf("") }
    var globalTracks by remember { mutableStateOf<List<AudioTrack>>(emptyList()) }
    var isSearchingMusic by remember { mutableStateOf(false) }

    // Tag People & Audience State
    var showTagPeopleSheet by remember { mutableStateOf(false) }
    var taggedUsers by remember { mutableStateOf<List<com.devil.finaldestiny.model.UserProfile>>(emptyList()) }
    var tagSearchQuery by remember { mutableStateOf("") }
    var tagSearchResultUsers by remember { mutableStateOf<List<com.devil.finaldestiny.model.UserProfile>>(emptyList()) }
    var isSearchingTagUsers by remember { mutableStateOf(false) }

    var showAudiencePickerSheet by remember { mutableStateOf(false) }
    var selectedAudience by remember { mutableStateOf("Public (Everyone)") }

    // Photo Filters, Crop & Overlay State
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    var overlayTextValue by remember { mutableStateOf("") }
    var showOverlayTextDialog by remember { mutableStateOf(false) }
    var cropAspectRatioLabel by remember { mutableStateOf("Aspect 9:16 (Full)") }

    // Active Media Uri and Draft State
    var activeMediaUri by remember { mutableStateOf(mediaUri) }
    var showDiscardDraftDialog by remember { mutableStateOf(false) }
    var showResumeDraftDialog by remember { mutableStateOf(com.devil.finaldestiny.data.ReelDraftManager.hasDraft(context)) }

    val loadedBitmap = rememberLoadedImage(context, activeMediaUri)

    val handleBackAction = {
        if (!activeMediaUri.isNullOrBlank() || captionText.isNotBlank() || selectedAudioTrack != null) {
            showDiscardDraftDialog = true
        } else {
            onBack()
        }
    }

    androidx.activity.compose.BackHandler(enabled = true) {
        handleBackAction()
    }

    // Dynamic Geolocation & Search State
    var detectedCity by remember { mutableStateOf("Detecting location...") }
    var locationChips by remember { mutableStateOf<List<String>>(emptyList()) }
    var showLocationPickerSheet by remember { mutableStateOf(false) }
    var locationSearchQuery by remember { mutableStateOf("") }
    var searchLocationResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var detailedLocationResults by remember { mutableStateOf<List<com.devil.finaldestiny.data.LocationPlaceItem>>(emptyList()) }
    var nearbyLandmarkItems by remember { mutableStateOf<List<com.devil.finaldestiny.data.LocationPlaceItem>>(emptyList()) }
    var isSearchingLocation by remember { mutableStateOf(false) }

    // Final Destiny AI Studio State
    var showAiVideoGradingSheet by remember { mutableStateOf(false) }
    var showAiPhotoStudioSheet by remember { mutableStateOf(false) }
    var aiVideoPromptText by remember { mutableStateOf("") }
    var aiPhotoPromptText by remember { mutableStateOf("") }
    var isProcessingAiVideo by remember { mutableStateOf(false) }
    var isProcessingAiPhoto by remember { mutableStateOf(false) }
    var customAiColorMatrix by remember { mutableStateOf<ColorMatrix?>(null) }
    var previousAiColorMatrix by remember { mutableStateOf<ColorMatrix?>(null) }

    LaunchedEffect(Unit) {
        val place = com.devil.finaldestiny.data.GlobalLocationRepository.detectCurrentLocationWithGpsDetailed(context)
        if (place.title.isNotBlank() && place.title != "Select Location") {
            detectedCity = place.title
            locationChips = com.devil.finaldestiny.data.GlobalLocationRepository.generateLocationChipsForCity(place.title)
        } else {
            detectedCity = "Current Location"
        }
    }

    LaunchedEffect(locationSearchQuery, showLocationPickerSheet) {
        if (showLocationPickerSheet) {
            if (nearbyLandmarkItems.isEmpty()) {
                nearbyLandmarkItems = com.devil.finaldestiny.data.GlobalLocationRepository.fetchNearbyLandmarks(detectedCity)
            }
            if (locationSearchQuery.isNotBlank()) {
                isSearchingLocation = true
                detailedLocationResults = com.devil.finaldestiny.data.GlobalLocationRepository.searchGlobalLocationsDetailed(context, locationSearchQuery)
                isSearchingLocation = false
            } else {
                detailedLocationResults = emptyList()
            }
        }
    }

    LaunchedEffect(tagSearchQuery, showTagPeopleSheet) {
        if (showTagPeopleSheet) {
            isSearchingTagUsers = true
            val fetched = com.devil.finaldestiny.data.SupabaseAuthClient.fetchShareSheetUsers(user.id)
            tagSearchResultUsers = if (tagSearchQuery.isBlank()) {
                fetched
            } else {
                fetched.filter { 
                    it.name.contains(tagSearchQuery, ignoreCase = true) || 
                    it.handle.contains(tagSearchQuery, ignoreCase = true) 
                }
            }
            isSearchingTagUsers = false
        }
    }

    LaunchedEffect(musicSearchQuery, showMusicPickerSheet) {
        if (showMusicPickerSheet) {
            isSearchingMusic = true
            globalTracks = GlobalMusicRepository.searchGlobalMusic(musicSearchQuery)
            isSearchingMusic = false
        }
    }

    if (showMusicPickerScreen) {
        MusicPickerScreen(
            onSelectTrack = { track ->
                selectedAudioTrack = track
                showMusicPickerScreen = false
                Toast.makeText(context, "🎵 Attached: ${track.title}", Toast.LENGTH_SHORT).show()
            },
            onBack = { showMusicPickerScreen = false }
        )
        return
    }

    if (showMoreOptionsScreen) {
        InstagramMoreOptionsScreen(
            isScheduled = isScheduled,
            scheduledDateTimeStr = scheduledDateTimeStr,
            onScheduleToggle = { checked ->
                isScheduled = checked
                if (checked) {
                    showDateTimePicker(context) { selected ->
                        scheduledDateTimeStr = selected
                    }
                } else {
                    scheduledDateTimeStr = null
                }
            },
            isCommentsDisabled = isCommentsDisabled,
            onCommentsDisabledToggle = { isCommentsDisabled = it },
            isHideLikesEnabled = isHideLikesEnabled,
            onHideLikesToggle = { isHideLikesEnabled = it },
            isHideSharesEnabled = isHideSharesEnabled,
            onHideSharesToggle = { isHideSharesEnabled = it },
            isFacebookAutoShare = isFacebookAutoShare,
            onFacebookAutoShareToggle = { isFacebookAutoShare = it },
            altTextValue = altTextValue,
            onOpenAltText = { showAltTextDialog = true },
            onBack = { showMoreOptionsScreen = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isReel) "New reel" else "New post",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBackAction) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val finalCaption = captionText.ifBlank { if (isReel) "New Reel Video! 🎬" else "New Post Moment 📸" }
                            com.devil.finaldestiny.data.ReelDraftManager.clearDraft(context)
                            onPublish(
                                finalCaption,
                                activeMediaUri ?: mediaUri,
                                selectedAudioTrack?.title ?: "Original Audio",
                                selectedAudioTrack?.artist ?: "Creator",
                                selectedAudioTrack?.audioUrl,
                                isAiLabelEnabled,
                                isCommentsDisabled,
                                isHideLikesEnabled,
                                isHideSharesEnabled,
                                scheduledDateTimeStr,
                                altTextValue.ifBlank { null },
                                samplePhotoFilters[selectedFilterIndex].name,
                                overlayTextValue.ifBlank { null },
                                ctaLinkInput.ifBlank { null },
                                ctaLabelSelected,
                                isPaidPartnership,
                                if (isPromotionActive) "active" else "none",
                                promotionBudgetInput.toDoubleOrNull() ?: 0.0
                            )
                        }
                    ) {
                        Text("Share", color = Color(0xFF3897F0), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Button(
                    onClick = {
                        val finalCaption = captionText.ifBlank { if (isReel) "New Reel Video! 🎬" else "New Post Moment 📸" }
                        com.devil.finaldestiny.data.ReelDraftManager.clearDraft(context)
                        onPublish(
                            finalCaption,
                            activeMediaUri ?: mediaUri,
                            selectedAudioTrack?.title ?: "Original Audio",
                            selectedAudioTrack?.artist ?: "Creator",
                            selectedAudioTrack?.audioUrl,
                            isAiLabelEnabled,
                            isCommentsDisabled,
                            isHideLikesEnabled,
                            isHideSharesEnabled,
                            scheduledDateTimeStr,
                            altTextValue.ifBlank { null },
                            samplePhotoFilters[selectedFilterIndex].name,
                            overlayTextValue.ifBlank { null },
                            ctaLinkInput.ifBlank { null },
                            ctaLabelSelected,
                            isPaidPartnership,
                            if (isPromotionActive) "active" else "none",
                            promotionBudgetInput.toDoubleOrNull() ?: 0.0
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Share", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            val activeFilterMatrix = if (selectedFilterIndex == 1 && customAiColorMatrix != null) {
                customAiColorMatrix
            } else {
                samplePhotoFilters.getOrNull(selectedFilterIndex)?.colorMatrix
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .drawWithContent {
                        if (activeFilterMatrix != null) {
                            drawIntoCanvas { canvas ->
                                val paint = Paint().apply {
                                    colorFilter = ColorFilter.colorMatrix(activeFilterMatrix)
                                }
                                canvas.saveLayer(Rect(0f, 0f, size.width, size.height), paint)
                                drawContent()
                                canvas.restore()
                            }
                        } else {
                            drawContent()
                        }
                    }
                    .background(Color.Black)
            ) {
                if (!mediaUri.isNullOrBlank() && (mediaUri.contains("video", ignoreCase = true) || isReel) && loadedBitmap == null) {
                    ExoVideoPlayerView(
                        videoUri = mediaUri,
                        colorMatrix = activeFilterMatrix,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (loadedBitmap != null) {
                    Image(
                        bitmap = loadedBitmap,
                        contentDescription = "Media Preview",
                        contentScale = ContentScale.Crop,
                        colorFilter = if (activeFilterMatrix != null) ColorFilter.colorMatrix(activeFilterMatrix) else null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (!mediaUri.isNullOrBlank()) {
                    ExoVideoPlayerView(
                        videoUri = mediaUri,
                        colorMatrix = activeFilterMatrix,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = if (isReel) Icons.Default.Videocam else Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(64.dp)
                    )
                }

                // Interactive Tagged User Pill Overlay with App Badge
                if (taggedUsers.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.65f), shape = RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("✨", fontSize = 12.sp)
                        taggedUsers.forEach { tagged ->
                            Text(
                                if (tagged.handle.startsWith("@")) tagged.handle else "@${tagged.handle}",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Overlay Text if present
                if (overlayTextValue.isNotBlank()) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.Center).padding(12.dp)
                    ) {
                        Text(
                            text = overlayTextValue,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Top right tool buttons (AI Studio, Crop & Aspect Ratio, Filter & Text Edit)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    IconButton(
                        onClick = { showAiPhotoStudioSheet = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF7F00FF), Color(0xFFE100FF))))
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Final Destiny AI Studio", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = {
                            val ratios = listOf("Aspect 9:16 (Full)", "Aspect 4:5 (Standard)", "Aspect 1:1 (Square)")
                            val nextRatio = ratios[(ratios.indexOf(cropAspectRatioLabel) + 1) % ratios.size]
                            cropAspectRatioLabel = nextRatio
                            Toast.makeText(context, "✂️ Crop: $nextRatio", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Crop, contentDescription = "Crop & Scale", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = { showOverlayTextDialog = true },
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.TextFields, contentDescription = "Text Overlay", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // FILTER CAROUSEL ROW
            Text(
                "Filters & Color Effects",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E8E93),
                modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 4.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(samplePhotoFilters.indices.toList()) { index ->
                    val filterItem = samplePhotoFilters[index]
                    val isSelected = selectedFilterIndex == index

                    val gradientColors = when (filterItem.name) {
                        "Original" -> listOf(Color(0xFF3897F0), Color(0xFF00C6FF))
                        "✨ Final Destiny AI" -> listOf(Color(0xFF7F00FF), Color(0xFFE100FF))
                        "Royal Velvet" -> listOf(Color(0xFF7F00FF), Color(0xFFE100FF))
                        "Golden Hour" -> listOf(Color(0xFFFF8C00), Color(0xFFFFD700))
                        "Cinema Noir" -> listOf(Color(0xFF434343), Color(0xFF000000))
                        "Vintage Film" -> listOf(Color(0xFFD4145A), Color(0xFFFBB03B))
                        "Cyberpunk 2077" -> listOf(Color(0xFFFF007F), Color(0xFF00E5FF))
                        "Emerald Moody" -> listOf(Color(0xFF0575E6), Color(0xFF00F260))
                        "Champagne Lux" -> listOf(Color(0xFFE0EAFC), Color(0xFFCFDEF3))
                        "Miami Sunset" -> listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))
                        "Dark Knight" -> listOf(Color(0xFF1F1C2C), Color(0xFF928DAB))
                        "Monaco Sun" -> listOf(Color(0xFFF857A6), Color(0xFFFF5858))
                        "Soft Porcelain" -> listOf(Color(0xFFA8C0FF), Color(0xFF3F2B96))
                        else -> listOf(Color(0xFF8E8E93), Color(0xFF636366))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            selectedFilterIndex = index
                            if (index == 1) {
                                showAiVideoGradingSheet = true
                            }
                        }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    brush = if (isSelected) Brush.linearGradient(gradientColors) else Brush.linearGradient(listOf(Color(0xFFE5E5EA), Color(0xFFC7C7CC))),
                                    shape = CircleShape
                                )
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(gradientColors))
                        ) {
                            Icon(
                                imageVector = if (filterItem.name == "Original") Icons.Default.FilterNone else Icons.Default.AutoAwesome,
                                contentDescription = filterItem.name,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = filterItem.name,
                            fontSize = 11.sp,
                            color = if (isSelected) Color(0xFF3897F0) else Color(0xFF262626),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp)

            // 2. CAPTION INPUT & CHIPS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    placeholder = { Text("Write a caption...", color = Color(0xFF8E8E93), fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f).heightIn(min = 60.dp)
                )
            }

            // Quick Chips Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = { captionText += " #Poll" },
                    label = { Text("= Poll", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = false,
                    onClick = { captionText += " #Prompt" },
                    label = { Text("💬 Prompt", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp)

            // 3. INSTAGRAM OPTIONS LIST
            // Add Audio Option
            ListItem(
                headlineContent = { Text("Add audio", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                leadingContent = { Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Black) },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedAudioTrack != null) {
                            Text(
                                selectedAudioTrack!!.title,
                                fontSize = 12.sp,
                                color = Color(0xFF3897F0),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF8E8E93))
                    }
                },
                modifier = Modifier.clickable { showMusicPickerScreen = true }
            )

            // Tag People Option
            ListItem(
                headlineContent = { Text("Tag people", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                leadingContent = { Icon(Icons.Default.PersonOutline, contentDescription = null, tint = Color.Black) },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (taggedUsers.isNotEmpty()) {
                            Text(
                                "${taggedUsers.size} tagged",
                                fontSize = 12.sp,
                                color = Color(0xFF3897F0),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF8E8E93))
                    }
                },
                modifier = Modifier.clickable { showTagPeopleSheet = true }
            )

            // Add Location Option & Dynamic Chips
            Column {
                ListItem(
                    headlineContent = { Text("Add location", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                    leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Black) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectedLocation.isNotBlank()) {
                                Text(selectedLocation, fontSize = 12.sp, color = Color(0xFF3897F0), fontWeight = FontWeight.Bold, maxLines = 1)
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF8E8E93))
                        }
                    },
                    modifier = Modifier.clickable { showLocationPickerSheet = true }
                )
                if (locationChips.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(locationChips) { loc ->
                            AssistChip(
                                onClick = { selectedLocation = loc },
                                label = { Text(loc, fontSize = 12.sp, color = if (selectedLocation == loc) Color(0xFF3897F0) else Color.Black) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFF2F2F7))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Add AI Label Toggle
            ListItem(
                headlineContent = { Text("Add AI label", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                supportingContent = {
                    Text(
                        "We require you to label certain realistic content that's made with AI.",
                        fontSize = 11.sp,
                        color = Color(0xFF8E8E93)
                    )
                },
                leadingContent = { Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.Black) },
                trailingContent = {
                    Switch(
                        checked = isAiLabelEnabled,
                        onCheckedChange = { isAiLabelEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF3897F0))
                    )
                }
            )

            HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp)

            // Audience Option
            ListItem(
                headlineContent = { Text("Audience", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                leadingContent = { Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.Black) },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedAudience, fontSize = 12.sp, color = Color(0xFF3897F0), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF8E8E93))
                    }
                },
                modifier = Modifier.clickable { showAudiencePickerSheet = true }
            )

            // Also share on... Option
            ListItem(
                headlineContent = { Text("Also share on...", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                leadingContent = { Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black) },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Off", fontSize = 12.sp, color = Color(0xFF8E8E93))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF8E8E93))
                    }
                }
            )

            HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp)

            // ----------------------------------------------------------------
            // MONETIZATION / PAID PARTNERSHIP GATEKEEPER (500 FOLLOWER THRESHOLD)
            // ----------------------------------------------------------------
            val isMonetizationUnlocked = user.followerCount >= 500

            if (!isMonetizationUnlocked) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCD34D)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Paid Branding & Action Link Locked 🔒", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                            Text("Paid Branding unlocks at 500 followers (Current: ${user.followerCount}/500)", fontSize = 11.sp, color = Color(0xFFB45309))
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Paid Partnership & Action Link 💼", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Toggle: Paid Partnership
                    ListItem(
                        headlineContent = { Text("Paid partnership label", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Adds 'Paid partnership' tag to post header", fontSize = 11.sp, color = Color(0xFF8E8E93)) },
                        trailingContent = {
                            Switch(
                                checked = isPaidPartnership,
                                onCheckedChange = { isPaidPartnership = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF3897F0))
                            )
                        }
                    )

                    // Action Link Input
                    OutlinedTextField(
                        value = ctaLinkInput,
                        onValueChange = { ctaLinkInput = it },
                        label = { Text("Action Link URL", fontSize = 12.sp, color = Color.Gray) },
                        placeholder = { Text("https://yourwebsite.com/deal", fontSize = 12.sp, color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF3897F0), unfocusedBorderColor = Color.LightGray),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // CTA Button Label Selector Chips
                    Text("Select CTA Button Label:", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        val ctaOptions = listOf("Shop now", "Sign up", "Visit site", "Install app", "Learn more")
                        items(ctaOptions) { labelOpt ->
                            FilterChip(
                                selected = (ctaLabelSelected == labelOpt),
                                onClick = { ctaLabelSelected = labelOpt },
                                label = { Text(labelOpt, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF3897F0),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // In-App Revenue Promotion Checkout Button
                    Button(
                        onClick = { showPromotionCheckoutDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isPromotionActive) Color(0xFF166534) else Color(0xFF7C3AED)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isPromotionActive) "✅ Promotion Active (Paid)" else "Promote Post (In-App Revenue)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp)

            // More Options Row (Reference 2 & 3 Link)
            ListItem(
                headlineContent = { Text("More options", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                leadingContent = { Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.Black) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF8E8E93)) },
                modifier = Modifier.clickable { showMoreOptionsScreen = true }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // IN-APP REVENUE PROMOTION CHECKOUT DIALOG
    if (showPromotionCheckoutDialog) {
        val budgetVal = promotionBudgetInput.toDoubleOrNull() ?: 500.0
        val platformCut = budgetVal * 0.20
        val netBudget = budgetVal * 0.80

        AlertDialog(
            onDismissRequest = { showPromotionCheckoutDialog = false },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFF7C3AED))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("💰 Promote Post Checkout", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Set campaign budget to feature this post in discovery feeds:", fontSize = 12.sp, color = Color.Gray)

                    OutlinedTextField(
                        value = promotionBudgetInput,
                        onValueChange = { promotionBudgetInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Promotion Budget (₹)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF7C3AED)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Total Campaign:", fontSize = 12.sp, color = Color.Black)
                                Text("₹${budgetVal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Platform Commission Cut (20%):", fontSize = 12.sp, color = Color(0xFFDC2626))
                                Text("- ₹${platformCut.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                            }
                            HorizontalDivider(color = Color(0xFFD1D5DB))
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Net Ad Reach Credit:", fontSize = 12.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                                Text("₹${netBudget.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isPromotionActive = true
                        isPaidPartnership = true
                        showPromotionCheckoutDialog = false
                        Toast.makeText(context, "✅ Paid Promotion Activated! Platform Cut 20%: ₹${platformCut.toInt()}", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Text("Pay & Activate Promotion 💳", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPromotionCheckoutDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // GLOBAL MUSIC SEARCH BOTTOM SHEET
    if (showMusicPickerSheet) {
        AlertDialog(
            onDismissRequest = { showMusicPickerSheet = false },
            containerColor = Color.White,
            title = {
                Column {
                    Text("🎵 Search Global Music", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = musicSearchQuery,
                        onValueChange = { musicSearchQuery = it },
                        placeholder = { Text("Search any song, artist, album...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            text = {
                Box(modifier = Modifier.height(280.dp).fillMaxWidth()) {
                    if (isSearchingMusic) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF3897F0))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(globalTracks) { track ->
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(track.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black, maxLines = 1)
                                        Text("${track.artist} • ${track.duration}", fontSize = 11.sp, color = Color(0xFF8E8E93))
                                    }
                                    Button(
                                        onClick = {
                                            selectedAudioTrack = track
                                            showMusicPickerSheet = false
                                            Toast.makeText(context, "🎵 Attached: ${track.title}", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Add", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMusicPickerSheet = false }) {
                    Text("Close", color = Color(0xFF8E8E93))
                }
            }
        )
    }

    // OVERLAY TEXT DIALOG
    if (showOverlayTextDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayTextDialog = false },
            title = { Text("Add Text to Photo", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = overlayTextValue,
                    onValueChange = { overlayTextValue = it },
                    label = { Text("Text overlay") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { showOverlayTextDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // NATIVE IN-APP FINAL DESTINY AI VIDEO GRADING SHEET
    if (showAiVideoGradingSheet) {
        AlertDialog(
            onDismissRequest = { showAiVideoGradingSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFF7F00FF), Color(0xFFE100FF))))
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Final Destiny AI Studio", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    IconButton(onClick = { showAiVideoGradingSheet = false }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8E8E93))
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Describe ANY video color grading, lighting, or mood (e.g. 'Warm golden sunset with rich contrast', 'Cyberpunk neon night', 'Vintage 35mm film').",
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = aiVideoPromptText,
                        onValueChange = { aiVideoPromptText = it },
                        placeholder = { Text("Type custom AI prompt...", fontSize = 13.sp, color = Color(0xFF8E8E93)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7F00FF),
                            unfocusedBorderColor = Color(0xFFE5E5EA),
                            focusedContainerColor = Color(0xFFF9F5FF),
                            unfocusedContainerColor = Color(0xFFF2F2F7)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (isProcessingAiVideo) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF7F00FF))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Synthesizing AI Color Matrix...", fontSize = 12.sp, color = Color(0xFF7F00FF), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (aiVideoPromptText.isNotBlank()) {
                            isProcessingAiVideo = true
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(600)
                                customAiColorMatrix = generateAiColorMatrixFromPrompt(aiVideoPromptText)
                                selectedFilterIndex = 1
                                isProcessingAiVideo = false
                                showAiVideoGradingSheet = false
                                Toast.makeText(context, "✨ Applied AI Look: '${aiVideoPromptText.take(20)}...'", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Please type an AI prompt first!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F00FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply AI Look", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // NATIVE IN-APP FINAL DESTINY AI PHOTO TRANSFORMATION SHEET
    if (showAiPhotoStudioSheet) {
        AlertDialog(
            onDismissRequest = { showAiPhotoStudioSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFFFF007F), Color(0xFF7F00FF))))
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Final Destiny Photo AI", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    IconButton(onClick = { showAiPhotoStudioSheet = false }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8E8E93))
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Describe ANY visual transformation, background mood, or artistic effect for your photo.",
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = aiPhotoPromptText,
                        onValueChange = { aiPhotoPromptText = it },
                        placeholder = { Text("e.g. 'Golden hour sunset bloom with sharp details'", fontSize = 13.sp, color = Color(0xFF8E8E93)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF007F),
                            unfocusedBorderColor = Color(0xFFE5E5EA),
                            focusedContainerColor = Color(0xFFFFF0F5),
                            unfocusedContainerColor = Color(0xFFF2F2F7)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (isProcessingAiPhoto) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFFFF007F))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Transforming Generative AI Photo Canvas...", fontSize = 12.sp, color = Color(0xFFFF007F), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            if (aiPhotoPromptText.isNotBlank()) {
                                isProcessingAiPhoto = true
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(700)
                                    previousAiColorMatrix = customAiColorMatrix
                                    customAiColorMatrix = generateAiColorMatrixFromPrompt(aiPhotoPromptText)
                                    selectedFilterIndex = 1
                                    isProcessingAiPhoto = false
                                    Toast.makeText(context, "✨ Photo Transformed! Use Keep or Undo.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Please type a transformation prompt!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Transform Photo ✨", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    if (previousAiColorMatrix != null || customAiColorMatrix != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    customAiColorMatrix = previousAiColorMatrix
                                    if (customAiColorMatrix == null) selectedFilterIndex = 0
                                    Toast.makeText(context, "↩️ Transformation Undone", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Undo ↩️", fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    showAiPhotoStudioSheet = false
                                    Toast.makeText(context, "✅ Changes Kept!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Keep Changes ✅", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        )
    }

    // LIVE GPS & LANDMARKS LOCATION PICKER DIALOG
    if (showLocationPickerSheet) {
        AlertDialog(
            onDismissRequest = { showLocationPickerSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📍 Select Location", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        IconButton(onClick = { showLocationPickerSheet = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8E8E93))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = locationSearchQuery,
                        onValueChange = { locationSearchQuery = it },
                        placeholder = { Text("Search city, landmark, or area...", fontSize = 13.sp, color = Color(0xFF8E8E93)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF3897F0)) },
                        trailingIcon = {
                            if (locationSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { locationSearchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF8E8E93))
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3897F0),
                            unfocusedBorderColor = Color(0xFFE5E5EA),
                            focusedContainerColor = Color(0xFFF2F2F7),
                            unfocusedContainerColor = Color(0xFFF2F2F7)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            text = {
                Box(modifier = Modifier.height(340.dp).fillMaxWidth()) {
                    if (isSearchingLocation) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF3897F0))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            // Row 1: USE CURRENT LOCATION (GPS Target Icon with Blue Accent)
                            item {
                                Surface(
                                    color = Color(0xFFF0F8FF),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            selectedLocation = if (detectedCity != "Add location" && detectedCity != "Detecting location...") detectedCity else "Current Location"
                                            showLocationPickerSheet = false
                                            Toast.makeText(context, "📍 Location set: $selectedLocation", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(listOf(Color(0xFF3897F0), Color(0xFF00C6FF))))
                                        ) {
                                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Use Current Location", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF3897F0))
                                            Text(if (detectedCity.isNotBlank()) detectedCity else "Fetching GPS coordinates...", fontSize = 11.sp, color = Color(0xFF8E8E93))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // If user is searching: display live location search results
                            if (locationSearchQuery.isNotBlank()) {
                                item {
                                    Text("Search Results", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E8E93), modifier = Modifier.padding(vertical = 4.dp))
                                }
                                items(detailedLocationResults) { place ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedLocation = place.title
                                                showLocationPickerSheet = false
                                                Toast.makeText(context, "📍 Location set: ${place.title}", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(vertical = 10.dp, horizontal = 4.dp)
                                    ) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF3897F0))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(place.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black, maxLines = 1)
                                            Text(place.subtitle, fontSize = 11.sp, color = Color(0xFF8E8E93), maxLines = 1)
                                        }
                                    }
                                    HorizontalDivider(color = Color(0xFFF2F2F7), thickness = 0.5.dp)
                                }
                            } else {
                                // Row 2: NEARBY LANDMARKS / PLACES LIST
                                item {
                                    Text("Nearby Landmarks & Places", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E8E93), modifier = Modifier.padding(vertical = 6.dp))
                                }
                                items(nearbyLandmarkItems) { landmark ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedLocation = landmark.title
                                                showLocationPickerSheet = false
                                                Toast.makeText(context, "📍 Location set: ${landmark.title}", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(vertical = 10.dp, horizontal = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF8E8E93))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(landmark.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.Black, maxLines = 1)
                                            Text(landmark.subtitle, fontSize = 11.sp, color = Color(0xFF8E8E93), maxLines = 1)
                                        }
                                    }
                                    HorizontalDivider(color = Color(0xFFF2F2F7), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocationPickerSheet = false }) {
                    Text("Close", color = Color(0xFF8E8E93), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // TAG PEOPLE DIALOG
    if (showTagPeopleSheet) {
        AlertDialog(
            onDismissRequest = { showTagPeopleSheet = false },
            containerColor = Color.White,
            title = {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🏷️ Tag People", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        if (taggedUsers.isNotEmpty()) {
                            TextButton(onClick = { taggedUsers = emptyList() }) {
                                Text("Clear All", fontSize = 12.sp, color = Color.Red)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tagSearchQuery,
                        onValueChange = { tagSearchQuery = it },
                        placeholder = { Text("Search users to tag...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            text = {
                Box(modifier = Modifier.height(260.dp).fillMaxWidth()) {
                    if (isSearchingTagUsers) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF3897F0))
                    } else if (tagSearchResultUsers.isEmpty()) {
                        Text("No users found", modifier = Modifier.align(Alignment.Center), color = Color.Gray, fontSize = 13.sp)
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(tagSearchResultUsers) { userItem ->
                                val isTagged = taggedUsers.any { it.id == userItem.id }
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            taggedUsers = if (isTagged) {
                                                taggedUsers.filter { it.id != userItem.id }
                                            } else {
                                                taggedUsers + userItem
                                            }
                                        }
                                        .padding(vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        com.devil.finaldestiny.ui.components.ProfileAvatarView(
                                            name = userItem.name,
                                            profilePictureUri = userItem.profilePictureUri,
                                            size = 36.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(userItem.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black, maxLines = 1)
                                            Text(if (userItem.handle.startsWith("@")) userItem.handle else "@${userItem.handle}", fontSize = 11.sp, color = Color(0xFF8E8E93))
                                        }
                                    }
                                    Checkbox(
                                        checked = isTagged,
                                        onCheckedChange = { checked ->
                                            taggedUsers = if (checked) taggedUsers + userItem else taggedUsers.filter { it.id != userItem.id }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3897F0))
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTagPeopleSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0))
                ) {
                    Text("Done (${taggedUsers.size})", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // AUDIENCE PICKER DIALOG
    if (showAudiencePickerSheet) {
        AlertDialog(
            onDismissRequest = { showAudiencePickerSheet = false },
            containerColor = Color.White,
            title = { Text("👥 Select Audience", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val options = listOf(
                        "Public (Everyone)" to "Anyone on or off Instagram can see this post",
                        "Followers Only" to "Only your approved followers can see this post",
                        "Private (Only Me)" to "Visible only to you on your profile grid"
                    )
                    options.forEach { (title, subtitle) ->
                        val isSelected = selectedAudience == title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFEFF6FF) else Color.Transparent)
                                .clickable {
                                    selectedAudience = title
                                    showAudiencePickerSheet = false
                                }
                                .padding(12.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    selectedAudience = title
                                    showAudiencePickerSheet = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3897F0))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                Text(subtitle, fontSize = 11.sp, color = Color(0xFF8E8E93))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAudiencePickerSheet = false }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            }
        )
    }

    // RESUME DRAFT DIALOG
    if (showResumeDraftDialog) {
        AlertDialog(
            onDismissRequest = { showResumeDraftDialog = false },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EditNote, contentDescription = null, tint = Color(0xFF3897F0))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue Previous Draft?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            text = {
                Text(
                    "You have a saved reel draft from earlier. Would you like to resume your edits or start a new video?",
                    fontSize = 13.sp,
                    color = Color(0xFF3C3C43)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val draft = com.devil.finaldestiny.data.ReelDraftManager.getDraft(context)
                        if (draft != null) {
                            activeMediaUri = draft.mediaUri
                            captionText = draft.caption
                            selectedFilterIndex = draft.filterIndex.coerceIn(0, samplePhotoFilters.lastIndex)
                            selectedLocation = draft.location.ifBlank { selectedLocation }
                            if (!draft.audioTitle.isNullOrBlank() && !draft.audioUrl.isNullOrBlank()) {
                                selectedAudioTrack = AudioTrack(draft.audioTitle, draft.audioArtist ?: "Creator", draft.audioUrl, "0:30")
                            }
                            Toast.makeText(context, "Draft loaded 📝", Toast.LENGTH_SHORT).show()
                        }
                        showResumeDraftDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0))
                ) {
                    Text("Resume Draft", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        com.devil.finaldestiny.data.ReelDraftManager.clearDraft(context)
                        showResumeDraftDialog = false
                        Toast.makeText(context, "Draft discarded", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Start New", color = Color.Red)
                }
            }
        )
    }

    // SAVE OR DISCARD DRAFT DIALOG
    if (showDiscardDraftDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDraftDialog = false },
            containerColor = Color.White,
            title = { Text("Save draft?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black) },
            text = { Text("If you go back now, you can save your changes as a draft or discard them.", fontSize = 13.sp, color = Color(0xFF3C3C43)) },
            confirmButton = {
                Button(
                    onClick = {
                        com.devil.finaldestiny.data.ReelDraftManager.saveDraft(
                            context,
                            com.devil.finaldestiny.data.ReelDraft(
                                mediaUri = activeMediaUri ?: mediaUri,
                                caption = captionText,
                                filterIndex = selectedFilterIndex,
                                location = selectedLocation,
                                audioTitle = selectedAudioTrack?.title,
                                audioArtist = selectedAudioTrack?.artist,
                                audioUrl = selectedAudioTrack?.audioUrl,
                                isReel = isReel
                            )
                        )
                        showDiscardDraftDialog = false
                        Toast.makeText(context, "Draft saved 📝", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0))
                ) {
                    Text("Save as Draft", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            com.devil.finaldestiny.data.ReelDraftManager.clearDraft(context)
                            showDiscardDraftDialog = false
                            Toast.makeText(context, "Edits discarded 🗑️", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    ) {
                        Text("Discard", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { showDiscardDraftDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            }
        )
    }
}

private fun showDateTimePicker(context: Context, onSelect: (String) -> Unit) {
    val cal = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    onSelect(String.format("%04d-%02d-%02d %02d:%02d", year, month + 1, dayOfMonth, hourOfDay, minute))
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}
