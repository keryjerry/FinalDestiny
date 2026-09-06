package com.devil.finaldestiny.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.model.SeatRole
import com.devil.finaldestiny.model.SofaSeat
import com.devil.finaldestiny.ui.theme.*

@Composable
fun SofaSeatComposable(
    seat: SofaSeat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user = seat.userProfile
    val isOccupied = user != null

    // Waveform Animation for speaking indicator
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val waveHeight1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse), label = "w1"
    )
    val waveHeight2 by infiniteTransition.animateFloat(
        initialValue = 12f, targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse), label = "w2"
    )

    // Glassmorphism background: #2A0510 with 60% opacity
    val glassmorphismBg = Color(0x992A0510)
    val goldBorderColor = Color(0xFFD4AF37)

    Box(
        modifier = modifier
            .width(74.dp)
            .height(104.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(glassmorphismBg)
            .border(
                width = if (seat.isSpeaking) 2.dp else 1.dp,
                color = if (seat.isSpeaking) LiveIndicatorGreen else goldBorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(6.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Row: Seat Number & Role/Link Badge
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(WineRedDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${seat.seatIndex}",
                        fontSize = 9.sp,
                        color = LightGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (seat.role == SeatRole.HOST) {
                    Text(text = "👑", fontSize = 11.sp)
                } else if (seat.micLinkTitle != null) {
                    Text(text = "🔗", fontSize = 10.sp)
                } else if (seat.isLocked) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = LightGold.copy(0.6f), modifier = Modifier.size(11.dp))
                }
            }

            // Avatar Tile (Rounded Square 12.dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(WineRedMedium, WineRedDark)
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = if (seat.isSpeaking) LiveIndicatorGreen else goldBorderColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                if (isOccupied) {
                    Text(
                        text = user?.name?.take(1) ?: "U",
                        color = MetallicGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (seat.isMuted) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MicOff,
                                contentDescription = "Muted",
                                tint = HeartRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Invite",
                        tint = LightGold.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Animated Waveform indicator when speaking
            if (seat.isSpeaking && !seat.isMuted) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(12.dp)
                ) {
                    Box(modifier = Modifier.width(2.dp).height(waveHeight1.dp).background(LiveIndicatorGreen))
                    Box(modifier = Modifier.width(2.dp).height(waveHeight2.dp).background(LiveIndicatorGreen))
                    Box(modifier = Modifier.width(2.dp).height(waveHeight1.dp).background(LiveIndicatorGreen))
                }
            } else {
                Text(
                    text = if (isOccupied) user?.name ?: "Guest" else "Empty",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isOccupied) LightGold else LightGold.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
