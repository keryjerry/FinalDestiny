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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SettingsSubScreen {
    MAIN_HUB,
    ACCOUNTS_CENTER,
    SAVED_AND_ARCHIVE,
    NOTIFICATIONS_AND_USAGE,
    PRIVACY_AND_SOCIAL,
    SAFETY_AND_CONTENT,
    UTILITIES_AND_SUPPORT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsActivityScreen(
    userSettings: UserSettingsState,
    onUpdateSettings: (UserSettingsState) -> Unit,
    onLogOutAllSessions: () -> Unit,
    onClearSearchHistory: () -> Unit,
    onRequestDataExport: () -> Unit,
    onAddKeywordToBlacklist: (String) -> Unit,
    onUnblockUser: (String) -> Unit,
    onResetFeedInterests: () -> Unit,
    onNavigateToCreatorTools: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var currentSubScreen by remember { mutableStateOf(SettingsSubScreen.MAIN_HUB) }
    var searchQuery by remember { mutableStateOf("") }

    when (currentSubScreen) {
        SettingsSubScreen.ACCOUNTS_CENTER -> {
            AccountsCenterScreen(
                userSettings = userSettings,
                onUpdateSettings = onUpdateSettings,
                onLogOutAllSessions = onLogOutAllSessions,
                onBack = { currentSubScreen = SettingsSubScreen.MAIN_HUB }
            )
        }

        SettingsSubScreen.SAVED_AND_ARCHIVE -> {
            SavedAndArchiveScreen(
                userSettings = userSettings,
                onUpdateSettings = onUpdateSettings,
                onClearSearchHistory = onClearSearchHistory,
                onRequestDataExport = onRequestDataExport,
                onBack = { currentSubScreen = SettingsSubScreen.MAIN_HUB }
            )
        }

        SettingsSubScreen.NOTIFICATIONS_AND_USAGE -> {
            NotificationsAndUsageScreen(
                userSettings = userSettings,
                onUpdateSettings = onUpdateSettings,
                onBack = { currentSubScreen = SettingsSubScreen.MAIN_HUB }
            )
        }

        SettingsSubScreen.PRIVACY_AND_SOCIAL -> {
            PrivacyAndSocialControlsScreen(
                userSettings = userSettings,
                onUpdateSettings = onUpdateSettings,
                onAddKeywordToBlacklist = onAddKeywordToBlacklist,
                onUnblockUser = onUnblockUser,
                onBack = { currentSubScreen = SettingsSubScreen.MAIN_HUB }
            )
        }

        SettingsSubScreen.SAFETY_AND_CONTENT -> {
            SafetyAndContentScreen(
                userSettings = userSettings,
                onUpdateSettings = onUpdateSettings,
                onResetFeedInterests = onResetFeedInterests,
                onBack = { currentSubScreen = SettingsSubScreen.MAIN_HUB }
            )
        }

        SettingsSubScreen.UTILITIES_AND_SUPPORT -> {
            UtilitiesAndSupportScreen(
                userSettings = userSettings,
                onUpdateSettings = onUpdateSettings,
                onNavigateToCreatorTools = onNavigateToCreatorTools,
                onBack = { currentSubScreen = SettingsSubScreen.MAIN_HUB }
            )
        }

        SettingsSubScreen.MAIN_HUB -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "Settings and activity",
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
                        .verticalScroll(rememberScrollState())
                ) {
                    // SEARCH BAR
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search settings", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3897F0),
                            unfocusedBorderColor = Color(0xFFE5E5EA),
                            focusedContainerColor = Color(0xFFF2F2F7),
                            unfocusedContainerColor = Color(0xFFF2F2F7)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // SECTION 1: YOUR ACCOUNT (META ACCOUNTS CENTER)
                    SettingsSectionHeader("Your account")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { currentSubScreen = SettingsSubScreen.ACCOUNTS_CENTER }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("∞", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF006494))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Accounts Center", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Password, security, personal details, ad preferences, payment methods & device permissions.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))

                    // SECTION 2: HOW YOU USE FINAL DESTINY
                    SettingsSectionHeader("How you use Final Destiny")
                    SettingsNavigationItem(
                        icon = Icons.Default.Bookmark,
                        title = "Saved Collections",
                        subtitle = "Manage posts, reels, and audio folders",
                        onClick = { currentSubScreen = SettingsSubScreen.SAVED_AND_ARCHIVE }
                    )
                    SettingsNavigationItem(
                        icon = Icons.Default.Archive,
                        title = "Archive Vault",
                        subtitle = "Stories & Live archives (30-day purge)",
                        onClick = { currentSubScreen = SettingsSubScreen.SAVED_AND_ARCHIVE }
                    )
                    SettingsNavigationItem(
                        icon = Icons.Default.History,
                        title = "Your Activity & Log",
                        subtitle = "Watch history, searches, likes & download data",
                        onClick = { currentSubScreen = SettingsSubScreen.SAVED_AND_ARCHIVE }
                    )
                    SettingsNavigationItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications & Quiet Mode",
                        subtitle = "Push alerts, DND schedule & Screen time",
                        onClick = { currentSubScreen = SettingsSubScreen.NOTIFICATIONS_AND_USAGE }
                    )

                    HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))

                    // SECTION 3: WHO CAN SEE YOUR CONTENT
                    SettingsSectionHeader("Who can see your content")
                    SettingsNavigationItem(
                        icon = Icons.Default.Lock,
                        title = "Account Privacy",
                        subtitle = if (userSettings.isPrivateAccount) "Private account" else "Public account",
                        onClick = { currentSubScreen = SettingsSubScreen.PRIVACY_AND_SOCIAL }
                    )
                    SettingsNavigationItem(
                        icon = Icons.Default.Star,
                        title = "Close Friends",
                        subtitle = "${userSettings.closeFriendsLists.size} lists configured",
                        onClick = { currentSubScreen = SettingsSubScreen.PRIVACY_AND_SOCIAL }
                    )
                    SettingsNavigationItem(
                        icon = Icons.Default.Block,
                        title = "Blocked & Muted",
                        subtitle = "${userSettings.blockedMutedUsers.size} accounts managed",
                        onClick = { currentSubScreen = SettingsSubScreen.PRIVACY_AND_SOCIAL }
                    )

                    HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))

                    // SECTION 4: HOW OTHERS INTERACT WITH YOU
                    SettingsSectionHeader("How others can interact with you")
                    SettingsNavigationItem(
                        icon = Icons.Default.ChatBubbleOutline,
                        title = "Messages & Story Replies",
                        subtitle = "Request gates & read receipts",
                        onClick = { currentSubScreen = SettingsSubScreen.PRIVACY_AND_SOCIAL }
                    )
                    SettingsNavigationItem(
                        icon = Icons.Default.AlternateEmail,
                        title = "Tags & Mentions",
                        subtitle = "Manual approval queue",
                        onClick = { currentSubScreen = SettingsSubScreen.PRIVACY_AND_SOCIAL }
                    )
                    SettingsNavigationItem(
                        icon = Icons.Default.Comment,
                        title = "Comments & Moderation Rules",
                        subtitle = "Offensive filter & custom keyword blacklist",
                        onClick = { currentSubScreen = SettingsSubScreen.PRIVACY_AND_SOCIAL }
                    )

                    HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))

                    // SECTION 5: WHAT YOU SEE & SAFETY
                    SettingsSectionHeader("What you see & Safety")
                    SettingsNavigationItem(
                        icon = Icons.Default.Explicit,
                        title = "Sensitive Content Controls",
                        subtitle = "Level: ${userSettings.sensitiveContentLevel.name}",
                        onClick = { currentSubScreen = SettingsSubScreen.SAFETY_AND_CONTENT }
                    )
                    SettingsNavigationItem(
                        icon = Icons.Default.RestartAlt,
                        title = "Reset Feed Recommendations",
                        subtitle = "Cold-start reset of user_interests",
                        onClick = { currentSubScreen = SettingsSubScreen.SAFETY_AND_CONTENT }
                    )
                    SettingsNavigationItem(
                        icon = Icons.Default.FamilyRestroom,
                        title = "Supervision & Teen Safety",
                        subtitle = "Parental supervision link",
                        onClick = { currentSubScreen = SettingsSubScreen.SAFETY_AND_CONTENT }
                    )

                    HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))

                    // SECTION 6: YOUR APP, MEDIA & UTILITIES
                    SettingsSectionHeader("Your app and media")
                    SettingsNavigationItem(
                        icon = Icons.Default.Accessibility,
                        title = "Accessibility & Motion",
                        subtitle = "Auto captions, HDR, font scaling",
                        onClick = { currentSubScreen = SettingsSubScreen.UTILITIES_AND_SUPPORT }
                    )
                    SettingsNavigationItem(
                        icon = Icons.Default.DataSaverOn,
                        title = "Media Quality & Data Saver",
                        subtitle = if (userSettings.dataSaverEnabled) "Data Saver ON" else "Highest Quality ON",
                        onClick = { currentSubScreen = SettingsSubScreen.UTILITIES_AND_SUPPORT }
                    )
                    SettingsNavigationItem(
                        icon = Icons.Default.Translate,
                        title = "Language & Auto-translation",
                        subtitle = userSettings.preferredLanguage,
                        onClick = { currentSubScreen = SettingsSubScreen.UTILITIES_AND_SUPPORT }
                    )
                    SettingsNavigationItem(
                        icon = Icons.Default.QrCode,
                        title = "Invites & QR Code Sharing",
                        subtitle = "Native ShareSheet & QR generator",
                        onClick = { currentSubScreen = SettingsSubScreen.UTILITIES_AND_SUPPORT }
                    )

                    HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))

                    // SECTION 7: HELP & SUPPORT
                    SettingsSectionHeader("More info and support")
                    SettingsNavigationItem(
                        icon = Icons.Default.HelpOutline,
                        title = "Help & Support Center",
                        subtitle = "Report a problem & diagnostic logs",
                        onClick = { currentSubScreen = SettingsSubScreen.UTILITIES_AND_SUPPORT }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF8E8E93),
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black) },
        supportingContent = { Text(subtitle, fontSize = 11.sp, color = Color.Gray) },
        leadingContent = { Icon(icon, contentDescription = null, tint = Color.Black) },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray) },
        modifier = Modifier.clickable { onClick() }
    )
}
