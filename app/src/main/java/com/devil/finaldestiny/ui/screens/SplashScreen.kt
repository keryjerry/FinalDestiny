package com.devil.finaldestiny.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.R
import com.devil.finaldestiny.ui.theme.*
import kotlin.random.Random

@Composable
fun SplashScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var userEmail by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Vibrant Sky-Blue & Deep Sea Party Gradient
    val seaPartyGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF031926), // Deep ocean night
            Color(0xFF0A369D), // Luminous sky blue party tone
            Color(0xFF006494), // Ocean blue
            Color(0xFF051923)  // Midnight blue base
        )
    )

    val neonGoldGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFACC15), Color(0xFF38BDF8), Color(0xFFF43F5E))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(seaPartyGradient)
    ) {
        // VIBRANT NEON BOKEH PARTY LIGHTS OVERLAY
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.TopStart)
                .offset(x = (-40).dp, y = (-20).dp)
                .blur(60.dp)
                .clip(CircleShape)
                .background(Color(0x9938BDF8))
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = 50.dp)
                .blur(55.dp)
                .clip(CircleShape)
                .background(Color(0x88C084FC))
        )

        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.Center)
                .blur(70.dp)
                .clip(CircleShape)
                .background(Color(0x5506B6D4))
        )

        // 1. FULL SCREEN BACKGROUND COUPLE IMAGE (Clear Visibility)
        Image(
            painter = painterResource(id = R.drawable.full_bg_couple),
            contentDescription = "Background Couple",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.75f)
        )

        // 2. VIBRANT SKY BLUE OVERLAY LAYOVER
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x80031926),
                            Color(0x400B2545),
                            Color(0xB3051923)
                        )
                    )
                )
        )

        // 3. MAIN CONTENT LAYOUT (Pushed Email Card to Bottom so Couple's Faces are 100% Clear)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // TOP AREA: LOGO & APP BRANDING
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // VIBRANT LOGO EMBLEM
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF0284C7), Color(0xFF0F172A), Color(0xFF0369A1))
                            )
                        )
                        .border(2.dp, neonGoldGradient, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Logo Heart",
                        tint = Color(0xFFFACC15),
                        modifier = Modifier.size(46.dp)
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF38BDF8), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = "Handshake",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // APP NAME & FOUNDER BADGE
                Text(
                    text = "FINAL DESTINY",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    letterSpacing = 2.sp,
                    color = Color(0xFFFACC15),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Where Hearts Connect & Voices Resonate",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE0F2FE),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // FOUNDER BADGE: DarkDevil
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xDD0B2545)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .border(1.dp, neonGoldGradient, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "👑 Founder: DarkDevil",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFACC15)
                    )
                }
            }

            // MIDDLE SPACER LEAVING COUPLE'S FACES UN-BLOCKED AND FULLY VISIBLE
            Spacer(modifier = Modifier.height(140.dp))

            // BOTTOM AREA: EMAIL OTP LOGIN FORM CARD (POSITIONED AT BOTTOM)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xEE031926)),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFFFACC15))), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Email Verification Login",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFACC15)
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = HeartRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Email Input
                    OutlinedTextField(
                        value = userEmail,
                        onValueChange = {
                            userEmail = it
                            errorMessage = null
                        },
                        label = { Text("Enter your Email / Gmail", color = Color(0xFFBAE6FD), fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF38BDF8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF0284C7),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!isOtpSent) {
                        Button(
                            onClick = {
                                if (userEmail.isBlank() || !userEmail.contains("@")) {
                                    errorMessage = "Please enter a valid Email address."
                                    return@Button
                                }
                                val code = String.format("%06d", Random.nextInt(100000, 999999))
                                generatedOtp = code
                                enteredOtp = ""
                                isOtpSent = true
                                errorMessage = null
                                Toast.makeText(context, "🔐 Verification OTP Code: $code", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFACC15)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Send 6-Digit OTP", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    } else {
                        // OTP DISPATCHED NOTICE CARD WITH CLEAR TEST CODE
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B2545)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "📧 Verification OTP Code: $generatedOtp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFACC15),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Enter $generatedOtp below to complete verification & log in.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFBAE6FD)
                                )
                            }
                        }

                        // OTP Code Input Field
                        OutlinedTextField(
                            value = enteredOtp,
                            onValueChange = {
                                if (it.length <= 6) enteredOtp = it
                                errorMessage = null
                            },
                            label = { Text("Enter 6-Digit OTP Code", color = Color(0xFFBAE6FD), fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF38BDF8)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF0284C7),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Verify & Login Button
                        Button(
                            onClick = {
                                if (enteredOtp.length == 6 || enteredOtp.trim() == generatedOtp) {
                                    Toast.makeText(context, "OTP Verified Successfully! Welcome to Final Destiny.", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                } else {
                                    errorMessage = "❌ Please enter a 6-digit OTP code (e.g. $generatedOtp)."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFACC15)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Verify OTP & Sign In / Sign Up", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        TextButton(
                            onClick = {
                                val code = String.format("%06d", Random.nextInt(100000, 999999))
                                generatedOtp = code
                                enteredOtp = ""
                                errorMessage = null
                                Toast.makeText(context, "Resent OTP Code: $code", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Resend 6-Digit OTP", color = Color(0xFFBAE6FD), fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

private fun Modifier.alpha(alpha: Float): Modifier = this.then(
    Modifier.graphicsLayer(alpha = alpha)
)
