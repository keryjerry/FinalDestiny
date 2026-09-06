package com.devil.finaldestiny.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.ui.components.BrandHeader
import com.devil.finaldestiny.ui.theme.*

@Composable
fun SplashScreen(
    onGoogleSignInSuccess: () -> Unit,
    onShowPermissionsModal: () -> Unit
) {
    var is18PlusChecked by remember { mutableStateOf(true) }
    var isAiScanningConsentChecked by remember { mutableStateOf(true) }
    var isPrivacyPolicyChecked by remember { mutableStateOf(true) }
    var showSmsOtpDialog by remember { mutableStateOf(false) }
    var smsPhoneInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val canProceed = is18PlusChecked && isAiScanningConsentChecked && isPrivacyPolicyChecked

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Top Brand Header Emblem
            BrandHeader(tagline = "Where Hearts Connect & Voices Resonate")

            // Compliance & Consent Checkboxes
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackgroundTransparent),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CrimsonVelvet, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Mandatory Terms & AI Safety Gate",
                        style = MaterialTheme.typography.titleLarge,
                        color = MetallicGold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { is18PlusChecked = !is18PlusChecked }
                    ) {
                        Checkbox(
                            checked = is18PlusChecked,
                            onCheckedChange = { is18PlusChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = MetallicGold)
                        )
                        Text(
                            text = "Mandatory 18+ Adult Confirmation Checkbox",
                            fontSize = 12.sp,
                            color = LightGold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isAiScanningConsentChecked = !isAiScanningConsentChecked }
                    ) {
                        Checkbox(
                            checked = isAiScanningConsentChecked,
                            onCheckedChange = { isAiScanningConsentChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = MetallicGold)
                        )
                        Text(
                            text = "AI Real-Time Vision & Profanity Content Scanning Consent",
                            fontSize = 12.sp,
                            color = LightGold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isPrivacyPolicyChecked = !isPrivacyPolicyChecked }
                    ) {
                        Checkbox(
                            checked = isPrivacyPolicyChecked,
                            onCheckedChange = { isPrivacyPolicyChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = MetallicGold)
                        )
                        Text(
                            text = "Acknowledge Privacy Policy & Community Safety Rules",
                            fontSize = 12.sp,
                            color = LightGold
                        )
                    }
                }
            }

            // Auth Action Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = HeartRed, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Primary Entry Point: Single-tap Google OAuth 2.0
                Button(
                    onClick = {
                        if (canProceed) {
                            onGoogleSignInSuccess()
                        } else {
                            errorMessage = "Please accept all mandatory terms and AI scanning consent."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "G ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = WineRedDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Continue with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = WineRedDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SMS Fallback Button for diamond withdrawal recovery and security
                OutlinedButton(
                    onClick = { showSmsOtpDialog = true },
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = GoldGradient),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = LightGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "SMS Phone OTP Fallback (Account Security)", color = LightGold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onShowPermissionsModal) {
                    Text(text = "View Granular OS Permissions Modal", color = DarkGold, fontSize = 12.sp)
                }
            }
        }
    }

    // SMS OTP Fallback Dialog
    if (showSmsOtpDialog) {
        AlertDialog(
            onDismissRequest = { showSmsOtpDialog = false },
            containerColor = CardBackground,
            title = { Text("SMS OTP Account Recovery", color = MetallicGold) },
            text = {
                Column {
                    Text("Enter mobile number for instant verification code:", fontSize = 12.sp, color = LightGold)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = smsPhoneInput,
                        onValueChange = { smsPhoneInput = it },
                        label = { Text("Phone Number (+91)", color = LightGold) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = DarkGold),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSmsOtpDialog = false
                        onGoogleSignInSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
                ) {
                    Text("Verify & Continue", color = WineRedDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSmsOtpDialog = false }) {
                    Text("Cancel", color = LightGold)
                }
            }
        )
    }
}
