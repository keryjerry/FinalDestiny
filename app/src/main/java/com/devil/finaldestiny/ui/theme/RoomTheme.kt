package com.devil.finaldestiny.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class RoomTheme(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val gradientColors: List<Color>
) {
    val brush: Brush
        get() = Brush.verticalGradient(colors = gradientColors)
}

val AvailableRoomThemes = listOf(
    RoomTheme("red_velvet", "Red Velvet Luxury", "🍷", "Classic Deep Red & Gold", listOf(Color(0xFF5C1027), Color(0xFF3A0817), Color(0xFF1C040D))),
    RoomTheme("mountain_sunset", "Mountain Sunset", "🏔️", "Warm Amber & Purple Glow", listOf(Color(0xFF4A1525), Color(0xFF7C2D12), Color(0xFF1E1B4B))),
    RoomTheme("tropical_beach", "Tropical Beach", "🏖️", "Cyan Waters & Gold Sun", listOf(Color(0xFF064E3B), Color(0xFF0891B2), Color(0xFF0F172A))),
    RoomTheme("cyberpunk_neon", "Cyberpunk Neon", "🌃", "Vibrant Purple & Magenta", listOf(Color(0xFF4C1D95), Color(0xFF831843), Color(0xFF0F172A))),
    RoomTheme("galaxy_stars", "Galaxy Cosmos", "🌌", "Deep Violet & Cosmic Black", listOf(Color(0xFF0F172A), Color(0xFF312E81), Color(0xFF000000))),
    RoomTheme("velvet_rose", "Velvet Rose", "🌹", "Romantic Crimson & Rose", listOf(Color(0xFF831843), Color(0xFF9F1239), Color(0xFF4C0519))),
    RoomTheme("ocean_waves", "Deep Ocean", "🌊", "Aqua Waves & Deep Sea", listOf(Color(0xFF0C4A6E), Color(0xFF0369A1), Color(0xFF0F172A))),
    RoomTheme("gold_luxury", "Royal Gold Amber", "🏎️", "High Class Amber & Gold", listOf(Color(0xFF451A03), Color(0xFF78350F), Color(0xFF1A030A))),
    RoomTheme("disco_party", "Vibrant Disco", "💃", "Magenta & Violet Party Lights", listOf(Color(0xFF701A75), Color(0xFFA21CAF), Color(0xFF3B0764))),
    RoomTheme("fireworks_night", "Fireworks Night", "🎆", "Midnight Blue & Gold Sparkle", listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF0F172A))),
    RoomTheme("emerald_forest", "Emerald Forest", "🌿", "Lush Emerald & Deep Teal", listOf(Color(0xFF064E3B), Color(0xFF14532D), Color(0xFF022C22))),
    RoomTheme("midnight_purple", "Midnight Purple", "🔮", "Royal Purple & Obsidian", listOf(Color(0xFF3B0764), Color(0xFF581C87), Color(0xFF180828))),
    RoomTheme("cherry_blossom", "Cherry Blossom", "🌸", "Soft Pink & Pastel Violet", listOf(Color(0xFF831843), Color(0xFFBE185D), Color(0xFF4C0519))),
    RoomTheme("aurora_borealis", "Aurora Borealis", "🌌", "Northern Lights Teal & Emerald", listOf(Color(0xFF065F46), Color(0xFF0F766E), Color(0xFF0F172A))),
    RoomTheme("dark_onyx", "Dark Onyx", "🖤", "Matte Obsidian & Charcoal", listOf(Color(0xFF18181B), Color(0xFF27272A), Color(0xFF09090B)))
)
