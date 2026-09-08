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

    var isSignUpMode by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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

        // 1. FULL SCREEN BACKGROUND COUPLE IMAGE
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

        // 3. MAIN CONTENT LAYOUT
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
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
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

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xDD0B2545)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .border(1.dp, neonGoldGradient, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "👑 Supabase GoTrue Auth",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFACC15)
                    )
                }
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
                    // TAB SELECTOR: SIGN IN VS SIGN UP
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0B2545))
                            .padding(4.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isSignUpMode) Color(0xFF38BDF8) else Color.Transparent)
                                .clickable {
                                    isSignUpMode = false
                                    errorMessage = null
                                }
                        ) {
                            Text(
                                text = "Sign In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (!isSignUpMode) Color(0xFF0F172A) else Color(0xFFBAE6FD)
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSignUpMode) Color(0xFFFACC15) else Color.Transparent)
                                .clickable {
                                    isSignUpMode = true
                                    errorMessage = null
                                }
                        ) {
                            Text(
                                text = "Register / Sign Up",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSignUpMode) Color(0xFF0F172A) else Color(0xFFBAE6FD)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSignUpMode) "Create Supabase Account" else "Supabase Authentication Login",
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

                    // Full Name Input (Only shown in Sign Up mode)
                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = userName,
                            onValueChange = {
                                userName = it
                                errorMessage = null
                            },
                            label = { Text("Full Name / Display Name", color = Color(0xFFBAE6FD), fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF38BDF8)) },
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

                    // Email Input Field
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
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password Input Field
                    OutlinedTextField(
                        value = userPassword,
                        onValueChange = {
                            userPassword = it
                            errorMessage = null
                        },
                        label = { Text("Password", color = Color(0xFFBAE6FD), fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF38BDF8)) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Password Visibility",
                                    tint = Color(0xFF38BDF8)
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

                    Spacer(modifier = Modifier.height(4.dp))

                    // ACTION BUTTON (REAL NETWORK CALL TO SUPABASE GO-TRUE AUTH SERVER)
                    Button(
                        onClick = {
                            if (isLoading) return@Button
                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null

                                if (isSignUpMode) {
                                    val result = SupabaseAuthClient.signUpWithEmail(
                                        context = context,
                                        name = userName,
                                        email = userEmail,
                                        pass = userPassword
                                    )
                                    isLoading = false

                                    when (result) {
                                        is AuthResult.Success -> {
                                            Toast.makeText(context, "🎉 Welcome to Final Destiny, ${result.email}!", Toast.LENGTH_SHORT).show()
                                            repository?.syncAuthenticatedUser(result.uid, result.email, userName)
                                            onLoginSuccess()
                                        }
                                        is AuthResult.Error -> {
                                            Toast.makeText(context, "❌ Sign Up Failed: ${result.message}", Toast.LENGTH_LONG).show()
                                            errorMessage = result.message
                                        }
                                    }
                                } else {
                                    val result = SupabaseAuthClient.signInWithEmail(
                                        context = context,
                                        email = userEmail,
                                        pass = userPassword
                                    )
                                    isLoading = false

                                    when (result) {
                                        is AuthResult.Success -> {
                                            Toast.makeText(context, "✅ Signed in as ${result.email}", Toast.LENGTH_SHORT).show()
                                            repository?.syncAuthenticatedUser(result.uid, result.email, null)
                                            onLoginSuccess()
                                        }
                                        is AuthResult.Error -> {
                                            Toast.makeText(context, "❌ Login Failed: ${result.message}", Toast.LENGTH_LONG).show()
                                            errorMessage = result.message
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSignUpMode) Color(0xFFFACC15) else Color(0xFF38BDF8)
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
                            Text("Connecting to Supabase...", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        } else {
                            Text(
                                text = if (isSignUpMode) "Register & Create Account 🚀" else "Sign In with Supabase 🔐",
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
