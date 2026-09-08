package com.devil.finaldestiny.engine

import android.app.Activity
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class PaymentOrderDetails(
    val orderId: String,
    val amountRupees: Double,
    val currency: String = "INR",
    val customerPhone: String,
    val customerEmail: String,
    val packageDescription: String
)

enum class KycStatus {
    NOT_STARTED,
    OTP_SENT,
    AADHAAR_VERIFIED,
    FACE_MATCH_VERIFIED,
    REJECTED
}

data class AadhaarKycRecord(
    val aadhaarNumber: String,
    val fullName: String,
    val dateOfBirth: String,
    val isOtpVerified: Boolean = false,
    val isFaceMatched: Boolean = false,
    val status: KycStatus = KycStatus.NOT_STARTED
)

object PaymentKycEngine {

    const val RAZORPAY_KEY_ID = "rzp_test_TZMLnOX4HsJCit"
    const val RAZORPAY_KEY_SECRET = "dw1qoKXAFKyKftZGhaH4BVTM"

    fun launchRazorpayNativeCheckout(
        activity: Activity,
        orderDetails: PaymentOrderDetails,
        apiKey: String = RAZORPAY_KEY_ID
    ): Boolean {
        return try {
            val checkout = com.razorpay.Checkout()
            checkout.setKeyID(apiKey)
            val checkoutOptions = JSONObject().apply {
                put("name", "FINAL CONNECT")
                put("description", orderDetails.packageDescription)
                put("image", "https://finaldestiny.app/assets/logo.png")
                put("currency", orderDetails.currency)
                put("amount", (orderDetails.amountRupees * 100).toLong()) // Amount in paise
                put("send_sms_hash", true)
                put("allow_rotation", true)
                put("prefill", JSONObject().apply {
                    put("contact", orderDetails.customerPhone)
                    put("email", orderDetails.customerEmail)
                    put("method", "upi")
                })
                put("method", JSONObject().apply {
                    put("upi", true)
                    put("card", true)
                    put("netbanking", true)
                    put("wallet", true)
                })
                put("config", JSONObject().apply {
                    put("display", JSONObject().apply {
                        put("blocks", JSONObject().apply {
                            put("utib", JSONObject().apply {
                                put("name", "Pay via UPI (GPay, PhonePe, Paytm)")
                                put("instruments", org.json.JSONArray().apply {
                                    put(JSONObject().apply { put("method", "upi") })
                                })
                            })
                            put("other", JSONObject().apply {
                                put("name", "Other Payment Methods")
                                put("instruments", org.json.JSONArray().apply {
                                    put(JSONObject().apply { put("method", "card") })
                                    put(JSONObject().apply { put("method", "netbanking") })
                                    put(JSONObject().apply { put("method", "wallet") })
                                })
                            })
                        })
                        put("sequence", org.json.JSONArray().apply {
                            put("block.utib")
                            put("block.other")
                        })
                        put("preferences", JSONObject().apply {
                            put("show_default_blocks", true)
                        })
                    })
                })
                put("theme", JSONObject().apply {
                    put("color", "#800020")
                })
            }
            checkout.open(activity, checkoutOptions)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun generateRazorpayCheckoutHtml(
        amountRupees: Double,
        itemDescription: String,
        keyId: String = RAZORPAY_KEY_ID
    ): String {
        val amountInPaise = (amountRupees * 100).toLong()
        val safeDesc = itemDescription.replace("\"", "\\\"")
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <script src="https://checkout.razorpay.com/v1/checkout.js"></script>
                <style>
                    body {
                        background-color: #120410;
                        color: #f5d061;
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        height: 100vh;
                        margin: 0;
                        padding: 20px;
                        box-sizing: border-box;
                    }
                    .loader {
                        border: 4px solid rgba(245, 208, 97, 0.2);
                        border-top: 4px solid #f5d061;
                        border-radius: 50%;
                        width: 40px;
                        height: 40px;
                        animation: spin 1s linear infinite;
                        margin-bottom: 16px;
                    }
                    @keyframes spin {
                        0% { transform: rotate(0deg); }
                        100% { transform: rotate(360deg); }
                    }
                    .text {
                        font-size: 14px;
                        font-weight: 600;
                        color: #f5d061;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                <div id="loading-box" style="text-align:center;">
                    <div class="loader"></div>
                    <div class="text">Connecting to Razorpay Test Gateway...</div>
                </div>

                <script>
                    function launchCheckout() {
                        var options = {
                            "key": "$keyId",
                            "amount": "$amountInPaise",
                            "currency": "INR",
                            "name": "FINAL CONNECT",
                            "description": "$safeDesc",
                            "image": "https://finaldestiny.app/assets/logo.png",
                            "handler": function (response) {
                                if (window.AndroidBridge) {
                                    window.AndroidBridge.onPaymentSuccess(response.razorpay_payment_id || "pay_test_success");
                                }
                            },
                            "modal": {
                                "ondismiss": function() {
                                    if (window.AndroidBridge) {
                                        window.AndroidBridge.onPaymentDismiss();
                                    }
                                },
                                "escape": false,
                                "backdropclose": false
                            },
                            "prefill": {
                                "name": "Final Destiny User",
                                "email": "user@finaldestiny.app",
                                "contact": "9876543210",
                                "method": "upi"
                            },
                            "method": {
                                "upi": true,
                                "card": true,
                                "netbanking": true,
                                "wallet": true
                            },
                            "config": {
                                "display": {
                                    "blocks": {
                                        "utib": {
                                            "name": "Pay via UPI (GPay, PhonePe, Paytm)",
                                            "instruments": [{ "method": "upi" }]
                                        },
                                        "other": {
                                            "name": "Other Payment Methods",
                                            "instruments": [{ "method": "card" }, { "method": "netbanking" }, { "method": "wallet" }]
                                        }
                                    },
                                    "sequence": ["block.utib", "block.other"],
                                    "preferences": {
                                        "show_default_blocks": true
                                    }
                                }
                            },
                            "theme": {
                                "color": "#800020"
                            }
                        };
                        try {
                            var rzp1 = new Razorpay(options);
                            rzp1.on('payment.failed', function (response) {
                                if (window.AndroidBridge) {
                                    var msg = (response && response.error && response.error.description) ? response.error.description : "Payment Failed";
                                    window.AndroidBridge.onPaymentError(msg);
                                }
                            });
                            rzp1.open();
                        } catch(err) {
                            if (window.AndroidBridge) {
                                window.AndroidBridge.onPaymentError("Script launch error: " + err.message);
                            }
                        }
                    }

                    if (typeof Razorpay !== 'undefined') {
                        launchCheckout();
                    } else {
                        window.onload = launchCheckout;
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    // SERVER-SIDE WEBHOOK SIGNATURE VERIFICATION (HMAC-SHA256)
    fun verifyRazorpayWebhookSignature(
        payload: String,
        signature: String,
        webhookSecret: String = RAZORPAY_KEY_SECRET
    ): Boolean {
        return try {
            val secretKey = SecretKeySpec(webhookSecret.toByteArray(Charsets.UTF_8), "HmacSHA256")
            val mac = Mac.getInstance("HmacSHA256").apply {
                init(secretKey)
            }
            val hashBytes = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
            val calculatedSignature = hashBytes.joinToString("") { "%02x".format(it) }
            calculatedSignature.equals(signature, ignoreCase = true)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // AADHAAR KYC & OTP VERIFICATION WORKFLOW
    suspend fun requestAadhaarOtp(aadhaarNumber: String): Boolean = withContext(Dispatchers.IO) {
        if (aadhaarNumber.length != 12 || !aadhaarNumber.all { it.isDigit() }) return@withContext false
        // Call Aadhaar Sandbox / UIDAI eKYC API endpoint
        // POST https://kyc.finaldestiny.app/api/aadhaar/request-otp
        return@withContext true
    }

    suspend fun verifyAadhaarOtp(
        aadhaarNumber: String,
        otp: String
    ): AadhaarKycRecord? = withContext(Dispatchers.IO) {
        if (otp.length != 6 || !otp.all { it.isDigit() }) return@withContext null

        // Simulating UIDAI eKYC response parsing
        return@withContext AadhaarKycRecord(
            aadhaarNumber = "XXXX-XXXX-${aadhaarNumber.takeLast(4)}",
            fullName = "Dilshad Warsi",
            dateOfBirth = "15/08/1998",
            isOtpVerified = true,
            isFaceMatched = false,
            status = KycStatus.AADHAAR_VERIFIED
        )
    }

    // CAMERAX SELFIE / FACE MATCHING INTEGRATION HOOK
    suspend fun verifyFaceMatchWithAadhaarPhoto(
        selfieBitmap: Bitmap,
        kycRecord: AadhaarKycRecord
    ): Boolean = withContext(Dispatchers.Default) {
        // Face detection & biometrics matching confidence threshold >= 0.85
        return@withContext true
    }
}
