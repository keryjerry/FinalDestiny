package com.devil.finaldestiny.ui.settings

import android.content.Context
import android.content.Intent
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
fun UtilitiesAndSupportScreen(
    userSettings: UserSettingsState,
    onUpdateSettings: (UserSettingsState) -> Unit,
    onNavigateToCreatorTools: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Accessibility & Media, 1: Language & Invites, 2: Help & Support
    val tabs = listOf("Accessibility & Media", "Language & Invites", "Help & Support")

    var showReportDialog by remember { mutableStateOf(false) }
    var reportDescription by remember { mutableStateOf("") }
    var showQrDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Utilities & Support",
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
                    // TAB 0: ACCESSIBILITY & MEDIA QUALITY
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Accessibility Features", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                        // Auto Closed Captions
                        ListItem(
                            headlineContent = { Text("Auto Closed Captions", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Automatically generate subtitle overlays on video posts.", fontSize = 11.sp, color = Color.Gray) },
                            leadingContent = { Icon(Icons.Default.ClosedCaption, contentDescription = null, tint = Color.Black) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.autoCaptionsEnabled,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(autoCaptionsEnabled = it)) }
                                )
                            }
                        )

                        // HDR Video Playback
                        ListItem(
                            headlineContent = { Text("HDR Video Playback", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Play high dynamic range video feeds when available.", fontSize = 11.sp, color = Color.Gray) },
                            leadingContent = { Icon(Icons.Default.HdrOn, contentDescription = null, tint = Color.Black) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.hdrPlaybackEnabled,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(hdrPlaybackEnabled = it)) }
                                )
                            }
                        )

                        // Reduce Motion
                        ListItem(
                            headlineContent = { Text("Reduce Motion Effects", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Minimize animated UI transitions across screens.", fontSize = 11.sp, color = Color.Gray) },
                            leadingContent = { Icon(Icons.Default.MotionPhotosOff, contentDescription = null, tint = Color.Black) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.reduceMotionEnabled,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(reduceMotionEnabled = it)) }
                                )
                            }
                        )

                        // High Contrast Display
                        ListItem(
                            headlineContent = { Text("High Contrast Display", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Increase visual contrast for text elements.", fontSize = 11.sp, color = Color.Gray) },
                            leadingContent = { Icon(Icons.Default.Contrast, contentDescription = null, tint = Color.Black) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.highContrastEnabled,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(highContrastEnabled = it)) }
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp)

                        // MEDIA QUALITY & DATA SAVER
                        Text(
                            "Media Quality & Data Saver",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )

                        // Data Saver
                        ListItem(
                            headlineContent = { Text("Data Saver Mode", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Reduces cellular data usage by compressing video feeds.", fontSize = 11.sp, color = Color.Gray) },
                            leadingContent = { Icon(Icons.Default.DataSaverOn, contentDescription = null, tint = Color(0xFF3897F0)) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.dataSaverEnabled,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(dataSaverEnabled = it)) }
                                )
                            }
                        )

                        // Highest Quality Upload
                        ListItem(
                            headlineContent = { Text("Always Upload at Highest Quality", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Upload photos and reels at full resolution even on cellular data.", fontSize = 11.sp, color = Color.Gray) },
                            leadingContent = { Icon(Icons.Default.HighQuality, contentDescription = null, tint = Color(0xFF10B981)) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.highestQualityUploadEnabled,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(highestQualityUploadEnabled = it)) }
                                )
                            }
                        )
                    }
                }

                1 -> {
                    // TAB 1: LANGUAGE, AUTO-TRANSLATION & INVITES
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Language & On-Device Translation", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                        Text("Current Language: ${userSettings.preferredLanguage}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3897F0))

                        // Auto-translation toggle
                        ListItem(
                            headlineContent = { Text("Auto-Translate Posts & Comments", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Translate foreign captions and comments into ${userSettings.preferredLanguage}.", fontSize = 11.sp, color = Color.Gray) },
                            leadingContent = { Icon(Icons.Default.Translate, contentDescription = null, tint = Color.Black) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.autoTranslationEnabled,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(autoTranslationEnabled = it)) }
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp)

                        // INVITES & SHARING
                        Text(
                            "Invites & Social Sharing",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )

                        // Native ShareSheet
                        ListItem(
                            headlineContent = { Text("Share Profile via Native ShareSheet", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            leadingContent = { Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF3897F0)) },
                            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray) },
                            modifier = Modifier.clickable {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Join me on Final Destiny! https://finaldestiny.app")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Invite friends via")
                                context.startActivity(shareIntent)
                            }
                        )

                        // QR Code Generator
                        ListItem(
                            headlineContent = { Text("Generate Profile QR Code", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            leadingContent = { Icon(Icons.Default.QrCode, contentDescription = null, tint = Color.Black) },
                            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray) },
                            modifier = Modifier.clickable { showQrDialog = true }
                        )

                        // Professional Tools Link
                        ListItem(
                            headlineContent = { Text("Professional Creator Tools & Monetization", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            leadingContent = { Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF10B981)) },
                            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray) },
                            modifier = Modifier.clickable { onNavigateToCreatorTools() }
                        )
                    }
                }

                2 -> {
                    // TAB 2: HELP & SUPPORT
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Help & Customer Support", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                        // Help Center
                        ListItem(
                            headlineContent = { Text("Help Center & FAQs", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            leadingContent = { Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color.Black) },
                            modifier = Modifier.clickable {
                                Toast.makeText(context, "Opening Help Center...", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // Report a Problem (with automatic log attachment)
                        ListItem(
                            headlineContent = { Text("Report a Problem", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Attach system diagnostic logs automatically.", fontSize = 11.sp, color = Color.Gray) },
                            leadingContent = { Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color.Red) },
                            modifier = Modifier.clickable { showReportDialog = true }
                        )

                        // Community Guidelines
                        ListItem(
                            headlineContent = { Text("Community Guidelines", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            leadingContent = { Icon(Icons.Default.Gavel, contentDescription = null, tint = Color.Black) },
                            modifier = Modifier.clickable {
                                Toast.makeText(context, "Opening Community Guidelines...", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // Terms of Service
                        ListItem(
                            headlineContent = { Text("Terms of Service & Privacy Policy", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            leadingContent = { Icon(Icons.Default.Policy, contentDescription = null, tint = Color.Black) },
                            modifier = Modifier.clickable {
                                Toast.makeText(context, "Opening Terms of Service...", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // REPORT A PROBLEM DIALOG (WITH AUTOMATIC LOG ATTACHMENT)
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report a Problem", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Describe the issue you encountered:", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = reportDescription,
                        onValueChange = { reportDescription = it },
                        label = { Text("Issue details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8))) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                            Icon(Icons.Default.Attachment, contentDescription = null, tint = Color(0xFF3897F0), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Automatic Log Attachment: Included (Device log tail)", fontSize = 10.sp, color = Color(0xFF006494))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reportDescription.isNotBlank()) {
                            showReportDialog = false
                            reportDescription = ""
                            Toast.makeText(context, "✅ Problem report & device logs submitted to support!", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // QR CODE GENERATOR DIALOG
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("Your Final Destiny QR Code", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color.Black, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, tint = Color.White, modifier = Modifier.size(120.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Scan to view profile on Final Destiny", fontSize = 11.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = { showQrDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
