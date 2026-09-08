package com.devil.finaldestiny.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.R
import com.devil.finaldestiny.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "About Final Destiny",
                            color = NavyTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NavyTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyBlueHeader)
            )
        },
        containerColor = SkyBlueBgLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // APP BANNER CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = SkyBlueCardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SkyBlueBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .border(2.dp, BrightCyanAccent, RoundedCornerShape(18.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Final Destiny Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "FINAL DESTINY",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyTextPrimary
                    )
                    Text(
                        text = "India's Premier Social, Audio Live & Creator Network",
                        fontSize = 13.sp,
                        color = SlateTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = SkyBlueHeader,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Version 2.4.0 (Production Build)",
                            fontSize = 11.sp,
                            color = SkyBluePrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // CREATOR MONETIZATION PROGRAM SECTION
            Card(
                colors = CardDefaults.cardColors(containerColor = SkyBlueCardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BrightCyanAccent, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = VerifiedBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🚀 Creator Monetization Program",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyTextPrimary
                        )
                    }
                    Divider(color = SkyBlueBorder.copy(0.5f))

                    Text(
                        text = "We are excited to announce our upcoming Creator Monetization Engine! Final Destiny is empowering Indian creators with real cash revenue streams:",
                        fontSize = 13.sp,
                        color = SlateTextSecondary,
                        lineHeight = 18.sp
                    )

                    FeatureItem(
                        icon = Icons.Default.Videocam,
                        title = "1. Reel & Short Video Payouts",
                        description = "Earn directly based on verified views and user engagement on your vertical reels."
                    )

                    FeatureItem(
                        icon = Icons.Default.Storefront,
                        title = "2. Brand Sponsorship Partnerships",
                        description = "Monetized hosts get matched with top Indian brands for paid product placements and collab posts."
                    )

                    FeatureItem(
                        icon = Icons.Default.Payments,
                        title = "3. Direct Bank & UPI Payouts",
                        description = "Seamlessly withdraw your earnings to any Indian bank account or UPI ID with 0 hidden fees."
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SkyBlueHeader),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔔 Official launch notification will be sent automatically to all registered creators upon rollout!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyTextPrimary
                            )
                        }
                    }
                }
            }

            // APP VISION & FEATURES SECTION
            Card(
                colors = CardDefaults.cardColors(containerColor = SkyBlueCardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SkyBlueBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Explore, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Core Experience & Vision", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyTextPrimary)
                    }
                    Divider(color = SkyBlueBorder.copy(0.5f))

                    FeatureItem(
                        icon = Icons.Default.Mic,
                        title = "10-Mic Live Audio Party Rooms",
                        description = "Connect with voice hosts, enjoy live music jams, and link mics with friends."
                    )
                    FeatureItem(
                        icon = Icons.Default.Favorite,
                        title = "Final Destiny Dating & Match Engine",
                        description = "Swipe right on genuine profiles with verified photos and real-time interest matching."
                    )
                    FeatureItem(
                        icon = Icons.Default.Security,
                        title = "Vision Sentinel AI Safety",
                        description = "Real-time content moderation and Aadhaar eKYC to guarantee 100% authentic community interactions."
                    )
                }
            }

            // FOOTER & COPYRIGHT
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            ) {
                Text("Made with ❤️ in India", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyTextPrimary)
                Text("© 2026 Final Destiny Technologies Inc. All rights reserved.", fontSize = 11.sp, color = MutedSkyText)
            }
        }
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SkyBlueBgLight)
        ) {
            Icon(icon, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyTextPrimary)
            Text(description, fontSize = 12.sp, color = SlateTextSecondary, lineHeight = 16.sp)
        }
    }
}
