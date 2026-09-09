package com.devil.finaldestiny.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun NotificationsAndUsageScreen(
    userSettings: UserSettingsState,
    onUpdateSettings: (UserSettingsState) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Notifications, 1: Quiet Mode & Time spent
    val tabs = listOf("Notifications Engine", "Quiet Mode & Screen Time")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notifications & Usage",
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF3897F0)
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
                    // TAB 0: NOTIFICATIONS ENGINE
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Push, Email & SMS Notifications", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Control how and when Final Destiny alerts you.", fontSize = 11.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Likes & Reactions
                        ListItem(
                            headlineContent = { Text("Likes & Reactions", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            leadingContent = { Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFED4956)) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.pushLikesEnabled,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(pushLikesEnabled = it)) }
                                )
                            }
                        )

                        // Mentions & Tags
                        ListItem(
                            headlineContent = { Text("Mentions & Tags", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            leadingContent = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = Color(0xFF3897F0)) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.pushMentionsEnabled,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(pushMentionsEnabled = it)) }
                                )
                            }
                        )

                        // Direct Messages
                        ListItem(
                            headlineContent = { Text("Direct Messages", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            leadingContent = { Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF7000FF)) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.pushDirectMessagesEnabled,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(pushDirectMessagesEnabled = it)) }
                                )
                            }
                        )

                        // Audio & Video Calls
                        ListItem(
                            headlineContent = { Text("Audio & Video Room Calls", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            leadingContent = { Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF10B981)) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.pushCallsEnabled,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(pushCallsEnabled = it)) }
                                )
                            }
                        )

                        // Security & Login Alerts
                        ListItem(
                            headlineContent = { Text("Security & Login Alerts", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                            leadingContent = { Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFE65100)) },
                            trailingContent = {
                                Switch(
                                    checked = userSettings.pushSecurityAlertsEnabled,
                                    onCheckedChange = { onUpdateSettings(userSettings.copy(pushSecurityAlertsEnabled = it)) }
                                )
                            }
                        )
                    }
                }

                1 -> {
                    // TAB 1: QUIET MODE / DND & TIME MANAGEMENT
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Quiet Mode / Do Not Disturb", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Mute notifications during scheduled hours.", fontSize = 11.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Enable Quiet Mode", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Switch(
                                        checked = userSettings.quietModeEnabled,
                                        onCheckedChange = { onUpdateSettings(userSettings.copy(quietModeEnabled = it)) }
                                    )
                                }

                                if (userSettings.quietModeEnabled) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Quiet Mode Schedule: ${userSettings.quietModeStartTime} to ${userSettings.quietModeEndTime}", fontSize = 12.sp, color = Color(0xFF3897F0), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Time Spent & Usage Limits", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Daily average on Final Destiny: ${userSettings.dailyScreenTimeMinutes} mins", fontSize = 11.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(10.dp))

                        // DAILY USAGE BAR CHART SIMULATION
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Weekly Daily Average", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                ) {
                                    val days = listOf("M" to 30, "T" to 45, "W" to 20, "T" to 55, "F" to 40, "S" to 70, "S" to 42)
                                    days.forEach { (day, mins) ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom,
                                            modifier = Modifier.fillMaxHeight()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(16.dp)
                                                    .height((mins * 1.2).dp)
                                                    .background(if (day == "S") Color(0xFF3897F0) else Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(day, fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Break Reminders Dropdown
                        Text("Set Break Reminder", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            listOf(0, 15, 30, 45).forEach { mins ->
                                FilterChip(
                                    selected = (userSettings.breakReminderMinutes == mins),
                                    onClick = {
                                        onUpdateSettings(userSettings.copy(breakReminderMinutes = mins))
                                        Toast.makeText(context, if (mins == 0) "Break reminders off" else "Reminder set for every $mins mins", Toast.LENGTH_SHORT).show()
                                    },
                                    label = { Text(if (mins == 0) "Off" else "$mins mins", fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
