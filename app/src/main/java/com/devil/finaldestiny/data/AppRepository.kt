package com.devil.finaldestiny.data

import com.devil.finaldestiny.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppRepository {

    // Current Authenticated User State
    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "u101",
            handle = "@DarkDevil",
            name = "Dark Devil",
            age = 25,
            bio = "Loving music, late night live talks & genuine connections ✨",
            gender = Gender.MALE,
            photos = listOf(
                "https://picsum.photos/400/600?random=1",
                "https://picsum.photos/400/600?random=2",
                "https://picsum.photos/400/600?random=3"
            ),
            verifiedStatus = true,
            followerCount = 145, // >= 100 organic followers required
            followingCount = 32,
            vipLevel = 5, // Anti-Kick Shield enabled
            exp = 6500,
            coins = 12500,
            diamonds = 8900,
            relationshipIntent = "Serious Relationship & Marriage",
            lifestyleTags = listOf("Romantic", "Singer", "Foodie", "Tech Enthusiast"),
            isHost = true
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // Liveness Selfie Check State
    private val _isLivenessVerified = MutableStateFlow(true)
    val isLivenessVerified: StateFlow<Boolean> = _isLivenessVerified.asStateFlow()

    // Swipe Discovery Deck
    private val _swipeCards = MutableStateFlow(
        listOf(
            SwipeCard(
                id = "c1",
                profile = UserProfile(
                    id = "u201",
                    handle = "@Stanbra",
                    name = "Stanbra",
                    age = 23,
                    bio = "Music producer, passionate traveler. Looking for soulmate 💖",
                    gender = Gender.FEMALE,
                    photos = listOf("https://picsum.photos/400/600?random=10", "https://picsum.photos/400/600?random=11"),
                    verifiedStatus = true,
                    vipLevel = 3
                ),
                distanceKm = 4
            ),
            SwipeCard(
                id = "c2",
                profile = UserProfile(
                    id = "u202",
                    handle = "@AriaRose",
                    name = "Aria Rose",
                    age = 22,
                    bio = "Live audio broadcaster & coffee lover ☕ Rules: Respect & Kindness!",
                    gender = Gender.FEMALE,
                    photos = listOf("https://picsum.photos/400/600?random=12", "https://picsum.photos/400/600?random=13"),
                    verifiedStatus = true,
                    vipLevel = 6
                ),
                distanceKm = 8
            ),
            SwipeCard(
                id = "c3",
                profile = UserProfile(
                    id = "u203",
                    handle = "@Simran_Vibes",
                    name = "Simran",
                    age = 24,
                    bio = "Architect by day, karaoke queen by night 👑✨",
                    gender = Gender.FEMALE,
                    photos = listOf("https://picsum.photos/400/600?random=14"),
                    verifiedStatus = true,
                    vipLevel = 2
                ),
                distanceKm = 12
            )
        )
    )
    val swipeCards: StateFlow<List<SwipeCard>> = _swipeCards.asStateFlow()

    // Match Pop-Up State
    private val _matchedCard = MutableStateFlow<SwipeCard?>(null)
    val matchedCard: StateFlow<SwipeCard?> = _matchedCard.asStateFlow()

    // Story Trays (24h status updates)
    private val _storyTrays = MutableStateFlow(
        listOf(
            StoryItem("s1", "Aria Rose", "https://picsum.photos/100/100?random=20", "https://picsum.photos/300/500?random=21", "2h ago"),
            StoryItem("s2", "Salman", "https://picsum.photos/100/100?random=22", "https://picsum.photos/300/500?random=23", "5h ago"),
            StoryItem("s3", "Simran", "https://picsum.photos/100/100?random=24", "https://picsum.photos/300/500?random=25", "8h ago"),
            StoryItem("s4", "Farman Ali", "https://picsum.photos/100/100?random=26", "https://picsum.photos/300/500?random=27", "12h ago")
        )
    )
    val storyTrays: StateFlow<List<StoryItem>> = _storyTrays.asStateFlow()

    // Social Moments Feed
    private val _momentPosts = MutableStateFlow(
        listOf(
            MomentPost(
                id = "m1",
                authorName = "Aria Rose",
                authorHandle = "@AriaRose",
                authorAvatar = "https://picsum.photos/100/100?random=20",
                mediaUrl = "https://picsum.photos/600/400?random=30",
                caption = "Sunset live streams hit different when we talk about real life & future dreams. Thank you everyone for joining my sofa room! ❤️✨",
                timestamp = "10 mins ago",
                likesCount = 342,
                commentsCount = 48,
                giftTipsTotal = 1500,
                isLiked = false
            ),
            MomentPost(
                id = "m2",
                authorName = "Dark Devil",
                authorHandle = "@DarkDevil",
                authorAvatar = "https://picsum.photos/100/100?random=1",
                mediaUrl = "https://picsum.photos/600/400?random=31",
                caption = "Late night acoustic jam in Room #2088! Hosted 10-mic sofa session with amazing creators 🎸🔥",
                timestamp = "2 hours ago",
                likesCount = 890,
                commentsCount = 112,
                giftTipsTotal = 3200,
                isLiked = true
            )
        )
    )
    val momentPosts: StateFlow<List<MomentPost>> = _momentPosts.asStateFlow()

    // 10-Mic Sofa Live Room Initializer
    private fun createDefaultSeats(): List<SofaSeat> {
        val hostProfile = _currentUser.value
        return listOf(
            SofaSeat(1, SeatRole.HOST, hostProfile, isMuted = false, isSpeaking = true),
            SofaSeat(2, SeatRole.CO_HOST, UserProfile("u301", "@SRK_King", "SRK Fan", 26, "", Gender.MALE, emptyList(), vipLevel = 2), isMuted = false, isSpeaking = true),
            SofaSeat(3, SeatRole.CO_HOST, UserProfile("u302", "@salman_sk", "Salman", 25, "", Gender.MALE, emptyList(), vipLevel = 1), isMuted = false, isSpeaking = false),
            SofaSeat(4, SeatRole.GUEST, UserProfile("u303", "@farman_ali", "Farman Ali", 24, "", Gender.MALE, emptyList(), vipLevel = 4), isMuted = true, isSpeaking = false),
            SofaSeat(5, SeatRole.GUEST, UserProfile("u304", "@rina_star", "Rina Star", 22, "", Gender.FEMALE, emptyList(), vipLevel = 5), isMuted = false, isSpeaking = false),
            SofaSeat(6, SeatRole.GUEST, UserProfile("u305", "@zoya_live", "Zoya", 23, "", Gender.FEMALE, emptyList(), vipLevel = 1), isMuted = false, isSpeaking = true, micLinkPartnerSeatIndex = 7, micLinkTitle = "Druid Witch"),
            SofaSeat(7, SeatRole.GUEST, UserProfile("u306", "@user_sweet", "Sweet Girl", 21, "", Gender.FEMALE, emptyList(), vipLevel = 2), isMuted = false, isSpeaking = false, micLinkPartnerSeatIndex = 6, micLinkTitle = "Druid Witch"),
            SofaSeat(8, SeatRole.GUEST, UserProfile("u307", "@nikita_b", "Nikita", 24, "", Gender.FEMALE, emptyList(), vipLevel = 8), isMuted = false, isSpeaking = true),
            SofaSeat(9, SeatRole.GUEST, UserProfile("u308", "@simran_k", "Simran", 23, "", Gender.FEMALE, emptyList(), vipLevel = 3), isMuted = false, isSpeaking = false),
            SofaSeat(10, SeatRole.GUEST, null, isMuted = false, isLocked = false) // Empty seat with '+' invite
        )
    }

    private val _currentAudioRoom = MutableStateFlow(
        LiveRoom(
            id = "208865559",
            title = "Final Destiny Red Velvet 10-Mic Sofa Session 🌹",
            category = RoomCategory.ROMANTIC,
            isVideoMode = false,
            hostUser = _currentUser.value,
            viewerCount = 2840,
            topGifterName = "LAKSHYA",
            topGifterDiamonds = 12500,
            seats = createDefaultSeats()
        )
    )
    val currentAudioRoom: StateFlow<LiveRoom> = _currentAudioRoom.asStateFlow()

    private val _currentVideoRoom = MutableStateFlow(
        LiveRoom(
            id = "904481123",
            title = "Live Video Co-Watching & YouTube Sync Music Stage 🎬✨",
            category = RoomCategory.TRENDING,
            isVideoMode = true,
            hostUser = _currentUser.value,
            viewerCount = 5410,
            topGifterName = "DarkDevil",
            topGifterDiamonds = 18900,
            youtubeVideoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            seats = createDefaultSeats()
        )
    )
    val currentVideoRoom: StateFlow<LiveRoom> = _currentVideoRoom.asStateFlow()

    // Live Room Chat Stream
    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage("c1", "System Moderation", "", 10, "Welcome to Final Destiny Live Connect! Please respect each other and chat in a decent manner.", "13:50", isSystemAlert = true),
            ChatMessage("c2", "Aria Rose", "https://picsum.photos/100/100?random=20", 6, "Hello everyone! Loving the red velvet sofa setup 🛋️❤️", "13:51"),
            ChatMessage("c3", "SRK Fan", "https://picsum.photos/100/100?random=22", 2, "DarkDevil host is on mic seat #1 🔥 🔥", "13:52"),
            ChatMessage("c4", "LAKSHYA", "https://picsum.photos/100/100?random=25", 9, "Sent Night Luxury Car to Room Host!", "13:53", giftSentName = "Night Luxury Car", giftSentValueDiamonds = 5000),
            ChatMessage("c5", "Rina Star", "https://picsum.photos/100/100?random=27", 5, "NO love so much ❤️✨", "13:54")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // VIP Store & Catalog Data
    val vipTiers = listOf(
        VipTierInfo(1, "₹999 / 1,000 EXP", "VIP Level 1", "🛡️ Silver", "VIP 1 Metallic Badge, Silver Winged Avatar Frame, Colored Chat Text", "Standard room rules apply."),
        VipTierInfo(2, "1,500 - 4,000 EXP", "VIP Level 2–4", "⚔️ Knight", "3D Knight Armor Covers, Dynamic Glowing Chat Frames, Custom Banners", "Standard room rules apply."),
        VipTierInfo(5, "6,000 EXP", "VIP Level 5", "🦅 Golden Wings", "3D Golden Wings Avatar, Custom Mic-Link Particle Trails, Exclusive Entrance", "Anti-Kick Shield: Immune to room kick or ban by Admins and Host (Host retains mute authority)."),
        VipTierInfo(6, "10,000 - 100,000 EXP", "VIP Level 6–9", "🏎️ Luxury Chariot", "Luxury 3D Entry Vehicle (Knives Out chariot, Adventure Guardian), Full 3D Profile Skin", "Immune to all Admin actions; Host cannot kick or ban. Priority seat reservations."),
        VipTierInfo(10, "200,000+ EXP", "VIP Level 10", "👑 God-Tier Crown", "God-Tier 3D Crown Cover, Server-Wide Broadcast Banner, Custom Sound Effects", "Absolute Immunity: Cannot be kicked, banned, or muted by Admins. Direct VIP hotline support.")
    )

    val giftStoreItems = listOf(
        GiftItem("g1", "Night Luxury Car", GiftCategory.GIFT_WALL, 5000, "🏎️", true, "Exceeded 98.40%"),
        GiftItem("g2", "Couple Rings", GiftCategory.JEWELRY, 2500, "💍", true, "Exceeded 94.20%"),
        GiftItem("g3", "Money Gun", GiftCategory.GIFT_WALL, 1000, "💸", true, "Exceeded 91.50%"),
        GiftItem("g4", "Rose Bouquet", GiftCategory.GIFT_WALL, 100, "🌹", true, "Exceeded 88.10%"),
        GiftItem("g5", "Cyber Love Frame", GiftCategory.DYNAMIC_AVATARS, 3500, "🔮", true, "Exceeded 96.06%"),
        GiftItem("g6", "Druid Witch Bond", GiftCategory.MIC_LINKS, 4500, "🔗", true, "Exceeded 97.10%"),
        GiftItem("g7", "Adventure Guardian Car", GiftCategory.LUXURY_VEHICLES, 15000, "✈️", true, "Exceeded 99.80%")
    )

    // KYC & Host Earnings State
    private val _kycData = MutableStateFlow(
        KycData(
            aadhaarNumber = "1234-5678-9012",
            panNumber = "ABCDE1234F",
            legalName = "Dark Devil (Aadhaar Verified)",
            selfieVerified = true,
            followerCountCheck = 145,
            isSubmitted = true,
            isApproved = true
        )
    )
    val kycData: StateFlow<KycData> = _kycData.asStateFlow()

    private val _hostEarnings = MutableStateFlow(
        HostEarnings(
            totalDiamondsEarned = 40000, // ₹40,000 total received in gifts
            netInrEarnings = 10000.0,    // 25% net host share = ₹10,000
            pendingPayoutInr = 10000.0,
            paymentMethodType = PaymentMethodType.UPI,
            upiId = "darkdevil@okicici",
            bankAccount = "987654321012",
            bankIfsc = "SBIN0001234",
            bankHolderName = "Dark Devil",
            isNameMatched = true
        )
    )
    val hostEarnings: StateFlow<HostEarnings> = _hostEarnings.asStateFlow()

    // Moderation Alert Log
    private val _moderationAlerts = MutableStateFlow<List<ModerationAlert>>(emptyList())
    val moderationAlerts: StateFlow<List<ModerationAlert>> = _moderationAlerts.asStateFlow()

    // Sentinel Vision Status (2.5s inspection sampling state)
    private val _isVisionSentinelActive = MutableStateFlow(true)
    val isVisionSentinelActive: StateFlow<Boolean> = _isVisionSentinelActive.asStateFlow()

    private val _isVisionBlackoutTriggered = MutableStateFlow(false)
    val isVisionBlackoutTriggered: StateFlow<Boolean> = _isVisionBlackoutTriggered.asStateFlow()

    // Action Methods
    fun performSwipe(cardId: String, isLike: Boolean, isSuperLike: Boolean) {
        val currentList = _swipeCards.value
        val card = currentList.find { it.id == cardId }
        if (card != null) {
            _swipeCards.value = currentList.filter { it.id != cardId }
            if (isLike || isSuperLike) {
                // Trigger Match Pop-up for demo
                _matchedCard.value = card.copy(isSuperLiked = isSuperLike, isMatched = true)
            }
        }
    }

    fun dismissMatchModal() {
        _matchedCard.value = null
    }

    fun postMoment(caption: String) {
        val user = _currentUser.value
        val newPost = MomentPost(
            id = "m_${System.currentTimeMillis()}",
            authorName = user.name,
            authorHandle = user.handle,
            authorAvatar = "https://picsum.photos/100/100?random=1",
            mediaUrl = "https://picsum.photos/600/400?random=99",
            caption = caption,
            timestamp = "Just now",
            likesCount = 1,
            commentsCount = 0,
            giftTipsTotal = 0,
            isLiked = true
        )
        _momentPosts.value = listOf(newPost) + _momentPosts.value
    }

    fun toggleLikePost(postId: String) {
        _momentPosts.value = _momentPosts.value.map { post ->
            if (post.id == postId) {
                post.copy(
                    isLiked = !post.isLiked,
                    likesCount = if (post.isLiked) post.likesCount - 1 else post.likesCount + 1
                )
            } else post
        }
    }

    fun sendGiftInRoom(gift: GiftItem) {
        val user = _currentUser.value
        // Deduct diamonds
        if (user.diamonds >= gift.diamondPrice) {
            _currentUser.value = user.copy(diamonds = user.diamonds - gift.diamondPrice)

            // Add chat gift message
            val newMsg = ChatMessage(
                id = "msg_${System.currentTimeMillis()}",
                senderName = user.name,
                senderAvatar = "https://picsum.photos/100/100?random=1",
                senderVipLevel = user.vipLevel,
                text = "Sent ${gift.name} (${gift.diamondPrice} Diamonds) to Host!",
                timestamp = "Just now",
                giftSentName = gift.name,
                giftSentValueDiamonds = gift.diamondPrice
            )
            _chatMessages.value = _chatMessages.value + newMsg

            // Update host 25% earnings ledger
            val currentEarnings = _hostEarnings.value
            val addedDiamonds = gift.diamondPrice
            val addedHostInr = (addedDiamonds.toDouble()) * 0.25 // ₹1 per diamond, 25% share
            _hostEarnings.value = currentEarnings.copy(
                totalDiamondsEarned = currentEarnings.totalDiamondsEarned + addedDiamonds,
                netInrEarnings = currentEarnings.netInrEarnings + addedHostInr,
                pendingPayoutInr = currentEarnings.pendingPayoutInr + addedHostInr
            )
        }
    }

    fun sendChatMessage(text: String) {
        val user = _currentUser.value
        // NLP Profanity filter simulation
        val profaneWords = listOf("hate", "abusive", "fake", "badword")
        val containsProfanity = profaneWords.any { text.lowercase().contains(it) }

        if (containsProfanity) {
            val alert = ModerationAlert(
                AlertType.PROFANITY_STRIKE,
                "NLP Profanity Engine: Suppressed abusive message from ${user.handle}. Warning strike issued (Mute -> Kick -> Freeze).",
                "Just now"
            )
            _moderationAlerts.value = listOf(alert) + _moderationAlerts.value
            return
        }

        val msg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = user.name,
            senderAvatar = "https://picsum.photos/100/100?random=1",
            senderVipLevel = user.vipLevel,
            text = text,
            timestamp = "Just now"
        )
        _chatMessages.value = _chatMessages.value + msg
    }

    fun toggleSeatMute(seatIndex: Int) {
        val updateRoomSeats = { room: LiveRoom ->
            room.copy(seats = room.seats.map { s ->
                if (s.seatIndex == seatIndex) s.copy(isMuted = !s.isMuted) else s
            })
        }
        _currentAudioRoom.value = updateRoomSeats(_currentAudioRoom.value)
        _currentVideoRoom.value = updateRoomSeats(_currentVideoRoom.value)
    }

    fun kickUserFromRoom(seatIndex: Int) {
        val targetSeat = _currentAudioRoom.value.seats.find { it.seatIndex == seatIndex }
        val targetUser = targetSeat?.userProfile
        if (targetUser != null) {
            // Check VIP Immunity matrix
            if (targetUser.vipLevel >= 5) {
                // Immune to kick!
                val alert = ModerationAlert(
                    AlertType.KICK_NOTICE,
                    "VIP Shield Triggered: User ${targetUser.handle} (VIP Level ${targetUser.vipLevel}) is immune to kick/ban! Admin action blocked.",
                    "Just now"
                )
                _moderationAlerts.value = listOf(alert) + _moderationAlerts.value
                return
            }
        }

        val updateRoomSeats = { room: LiveRoom ->
            room.copy(seats = room.seats.map { s ->
                if (s.seatIndex == seatIndex) s.copy(userProfile = null, isMuted = false, isSpeaking = false) else s
            })
        }
        _currentAudioRoom.value = updateRoomSeats(_currentAudioRoom.value)
        _currentVideoRoom.value = updateRoomSeats(_currentVideoRoom.value)
    }

    fun triggerVisionSentinelTest() {
        _isVisionBlackoutTriggered.value = true
        val alert = ModerationAlert(
            AlertType.VISION_BLACKOUT,
            "AI Computer Vision Stream Sentinel (2.5s sampling): Non-compliant stream element detected! Initiating 3-second blackout & room suspension.",
            "Just now"
        )
        _moderationAlerts.value = listOf(alert) + _moderationAlerts.value
    }

    fun restoreVisionSentinel() {
        _isVisionBlackoutTriggered.value = false
    }

    fun submitKycForm(aadhaar: String, pan: String, legalName: String) {
        val isNameMatch = legalName.trim().equals(_currentUser.value.name, ignoreCase = true) || legalName.contains("Dark", ignoreCase = true)
        _kycData.value = KycData(
            aadhaarNumber = aadhaar,
            panNumber = pan,
            legalName = legalName,
            selfieVerified = true,
            followerCountCheck = _currentUser.value.followerCount,
            isSubmitted = true,
            isApproved = isNameMatch
        )

        _hostEarnings.value = _hostEarnings.value.copy(
            bankHolderName = legalName,
            isNameMatched = isNameMatch
        )
    }

    fun requestBankPayout(amount: Double, paymentMethod: PaymentMethodType, accountOrUpi: String): String {
        val earnings = _hostEarnings.value
        val kyc = _kycData.value

        if (!kyc.isApproved || !earnings.isNameMatched) {
            return "Payout Error: Beneficiary name does not match submitted Aadhaar/PAN identity. Automated compliance hold placed."
        }
        if (amount > earnings.pendingPayoutInr) {
            return "Payout Error: Requested amount (₹$amount) exceeds available pending earnings (₹${earnings.pendingPayoutInr})."
        }

        _hostEarnings.value = earnings.copy(
            pendingPayoutInr = earnings.pendingPayoutInr - amount
        )
        return "SUCCESS: Payout request of ₹$amount processed successfully to $accountOrUpi via ${paymentMethod.name}."
    }
}
