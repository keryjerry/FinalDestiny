package com.devil.finaldestiny.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.ui.theme.*

@Composable
fun BrandHeader(
    modifier: Modifier = Modifier,
    tagline: String = "Where Hearts Connect & Voices Resonate"
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // 3D Interlocking Golden Hearts & Handshake Emblem Container
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(110.dp)
                .shadow(16.dp, CircleShape, spotColor = MetallicGold)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(WineRedLight, CrimsonVelvet, WineRedDark)
                    )
                )
                .border(3.dp, GoldGradient, CircleShape)
        ) {
            // Heart outer ring icon
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = LightGold,
                modifier = Modifier.size(76.dp)
            )

            // Inner Handshake Symbol (Trust & Union)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(WineRedMedium.copy(alpha = 0.85f))
                    .border(1.5.dp, MetallicGold, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Handshake,
                    contentDescription = null,
                    tint = MetallicGold,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Title Typography in Embossed Metallic Gold Lettering
        Text(
            text = "FINAL DESTINY",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            letterSpacing = 2.sp,
            color = MetallicGold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = tagline,
            style = MaterialTheme.typography.bodyMedium,
            color = LightGold.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
    }
}
