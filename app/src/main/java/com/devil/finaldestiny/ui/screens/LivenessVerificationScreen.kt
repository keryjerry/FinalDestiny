package com.devil.finaldestiny.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.ui.theme.*

@Composable
fun LivenessVerificationScreen(
    onVerificationCompleted: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Blink, 2: Head Turn, 3: Completed
    var isProcessing by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )

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
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(20.dp))
                Icon(Icons.Default.Shield, contentDescription = null, tint = VerifiedBlue, modifier = Modifier.size(44.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Biometric Facial Liveness",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MetallicGold
                )
                Text(
                    text = "Real-Time Blink & Head-Turn Validation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightGold.copy(alpha = 0.8f)
                )
            }

            // Camera Viewfinder Simulation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(260.dp)
                    .scale(if (step < 3) pulseScale else 1.0f)
                    .clip(CircleShape)
                    .background(CardBackground)
                    .border(
                        width = 4.dp,
                        color = if (step == 3) VerifiedBlue else MetallicGold,
                        shape = CircleShape
                    )
            ) {
                if (step == 3) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerifiedBlue, modifier = Modifier.size(72.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Selfie Verified!", color = VerifiedBlue, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Blue Shield Badge Unlocked 🛡️", color = LightGold, fontSize = 12.sp)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Face, contentDescription = null, tint = LightGold, modifier = Modifier.size(96.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (step == 1) "Please Blink Slowly 👁️" else "Slowly Turn Head Right ↗️",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MetallicGold
                        )
                    }
                }
            }

            // Status & Verification Action Button
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                if (step < 3) {
                    Text(
                        text = "Step $step of 2: AI Biometric comparison matching profile pictures with live human operator.",
                        fontSize = 12.sp,
                        color = LightGold.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (step == 1) step = 2
                            else if (step == 2) step = 3
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = if (step == 1) "Simulate Blink Action" else "Simulate Head Turn",
                            color = WineRedDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    Button(
                        onClick = onVerificationCompleted,
                        colors = ButtonDefaults.buttonColors(containerColor = VerifiedBlue),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(text = "Enter Final Destiny Hub 🚀", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
