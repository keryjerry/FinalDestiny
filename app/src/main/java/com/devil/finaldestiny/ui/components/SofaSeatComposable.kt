package com.devil.finaldestiny.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    val goldBorderColor = Color(0xFFD4AF37)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .width(68.dp)
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        // Seat Circle Container (Matching exact sleek mic seat circle)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
                .border(
                    width = if (seat.isSpeaking && !seat.isMuted) 2.dp else 1.dp,
                    color = when {
                        seat.isSpeaking && !seat.isMuted -> LiveIndicatorGreen
                        seat.role == SeatRole.HOST -> MetallicGold
                        seat.isLocked -> Color.Gray.copy(alpha = 0.5f)
                        else -> goldBorderColor.copy(alpha = 0.4f)
                    },
                    shape = CircleShape
                )
        ) {
            if (isOccupied) {
                // Occupied seat: Show User Profile Avatar
                ProfileAvatarView(
                    name = user?.name ?: "User",
                    profilePictureUri = user?.profilePictureUri,
                    gender = user?.gender ?: com.devil.finaldestiny.model.Gender.FEMALE,
                    size = 50.dp,
                    showBorder = false,
                    modifier = Modifier.clip(CircleShape)
                )

                // Mute overlay on occupied seat if muted
                if (seat.isMuted) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f)),
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
                // Empty seat: Show sleek white Mic vector icon or Lock icon
                if (seat.isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked Seat",
                        tint = LightGold.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Empty Mic Seat",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Host Crown Badge overlay on top-left of circle
            if (seat.role == SeatRole.HOST) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopStart)
                        .clip(CircleShape)
                        .background(DarkGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👑", fontSize = 9.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Text under circle: Seat Number or User Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (seat.isSpeaking && !seat.isMuted) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(10.dp)
                ) {
                    Box(modifier = Modifier.width(2.dp).height(waveHeight1.dp).background(LiveIndicatorGreen))
                    Box(modifier = Modifier.width(2.dp).height(waveHeight2.dp).background(LiveIndicatorGreen))
                    Box(modifier = Modifier.width(2.dp).height(waveHeight1.dp).background(LiveIndicatorGreen))
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = if (isOccupied) (user?.name ?: "User") else "${seat.seatIndex}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isOccupied) LightGold else Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

