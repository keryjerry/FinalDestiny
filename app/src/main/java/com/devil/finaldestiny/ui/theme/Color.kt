package com.devil.finaldestiny.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// WARM DULL OFF-WHITE EYE-CARE PALETTE (#F7F5F0)
val SkyBlueBgLight = Color(0xFFF7F5F0)        // Warm eggshell / dull cream-white canvas background (#F7F5F0)
val SkyBlueHeader = Color(0xFFEFECE6)         // Soft warm header fill
val SkyBlueCardBg = Color(0xFFF0EEE8)         // Warm dull off-white card surface (#F0EEE8 / #FAF8F5)
val SkyBlueBorder = Color(0xFFE5E0D8)         // Soft warm border (#E5E0D8)
val SkyBluePrimary = Color(0xFF0284C7)        // Muted sky blue for primary buttons
val BrightCyanAccent = Color(0xFF0284C7)      // Muted sky cyan accent
val SoftSkyAccent = Color(0xFFE0E7FF)        // Soft warm glow

// HIGH CONTRAST TYPOGRAPHY COLORS (#1C1917 Deep Charcoal & #78716C Muted Stone)
val NavyTextPrimary = Color(0xFF1C1917)       // Warm deep charcoal for sharp headers/text
val SlateTextSecondary = Color(0xFF78716C)    // Muted stone gray for body text/subtitles
val MutedSkyText = Color(0xFFA8A29E)          // Muted stone for captions/metadata

// LEGACY ALIASES (Backward Compatibility mapped to Warm Off-White Palette)
val WineRedDark = SkyBlueBgLight
val WineRedMedium = SkyBlueHeader
val WineRedLight = Color(0xFFE5E0D8)
val CrimsonVelvet = SkyBlueBorder
val DeepCrimson = SkyBluePrimary

// Accent Highlights & Badges
val MetallicGold = BrightCyanAccent
val LightGold = NavyTextPrimary
val DarkGold = SkyBluePrimary
val GoldGradientEnd = BrightCyanAccent

// Surface & Card Colors
val CardBackground = SkyBlueCardBg
val CardBackgroundTransparent = Color(0xF5F0EEE8)
val SurfaceDark = SkyBlueBgLight
val GlassmorphismOverlay = Color(0x330284C7)
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