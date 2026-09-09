package com.devil.finaldestiny.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.devil.finaldestiny.Screen
import com.devil.finaldestiny.model.UserProfile
import com.devil.finaldestiny.ui.components.ProfileAvatarView

@Composable
fun FloatingGalaxyNavPill(
    currentScreen: Screen,
    user: UserProfile = UserProfile(),
    unreadNotificationCount: Int = 0,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GalaxyPillShimmer")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    val borderPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BorderPulseAlpha"
    )

    val vortexRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "VortexRotation"
    )

    // Deep Galaxy base color (0xFF0B0D19 - Deep Cosmic Void)
    val galaxyBaseColor = Color(0xFF0B0D19)
    val borderAlphaClamped = (borderPulseAlpha * 0.65f).coerceIn(0.2f, 1.0f)
    val borderGlowColor = Color(0xFF7C3AED).copy(alpha = borderAlphaClamped)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 20.dp)
            .height(64.dp)
            .clip(CircleShape)
            .background(galaxyBaseColor)
            .drawWithCache {
                val nebulaBrush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF0B0D19), // Core: Deep Cosmic Void
                        Color(0xFF7C3AED), // Orbiting Highlight: Electric Violet
                        Color(0xFF06B6D4), // Orbiting Highlight: Astral Cyan
                        Color(0xFFD946EF), // Orbiting Highlight: Deep Magenta
                        Color(0xFF0B0D19)  // Deep Cosmic Void
                    )
                )
                val starsBrush = Brush.radialGradient(
                    colors = listOf(Color(0x447C3AED), Color(0x2206B6D4), Color.Transparent)
                )
                onDrawBehind {
                    rotate(sweepAngle) {
                        drawRect(brush = nebulaBrush)
                    }
                    drawRect(brush = starsBrush)
                }
            }
            .border(width = 1.2.dp, color = borderGlowColor, shape = CircleShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. HOME ICON
            GalaxyNavItem(
                isSelected = currentScreen == Screen.SECONDARY_FEED || currentScreen == Screen.PRIMARY_DASHBOARD,
                onClick = { onNavigate(Screen.SECONDARY_FEED) }
            ) { isSelected ->
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                    modifier = Modifier.size(24.dp)
                )
            }

            // 2. REELS ICON
            GalaxyNavItem(
                isSelected = false,
                onClick = { onNavigate(Screen.SECONDARY_FEED) }
            ) { isSelected ->
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = "Reels",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(24.dp)
                )
            }

            // 3. SHARE / DM VORTEX CENTER BUTTON (Spiral Galaxy Vortex)
            val isDmSelected = currentScreen == Screen.INBOX
            GalaxyVortexCenterButton(
                isSelected = isDmSelected,
                vortexRotation = vortexRotation,
                onClick = { onNavigate(Screen.INBOX) }
            )

            // 4. SEARCH ICON
            GalaxyNavItem(
                isSelected = currentScreen == Screen.SEARCH_EXPLORE,
                onClick = { onNavigate(Screen.SEARCH_EXPLORE) }
            ) { isSelected ->
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                    modifier = Modifier.size(24.dp)
                )
            }

            // 5. PROFILE ICON WITH ONLINE/NOTIFICATION BADGE
            GalaxyNavItem(
                isSelected = currentScreen == Screen.USER_PROFILE,
                onClick = { onNavigate(Screen.USER_PROFILE) }
            ) { isSelected ->
                Box(contentAlignment = Alignment.TopEnd) {
                    ProfileAvatarView(
                        name = user.name.ifBlank { "Me" },
                        profilePictureUri = user.profilePictureUri,
                        size = 28.dp,
                        showBorder = isSelected,
                        borderColor = if (isSelected) Color(0xFF38BDF8) else Color.Transparent
                    )
                    // Online/notification dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                            .border(1.dp, Color(0xFF050510), CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun GalaxyNavItem(
    isSelected: Boolean,
    onClick: () -> Unit,
    content: @Composable (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.82f else if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "GalaxyNavItemScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                }
            )
    ) {
        content(isSelected)
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun GalaxyVortexCenterButton(
    isSelected: Boolean,
    vortexRotation: Float,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else if (isSelected) 1.18f else 1.05f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "VortexButtonScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(52.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                }
            )
            .drawWithCache {
                val vortexGradient = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF6366F1),
                        Color(0xFFA855F7),
                        Color(0xFFEC4899),
                        Color(0xFF3B82F6),
                        Color(0xFF6366F1)
                    )
                )
                onDrawBehind {
                    rotate(vortexRotation) {
                        drawCircle(
                            brush = vortexGradient,
                            radius = size.minDimension / 2f
                        )
                    }
                    // Inner dark core of vortex
                    drawCircle(
                        color = Color(0xFF0F0B29),
                        radius = (size.minDimension / 2f) - 3.dp.toPx()
                    )
                }
            }
            .clip(CircleShape)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Direct Messages / Share",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}
