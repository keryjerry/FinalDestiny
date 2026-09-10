package com.devil.finaldestiny.model

enum class Gender { MALE, FEMALE, OTHER }
enum class RoomCategory { TRENDING, REGIONAL, ROMANTIC }
enum class SeatRole { HOST, CO_HOST, GUEST }
enum class GiftCategory { GIFT_WALL, DYNAMIC_AVATARS, MIC_LINKS, LUXURY_VEHICLES, JEWELRY }
enum class PaymentMethodType { UPI, BANK_WIRE }
enum class AlertType { VISION_BLACKOUT, PROFANITY_STRIKE, KICK_NOTICE, BAN_NOTICE }

data class UserProfile(
    val id: String = "usr_me",
    val handle: String = "@destiny_creator",
    val name: String = "Destiny Creator",
    val age: Int = 23,
    val bio: String = "Creating reels & live streams on Final Destiny ✨",
    val gender: Gender = Gender.FEMALE,
    val photos: List<String> = emptyList(),
    val verifiedStatus: Boolean = true,
    val followerCount: Int = 520,
    val followingCount: Int = 145,
    val vipLevel: Int = 1,
    val exp: Int = 1200,
    val coins: Int = 5000,
    val diamonds: Int = 2500,
    val relationshipIntent: String = "Serious Dating & Marriage",
    val lifestyleTags: List<String> = listOf("Travel", "Music", "Fitness", "Coffee Lover"),
    val isHost: Boolean = true,
    val profilePictureUri: String? = null,
    val accountType: String = "Creator",
    val creatorCategory: String = "Digital creator",
    val displayCategoryOnProfile: Boolean = true,
    val payoutUpi: String = "creator@okaxis",
    val minimumAge: Int = 0,
    val brandedContentEnabled: Boolean = true,
    val crosspostingEnabled: Boolean = false,
    val trialReelsEnabled: Boolean = true,
    val savedReplies: List<String> = listOf("Thanks for reaching out! ✨", "Check out my latest Reel!", "Collaborations: dm@finaldestiny.app")
)

data class SwipeCard(
    val id: String,
    val profile: UserProfile,
    val distanceKm: Int,
    val isSuperLiked: Boolean = false,
    val isMatched: Boolean = false
)

data class SearchResultUser(
    val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String?,
    val matchChip: String,
    val isVerified: Boolean = true,
    val isFollowing: Boolean = false
)

data class DirectMessageConversation(
    val id: String,
    val userName: String,
    val userHandle: String,
    val userAvatar: String?,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = true
)

data class AudioTrack(
    val id: String,
    val title: String,
    val artist: String,
    val albumCoverUrl: String? = null,
    val audioUrl: String? = null,
    val duration: String = "0:30",
    val isSaved: Boolean = false
)

enum class MediaType { PHOTO, REEL_VIDEO }

data class MomentComment(
    val senderName: String,
    val text: String,
    val timestamp: String
)

data class MomentPost(
    val id: String,
    val userId: String = "",
    val authorName: String,
    val authorHandle: String,
    val authorAvatar: String,
    val mediaUrl: String,
    val caption: String,
    val timestamp: String,
    val likesCount: Int,
    val commentsCount: Int,
    val giftTipsTotal: Int,
    val isLiked: Boolean = false,
    val isFollowingAuthor: Boolean = false,
    val comments: List<MomentComment> = listOf(
        MomentComment("Stanbra", "Love this post! ❤️", "5m ago"),
        MomentComment("Simran", "Amazing vibes! 🔥", "2m ago")
    ),
    val mediaUri: String? = null,
    val mediaType: MediaType = MediaType.PHOTO,
    val isSponsored: Boolean = false,
    val sponsorName: String? = null,
    val ctaText: String? = null,
    val ctaUrl: String? = null,
    val videoDuration: String? = "0:30",
    val viewsCount: Int = 0,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val audioTitle: String? = "Susheela Raman • Ye Meera Deewanapan",
    val audioArtist: String? = "Susheela Raman",
    val audioUrl: String? = null,
    val collaboratorName: String? = "mohanshakti",
    val repostsCount: Int = 2327,
    val sharesCount: Int = 1969,
    val isSaved: Boolean = false,
    val primaryCategory: String = "Entertainment",
    val subCategories: List<String> = listOf("Dance", "Music", "Vlog"),
    val hashtags: List<String> = listOf("#destiny", "#reels", "#viral"),
    val keywords: List<String> = listOf("dance", "music", "trending", "vibes"),
    val language: String = "en",
    val region: String = "Global",
    val audioTrackId: String? = null,
    val qualityScore: Float = 0.85f,
    val phash: String? = null,
    val viralVelocity: Float = 1.0f,
    val isAiGenerated: Boolean = false,
    val commentsDisabled: Boolean = false,
    val hideLikeCount: Boolean = false,
    val hideShareCount: Boolean = false,
    val scheduledAt: String? = null,
    val altText: String? = null,
    val appliedFilter: String? = null,
    val overlayText: String? = null,
    val ctaLabel: String? = null,
    val isPaidPartnership: Boolean = false,
    val promotionStatus: String = "none",
    val promotionBudget: Double = 0.0
)

data class StoryItem(
    val id: String,
    val authorName: String,
    val authorAvatar: String,
    val previewMedia: String,
    val timestamp: String,
    val isViewed: Boolean = false,
    val mediaUri: String? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val primaryCategory: String = "General",
    val authorId: String = "",
    val caption: String? = null,
    val backgroundStyle: String = "classic",
    val musicTrackTitle: String? = null,
    val musicArtistName: String? = null,
    val isCloseFriendsOnly: Boolean = false,
    val interactiveStickerType: String? = null,
    val interactiveStickerQuestion: String? = null,
    val interactiveStickerOptions: List<String> = emptyList(),
    val locationTag: String? = null,
    val mentorTag: String? = null,
    val isAiEnhanced: Boolean = false,
    val micLinkTitle: String? = null
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
    val topGifterName: String = "Top Supporter",
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

enum class NotificationType {
    LIKE,
    FOLLOW,
    MATCH,
    ROOM_INVITE,
    LIVE_ALERT,
    GIFT_TIP,
    SYSTEM
}

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val iconSymbol: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val actionTargetScreen: String? = null
)

data class DailyAnalyticsPoint(
    val dayLabel: String,
    val viewsCount: Int,
    val engagementCount: Int
)

data class PostGranularInsight(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val totalViews: Int,
    val peakViewingHours: String,
    val likeToViewRatioPct: Double,
    val commentsCount: Int,
    val sharesCount: Int,
    val viralVelocityBadge: String
)

data class CreatorMilestoneAlert(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val iconSymbol: String = "🎉"
)

data class CreatorAnalytics(
    val totalViews: Int = 124500,
    val viewsGrowthPct: Double = 18.4,
    val totalLikes: Int = 14820,
    val likesGrowthPct: Double = 12.6,
    val totalFollowers: Int = 145,
    val followersGrowthPct: Double = 35.2,
    val totalImpressions: Int = 340000,
    val avgWatchDuration: String = "0:24",
    val completionRatePct: Double = 78.5,
    val reachEngagementRatioPct: Double = 12.8,
    val dailyPoints7D: List<DailyAnalyticsPoint> = listOf(
        DailyAnalyticsPoint("Mon", 12000, 1400),
        DailyAnalyticsPoint("Tue", 18500, 2100),
        DailyAnalyticsPoint("Wed", 25400, 3200),
        DailyAnalyticsPoint("Thu", 19200, 2200),
        DailyAnalyticsPoint("Fri", 32100, 4100),
        DailyAnalyticsPoint("Sat", 45600, 5800),
        DailyAnalyticsPoint("Sun", 28400, 3600)
    ),
    val dailyPoints30D: List<DailyAnalyticsPoint> = listOf(
        DailyAnalyticsPoint("W1", 45000, 5600),
        DailyAnalyticsPoint("W2", 72000, 8900),
        DailyAnalyticsPoint("W3", 110000, 14200),
        DailyAnalyticsPoint("W4", 154000, 19800)
    ),
    val postInsights: List<PostGranularInsight> = listOf(
        PostGranularInsight(
            id = "p1",
            title = "🎬 Dance Choreography to Trending Beats",
            thumbnailUrl = "https://picsum.photos/300/400?random=88",
            totalViews = 45200,
            peakViewingHours = "8:00 PM - 10:30 PM",
            likeToViewRatioPct = 11.4,
            commentsCount = 480,
            sharesCount = 1240,
            viralVelocityBadge = "🔥 Trending: Gaining 500+ views/hr"
        ),
        PostGranularInsight(
            id = "p2",
            title = "Sunset Acoustic Jam Session",
            thumbnailUrl = "https://picsum.photos/300/400?random=30",
            totalViews = 28400,
            peakViewingHours = "9:30 PM - 11:30 PM",
            likeToViewRatioPct = 14.2,
            commentsCount = 310,
            sharesCount = 890,
            viralVelocityBadge = "⚡ High Engagement"
        ),
        PostGranularInsight(
            id = "p3",
            title = "Late Night 10-Mic Sofa Talk",
            thumbnailUrl = "https://picsum.photos/300/400?random=31",
            totalViews = 18900,
            peakViewingHours = "11:00 PM - 1:30 AM",
            likeToViewRatioPct = 9.8,
            commentsCount = 195,
            sharesCount = 420,
            viralVelocityBadge = "🎙️ Room Driver"
        )
    ),
    val milestoneAlerts: List<CreatorMilestoneAlert> = listOf(
        CreatorMilestoneAlert("m1", "🎉 Milestone Unlocked!", "Passed 100,000 Cumulative Views milestone on your reels!", "10m ago"),
        CreatorMilestoneAlert("m2", "🔥 Reel Viral Alert", "'Dance Choreography' reached 45,000 views!", "2h ago"),
        CreatorMilestoneAlert("m3", "📈 Audience Spurt", "+35 Followers joined from your recent live audio stream!", "5h ago")
    )
)

enum class EventType {
    LIKE, COMMENT, SHARE, SAVE_BOOKMARK, PROFILE_VISIT, STORY_VIEW, STORY_REPLY, FOLLOW,
    DWELL, WATCH_COMPLETE, REWATCH_LOOP, FAST_SKIP, NOT_INTERESTED, SEARCH_QUERY
}

data class TelemetryEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String,
    val postId: String,
    val eventType: EventType,
    val dwellTimeMs: Long = 0L,
    val scrollSpeed: Float = 0.0f,
    val completionRate: Float = 0.0f,
    val dropOffTimestamp: Float = 0.0f,
    val timestampMs: Long = System.currentTimeMillis()
)

data class UserInterestVector(
    val userId: String,
    val categoryAffinities: Map<String, Float> = emptyMap(),
    val tagAffinities: Map<String, Float> = emptyMap(),
    val audioAffinities: Map<String, Float> = emptyMap(),
    val creatorAffinities: Map<String, Float> = emptyMap(),
    val lastUpdatedMs: Long = System.currentTimeMillis()
)

data class RecommendedUser(
    val userProfile: UserProfile,
    val mutualFollowersCount: Int = 3,
    val sharedInterests: List<String> = listOf("Music", "Travel"),
    val intimacyScore: Float = 0.75f,
    val matchReason: String = "3 mutual friends & shared love for Music"
)
