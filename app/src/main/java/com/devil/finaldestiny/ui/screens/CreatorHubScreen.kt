package com.devil.finaldestiny.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorHubScreen(
    user: UserProfile,
    onSaveUser: (UserProfile) -> Unit,
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToTools: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showPayoutModal by remember { mutableStateOf(false) }
    var showAgeDialog by remember { mutableStateOf(false) }
    var showAccountSwitchDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var upiInput by remember(user) { mutableStateOf(user.payoutUpi) }
    var selectedAgeLimit by remember(user) { mutableIntStateOf(user.minimumAge) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Creator",
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
            // 1. Ads payments
            CreatorOptionRow("Ads payments") { showPayoutModal = true }

            // 2. Ad placements
            CreatorOptionRow("Ad placements") { Toast.makeText(context, "Ad placements preference updated", Toast.LENGTH_SHORT).show() }

            // 3. Branded content
            CreatorOptionRow("Branded content") {
                val updated = user.copy(brandedContentEnabled = !user.brandedContentEnabled)
                onSaveUser(updated)
                Toast.makeText(context, if (updated.brandedContentEnabled) "Branded content enabled" else "Branded content disabled", Toast.LENGTH_SHORT).show()
            }

            // 4. Crossposting
            CreatorOptionRow("Crossposting") {
                val updated = user.copy(crosspostingEnabled = !user.crosspostingEnabled)
                onSaveUser(updated)
                Toast.makeText(context, if (updated.crosspostingEnabled) "Crossposting enabled" else "Crossposting disabled", Toast.LENGTH_SHORT).show()
            }

            // 5. Share past reels to Facebook
            CreatorOptionRow("Share past reels to Facebook") { Toast.makeText(context, "Reels auto-share connected to Facebook", Toast.LENGTH_SHORT).show() }

            // 6. View counts on profile
            CreatorOptionRow("View counts on profile") { Toast.makeText(context, "View counts visible on profile", Toast.LENGTH_SHORT).show() }

            // 7. Partnership ads
            CreatorOptionRow("Partnership ads") { Toast.makeText(context, "Partnership ads dashboard opened", Toast.LENGTH_SHORT).show() }

            // 8. Link profile to shops
            CreatorOptionRow("Link profile to shops") { Toast.makeText(context, "Linked Creator Shop profile", Toast.LENGTH_SHORT).show() }

            // 9. Response Suggestions
            CreatorOptionRow("Response Suggestions") { Toast.makeText(context, "AI Response Suggestions Active", Toast.LENGTH_SHORT).show() }

            // 10. Saved Reply
            CreatorOptionRow("Saved Reply") { Toast.makeText(context, "Saved DM replies manager", Toast.LENGTH_SHORT).show() }

            // 11. Frequently Asked Questions (with On > status)
            CreatorOptionRow("Frequently Asked Questions", statusText = "On") { showFaqDialog = true }

            // 12. Minimum age
            CreatorOptionRow("Minimum age", statusText = if (user.minimumAge > 0) "${user.minimumAge}+" else "") { showAgeDialog = true }

            // 13. Monetisation Status (Live follower threshold check)
            val isEligible = user.followerCount >= 500
            ListItem(
                headlineContent = { Text("Monetisation Status", fontSize = 15.sp, color = Color.Black) },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isEligible) {
                            Text("Eligible ", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        } else {
                            Text("500 Followers Required ", fontSize = 12.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF8E8E93))
                    }
                },
                modifier = Modifier.clickable {
                    val msg = if (isEligible) "✅ Account Eligible for Creator Monetisation & Gifts!" else "⚠️ Requires 500 followers. Current: ${user.followerCount}"
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            )

            // 14. Appointment requests
            CreatorOptionRow("Appointment requests") { Toast.makeText(context, "Appointment requests enabled", Toast.LENGTH_SHORT).show() }

            // 15. Switch account type
            CreatorOptionRow("Switch account type") { showAccountSwitchDialog = true }

            // 16. Add new professional account
            CreatorOptionRow("Add new professional account") { Toast.makeText(context, "Add professional account flow", Toast.LENGTH_SHORT).show() }

            Spacer(modifier = Modifier.height(16.dp))

            // Edit Profile Text Link
            Text(
                text = "Edit profile",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3897F0),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onNavigateToEditProfile() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ADS PAYMENTS & PAYOUT MODAL
    if (showPayoutModal) {
        AlertDialog(
            onDismissRequest = { showPayoutModal = false },
            title = { Text("Ads Payments & Payouts", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Available Payout Balance: ₹4,850.00", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    Text("Enter UPI ID or Bank Account Details for Payouts:")
                    OutlinedTextField(
                        value = upiInput,
                        onValueChange = { upiInput = it },
                        label = { Text("UPI ID (e.g. name@okaxis)") },
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
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0))
                ) {
                    Text("Save Payout UPI")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPayoutModal = false }) {
                    Text("Close", color = Color(0xFF8E8E93))
                }
            }
        )
    }

    // MINIMUM AGE DIALOG
    if (showAgeDialog) {
        AlertDialog(
            onDismissRequest = { showAgeDialog = false },
            title = { Text("Set Minimum Age Restriction", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Restricts content visibility based on user age:")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(0, 13, 18, 21).forEach { ageLimit ->
                            FilterChip(
                                selected = selectedAgeLimit == ageLimit,
                                onClick = { selectedAgeLimit = ageLimit },
                                label = { Text(if (ageLimit == 0) "No Limit" else "$ageLimit+") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = user.copy(minimumAge = selectedAgeLimit)
                        onSaveUser(updated)
                        showAgeDialog = false
                        Toast.makeText(context, "Minimum age set to $selectedAgeLimit+", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Apply")
                }
            }
        )
    }

    // SWITCH ACCOUNT TYPE DIALOG
    if (showAccountSwitchDialog) {
        val nextType = if (user.accountType == "Personal") "Creator" else "Personal"
        AlertDialog(
            onDismissRequest = { showAccountSwitchDialog = false },
            title = { Text("Switch to $nextType Account?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Switching account type changes your profile features, insights, and category display.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = user.copy(accountType = nextType)
                        onSaveUser(updated)
                        showAccountSwitchDialog = false
                        Toast.makeText(context, "Switched to $nextType Account!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0))
                ) {
                    Text("Switch to $nextType")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountSwitchDialog = false }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            }
        )
    }

    // FAQ DIALOG
    if (showFaqDialog) {
        AlertDialog(
            onDismissRequest = { showFaqDialog = false },
            title = { Text("Frequently Asked Questions", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("1. How do I request brand collaborations?", fontWeight = FontWeight.Bold)
                    Text("DM your portfolio link or email business@finaldestiny.app", fontSize = 12.sp, color = Color(0xFF8E8E93))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("2. When are payouts processed?", fontWeight = FontWeight.Bold)
                    Text("Payouts are processed weekly directly to your linked UPI.", fontSize = 12.sp, color = Color(0xFF8E8E93))
                }
            },
            confirmButton = {
                Button(onClick = { showFaqDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun CreatorOptionRow(
    title: String,
    statusText: String = "",
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontSize = 15.sp, color = Color.Black) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (statusText.isNotBlank()) {
                    Text(statusText, fontSize = 13.sp, color = Color(0xFF8E8E93))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF8E8E93))
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}
