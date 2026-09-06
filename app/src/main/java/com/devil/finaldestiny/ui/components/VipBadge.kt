package com.devil.finaldestiny.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.ui.theme.*

@Composable
fun VipBadge(
    vipLevel: Int,
    modifier: Modifier = Modifier
) {
    val (badgeBg, badgeBorder, badgeText, badgeIcon) = when {
        vipLevel >= 10 -> Quad(VipLevel10Crown, MetallicGold, "VIP 10 GOD-TIER", "👑")
        vipLevel >= 6 -> Quad(VipLevel6Vehicle, LightGold, "VIP $vipLevel CHARIOT", "🏎️")
        vipLevel >= 5 -> Quad(VipLevel5Wings, DarkGold, "VIP 5 WINGS", "🦅")
        vipLevel >= 2 -> Quad(VipLevel2Knight, VipLevel1Silver, "VIP $vipLevel KNIGHT", "⚔️")
        else -> Quad(VipLevel1Silver, Color.White, "VIP 1 SILVER", "🛡️")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(badgeBg.copy(alpha = 0.3f))
            .border(1.dp, badgeBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = badgeIcon, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = badgeText,
                color = LightGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
