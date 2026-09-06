package com.devil.finaldestiny.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.model.SwipeCard
import com.devil.finaldestiny.ui.components.VipBadge
import com.devil.finaldestiny.ui.theme.*

@Composable
fun DiscoverSwipeScreen(
    cards: List<SwipeCard>,
    matchedCard: SwipeCard?,
    onSwipeRight: (String) -> Unit,
    onSwipeLeft: (String) -> Unit,
    onSwipeUpSuperLike: (String) -> Unit,
    onDismissMatchModal: () -> Unit,
    onStartCallInvitation: () -> Unit
) {
    val currentCard = cards.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGradient)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (currentCard != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                // Discover Header
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("DISCOVER MATCHES", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MetallicGold, letterSpacing = 1.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MetallicGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Within ${currentCard.distanceKm} km", fontSize = 12.sp, color = LightGold)
                    }
                }

                // 9:16 Vertical Tinder-Style Profile Card (PRD Section 3.3)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp)
                        .border(1.5.dp, GoldGradient, RoundedCornerShape(24.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Background Photo Simulation
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(WineRedLight, WineRedDark, WineRedDark)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = currentCard.profile.name.take(1),
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MetallicGold.copy(0.8f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("High-Res Profile Photo", color = LightGold.copy(0.6f), fontSize = 14.sp)
                            }
                        }

                        // Gradient Overlay for Text Readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, WineRedDark.copy(alpha = 0.85f), WineRedDark)
                                    )
                                )
                        )

                        // Top Shortcuts: Verified Badge & Instant Follow
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            VipBadge(vipLevel = currentCard.profile.vipLevel)

                            // Follow & Connect Shortcut Button
                            Button(
                                onClick = { /* Instant follow */ },
                                colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("+ Follow", fontSize = 11.sp, color = WineRedDark, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Bottom Profile Details Container
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${currentCard.profile.name}, ${currentCard.profile.age}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightGold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = VerifiedBlue, modifier = Modifier.size(22.dp))
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = currentCard.profile.bio, fontSize = 13.sp, color = LightGold.copy(0.9f))

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "🎯 Intent: ${currentCard.profile.relationshipIntent}", fontSize = 11.sp, color = MetallicGold, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(8.dp))
                            // Lifestyle tags
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                currentCard.profile.lifestyleTags.take(3).forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(WineRedMedium)
                                            .border(1.dp, CrimsonVelvet, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(tag, fontSize = 10.sp, color = LightGold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Gesture Buttons: Pass, Like, Super Like
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    // Swipe Left (Pass)
                    IconButton(
                        onClick = { onSwipeLeft(currentCard.id) },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(WineRedMedium)
                            .border(2.dp, CrimsonVelvet, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Pass", tint = LightGold, modifier = Modifier.size(28.dp))
                    }

                    // Swipe Up (Super Like)
                    IconButton(
                        onClick = { onSwipeUpSuperLike(currentCard.id) },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MetallicGold)
                            .border(2.dp, LightGold, CircleShape)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Super Like", tint = WineRedDark, modifier = Modifier.size(32.dp))
                    }

                    // Swipe Right (Like)
                    IconButton(
                        onClick = { onSwipeRight(currentCard.id) },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(CrimsonVelvet)
                            .border(2.dp, MetallicGold, CircleShape)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = "Like", tint = HeartRed, modifier = Modifier.size(28.dp))
                    }
                }
            }
        } else {
            // Deck empty state
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("💖", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("You've viewed all nearby profiles!", fontSize = 16.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                Text("Check back soon for new social connections.", fontSize = 12.sp, color = LightGold)
            }
        }

        // Celebratory "It's a Match!" Pop-Up Modal
        if (matchedCard != null) {
            AlertDialog(
                onDismissRequest = onDismissMatchModal,
                containerColor = CardBackground,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("✨ IT'S A MATCH! ✨", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MetallicGold, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("You and ${matchedCard.profile.name} like each other!", fontSize = 13.sp, color = LightGold)
                    }
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(CrimsonVelvet)
                                .border(3.dp, MetallicGold, CircleShape)
                        ) {
                            Text(matchedCard.profile.name.take(1), fontSize = 36.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "🔒 Invitation-Only 1v1 Audio/Video Calling & Mutual DM permissions unlocked!",
                            fontSize = 12.sp,
                            color = LightGold,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDismissMatchModal()
                            onStartCallInvitation()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = WineRedDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Formal Call Invitation 📞", color = WineRedDark, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissMatchModal, modifier = Modifier.fillMaxWidth()) {
                        Text("Keep Swiping", color = LightGold.copy(0.8f))
                    }
                }
            )
        }
    }
}
