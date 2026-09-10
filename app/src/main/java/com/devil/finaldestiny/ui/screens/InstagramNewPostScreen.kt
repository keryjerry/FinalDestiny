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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.data.GlobalMusicRepository
import com.devil.finaldestiny.model.AudioTrack
import kotlinx.coroutines.launch
import java.util.Calendar

data class PhotoFilterItem(
    val name: String,
    val colorMatrix: ColorMatrix?
)

val samplePhotoFilters = listOf(
    PhotoFilterItem("Normal", null),
    PhotoFilterItem("Clarendon", ColorMatrix().apply { setToSaturation(1.4f) }),
    PhotoFilterItem("Juno", ColorMatrix(floatArrayOf(
        1.1f, 0f, 0f, 0f, 10f,
        0f, 1.0f, 0f, 0f, 10f,
        0f, 0f, 1.3f, 0f, 5f,
        0f, 0f, 0f, 1f, 0f
    ))),
    PhotoFilterItem("Ludwig", ColorMatrix(floatArrayOf(
        1.2f, 0.1f, 0f, 0f, 0f,
        0.1f, 1.1f, 0.1f, 0f, 0f,
        0f, 0.1f, 1.0f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))),
    PhotoFilterItem("Slumber", ColorMatrix().apply { setToSaturation(0.6f) }),
    PhotoFilterItem("Moon B&W", ColorMatrix().apply { setToSaturation(0f) })
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
    var selectedLocation by remember { mutableStateOf("Kolkata") }
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

    // Global Music Picker Sheet State
    var showMusicPickerSheet by remember { mutableStateOf(false) }
    var musicSearchQuery by remember { mutableStateOf("") }
    var globalTracks by remember { mutableStateOf<List<AudioTrack>>(emptyList()) }
    var isSearchingMusic by remember { mutableStateOf(false) }

    // Photo Filters, Crop & Overlay State
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    var overlayTextValue by remember { mutableStateOf("") }
    var showOverlayTextDialog by remember { mutableStateOf(false) }
    var cropAspectRatioLabel by remember { mutableStateOf("Aspect 9:16 (Full)") }

    val loadedBitmap = rememberLoadedImage(context, mediaUri)

    // Dynamic Geolocation & Search State
    var detectedCity by remember { mutableStateOf("Detecting location...") }
    var locationChips by remember { mutableStateOf<List<String>>(emptyList()) }
    var showLocationPickerSheet by remember { mutableStateOf(false) }
    var locationSearchQuery by remember { mutableStateOf("") }
    var searchLocationResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSearchingLocation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val currentLoc = com.devil.finaldestiny.data.GlobalLocationRepository.detectCurrentLocationWithGps(context)
        detectedCity = currentLoc
        selectedLocation = currentLoc
        locationChips = com.devil.finaldestiny.data.GlobalLocationRepository.generateLocationChipsForCity(currentLoc)
    }

    LaunchedEffect(locationSearchQuery, showLocationPickerSheet) {
        if (showLocationPickerSheet && locationSearchQuery.isNotBlank()) {
            isSearchingLocation = true
            searchLocationResults = com.devil.finaldestiny.data.GlobalLocationRepository.searchGlobalLocations(locationSearchQuery)
            isSearchingLocation = false
        }
    }

    LaunchedEffect(musicSearchQuery, showMusicPickerSheet) {
        if (showMusicPickerSheet) {
            isSearchingMusic = true
            globalTracks = GlobalMusicRepository.searchGlobalMusic(musicSearchQuery)
            isSearchingMusic = false
        }
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
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val finalCaption = captionText.ifBlank { if (isReel) "New Reel Video! 🎬" else "New Post Moment 📸" }
                            onPublish(
                                finalCaption,
                                mediaUri,
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
                        onPublish(
                            finalCaption,
                            mediaUri,
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
            // 1. MEDIA PREVIEW & FILTER CAROUSEL
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color(0xFFF2F2F7))
            ) {
                if (!mediaUri.isNullOrBlank() && (mediaUri.contains("video", ignoreCase = true) || isReel) && loadedBitmap == null) {
                    ExoVideoPlayerView(
                        videoUri = mediaUri,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (loadedBitmap != null) {
                    val filter = samplePhotoFilters[selectedFilterIndex].colorMatrix
                    Image(
                        bitmap = loadedBitmap,
                        contentDescription = "Media Preview",
                        contentScale = ContentScale.Crop,
                        colorFilter = if (filter != null) ColorFilter.colorMatrix(filter) else null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (!mediaUri.isNullOrBlank()) {
                    ExoVideoPlayerView(
                        videoUri = mediaUri,
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

                // Top right tool buttons (Crop & Aspect Ratio, Filter & Text Edit)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(samplePhotoFilters.indices.toList()) { index ->
                    val filterItem = samplePhotoFilters[index]
                    val isSelected = selectedFilterIndex == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedFilterIndex = index }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF3897F0) else Color(0xFFE5E5EA),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(Color(0xFFF2F2F7))
                        ) {
                            Text(filterItem.name.take(2), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Text(
                            filterItem.name,
                            fontSize = 10.sp,
                            color = if (isSelected) Color(0xFF3897F0) else Color.Black,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
                modifier = Modifier.clickable { showMusicPickerSheet = true }
            )

            // Tag People Option
            ListItem(
                headlineContent = { Text("Tag people", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                leadingContent = { Icon(Icons.Default.PersonOutline, contentDescription = null, tint = Color.Black) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF8E8E93)) },
                modifier = Modifier.clickable { Toast.makeText(context, "🏷️ Tag People Opened", Toast.LENGTH_SHORT).show() }
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

            // Only post to profile Option
            ListItem(
                headlineContent = { Text("Only post to profile", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("Try Instagram Plus", fontSize = 11.sp, color = Color(0xFF3897F0)) },
                leadingContent = { Icon(Icons.Default.GridOn, contentDescription = null, tint = Color.Black) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF8E8E93)) },
                modifier = Modifier.clickable { isOnlyPostToProfile = !isOnlyPostToProfile }
            )

            // Audience Option
            ListItem(
                headlineContent = { Text("Audience", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                leadingContent = { Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.Black) },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Everyone", fontSize = 12.sp, color = Color(0xFF8E8E93))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF8E8E93))
                    }
                }
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

    // LIVE GLOBAL LOCATION PICKER DIALOG
    if (showLocationPickerSheet) {
        AlertDialog(
            onDismissRequest = { showLocationPickerSheet = false },
            containerColor = Color.White,
            title = {
                Column {
                    Text("📍 Search Location", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = locationSearchQuery,
                        onValueChange = { locationSearchQuery = it },
                        placeholder = { Text("Search city, area, landmark...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            text = {
                Box(modifier = Modifier.height(260.dp).fillMaxWidth()) {
                    if (isSearchingLocation) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF3897F0))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedLocation = detectedCity
                                            showLocationPickerSheet = false
                                        }
                                        .padding(vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF3897F0))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Use Current Location", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3897F0))
                                        Text(detectedCity, fontSize = 11.sp, color = Color(0xFF8E8E93))
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp)
                            }

                            items(searchLocationResults) { locName ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedLocation = locName
                                            showLocationPickerSheet = false
                                        }
                                        .padding(vertical = 10.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF8E8E93))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(locName, fontSize = 13.sp, color = Color.Black, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocationPickerSheet = false }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
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
