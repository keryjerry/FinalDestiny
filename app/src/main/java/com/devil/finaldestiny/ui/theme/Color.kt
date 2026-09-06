package com.devil.finaldestiny.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Wine Red & Deep Crimson Theme
val WineRedDark = Color(0xFF1C040D)
val WineRedMedium = Color(0xFF3A0817)
val WineRedLight = Color(0xFF5C1027)
val CrimsonVelvet = Color(0xFF8B0000)
val DeepCrimson = Color(0xFF4A0010)

// Metallic Gold Highlights
val MetallicGold = Color(0xFFFFD700)
val LightGold = Color(0xFFFFF1A8)
val DarkGold = Color(0xFFC59B27)
val GoldGradientEnd = Color(0xFFE6AC00)

// Surface & Card Colors
val CardBackground = Color(0xFF2B0C15)
val CardBackgroundTransparent = Color(0xD92B0C15)
val SurfaceDark = Color(0xFF160309)
val GlassmorphismOverlay = Color(0x33FFD700)
val SeatVelvetBg = Color(0xFF50081B)

// Accent Status & Badge Colors
val LiveIndicatorGreen = Color(0xFF00E676)
val VerifiedBlue = Color(0xFF00B0FF)
val SuperLikeYellow = Color(0xFFFFC107)
val HeartRed = Color(0xFFFF1744)

// VIP Tier Colors
val VipLevel1Silver = Color(0xFFC0C0C0)
val VipLevel2Knight = Color(0xFF90A4AE)
val VipLevel5Wings = Color(0xFFFFD700)
val VipLevel6Vehicle = Color(0xFFFF6D00)
val VipLevel10Crown = Color(0xFFFF1493)

// Theme Gradients
val PrimaryGradient = Brush.verticalGradient(
    colors = listOf(WineRedLight, WineRedMedium, WineRedDark)
)

val GoldGradient = Brush.horizontalGradient(
    colors = listOf(LightGold, MetallicGold, DarkGold)
)

val VelvetSofaGradient = Brush.verticalGradient(
    colors = listOf(CrimsonVelvet, SeatVelvetBg, DeepCrimson)
)