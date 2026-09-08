package com.devil.finaldestiny.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.model.GiftCategory
import com.devil.finaldestiny.model.GiftItem
import com.devil.finaldestiny.model.HostEarnings
import com.devil.finaldestiny.model.VipTierInfo
import com.devil.finaldestiny.ui.components.PaymentQrModalDialog
import com.devil.finaldestiny.ui.theme.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import kotlinx.coroutines.launch

data class RechargePack(
    val id: String,
    val diamonds: Int,
    val priceInr: Int,
    val bonusDiamonds: Int = 0,
    val badgeLabel: String = ""
)

data class ThemeEffectItem(
    val id: String,
    val name: String,
    val category: String,
    val icon: String,
    val priceDiamonds: Int,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VipStoreScreen(
    vipTiers: List<VipTierInfo>,
    giftStoreItems: List<GiftItem>,
    userVipLevel: Int,
    userDiamonds: Int,
    isMonetizedHost: Boolean = false,
    hostEarnings: HostEarnings? = null,
    onPurchaseAsset: (GiftItem) -> Unit,
    onTopUpDiamonds: (Int) -> Unit = {},
    onRefresh: suspend () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableStateOf(0) } // 0: Wallet & Recharge, 1: VIP & Noble, 2: Gift Wall, 3: Themes & Mic Effects, 4: Host Wallet
    var selectedRechargePack by remember { mutableStateOf<RechargePack?>(null) }
    var showPaymentModal by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("⚡ UPI / GPay / PhonePe / Paytm") }

    val rechargePacks = listOf(
        RechargePack("p1", 100, 10, bonusDiamonds = 10, badgeLabel = "STARTER"),
        RechargePack("p2", 500, 49, bonusDiamonds = 60, badgeLabel = "POPULAR"),
        RechargePack("p3", 1000, 99, bonusDiamonds = 150, badgeLabel = "BEST VALUE"),
        RechargePack("p4", 2500, 249, bonusDiamonds = 450, badgeLabel = "VIP CHOICE"),
        RechargePack("p5", 5000, 499, bonusDiamonds = 1000, badgeLabel = "MEGA PACK"),
        RechargePack("p6", 12000, 999, bonusDiamonds = 3000, badgeLabel = "KING PACK")
    )

    val themeEffectItems = listOf(
        ThemeEffectItem("t1", "Gold Royalty Frame", "PROFILE_FRAME", "👑", 150, "Shiny Gold Profile Border with VIP Crown"),
        ThemeEffectItem("t2", "Neon Cyber Mic Ring", "MIC_EFFECT", "🎙️", 200, "Animated Neon Pulse Aura for Stage Seats"),
        ThemeEffectItem("t3", "Fire Flame Room Title", "ROOM_TITLE", "🔥", 300, "Glowing Animated Flame Badge above Host Head"),
        ThemeEffectItem("t4", "Diamond Sparkle Aura", "PROFILE_FRAME", "💎", 250, "Sparkling Diamond Particle Profile Outline"),
        ThemeEffectItem("t5", "Angel Wings Mic Ring", "MIC_EFFECT", "🪽", 400, "Heavenly Golden Angel Wings on Microphone"),
        ThemeEffectItem("t6", "Destiny Legend Badge", "ROOM_TITLE", "🛡️", 500, "Legendary Hall of Fame Room Title Banner")
    )

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                try {
                    onRefresh()
                } finally {
                    isRefreshing = false
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PrimaryGradient)
                .padding(14.dp)
        ) {
        // TOP HEADER BAR & DIAMOND BALANCE
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MetallicGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .padding(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(WineRedMedium)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MetallicGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("🏪 DESTINY STORE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MetallicGold, letterSpacing = 0.5.sp)
                        Text("Unified Financial, Subscriptions & Gift Hub", fontSize = 10.sp, color = LightGold.copy(0.7f))
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(WineRedMedium)
                        .border(1.dp, MetallicGold, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💎 $userDiamonds", fontSize = 12.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // CATEGORY SCROLLABLE TAB ROW
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = CardBackground,
            contentColor = MetallicGold,
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, CrimsonVelvet, RoundedCornerShape(14.dp))
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("👛 Wallet & Recharge", fontSize = 11.sp, modifier = Modifier.padding(10.dp), color = if (selectedTab == 0) MetallicGold else LightGold.copy(0.6f), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("👑 VIP & Noble", fontSize = 11.sp, modifier = Modifier.padding(10.dp), color = if (selectedTab == 1) MetallicGold else LightGold.copy(0.6f), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("🎁 Gift Store", fontSize = 11.sp, modifier = Modifier.padding(10.dp), color = if (selectedTab == 2) MetallicGold else LightGold.copy(0.6f), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                Text("🎨 Mic & Profile Themes", fontSize = 11.sp, modifier = Modifier.padding(10.dp), color = if (selectedTab == 3) MetallicGold else LightGold.copy(0.6f), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }) {
                Text(if (isMonetizedHost) "💼 Host Earnings" else "🔒 Host Earnings", fontSize = 11.sp, modifier = Modifier.padding(10.dp), color = if (selectedTab == 4) MetallicGold else LightGold.copy(0.6f), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // TAB 0: WALLET RECHARGE & DIAMOND PACKS
        if (selectedTab == 0) {
            Column(modifier = Modifier.fillMaxSize()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = WineRedMedium),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MetallicGold, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("👛 Destiny Balance", fontSize = 12.sp, color = LightGold)
                            Text("💎 $userDiamonds Diamonds", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                        }

                        Button(
                            onClick = {
                                selectedRechargePack = rechargePacks[1]
                                showPaymentModal = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, tint = WineRedDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Top Up Now 💳", color = WineRedDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("💎 Select Diamond Top-Up Pack", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LightGold)
                Spacer(modifier = Modifier.height(6.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(rechargePacks) { pack ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .border(1.dp, MetallicGold.copy(0.6f), RoundedCornerShape(16.dp))
                                .clickable {
                                    selectedRechargePack = pack
                                    showPaymentModal = true
                                }
                                .padding(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                if (pack.badgeLabel.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(CrimsonVelvet)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(pack.badgeLabel, fontSize = 9.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                Text("💎 ${pack.diamonds}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                                if (pack.bonusDiamonds > 0) {
                                    Text("+${pack.bonusDiamonds} Bonus 🎁", fontSize = 10.sp, color = LiveIndicatorGreen, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = {
                                        selectedRechargePack = pack
                                        showPaymentModal = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("₹${pack.priceInr}", color = WineRedDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // TAB 1: VIP HIERARCHY MATRIX
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
                            Text("✨ Privileges: ${tier.visualAssets}", fontSize = 12.sp, color = LightGold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("🛡️ Special Immunity: ${tier.immunityDescription}", fontSize = 11.sp, color = LiveIndicatorGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (selectedTab == 2) {
            // TAB 2: GIFT WALL & PURCHASABLE ASSETS
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(giftStoreItems) { gift ->
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
        } else if (selectedTab == 3) {
            // TAB 3: MIC & PROFILE THEMES
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(themeEffectItems) { theme ->
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
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(WineRedMedium)
                                        .border(1.dp, MetallicGold, CircleShape)
                                ) {
                                    Text(theme.icon, fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(theme.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LightGold)
                                    Text(theme.description, fontSize = 10.sp, color = LightGold.copy(0.7f))
                                }
                            }

                            Button(
                                onClick = {
                                    if (userDiamonds >= theme.priceDiamonds) {
                                        Toast.makeText(context, "✨ Unlocked ${theme.name} Theme!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "⚠️ Need 💎 ${theme.priceDiamonds} Diamonds!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("💎 ${theme.priceDiamonds}", fontSize = 11.sp, color = WineRedDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 4) {
            // TAB 4: HOST MONETIZATION EARNINGS WALLET (LOCKED FOR NON-MONETIZED USERS)
            Column(modifier = Modifier.fillMaxSize()) {
                if (isMonetizedHost && hostEarnings != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, LiveIndicatorGreen, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LiveIndicatorGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Monetized Host Wallet Unlocked 🔓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LiveIndicatorGreen)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Total Earnings: ₹${hostEarnings.netInrEarnings}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                            Text("Available Diamonds to Cashout: 💎 ${hostEarnings.totalDiamondsEarned}", fontSize = 12.sp, color = LightGold)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Registered UPI Account: ${hostEarnings.upiId}", fontSize = 11.sp, color = LightGold.copy(0.8f))
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CrimsonVelvet, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = CrimsonVelvet, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Host Earnings Wallet Locked 🔒", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LightGold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Host financial earnings and bank payout wallet options are strictly locked for non-monetized users. Complete KYC verification in Creator Portal to unlock.",
                                fontSize = 11.sp,
                                color = LightGold.copy(0.7f),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

    // OFFICIAL PAYTM UPI QR PAYMENT MODAL
    if (showPaymentModal && selectedRechargePack != null) {
        val pack = selectedRechargePack!!
        PaymentQrModalDialog(
            amountInr = pack.priceInr,
            itemDescription = "💎 ${pack.diamonds} (+${pack.bonusDiamonds} Bonus) Diamonds Pack",
            onPaymentSuccess = {
                onTopUpDiamonds(pack.diamonds + pack.bonusDiamonds)
                showPaymentModal = false
            },
            onDismiss = { showPaymentModal = false }
        )
    }
}
