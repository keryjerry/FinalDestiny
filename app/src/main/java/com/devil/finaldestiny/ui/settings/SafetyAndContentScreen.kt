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
fun SafetyAndContentScreen(
    userSettings: UserSettingsState,
    onUpdateSettings: (UserSettingsState) -> Unit,
    onResetFeedInterests: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showFeedResetDialog by remember { mutableStateOf(false) }
    var showSupervisionDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Safety & Content Controls",
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
            // SECTION 1: SENSITIVE CONTENT CONTROLS
            Text(
                "Sensitive Content Controls",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E8E93),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Select Content Filtering Level:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    val levels = SensitiveContentLevel.entries.toTypedArray()
                    levels.forEach { lvl ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdateSettings(userSettings.copy(sensitiveContentLevel = lvl))
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (userSettings.sensitiveContentLevel == lvl),
                                onClick = { onUpdateSettings(userSettings.copy(sensitiveContentLevel = lvl)) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    when (lvl) {
                                        SensitiveContentLevel.LOW -> "Less (Low Filter)"
                                        SensitiveContentLevel.MEDIUM -> "Standard (Medium Filter)"
                                        SensitiveContentLevel.HIGH -> "More Strict (High Filter)"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    when (lvl) {
                                        SensitiveContentLevel.LOW -> "Allows more suggestive or sensitive media in Explore & Reels."
                                        SensitiveContentLevel.MEDIUM -> "Balanced recommendations based on safety guidelines."
                                        SensitiveContentLevel.HIGH -> "Maximum filtering of sensitive or graphic content."
                                    },
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp)

                    // Nudity Protection Flag
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Nudity & AI Safety Protection", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Automatically blurs potential explicit photos in DMs.", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = userSettings.nudityProtectionEnabled,
                            onCheckedChange = { onUpdateSettings(userSettings.copy(nudityProtectionEnabled = it)) }
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

            // SECTION 2: COLD-START FEED RESET
            Text(
                "Recommendation Feed Reset",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E8E93),
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
            )

            ListItem(
                headlineContent = { Text("Reset Feed Recommendations", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("Clears learned interest weights in user_interests to give a clean cold-start feed.", fontSize = 11.sp, color = Color.Gray) },
                leadingContent = { Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color(0xFF3897F0)) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.clickable { showFeedResetDialog = true }
            )

            HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

            // SECTION 3: TEEN SAFETY & PARENTAL SUPERVISION
            Text(
                "Supervision & Teen Safety",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E8E93),
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
            )

            ListItem(
                headlineContent = { Text("Parental Supervision Center", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                supportingContent = {
                    Text(
                        if (userSettings.teenSupervisionLinked) "Linked with Parent Account" else "Send link request to parent or guardian to set screen time limits.",
                        fontSize = 11.sp,
                        color = if (userSettings.teenSupervisionLinked) Color(0xFF166534) else Color.Gray
                    )
                },
                leadingContent = { Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = Color(0xFF10B981)) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.clickable { showSupervisionDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // FEED RESET DIALOG
    if (showFeedResetDialog) {
        AlertDialog(
            onDismissRequest = { showFeedResetDialog = false },
            title = { Text("Reset Suggestion Feed?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This action will clear all learned interest weights from user_interests on Supabase. Your home feed and reels feed will be reset to a clean cold-start state.",
                    fontSize = 13.sp,
                    color = Color.Black
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetFeedInterests()
                        showFeedResetDialog = false
                        Toast.makeText(context, "✨ Feed reset to cold-start state!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0))
                ) {
                    Text("Confirm Reset", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // SUPERVISION DIALOG
    if (showSupervisionDialog) {
        AlertDialog(
            onDismissRequest = { showSupervisionDialog = false },
            title = { Text("Parental Supervision Setup", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Parental Supervision lets parents see time spent, accounts followed, and enforce daily limits.", fontSize = 12.sp, color = Color.Gray)
                    Button(
                        onClick = {
                            onUpdateSettings(userSettings.copy(teenSupervisionLinked = true))
                            showSupervisionDialog = false
                            Toast.makeText(context, "Parental link invitation sent!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send Supervision Invite", color = Color.White)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSupervisionDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
