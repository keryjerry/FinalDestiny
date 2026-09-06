package com.devil.finaldestiny.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.data.SupabaseAuthClient
import com.devil.finaldestiny.ui.components.BrandHeader
import com.devil.finaldestiny.ui.theme.*

@Composable
fun SplashScreen(
    onGoogleSignInSuccess: () -> Unit,
    onShowPermissionsModal: () -> Unit
) {
    val context = LocalContext.current
    var is18PlusChecked by remember { mutableStateOf(true) }
    var isAiScanningConsentChecked by remember { mutableStateOf(true) }
    var isPrivacyPolicyChecked by remember { mutableStateOf(true) }

    var isAuthenticating by remember { mutableStateOf(false) }
    var showSmsOtpDialog by remember { mutableStateOf(false) }
    var smsPhoneInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Premium Deep Wine-Red Gradient Background (#3A0817 to #1C040D)
    val wineRedGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF3A0817),
            Color(0xFF280611),
            Color(0xFF1C040D)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(wineRedGradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Brand Header with Metallic Gold Title "Final Destiny"
            BrandHeader(tagline = "Where Hearts Connect & Voices Resonate")

            // Mandatory Legal Age Confirmation & AI Scanning Consent Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackgroundTransparent),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, CrimsonVelvet, RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = MetallicGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mandatory Legal & AI Safety Gate",
                            style = MaterialTheme.typography.titleLarge,
                            color = MetallicGold
                        )
                    }

                    HorizontalDivider(color = CrimsonVelvet.copy(alpha = 0.6f))

                    // Mandatory 18+ Legal Age Confirmation Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { is18PlusChecked = !is18PlusChecked }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = is18PlusChecked,
                            onCheckedChange = { is18PlusChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = MetallicGold, checkmarkColor = WineRedDark)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mandatory 18+ Legal Age Confirmation (18+ Adult Only)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightGold
                        )
                    }

                    // AI Real-Time Vision & Profanity Scanning Consent
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAiScanningConsentChecked = !isAiScanningConsentChecked }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = isAiScanningConsentChecked,
                            onCheckedChange = { isAiScanningConsentChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = MetallicGold, checkmarkColor = WineRedDark)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI Real-Time Vision (2.5s Stream Sentinel) & Profanity Consent",
                            fontSize = 12.sp,
                            color = LightGold
                        )
                    }

                    // Privacy Policy Acknowledgment
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isPrivacyPolicyChecked = !isPrivacyPolicyChecked }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = isPrivacyPolicyChecked,
                            onCheckedChange = { isPrivacyPolicyChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = MetallicGold, checkmarkColor = WineRedDark)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Accept Community Safety Rules & Zero Anti-Gaming Policy",
                            fontSize = 12.sp,
                            color = LightGold
                        )
                    }
                }
            }

            // Auth Action Center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = HeartRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Primary Entry Point: "Continue with Google" connected to Supabase Auth Client
                Button(
                    onClick = {
                        if (!is18PlusChecked) {
                            errorMessage = "⚠️ Mandatory: You must confirm that you are 18 years or older."
                            return@Button
                        }
                        if (!isAiScanningConsentChecked || !isPrivacyPolicyChecked) {
                            errorMessage = "⚠️ Please accept the AI content scanning consent & community terms."
                            return@Button
                        }

                        errorMessage = null
                        isAuthenticating = true

                        // Connect to Supabase Auth Client for Google OAuth
                        SupabaseAuthClient.signInWithGoogle(context) { isSuccess, err ->
                            isAuthenticating = false
                            if (isSuccess) {
                                onGoogleSignInSuccess()
                            } else {
                                errorMessage = err ?: "Supabase OAuth Sign-In failed"
                            }
                        }
                    },
                    enabled = !isAuthenticating,
                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    if (isAuthenticating) {
                        CircularProgressIndicator(
                            color = WineRedDark,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Connecting to Supabase Auth...", color = WineRedDark, fontWeight = FontWeight.Bold)
                    } else {
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
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SMS Phone OTP Fallback Button
                OutlinedButton(
                    onClick = { showSmsOtpDialog = true },
                    border = BorderStroke(1.dp, GoldGradient),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = LightGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "SMS Phone OTP Fallback (Account Recovery)", color = LightGold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onShowPermissionsModal) {
                    Text(text = "View Granular OS Privacy Permissions", color = DarkGold, fontSize = 12.sp)
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
