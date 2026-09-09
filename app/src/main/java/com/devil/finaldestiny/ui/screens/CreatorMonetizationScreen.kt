package com.devil.finaldestiny.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.model.*
import com.devil.finaldestiny.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorMonetizationScreen(
    kycData: KycData,
    hostEarnings: HostEarnings,
    userFollowers: Int,
    analytics: CreatorAnalytics = CreatorAnalytics(),
    onSubmitKyc: (String, String, String) -> Unit,
    onRequestPayout: (Double, PaymentMethodType, String) -> String,
    onRefresh: suspend () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    var selectedStudioTab by remember { mutableStateOf(0) } // 0: Growth & Charts, 1: Post Insights, 2: Milestones & Go-Live, 3: KYC & Payouts
    var chartPeriod7D by remember { mutableStateOf(true) } // true: 7D, false: 30D

    var isDashboardUnlocked by remember { mutableStateOf(false) }
    var passcodeAttempt by remember { mutableStateOf("") }
    var passcodeError by remember { mutableStateOf<String?>(null) }

    var aadhaarInput by remember { mutableStateOf(kycData.aadhaarNumber) }
    var panInput by remember { mutableStateOf(kycData.panNumber) }
    var legalNameInput by remember { mutableStateOf(kycData.legalName) }

    var payoutAmountInput by remember { mutableStateOf("2500") }
    var selectedMethod by remember { mutableStateOf(PaymentMethodType.UPI) }
    var upiOrAccountInput by remember { mutableStateOf("darkdevil@okicici") }

    var payoutResultMsg by remember { mutableStateOf<String?>(null) }

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
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(PrimaryGradient)
        ) {
            // CREATOR STUDIO COMPACT 56DP TOP HEADER BAR WITH BACK ARROW
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, MetallicGold.copy(0.7f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
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

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(WineRedMedium)
                                    .border(1.5.dp, MetallicGold, CircleShape)
                            ) {
                                Icon(Icons.Default.Analytics, contentDescription = null, tint = MetallicGold, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("📊 CREATOR STUDIO", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MetallicGold, letterSpacing = 0.5.sp)
                                Text("Velocity & Revenue Settlement", fontSize = 9.sp, color = LightGold)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = LiveIndicatorGreen.copy(0.15f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.border(1.dp, LiveIndicatorGreen, RoundedCornerShape(10.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(LiveIndicatorGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("LIVE ANALYTICS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = LiveIndicatorGreen)
                            }
                        }
                    }
                }
            }

            // CREATOR STUDIO SCROLLABLE TAB ROW
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedStudioTab,
                    containerColor = CardBackground,
                    contentColor = MetallicGold,
                    edgePadding = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, CrimsonVelvet, RoundedCornerShape(14.dp))
                ) {
                    Tab(selected = selectedStudioTab == 0, onClick = { selectedStudioTab = 0 }) {
                        Text("📈 Growth & Charts", fontSize = 11.sp, modifier = Modifier.padding(10.dp), color = if (selectedStudioTab == 0) MetallicGold else LightGold.copy(0.6f), fontWeight = FontWeight.Bold)
                    }
                    Tab(selected = selectedStudioTab == 1, onClick = { selectedStudioTab = 1 }) {
                        Text("🎬 Post Insights", fontSize = 11.sp, modifier = Modifier.padding(10.dp), color = if (selectedStudioTab == 1) MetallicGold else LightGold.copy(0.6f), fontWeight = FontWeight.Bold)
                    }
                    Tab(selected = selectedStudioTab == 2, onClick = { selectedStudioTab = 2 }) {
                        Text("🎯 Milestones & Live", fontSize = 11.sp, modifier = Modifier.padding(10.dp), color = if (selectedStudioTab == 2) MetallicGold else LightGold.copy(0.6f), fontWeight = FontWeight.Bold)
                    }
                    Tab(selected = selectedStudioTab == 3, onClick = { selectedStudioTab = 3 }) {
                        Text("💳 KYC & Payouts", fontSize = 11.sp, modifier = Modifier.padding(10.dp), color = if (selectedStudioTab == 3) MetallicGold else LightGold.copy(0.6f), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // TAB 0: 📈 GROWTH TRENDS, VELOCITY & INTERACTIVE PERFORMANCE CHART
            if (selectedStudioTab == 0) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // 1. GROWTH VELOCITY METRICS GRID (2x2)
                        Text("⚡ Velocity & Reach Metrics (Last 30 Days)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MetallicGold)

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            MetricVelocityCard(
                                title = "Total Views 👁️",
                                value = "%,d".format(analytics.totalViews),
                                trendLabel = "+%.1f%% vs last wk".format(analytics.viewsGrowthPct),
                                isPositive = analytics.viewsGrowthPct >= 0,
                                modifier = Modifier.weight(1f)
                            )
                            MetricVelocityCard(
                                title = "Total Likes ❤️",
                                value = "%,d".format(analytics.totalLikes),
                                trendLabel = "+%.1f%% vs last wk".format(analytics.likesGrowthPct),
                                isPositive = analytics.likesGrowthPct >= 0,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            MetricVelocityCard(
                                title = "Followers 👥",
                                value = "%,d".format(analytics.totalFollowers),
                                trendLabel = "+%.1f%% vs last wk".format(analytics.followersGrowthPct),
                                isPositive = analytics.followersGrowthPct >= 0,
                                modifier = Modifier.weight(1f)
                            )
                            MetricVelocityCard(
                                title = "Impressions 📡",
                                value = "%,d".format(analytics.totalImpressions),
                                trendLabel = "%.1f%% Engagement".format(analytics.reachEngagementRatioPct),
                                isPositive = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // 2. INTERACTIVE 7-DAY / 30-DAY PERFORMANCE CHART
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CrimsonVelvet, RoundedCornerShape(20.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text("📊 Performance Curve", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                                        Text("Daily views & engagement spikes", fontSize = 10.sp, color = LightGold.copy(0.7f))
                                    }

                                    // 7D / 30D Period Switcher
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(WineRedMedium)
                                            .border(1.dp, MetallicGold.copy(0.4f), RoundedCornerShape(10.dp))
                                    ) {
                                        Button(
                                            onClick = { chartPeriod7D = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (chartPeriod7D) MetallicGold else Color.Transparent),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("7 Days", fontSize = 10.sp, color = if (chartPeriod7D) WineRedDark else LightGold, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { chartPeriod7D = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (!chartPeriod7D) MetallicGold else Color.Transparent),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("30 Days", fontSize = 10.sp, color = if (!chartPeriod7D) WineRedDark else LightGold, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                HorizontalDivider(color = CrimsonVelvet, thickness = 1.dp)

                                // Bar Chart Visualization
                                val currentPoints = if (chartPeriod7D) analytics.dailyPoints7D else analytics.dailyPoints30D
                                val maxViews = (currentPoints.maxOfOrNull { it.viewsCount } ?: 50000).toFloat()

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .padding(top = 10.dp)
                                ) {
                                    currentPoints.forEach { pt ->
                                        val heightPct = (pt.viewsCount.toFloat() / maxViews).coerceIn(0.15f, 1.0f)
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "${pt.viewsCount / 1000}k",
                                                fontSize = 9.sp,
                                                color = MetallicGold,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .width(18.dp)
                                                    .fillMaxHeight(heightPct * 0.8f)
                                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(BrightCyanAccent, SkyBluePrimary)
                                                        )
                                                    )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = pt.dayLabel,
                                                fontSize = 9.sp,
                                                color = LightGold.copy(0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. AUDIENCE & WATCH TIME INSIGHTS
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CrimsonVelvet, RoundedCornerShape(20.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("⏱️ Audience Watch Time & Retention", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MetallicGold)

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text("Avg Watch Duration", fontSize = 11.sp, color = LightGold.copy(0.7f))
                                        Text(analytics.avgWatchDuration, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Reel Completion Rate", fontSize = 11.sp, color = LightGold.copy(0.7f))
                                        Text("%.1f%%".format(analytics.completionRatePct), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LiveIndicatorGreen)
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = { (analytics.completionRatePct / 100.0).toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = BrightCyanAccent,
                                    trackColor = WineRedMedium
                                )
                                Text("78.5% of viewers watch past the first 3 seconds of your uploaded reels.", fontSize = 10.sp, color = LightGold.copy(0.6f))
                            }
                        }
                    }
                }
            }

            // TAB 1: 🎬 PER-POST PERFORMANCE BREAKDOWN ("MY CONTENT & INSIGHTS")
            if (selectedStudioTab == 1) {
                item {
                    Text("🎬 Per-Post Granular Insights", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                }

                items(analytics.postInsights) { post ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CrimsonVelvet, RoundedCornerShape(18.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = post.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightGold,
                                    modifier = Modifier.weight(1f)
                                )
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SkyBluePrimary.copy(0.2f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.border(0.8.dp, BrightCyanAccent, RoundedCornerShape(8.dp))
                                ) {
                                    Text(
                                        text = post.viralVelocityBadge,
                                        fontSize = 9.sp,
                                        color = BrightCyanAccent,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = CrimsonVelvet, thickness = 0.8.dp)

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text("Total Views", fontSize = 9.sp, color = LightGold.copy(0.6f))
                                    Text("%,d".format(post.totalViews), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                                }
                                Column {
                                    Text("Peak Hours", fontSize = 9.sp, color = LightGold.copy(0.6f))
                                    Text(post.peakViewingHours, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = LightGold)
                                }
                                Column {
                                    Text("Like Ratio", fontSize = 9.sp, color = LightGold.copy(0.6f))
                                    Text("%.1f%%".format(post.likeToViewRatioPct), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LiveIndicatorGreen)
                                }
                                Column {
                                    Text("Comments / Shares", fontSize = 9.sp, color = LightGold.copy(0.6f))
                                    Text("${post.commentsCount} • ${post.sharesCount}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = LightGold)
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: 🎯 MONETIZATION & LIVE ELIGIBILITY MILESTONES
            if (selectedStudioTab == 2) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("🎯 Milestone Roadmap for Go-Live & Revenue Sharing", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MetallicGold)

                        // 1. GO LIVE MILESTONE (500 Followers)
                        val followerTarget = 500
                        val followerProgress = (userFollowers.toFloat() / followerTarget).coerceIn(0f, 1f)
                        val isGoLiveUnlocked = userFollowers >= followerTarget

                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (isGoLiveUnlocked) LiveIndicatorGreen else CrimsonVelvet, RoundedCornerShape(20.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isGoLiveUnlocked) Icons.Default.CheckCircle else Icons.Default.Mic,
                                            contentDescription = null,
                                            tint = if (isGoLiveUnlocked) LiveIndicatorGreen else DarkGold,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("1. 'Go Live' Broadcasting Milestone", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LightGold)
                                            Text("Unlocks 10-mic audio sofa & video stage hosting", fontSize = 10.sp, color = LightGold.copy(0.6f))
                                        }
                                    }
                                    Text("$userFollowers / $followerTarget", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                                }

                                LinearProgressIndicator(
                                    progress = { followerProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = if (isGoLiveUnlocked) LiveIndicatorGreen else MetallicGold,
                                    trackColor = WineRedMedium
                                )
                                Text(
                                    text = if (isGoLiveUnlocked) "🎉 UNLOCKED: You can now host live audio & video rooms!" else "%.1f%% completed. Gain %d more followers to unlock.".format(followerProgress * 100, followerTarget - userFollowers),
                                    fontSize = 10.sp,
                                    color = if (isGoLiveUnlocked) LiveIndicatorGreen else LightGold.copy(0.7f)
                                )
                            }
                        }

                        // 2. CUMULATIVE VIEWS MILESTONE (1,000,000 Views)
                        val viewTarget = 1000000
                        val viewProgress = (analytics.totalViews.toFloat() / viewTarget).coerceIn(0f, 1f)
                        val isViewsUnlocked = analytics.totalViews >= viewTarget

                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (isViewsUnlocked) LiveIndicatorGreen else CrimsonVelvet, RoundedCornerShape(20.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isViewsUnlocked) Icons.Default.CheckCircle else Icons.Default.MonetizationOn,
                                            contentDescription = null,
                                            tint = if (isViewsUnlocked) LiveIndicatorGreen else DarkGold,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("2. Cumulative 1 Million Reel Views", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LightGold)
                                            Text("Unlocks 25% Net Ad Share & Direct Bank Settlements", fontSize = 10.sp, color = LightGold.copy(0.6f))
                                        }
                                    }
                                    Text("%,d / 1M".format(analytics.totalViews), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                                }

                                LinearProgressIndicator(
                                    progress = { viewProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = if (isViewsUnlocked) LiveIndicatorGreen else BrightCyanAccent,
                                    trackColor = WineRedMedium
                                )
                                Text(
                                    text = if (isViewsUnlocked) "🎉 UNLOCKED: Eligible for real cash revenue sharing!" else "%.2f%% completed. Keep uploading reels to reach 1M views!".format(viewProgress * 100),
                                    fontSize = 10.sp,
                                    color = if (isViewsUnlocked) LiveIndicatorGreen else LightGold.copy(0.7f)
                                )
                            }
                        }

                        // PRE-QUALIFIED BADGE & MILESTONE ALERTS TRAY
                        Card(
                            colors = CardDefaults.cardColors(containerColor = WineRedDark),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, GoldGradient, RoundedCornerShape(20.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("✨ Pre-Qualified Creator Status", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                                }
                                Text(
                                    text = "Your account is pre-qualified for early monetization testing. Complete identity KYC verification in the Payouts tab to bind your UPI bank account.",
                                    fontSize = 10.sp,
                                    color = LightGold.copy(0.8f)
                                )
                            }
                        }

                        // MILESTONE ACTIVITY ALERTS TRAY
                        Text("🔔 Creator Activity & Milestone Alerts", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MetallicGold)

                        analytics.milestoneAlerts.forEach { alert ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardBackground),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CrimsonVelvet.copy(0.6f), RoundedCornerShape(14.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(alert.iconSymbol, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text(alert.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LightGold)
                                            Text(alert.timestamp, fontSize = 9.sp, color = LightGold.copy(0.5f))
                                        }
                                        Text(alert.message, fontSize = 10.sp, color = LightGold.copy(0.8f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 3: 💳 KYC VERIFICATION & BANK/UPI PAYOUT SETTLEMENT FORM
            if (selectedStudioTab == 3) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("💳 Identity KYC Verification & Payout Setup", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MetallicGold)

                        // 1. Identity Form Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CrimsonVelvet, RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Aadhaar & PAN Compliance Verification", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MetallicGold)

                                OutlinedTextField(
                                    value = legalNameInput,
                                    onValueChange = { legalNameInput = it },
                                    label = { Text("Full Legal Name (as per Aadhaar/PAN)", color = LightGold) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = CrimsonVelvet, focusedTextColor = LightGold, unfocusedTextColor = LightGold),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = aadhaarInput,
                                    onValueChange = { aadhaarInput = it },
                                    label = { Text("12-Digit Aadhaar Number", color = LightGold) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = CrimsonVelvet, focusedTextColor = LightGold, unfocusedTextColor = LightGold),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = panInput,
                                    onValueChange = { panInput = it },
                                    label = { Text("10-Character PAN Number", color = LightGold) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = CrimsonVelvet, focusedTextColor = LightGold, unfocusedTextColor = LightGold),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        onSubmitKyc(aadhaarInput, panInput, legalNameInput)
                                        Toast.makeText(context, "✅ Identity KYC Submitted for Automated Settlement Check!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Submit Identity KYC Documents", color = WineRedDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // 2. Bank Payout Settlement Form
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CrimsonVelvet, RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Bank & UPI Payout Settlement", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                                Text("Available Balance: ₹%.2f INR".format(hostEarnings.pendingPayoutInr), fontSize = 12.sp, color = LiveIndicatorGreen, fontWeight = FontWeight.Bold)

                                OutlinedTextField(
                                    value = payoutAmountInput,
                                    onValueChange = { payoutAmountInput = it },
                                    label = { Text("Withdrawal Amount (₹ INR)", color = LightGold) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = CrimsonVelvet, focusedTextColor = LightGold, unfocusedTextColor = LightGold),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = upiOrAccountInput,
                                    onValueChange = { upiOrAccountInput = it },
                                    label = { Text("Bound UPI ID or Bank Account Number", color = LightGold) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = CrimsonVelvet, focusedTextColor = LightGold, unfocusedTextColor = LightGold),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        val amt = payoutAmountInput.toDoubleOrNull() ?: 0.0
                                        val res = onRequestPayout(amt, selectedMethod, upiOrAccountInput)
                                        payoutResultMsg = res
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LiveIndicatorGreen),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Request Instant Bank Withdrawal 💸", color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                payoutResultMsg?.let { msg ->
                                    Text(
                                        text = msg,
                                        fontSize = 11.sp,
                                        color = if (msg.startsWith("SUCCESS")) LiveIndicatorGreen else Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricVelocityCard(
    title: String,
    value: String,
    trendLabel: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .border(1.dp, CrimsonVelvet, RoundedCornerShape(14.dp))
            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontSize = 10.sp, color = LightGold.copy(0.7f))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isPositive) "↗ " else "↘ ",
                    fontSize = 10.sp,
                    color = if (isPositive) LiveIndicatorGreen else Color.Red
                )
                Text(
                    text = trendLabel,
                    fontSize = 9.sp,
                    color = if (isPositive) LiveIndicatorGreen else Color.Red,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
