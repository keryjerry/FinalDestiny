package com.devil.finaldestiny.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.R
import com.devil.finaldestiny.data.AppRepository
import com.devil.finaldestiny.data.AuthResult
import com.devil.finaldestiny.data.SupabaseAuthClient
import com.devil.finaldestiny.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    repository: AppRepository? = null,
    onLoginSuccess: () -> Unit

) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var userEmail by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        SupabaseAuthClient.init(context)
        if (SupabaseAuthClient.isAuthenticated && SupabaseAuthClient.hasValidSession(context)) {
            onLoginSuccess()
        }
    }

    // Vibrant Sky-Blue & Deep Sea Party Gradient
    val seaPartyGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF031926),
            Color(0xFF0A369D),
            Color(0xFF006494),
            Color(0xFF051923)
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
        // FULL-BLEED OFFICIAL APP ICON BACKGROUND (CLEAR, VIBRANT & SHARP)
        SafeLogoImage(
            contentDescription = "Official App Logo Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.75f)
        )

        // ELEGANT GRADIENT OVERLAY SCRIM FOR HIGH CONTRAST & LEGIBILITY
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x550B1017),
                            Color(0x200B1017),
                            Color(0xCC0B1017)
                        )
                    )
                )
        )

        // MAIN CONTENT LAYOUT
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
                // VIBRANT OFFICIAL LOGO EMBLEM
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(2.dp, neonGoldGradient, RoundedCornerShape(20.dp))
                ) {
                    SafeLogoImage(
                        contentDescription = "Final Destiny Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

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

                // FOUNDER BRANDING
                Text(
                    text = "Founder: Dilshad",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFFD700),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(80.dp))

            // BOTTOM AREA: REAL SUPABASE AUTH FORM CARD (LOGIN / REGISTER)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xEE031926)),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFFFACC15))), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOtpSent) "Enter 6-Digit OTP" else "Secure Login",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFACC15)
                        )
                    }

                    if (errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, HeartRed, RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "⚠️ $errorMessage",
                                color = Color(0xFFFCA5A5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Email Input Field (Always shown, but disabled when OTP is sent)
                    OutlinedTextField(
                        value = userEmail,
                        onValueChange = {
                            userEmail = it
                            errorMessage = null
                        },
                        label = { Text("Email Address", color = Color(0xFFBAE6FD), fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF38BDF8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF0284C7),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        enabled = !isLoading && !isOtpSent,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // OTP Input Field (Only shown when OTP is sent)
                    if (isOtpSent) {
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = {
                                if (it.length <= 6) {
                                    otpCode = it
                                }
                                errorMessage = null
                            },
                            label = { Text("6-Digit OTP Code", color = Color(0xFFBAE6FD), fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF38BDF8)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF0284C7),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ACTION BUTTON
                    Button(
                        onClick = {
                            if (isLoading) return@Button
                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null

                                if (!isOtpSent) {
                                    val result = SupabaseAuthClient.sendOtpToEmail(
                                        context = context,
                                        email = userEmail
                                    )
                                    isLoading = false

                                    when (result) {
                                        is AuthResult.Success -> {
                                            Toast.makeText(context, "📩 OTP Sent to $userEmail", Toast.LENGTH_SHORT).show()
                                            isOtpSent = true
                                        }
                                        is AuthResult.Error -> {
                                            Toast.makeText(context, "❌ OTP Failed: ${result.message}", Toast.LENGTH_LONG).show()
                                            errorMessage = result.message
                                        }
                                    }
                                } else {
                                    val result = SupabaseAuthClient.verifyEmailOtp(
                                        context = context,
                                        email = userEmail,
                                        otp = otpCode
                                    )
                                    isLoading = false

                                    when (result) {
                                        is AuthResult.Success -> {
                                            Toast.makeText(context, "✅ Verified and Logged In!", Toast.LENGTH_SHORT).show()
                                            // The backend will create the profile automatically on verification if it's new.
                                            // Since we don't have a name field anymore, we just pass null for the name and the repository will handle it.
                                            repository?.syncAuthenticatedUser(result.uid, result.email, null)
                                            onLoginSuccess()
                                        }
                                        is AuthResult.Error -> {
                                            Toast.makeText(context, "❌ Verification Failed: ${result.message}", Toast.LENGTH_LONG).show()
                                            errorMessage = result.message
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOtpSent) Color(0xFFFACC15) else Color(0xFF38BDF8)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF0F172A),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing...", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        } else {
                            Text(
                                text = if (isOtpSent) "Verify & Login 🚀" else "Send OTP 📩",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
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

@Composable
fun SafeLogoImage(
    contentDescription: String,
    contentScale: ContentScale,
    modifier: Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}
