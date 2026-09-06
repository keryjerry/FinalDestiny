package com.devil.finaldestiny.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.model.GiftCategory
import com.devil.finaldestiny.model.GiftItem
import com.devil.finaldestiny.model.VipTierInfo
import com.devil.finaldestiny.ui.theme.*

@Composable
fun VipStoreScreen(
    vipTiers: List<VipTierInfo>,
    giftStoreItems: List<GiftItem>,
    userVipLevel: Int,
    userDiamonds: Int,
    onPurchaseAsset: (GiftItem) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: VIP Hierarchy, 1: Gift Wall, 2: Dynamic Avatars, 3: Mic Links, 4: Luxury Cars

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGradient)
            .padding(16.dp)
    ) {
        // Store Header & Diamond Balance
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text("VIP PRIVILEGE & ASSET STORE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MetallicGold, letterSpacing = 1.sp)
                Text("Current Rank: VIP Level $userVipLevel", fontSize = 11.sp, color = LightGold)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(WineRedMedium)
                    .border(1.dp, MetallicGold, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("💎 $userDiamonds Diamonds", fontSize = 12.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Tab Bar
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = CardBackground,
            contentColor = MetallicGold,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("VIP Matrix", fontSize = 12.sp, modifier = Modifier.padding(10.dp), color = if (selectedTab == 0) MetallicGold else LightGold.copy(0.6f))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Gift Wall", fontSize = 12.sp, modifier = Modifier.padding(10.dp), color = if (selectedTab == 1) MetallicGold else LightGold.copy(0.6f))
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("Avatars", fontSize = 12.sp, modifier = Modifier.padding(10.dp), color = if (selectedTab == 2) MetallicGold else LightGold.copy(0.6f))
            }
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                Text("Mic Links", fontSize = 12.sp, modifier = Modifier.padding(10.dp), color = if (selectedTab == 3) MetallicGold else LightGold.copy(0.6f))
            }
            Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }) {
                Text("Luxury Cars", fontSize = 12.sp, modifier = Modifier.padding(10.dp), color = if (selectedTab == 4) MetallicGold else LightGold.copy(0.6f))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Content
        if (selectedTab == 0) {
            // VIP Level Progression Matrix (Levels 1 to 10) (PRD Section 6.1)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(vipTiers) { tier ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (userVipLevel == tier.level) 2.dp else 1.dp,
                                color = if (userVipLevel == tier.level) MetallicGold else CrimsonVelvet,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(tier.badgeSymbol, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(tier.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                                }
                                Text(tier.expRequired, fontSize = 11.sp, color = LightGold, fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("✨ Assets: ${tier.visualAssets}", fontSize = 12.sp, color = LightGold)

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("🛡️ Special Immunity: ${tier.immunityDescription}", fontSize = 11.sp, color = LiveIndicatorGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Store Catalog (Gift Wall, Avatars, Mic Links, Cars) (PRD Section 6.2)
            val filteredCategory = when (selectedTab) {
                1 -> GiftCategory.GIFT_WALL
                2 -> GiftCategory.DYNAMIC_AVATARS
                3 -> GiftCategory.MIC_LINKS
                else -> GiftCategory.LUXURY_VEHICLES
            }

            val itemsList = giftStoreItems.filter { it.category == filteredCategory || selectedTab == 1 }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(itemsList) { gift ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CrimsonVelvet, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(WineRedDark)
                                        .border(1.dp, MetallicGold, CircleShape)
                                ) {
                                    Text(gift.iconSymbol, fontSize = 24.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(gift.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LightGold)
                                    Text("Rank Stat: ${gift.achievementPct}", fontSize = 10.sp, color = DarkGold)
                                    Text("Lighted Up Inventory Tracked 💡", fontSize = 10.sp, color = LiveIndicatorGreen)
                                }
                            }

                            Button(
                                onClick = { onPurchaseAsset(gift) },
                                colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("💎 ${gift.diamondPrice}", fontSize = 11.sp, color = WineRedDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
