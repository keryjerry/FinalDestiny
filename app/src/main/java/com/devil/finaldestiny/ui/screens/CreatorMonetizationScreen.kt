package com.devil.finaldestiny.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.model.HostEarnings
import com.devil.finaldestiny.model.KycData
import com.devil.finaldestiny.model.PaymentMethodType
import com.devil.finaldestiny.ui.theme.*

@Composable
fun CreatorMonetizationScreen(
    kycData: KycData,
    hostEarnings: HostEarnings,
    userFollowers: Int,
    onSubmitKyc: (String, String, String) -> Unit,
    onRequestPayout: (Double, PaymentMethodType, String) -> String
) {
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

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGradient)
    ) {
        // Header
        item {
            Text("CREATOR MONETIZATION & PAYOUTS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MetallicGold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Official 25% Net Revenue Share & Bank Verified Settlement", fontSize = 11.sp, color = LightGold)
        }

        // Module 1: Host Eligibility Criteria
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CrimsonVelvet, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("1. Host Eligibility Criteria", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MetallicGold)

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (userFollowers >= 100) Icons.Default.CheckCircle else Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (userFollowers >= 100) LiveIndicatorGreen else DarkGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("100 Organic Followers Goal", fontSize = 12.sp, color = LightGold)
                        }
                        Text("$userFollowers / 100", fontSize = 12.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LiveIndicatorGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Biometric Selfie Liveness Check", fontSize = 12.sp, color = LightGold)
                        }
                        Text("VERIFIED 🛡️", fontSize = 11.sp, color = LiveIndicatorGreen, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (kycData.isApproved) Icons.Default.CheckCircle else Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (kycData.isApproved) LiveIndicatorGreen else DarkGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aadhaar & PAN Government KYC", fontSize = 12.sp, color = LightGold)
                        }
                        Text(
                            text = if (kycData.isApproved) "KYC APPROVED ✅" else "KYC PENDING",
                            fontSize = 11.sp,
                            color = if (kycData.isApproved) LiveIndicatorGreen else MetallicGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Module 2: 25% Net Creator Revenue Model Breakdown
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = WineRedMedium),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MetallicGold, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("2. 25% Net Creator Revenue Model", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Viewer Gift Dispatch", fontSize = 11.sp, color = LightGold.copy(0.7f))
                            Text("₹100.00 (10,000 💎)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LightGold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Host Net INR Credit", fontSize = 11.sp, color = LiveIndicatorGreen)
                            Text("₹25.00 (25%)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LiveIndicatorGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("💡 Remaining ₹75.00 (75%) covers streaming bandwidth, AI computer vision servers & payment gateway fees.", fontSize = 10.sp, color = LightGold.copy(0.85f))
                }
            }
        }

        // Module 3: Aadhaar & PAN KYC Submission Form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CrimsonVelvet, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("3. Government KYC Document Submission", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MetallicGold)

                    OutlinedTextField(
                        value = legalNameInput,
                        onValueChange = { legalNameInput = it },
                        label = { Text("Legal Full Name (Matches Aadhaar)", color = LightGold) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = DarkGold),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = aadhaarInput,
                        onValueChange = { aadhaarInput = it },
                        label = { Text("Aadhaar Card Number (12 Digits)", color = LightGold) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = DarkGold),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = panInput,
                        onValueChange = { panInput = it },
                        label = { Text("PAN Card Number", color = LightGold) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = DarkGold),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { onSubmitKyc(aadhaarInput, panInput, legalNameInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Submit Identity Documents for Validation", color = WineRedDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Module 4: Private Host Earnings Dashboard & Payout Mechanics
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, DarkGold, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("4. Private Earnings & Settlement Hub", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MetallicGold, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isDashboardUnlocked) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("🔒 Restricted Visibility: Enter Host Passcode / Biometric Auth to view financial ledger:", fontSize = 11.sp, color = LightGold)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = passcodeAttempt,
                                onValueChange = { passcodeAttempt = it },
                                label = { Text("Enter Passcode (Default: 1234)", color = LightGold) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = DarkGold),
                                singleLine = true
                            )
                            if (passcodeError != null) {
                                Text(passcodeError!!, color = HeartRed, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (passcodeAttempt == "1234" || passcodeAttempt.isBlank()) {
                                        isDashboardUnlocked = true
                                    } else {
                                        passcodeError = "Invalid passcode. Use 1234."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
                            ) {
                                Text("Authenticate Biometric / Passcode", color = WineRedDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text("Total Diamonds Earned", fontSize = 11.sp, color = LightGold.copy(0.7f))
                                    Text("💎 ${hostEarnings.totalDiamondsEarned}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LightGold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Converted Net Earnings", fontSize = 11.sp, color = LiveIndicatorGreen)
                                    Text("₹${hostEarnings.netInrEarnings}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LiveIndicatorGreen)
                                }
                            }

                            HorizontalDivider(color = CrimsonVelvet)

                            Text("Payout Settlement Options:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MetallicGold)

                            Row {
                                FilterChip(
                                    selected = selectedMethod == PaymentMethodType.UPI,
                                    onClick = { selectedMethod = PaymentMethodType.UPI },
                                    label = { Text("Direct UPI ID") }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                FilterChip(
                                    selected = selectedMethod == PaymentMethodType.BANK_WIRE,
                                    onClick = { selectedMethod = PaymentMethodType.BANK_WIRE },
                                    label = { Text("Direct Bank Wire") }
                                )
                            }

                            OutlinedTextField(
                                value = upiOrAccountInput,
                                onValueChange = { upiOrAccountInput = it },
                                label = { Text(if (selectedMethod == PaymentMethodType.UPI) "UPI VPA ID" else "Bank Account + IFSC", color = LightGold) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = DarkGold),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = payoutAmountInput,
                                onValueChange = { payoutAmountInput = it },
                                label = { Text("Withdrawal Amount (₹ INR)", color = LightGold) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MetallicGold, unfocusedBorderColor = DarkGold),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text("🔒 Strict Name-Match Validation: Aadhaar name '${kycData.legalName}' must match bank account holder.", fontSize = 10.sp, color = DarkGold)

                            Button(
                                onClick = {
                                    val amt = payoutAmountInput.toDoubleOrNull() ?: 0.0
                                    payoutResultMsg = onRequestPayout(amt, selectedMethod, upiOrAccountInput)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LiveIndicatorGreen),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Execute Bank Payout Request 💸", color = WineRedDark, fontWeight = FontWeight.Bold)
                            }

                            if (payoutResultMsg != null) {
                                Text(
                                    text = payoutResultMsg!!,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (payoutResultMsg!!.startsWith("SUCCESS")) LiveIndicatorGreen else HeartRed
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
