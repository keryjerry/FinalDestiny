package com.devil.finaldestiny.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
fun AccountsCenterScreen(
    userSettings: UserSettingsState,
    onUpdateSettings: (UserSettingsState) -> Unit,
    onLogOutAllSessions: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showPasswordDialog by remember { mutableStateOf(false) }
    var show2faDialog by remember { mutableStateOf(false) }
    var showSessionsDialog by remember { mutableStateOf(false) }
    var showPersonalDetailsDialog by remember { mutableStateOf(false) }

    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }

    val activeSessions = remember {
        listOf(
            LoginSessionItem("s1", "Samsung Galaxy S24 Ultra", "Kolkata, West Bengal, India", "103.211.x.x", "Active Now", isCurrentDevice = true),
            LoginSessionItem("s2", "Chrome Browser (Windows 11)", "Siliguri, West Bengal, India", "49.37.x.x", "2 hours ago", isCurrentDevice = false),
            LoginSessionItem("s3", "iPad Pro 12.9\"", "Mumbai, Maharashtra, India", "157.33.x.x", "3 days ago", isCurrentDevice = false)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Meta • Accounts Center",
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
            // META ACCOUNTS CENTER BANNER
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("∞", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF006494))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Meta Accounts Center", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Manage your connected experiences and account settings across Final Destiny, Instagram, and Meta technologies.",
                        fontSize = 12.sp,
                        color = Color(0xFF6E6E73),
                        lineHeight = 16.sp
                    )
                }
            }

            // SECTION 1: ACCOUNT SETTINGS
            Text(
                "Account settings",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E8E93),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
            )

            // Password & Security
            ListItem(
                headlineContent = { Text("Password & Security", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("Change password, 2FA & security checks", fontSize = 11.sp, color = Color.Gray) },
                leadingContent = { Icon(Icons.Default.Security, contentDescription = null, tint = Color.Black) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.clickable { showPasswordDialog = true }
            )

            // Active Login Sessions
            ListItem(
                headlineContent = { Text("Active Login Sessions", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("3 active device sessions logged in", fontSize = 11.sp, color = Color.Gray) },
                leadingContent = { Icon(Icons.Default.Devices, contentDescription = null, tint = Color.Black) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.clickable { showSessionsDialog = true }
            )

            // Personal Details
            ListItem(
                headlineContent = { Text("Personal Details", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("${userSettings.email} • ${userSettings.phoneNumber}", fontSize = 11.sp, color = Color.Gray) },
                leadingContent = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color.Black) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.clickable { showPersonalDetailsDialog = true }
            )

            // Connected Payment Methods & Wallets
            ListItem(
                headlineContent = { Text("Payments & Wallets", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("Wallet Balance: ₹${userSettings.walletBalanceInr} • UPI Linked", fontSize = 11.sp, color = Color(0xFF166534)) },
                leadingContent = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.Black) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.clickable {
                    Toast.makeText(context, "💳 Wallet Balance: ₹${userSettings.walletBalanceInr} (UPI Auto-Linked)", Toast.LENGTH_LONG).show()
                }
            )

            HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp)

            // SECTION 2: DEVICE PERMISSIONS (NATIVE ANDROID INTENT)
            Text(
                "Device & App Permissions",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E8E93),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
            )

            ListItem(
                headlineContent = { Text("Android System App Permissions", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("Manage Camera, Microphone, Storage & Location access", fontSize = 11.sp, color = Color.Gray) },
                leadingContent = { Icon(Icons.Default.PermDeviceInformation, contentDescription = null, tint = Color(0xFF3897F0)) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.clickable {
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                        Toast.makeText(context, "Opening Android Settings...", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Unable to open device settings", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // PASSWORD & 2FA DIALOG
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Password & 2FA Security", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        label = { Text("Confirm New Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enable Two-Factor Auth (2FA)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = userSettings.is2faEnabled,
                            onCheckedChange = { isChecked ->
                                onUpdateSettings(userSettings.copy(is2faEnabled = isChecked))
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPasswordInput.isNotBlank() && newPasswordInput == confirmPasswordInput) {
                            Toast.makeText(context, "✅ Password changed successfully!", Toast.LENGTH_SHORT).show()
                            showPasswordDialog = false
                        } else if (newPasswordInput.isBlank()) {
                            showPasswordDialog = false
                        } else {
                            Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ACTIVE LOGIN SESSIONS DIALOG
    if (showSessionsDialog) {
        AlertDialog(
            onDismissRequest = { showSessionsDialog = false },
            title = { Text("Active Login Sessions", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activeSessions.forEach { session ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (session.isCurrentDevice) Color(0xFFF0FDF4) else Color(0xFFF8F9FA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    session.deviceName + if (session.isCurrentDevice) " (This Device)" else "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (session.isCurrentDevice) Color(0xFF166534) else Color.Black
                                )
                                Text("${session.location} • ${session.ipAddress}", fontSize = 11.sp, color = Color.Gray)
                                Text("Last active: ${session.lastActive}", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onLogOutAllSessions()
                        showSessionsDialog = false
                        Toast.makeText(context, "🔒 Logged out of all other devices!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Log out of all devices", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSessionsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // PERSONAL DETAILS DIALOG
    if (showPersonalDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showPersonalDetailsDialog = false },
            title = { Text("Personal Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Contact Email: ${userSettings.email}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Phone Number: ${userSettings.phoneNumber}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Birthday: ${userSettings.birthday}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Account ownership and control options are synced with Meta.", fontSize = 11.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = { showPersonalDetailsDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
