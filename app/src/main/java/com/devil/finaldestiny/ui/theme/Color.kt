package com.devil.finaldestiny.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// BRIGHT SKY BLUE PALETTE
val SkyBlueBgLight = Color(0xFFF0F7FF)        // Airy light sky background
val SkyBlueHeader = Color(0xFFE1F0FF)         // Soft sky header fill
val SkyBlueCardBg = Color(0xFFFFFFFF)         // Crisp white card surface
val SkyBlueBorder = Color(0xFFBBE0FF)         // Modern soft cyan/sky border
val SkyBluePrimary = Color(0xFF0077B6)        // Rich sky blue for primary buttons
val BrightCyanAccent = Color(0xFF00B4D8)      // Vibrant sky cyan accent
val SoftSkyAccent = Color(0xFF90E0EF)        // Soft sky glow

// HIGH CONTRAST TYPOGRAPHY COLORS
val NavyTextPrimary = Color(0xFF0B1E2D)       // Deep navy for sharp headers/title contrast
val SlateTextSecondary = Color(0xFF476072)    // Slate for body text/subtitles
val MutedSkyText = Color(0xFF6C8A9C)          // Muted text for captions/metadata

// LEGACY ALIASES (Backward Compatibility mapped to Sky Blue Palette)
val WineRedDark = SkyBlueBgLight
val WineRedMedium = SkyBlueHeader
val WineRedLight = Color(0xFFD0E8FF)
val CrimsonVelvet = SkyBlueBorder
val DeepCrimson = SkyBluePrimary

// Accent Highlights & Badges
val MetallicGold = BrightCyanAccent
val LightGold = NavyTextPrimary
val DarkGold = SkyBluePrimary
val GoldGradientEnd = BrightCyanAccent

// Surface & Card Colors
val CardBackground = SkyBlueCardBg
val CardBackgroundTransparent = Color(0xF5FFFFFF)
val SurfaceDark = SkyBlueBgLight
val GlassmorphismOverlay = Color(0x3300B4D8)
val SeatVelvetBg = Color(0xFFE3F2FD)

// Status & Badge Colors
val LiveIndicatorGreen = Color(0xFF00C853)
val VerifiedBlue = Color(0xFF0288D1)
val SuperLikeYellow = Color(0xFFFFB300)
val HeartRed = Color(0xFFFF3366)

// VIP Tier Colors
val VipLevel1Silver = Color(0xFF90A4AE)
val VipLevel2Knight = Color(0xFF546E7A)
val VipLevel5Wings = Color(0xFF00B4D8)
val VipLevel6Vehicle = Color(0xFF0288D1)
val VipLevel10Crown = Color(0xFF7B1FA2)

// Theme Gradients
val PrimaryGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFE0F2FE), Color(0xFFF0F9FF), Color(0xFFE2F1FC))
)

val SkyBlueGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFE0F2FE), Color(0xFFF8FAFC))
)

val GoldGradient = Brush.horizontalGradient(
    colors = listOf(BrightCyanAccent, SkyBluePrimary, BrightCyanAccent)
)

val VelvetSofaGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB), Color(0xFF90CAF9))
)