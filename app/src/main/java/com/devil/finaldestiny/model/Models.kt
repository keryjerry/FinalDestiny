package com.devil.finaldestiny.model

enum class Gender { MALE, FEMALE, OTHER }
enum class RoomCategory { TRENDING, REGIONAL, ROMANTIC }
enum class SeatRole { HOST, CO_HOST, GUEST }
enum class GiftCategory { GIFT_WALL, DYNAMIC_AVATARS, MIC_LINKS, LUXURY_VEHICLES, JEWELRY }
enum class PaymentMethodType { UPI, BANK_WIRE }
enum class AlertType { VISION_BLACKOUT, PROFANITY_STRIKE, KICK_NOTICE, BAN_NOTICE }

data class UserProfile(
    val id: String,
    val handle: String,
    val name: String,
    val age: Int,
    val bio: String,
    val gender: Gender,
    val photos: List<String>,
    val verifiedStatus: Boolean = true,
    val followerCount: Int = 120,
    val followingCount: Int = 45,
    val vipLevel: Int = 1,
    val exp: Int = 1200,
    val coins: Int = 5000,
    val diamonds: Int = 2500,
    val relationshipIntent: String = "Serious Dating & Marriage",
    val lifestyleTags: List<String> = listOf("Travel", "Music", "Fitness", "Coffee Lover"),
    val isHost: Boolean = true
)

data class SwipeCard(
    val id: String,
    val profile: UserProfile,
    val distanceKm: Int,
    val isSuperLiked: Boolean = false,
    val isMatched: Boolean = false
)

data class MomentPost(
    val id: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatar: String,
    val mediaUrl: String,
    val caption: String,
    val timestamp: String,
    val likesCount: Int,
    val commentsCount: Int,
    val giftTipsTotal: Int,
    val isLiked: Boolean = false
)

data class StoryItem(
    val id: String,
    val authorName: String,
    val authorAvatar: String,
    val previewMedia: String,
    val timestamp: String,
    val isViewed: Boolean = false
)

data class SofaSeat(
    val seatIndex: Int, // 1 to 10
    val role: SeatRole,
    val userProfile: UserProfile?,
    val isMuted: Boolean = false,
    val isLocked: Boolean = false,
    val isSpeaking: Boolean = false,
    val micLinkPartnerSeatIndex: Int? = null,
    val micLinkTitle: String? = null
)

data class LiveRoom(
    val id: String,
    val title: String,
    val category: RoomCategory,
    val isVideoMode: Boolean = false,
    val hostUser: UserProfile,
    val viewerCount: Int = 1420,
    val topGifterName: String = "DarkDevil",
    val topGifterDiamonds: Int = 8500,
    val youtubeVideoUrl: String = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    val seats: List<SofaSeat>
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val senderAvatar: String,
    val senderVipLevel: Int = 1,
    val text: String,
    val timestamp: String,
    val isSystemAlert: Boolean = false,
    val giftSentName: String? = null,
    val giftSentValueDiamonds: Int = 0
)

data class GiftItem(
    val id: String,
    val name: String,
    val category: GiftCategory,
    val diamondPrice: Int,
    val iconSymbol: String,
    val isLightedUp: Boolean = true,
    val achievementPct: String = "96.06%"
)

data class VipTierInfo(
    val level: Int,
    val expRequired: String,
    val title: String,
    val badgeSymbol: String,
    val visualAssets: String,
    val immunityDescription: String
)

data class KycData(
    val aadhaarNumber: String = "",
    val panNumber: String = "",
    val legalName: String = "",
    val selfieVerified: Boolean = true,
    val followerCountCheck: Int = 120,
    val isSubmitted: Boolean = false,
    val isApproved: Boolean = false
)

data class HostEarnings(
    val totalDiamondsEarned: Int = 10000,
    val netInrEarnings: Double = 2500.0, // 25% net share: 10,000 diamonds (₹10,000 worth) = ₹2,500
    val pendingPayoutInr: Double = 2500.0,
    val paymentMethodType: PaymentMethodType = PaymentMethodType.UPI,
    val upiId: String = "host@upi",
    val bankAccount: String = "987654321012",
    val bankIfsc: String = "SBIN0001234",
    val bankHolderName: String = "Aadhaar Match Host",
    val isNameMatched: Boolean = true
)

data class ModerationAlert(
    val alertType: AlertType,
    val message: String,
    val timestamp: String
)
