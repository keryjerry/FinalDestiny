package com.devil.finaldestiny.ui.screens

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
import com.devil.finaldestiny.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorToolsScreen(
    user: UserProfile,
    onSaveUser: (UserProfile) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showRecapSheet by remember { mutableStateOf(false) }
    var showPayoutModal by remember { mutableStateOf(false) }
    var showSavedRepliesDialog by remember { mutableStateOf(false) }
    var newReplyText by remember { mutableStateOf("") }
    var upiInput by remember(user) { mutableStateOf(user.payoutUpi) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tools",
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
            // SECTION 1: YOUR TOOLS
            Text(
                "Your tools",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            // Monthly recap
            ToolRowItem(
                icon = Icons.Default.History,
                title = "Monthly recap",
                subtitle = "See what you made happen last month.",
                badgeText = "New",
                onClick = { showRecapSheet = true }
            )

            // Best practices
            ToolRowItem(
                icon = Icons.Default.School,
                title = "Best practices",
                onClick = { Toast.makeText(context, "Opening Creator Best Practices guide...", Toast.LENGTH_SHORT).show() }
            )

            // Inspiration
            ToolRowItem(
                icon = Icons.Default.Lightbulb,
                title = "Inspiration",
                onClick = { Toast.makeText(context, "Opening Trending Inspiration hub...", Toast.LENGTH_SHORT).show() }
            )

            // Branded content
            ToolRowItem(
                icon = Icons.Default.AccountBox,
                title = "Branded content",
                onClick = { Toast.makeText(context, "Branded Content Partnerships", Toast.LENGTH_SHORT).show() }
            )

            // Partnership ads
            ToolRowItem(
                icon = Icons.Default.PeopleOutline,
                title = "Partnership ads",
                onClick = { Toast.makeText(context, "Partnership Ads active campaigns", Toast.LENGTH_SHORT).show() }
            )

            // Ad tools
            ToolRowItem(
                icon = Icons.Default.TrendingUp,
                title = "Ad tools",
                onClick = { Toast.makeText(context, "Promote & Boost Post tools", Toast.LENGTH_SHORT).show() }
            )

            // Competitive insights
            ToolRowItem(
                icon = Icons.Default.CellTower,
                title = "Competitive insights",
                subtitle = "Get Meta One",
                onClick = { Toast.makeText(context, "Competitive Analytics Suite", Toast.LENGTH_SHORT).show() }
            )

            // Trial reels
            ToolRowItem(
                icon = Icons.Default.PlayCircle,
                title = "Trial reels",
                badgeText = "New",
                onClick = {
                    val updated = user.copy(trialReelsEnabled = !user.trialReelsEnabled)
                    onSaveUser(updated)
                    Toast.makeText(context, if (updated.trialReelsEnabled) "Trial reels enabled" else "Trial reels disabled", Toast.LENGTH_SHORT).show()
                }
            )

            // Payouts
            ToolRowItem(
                icon = Icons.Default.AccountBalance,
                title = "Payouts",
                onClick = { showPayoutModal = true }
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp)

            // SECTION 2: TOOLS TO TRY
            Text(
                "Tools to try",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            // Saved replies
            ToolRowItem(
                icon = Icons.Default.Send,
                title = "Saved replies",
                subtitle = "Save replies to common questions",
                onClick = { showSavedRepliesDialog = true }
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // MONTHLY RECAP SHEET
    if (showRecapSheet) {
        AlertDialog(
            onDismissRequest = { showRecapSheet = false },
            title = { Text("Monthly Recap 📊", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Total Views Last 30 Days: 4,820", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3897F0))
                    Text("Post Impressions: 12,450", fontSize = 13.sp)
                    Text("New Followers: +145", fontSize = 13.sp, color = Color(0xFF10B981))
                    Text("Profile Interactions: 890", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showRecapSheet = false }) {
                    Text("Close")
                }
            }
        )
    }

    // PAYOUTS MODAL
    if (showPayoutModal) {
        AlertDialog(
            onDismissRequest = { showPayoutModal = false },
            title = { Text("Creator Payouts 🏦", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Current Earnings Balance: ₹4,850.00", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    Text("Linked Payout UPI / Bank Account:")
                    OutlinedTextField(
                        value = upiInput,
                        onValueChange = { upiInput = it },
                        label = { Text("UPI Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = user.copy(payoutUpi = upiInput)
                        onSaveUser(updated)
                        showPayoutModal = false
                        Toast.makeText(context, "Payout details updated!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save Payout UPI")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPayoutModal = false }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            }
        )
    }

    // SAVED REPLIES MANAGER DIALOG
    if (showSavedRepliesDialog) {
        AlertDialog(
            onDismissRequest = { showSavedRepliesDialog = false },
            title = { Text("Saved Replies", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Automated Quick DM Responses:", fontSize = 12.sp, color = Color(0xFF8E8E93))
                    user.savedReplies.forEach { reply ->
                        Surface(
                            color = Color(0xFFF2F2F7),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Text(reply, fontSize = 12.sp, color = Color.Black, modifier = Modifier.padding(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newReplyText,
                        onValueChange = { newReplyText = it },
                        label = { Text("Add new saved reply") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newReplyText.isNotBlank()) {
                            val updatedReplies = user.savedReplies + newReplyText
                            val updated = user.copy(savedReplies = updatedReplies)
                            onSaveUser(updated)
                            newReplyText = ""
                            Toast.makeText(context, "Added saved reply!", Toast.LENGTH_SHORT).show()
                        }
                        showSavedRepliesDialog = false
                    }
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavedRepliesDialog = false }) {
                    Text("Close", color = Color(0xFF8E8E93))
                }
            }
        )
    }
}

@Composable
private fun ToolRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontSize = 15.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
        supportingContent = if (subtitle != null) {
            { Text(subtitle, fontSize = 11.sp, color = Color(0xFF8E8E93)) }
        } else null,
        leadingContent = { Icon(icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp)) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (badgeText != null) {
                    Surface(
                        color = Color(0xFF3897F0),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            badgeText,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF8E8E93))
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}
