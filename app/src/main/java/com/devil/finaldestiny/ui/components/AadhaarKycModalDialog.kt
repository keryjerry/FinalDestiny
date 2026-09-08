package com.devil.finaldestiny.ui.components

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.engine.AadhaarKycRecord
import com.devil.finaldestiny.engine.KycStatus
import com.devil.finaldestiny.ui.theme.*

@Composable
fun AadhaarKycModalDialog(
    onDismiss: () -> Unit,
    onRequestOtp: (String) -> Unit,
    onVerifyOtp: (String, String) -> Unit,
    onVerifySelfie: (Bitmap) -> Unit,
    currentKycRecord: AadhaarKycRecord?
) {
    var aadhaarInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(1) } // 1: Aadhaar Input, 2: OTP Verification, 3: Camera Selfie Face Match
    var selfieBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            selfieBitmap = bitmap
            onVerifySelfie(bitmap)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = MetallicGold, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("🆔 Host Aadhaar KYC Verification", color = MetallicGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Verify your identity with UIDAI Aadhaar eKYC to unlock host streaming & financial payout features.",
                    fontSize = 11.sp,
                    color = LightGold
                )

                if (step == 1) {
                    OutlinedTextField(
                        value = aadhaarInput,
                        onValueChange = { if (it.length <= 12 && it.all { ch -> ch.isDigit() }) aadhaarInput = it },
                        label = { Text("Enter 12-Digit Aadhaar Number", color = LightGold, fontSize = 11.sp) },
                        placeholder = { Text("1234 5678 9012", color = LightGold.copy(0.4f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MetallicGold,
                            unfocusedBorderColor = DarkGold,
                            focusedTextColor = LightGold,
                            unfocusedTextColor = LightGold
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (step == 2) {
                    Text("OTP sent to Aadhaar-linked mobile number.", fontSize = 11.sp, color = LiveIndicatorGreen)
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) otpInput = it },
                        label = { Text("Enter 6-Digit OTP", color = LightGold, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MetallicGold,
                            unfocusedBorderColor = DarkGold,
                            focusedTextColor = LightGold,
                            unfocusedTextColor = LightGold
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (step == 3) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Step 3: Capture Face Selfie for Liveness & Aadhaar Match", fontSize = 11.sp, color = LightGold)
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(WineRedDark)
                                .border(1.5.dp, MetallicGold, RoundedCornerShape(14.dp))
                        ) {
                            if (selfieBitmap != null) {
                                Image(
                                    bitmap = selfieBitmap!!.asImageBitmap(),
                                    contentDescription = "Selfie",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = LightGold, modifier = Modifier.size(36.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = WineRedMedium)
                        ) {
                            Text("Open Camera 📸", color = LightGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step == 1 && aadhaarInput.length == 12) {
                        onRequestOtp(aadhaarInput)
                        step = 2
                    } else if (step == 2 && otpInput.length == 6) {
                        onVerifyOtp(aadhaarInput, otpInput)
                        step = 3
                    } else if (step == 3 && selfieBitmap != null) {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
            ) {
                Text(
                    text = when (step) {
                        1 -> "Request OTP 📩"
                        2 -> "Verify OTP ✓"
                        else -> "Complete KYC 🚀"
                    },
                    color = WineRedDark,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = LightGold)
            }
        }
    )
}
