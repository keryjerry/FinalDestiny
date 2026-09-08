package com.devil.finaldestiny.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.devil.finaldestiny.engine.PaymentKycEngine
import com.devil.finaldestiny.ui.theme.*

class RazorpayWebAppInterface(
    private val onSuccess: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onDismiss: () -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onPaymentSuccess(paymentId: String) {
        handler.post { onSuccess(paymentId) }
    }

    @JavascriptInterface
    fun onPaymentError(errorMsg: String) {
        handler.post { onError(errorMsg) }
    }

    @JavascriptInterface
    fun onPaymentDismiss() {
        handler.post { onDismiss() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentQrModalDialog(
    amountInr: Int,
    itemDescription: String,
    onPaymentSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showRazorpaySheet by remember { mutableStateOf(false) }

    if (showRazorpaySheet) {
        Dialog(
            onDismissRequest = { showRazorpaySheet = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = WineRedDark
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WineRedMedium)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = MetallicGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Razorpay Test Gateway", color = MetallicGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        IconButton(onClick = { showRazorpaySheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = LightGold)
                        }
                    }

                    // WebView Container
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                @SuppressLint("SetJavaScriptEnabled")
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true
                                webViewClient = WebViewClient()

                                addJavascriptInterface(
                                    RazorpayWebAppInterface(
                                        onSuccess = { paymentId ->
                                            showRazorpaySheet = false
                                            Toast.makeText(ctx, "✅ Razorpay Payment Successful!\nID: $paymentId", Toast.LENGTH_LONG).show()
                                            onPaymentSuccess()
                                            onDismiss()
                                        },
                                        onError = { error ->
                                            Toast.makeText(ctx, "❌ Razorpay Payment Error: $error", Toast.LENGTH_SHORT).show()
                                        },
                                        onDismiss = {
                                            showRazorpaySheet = false
                                            Toast.makeText(ctx, "Payment cancelled by user", Toast.LENGTH_SHORT).show()
                                        }
                                    ),
                                    "AndroidBridge"
                                )

                                val htmlData = PaymentKycEngine.generateRazorpayCheckoutHtml(
                                    amountRupees = amountInr.toDouble(),
                                    itemDescription = itemDescription
                                )
                                loadDataWithBaseURL("https://checkout.razorpay.com", htmlData, "text/html", "UTF-8", null)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CreditCard, contentDescription = null, tint = MetallicGold, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Razorpay Secure Checkout", color = MetallicGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Item & Amount Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = WineRedMedium),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MetallicGold.copy(0.6f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Package / Item:", fontSize = 11.sp, color = LightGold)
                            Text(itemDescription, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                        }
                        Text("₹$amountInr", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MetallicGold)
                    }
                }

                // Payment Options & Test Mode Info Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = WineRedDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CrimsonVelvet, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = LiveIndicatorGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Supported Payment Methods:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LightGold)
                        }
                        Text("💳 Cards (Visa / Mastercard / RuPay)\n🏦 NetBanking (SBI, HDFC, ICICI, Axis)\n📱 UPI Apps (GPay, Paytm, PhonePe, BHIM)\n👛 Digital Wallets", fontSize = 11.sp, color = LightGold.copy(0.85f))
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("🧪 Test Mode Key: ${PaymentKycEngine.RAZORPAY_KEY_ID}", fontSize = 9.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                    }
                }

                // Primary Razorpay Gateway Launch Button
                Button(
                    onClick = {
                        showRazorpaySheet = true
                        Toast.makeText(context, "🚀 Opening Razorpay Gateway...", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = WineRedDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pay ₹$amountInr via Razorpay 💳", color = WineRedDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onPaymentSuccess()
                    onDismiss()
                    Toast.makeText(context, "✅ Razorpay Payment Verified! Product Unlocked!", Toast.LENGTH_LONG).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = LiveIndicatorGreen)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Simulate Success (Test) ✅", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = LightGold)
            }
        }
    )
}

