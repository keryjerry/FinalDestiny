package com.devil.finaldestiny.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

    Box(
        modifier = modifier
            .width(68.dp)
            .height(96.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(VelvetSofaGradient)
            .border(
                width = if (seat.isSpeaking) 2.dp else 1.dp,
                color = if (seat.isSpeaking) MetallicGold else CrimsonVelvet.copy(alpha = 0.8f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Seat Header Role Indicator / Mic Link
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
            ) {
                // Seat Number Badge
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
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

                // Host Crown or Role Icon
                if (seat.role == SeatRole.HOST) {
                    Text(text = "👑", fontSize = 11.sp)
                } else if (seat.micLinkTitle != null) {
                    Text(text = "🔗", fontSize = 10.sp)
                } else if (seat.isLocked) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = LightGold.copy(0.6f), modifier = Modifier.size(11.dp))
                }
            }

            // Avatar Container with Glowing Waveform Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(WineRedDark)
                    .border(
                        width = if (seat.isSpeaking) 2.5.dp else 1.dp,
                        color = if (seat.isSpeaking) LiveIndicatorGreen else DarkGold.copy(0.5f),
                        shape = CircleShape
                    )
            ) {
                if (isOccupied) {
                    // Simulating user avatar initial / profile image
                    Text(
                        text = user?.name?.take(1) ?: "U",
                        color = MetallicGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Mute Overlay Icon
                    if (seat.isMuted) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
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
                    // Empty Seat '+' invite button
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Invite",
                        tint = LightGold.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // User Name Label below Sofa Seat
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
