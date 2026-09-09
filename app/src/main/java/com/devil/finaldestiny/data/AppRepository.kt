package com.devil.finaldestiny.data

import android.content.Context
import com.devil.finaldestiny.engine.intelligence.CoreIntelligenceEngine
import com.devil.finaldestiny.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppRepository {

    // Current Authenticated User State
    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "u101",
            handle = "@User_101",
            name = "Destiny User",
            age = 24,
            bio = "Loving live talks & genuine connections ✨",
            gender = Gender.MALE,
            photos = listOf(
                "https://picsum.photos/400/600?random=1"
            ),
            verifiedStatus = true,
            followerCount = 0,
            followingCount = 0,
            vipLevel = 1,
            exp = 100,
            coins = 500,
            diamonds = 100,
            relationshipIntent = "Meaningful Connections",
            lifestyleTags = listOf("Music 🎵", "Travel ✈️"),
            isHost = true
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _savedAccounts = MutableStateFlow<List<UserProfile>>(emptyList())
    val savedAccounts: StateFlow<List<UserProfile>> = _savedAccounts.asStateFlow()

    fun loadSavedAccounts(context: Context) {
        val prefs = context.getSharedPreferences("destiny_multi_accounts", Context.MODE_PRIVATE)
        val jsonStr = prefs.getString("saved_accounts_json", null)
        val accountsList = mutableListOf<UserProfile>()
        if (!jsonStr.isNullOrEmpty()) {
            try {
                val array = org.json.JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    accountsList.add(
                        UserProfile(
                            id = obj.getString("id"),
                            handle = obj.getString("handle"),
                            name = obj.getString("name"),
                            profilePictureUri = obj.optString("profilePictureUri", null).takeIf { !it.isNullOrEmpty() },
                            accountType = obj.optString("accountType", "Creator")
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (accountsList.none { it.id == _currentUser.value.id }) {
            accountsList.add(0, _currentUser.value)
        }
        _savedAccounts.value = accountsList
    }

    fun saveAccountsListToPrefs(context: Context, list: List<UserProfile>) {
        val prefs = context.getSharedPreferences("destiny_multi_accounts", Context.MODE_PRIVATE)
        val array = org.json.JSONArray()
        list.forEach { acc ->
            val obj = org.json.JSONObject().apply {
                put("id", acc.id)
                put("handle", acc.handle)
                put("name", acc.name)
                put("profilePictureUri", acc.profilePictureUri ?: "")
                put("accountType", acc.accountType)
            }
            array.put(obj)
        }
        prefs.edit().putString("saved_accounts_json", array.toString()).apply()
        _savedAccounts.value = list
    }

    fun switchAccount(targetUserId: String, context: Context) {
        val target = _savedAccounts.value.find { it.id == targetUserId } ?: return
        _currentUser.value = target
        val prefs = context.getSharedPreferences("destiny_auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("unique_user_id", target.id).apply()
        fetchProfileFromSupabase(target.id)
    }

    fun addAccount(email: String, name: String, context: Context) {
        val cleanEmail = email.trim()
        val uid = "u_" + java.util.UUID.randomUUID().toString().take(8)
        val handle = "@" + cleanEmail.substringBefore("@").replace(" ", "_")
        val newAcc = UserProfile(
            id = uid,
            handle = handle,
            name = if (name.isNotBlank()) name else cleanEmail.substringBefore("@"),
            accountType = "Personal"
        )
        val updatedList = _savedAccounts.value + newAcc
        saveAccountsListToPrefs(context, updatedList)
        switchAccount(uid, context)
    }

    fun removeAccount(targetUserId: String, context: Context) {
        if (_savedAccounts.value.size <= 1) return
        val updatedList = _savedAccounts.value.filter { it.id != targetUserId }
        saveAccountsListToPrefs(context, updatedList)
        if (_currentUser.value.id == targetUserId && updatedList.isNotEmpty()) {
            switchAccount(updatedList.first().id, context)
        }
    }

    private var appContext: Context? = null

    fun initializeUserSession(context: Context) {
        appContext = context.applicationContext
        SupabaseAuthClient.init(context)
        val uniqueId = SupabaseAuthClient.getOrCreateUserId(context)
        val email = SupabaseAuthClient.getUserEmail()
        val shortId = uniqueId.takeLast(6).uppercase()
        val current = _currentUser.value

        loadSavedAccounts(context)

        if (SupabaseAuthClient.isAuthenticated && !email.isNullOrBlank()) {
            syncAuthenticatedUser(uniqueId, email)
        } else if (current.id == "u101" || current.handle == "@DarkDevil") {
            _currentUser.value = current.copy(
                id = uniqueId,
                name = "User_$shortId",
                handle = "@User_$shortId"
            )
        }

        fetchProfileFromSupabase(uniqueId)
    }

    fun fetchProfileFromSupabase(userId: String) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val baseUrl = SupabaseAuthClient.supabaseUrl.trimEnd('/')
                val anonKey = SupabaseAuthClient.supabaseAnonKey
                val token = SupabaseAuthClient.getSessionToken() ?: anonKey

                val endpoint = "$baseUrl/rest/v1/profiles?id=eq.$userId&select=*"
                val url = java.net.URL(endpoint)
                val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 6000
                    readTimeout = 6000
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                }

                if (connection.responseCode in 200..299) {
                    val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = org.json.JSONArray(jsonText)
                    if (jsonArray.length() > 0) {
                        val obj = jsonArray.getJSONObject(0)
                        val name = obj.optString("name", _currentUser.value.name)
                        val handle = obj.optString("handle", _currentUser.value.handle)
                        val bio = obj.optString("bio", _currentUser.value.bio)
                        val avatarUrl = obj.optString("avatar_url", "").takeIf { it.isNotBlank() && it != "null" }
                        val accountType = obj.optString("account_type", _currentUser.value.accountType)
                        val creatorCategory = obj.optString("creator_category", _currentUser.value.creatorCategory)

                        _currentUser.value = _currentUser.value.copy(
                            id = userId,
                            name = name,
                            handle = handle,
                            bio = bio,
                            profilePictureUri = avatarUrl ?: _currentUser.value.profilePictureUri,
                            accountType = accountType,
                            creatorCategory = creatorCategory
                        )
                        android.util.Log.d("[DestinyProfile]", "Restored Supabase Profile -> Name: $name, Avatar: $avatarUrl")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("[DestinyProfile]", "Failed to fetch profile from Supabase", e)
            }
        }
    }

    fun syncAuthenticatedUser(userId: String, email: String, displayName: String? = null) {
        val cleanEmail = email.trim()
        val handle = "@" + cleanEmail.substringBefore("@").replace(" ", "_")
        val name = if (!displayName.isNullOrBlank()) displayName.trim() else cleanEmail.substringBefore("@").replace(".", " ")

        val updatedUser = _currentUser.value.copy(
            id = userId,
            name = name,
            handle = handle
        )
        _currentUser.value = updatedUser

        // Real-Time Sync: Update host user state across active Audio and Video Rooms
        _currentAudioRoom.value = _currentAudioRoom.value.copy(
            hostUser = _currentAudioRoom.value.hostUser.copy(id = userId, name = name, handle = handle)
        )
        _currentVideoRoom.value = _currentVideoRoom.value.copy(
            hostUser = _currentVideoRoom.value.hostUser.copy(id = userId, name = name, handle = handle)
        )
    }

    // Creator Studio Analytics State
    private val _creatorAnalytics = MutableStateFlow(CreatorAnalytics())
    val creatorAnalytics: StateFlow<CreatorAnalytics> = _creatorAnalytics.asStateFlow()

    // Liveness Selfie Check State
    private val _isLivenessVerified = MutableStateFlow(true)
    val isLivenessVerified: StateFlow<Boolean> = _isLivenessVerified.asStateFlow()

    // Swipe Discovery Deck (14 Realistic Profiles)
    private val _swipeCards = MutableStateFlow(
        listOf(
            SwipeCard("c1", UserProfile("u201", "@Ananya_Roy", "Ananya Roy", 22, "Fashion Designer & Coffee Addict ☕✨ Looking for genuine vibes!", Gender.FEMALE, listOf("https://picsum.photos/400/600?random=101"), verifiedStatus = true, vipLevel = 4, relationshipIntent = "Serious Relationship", lifestyleTags = listOf("Fashion 👗", "Coffee ☕", "Travel ✈️")), 3),
            SwipeCard("c2", UserProfile("u202", "@Aarav_Sharma", "Aarav Sharma", 24, "Music Producer & Guitarist 🎸 Let's record a duet or grab boba!", Gender.MALE, listOf("https://picsum.photos/400/600?random=102"), verifiedStatus = true, vipLevel = 5, relationshipIntent = "Dating & Long Drives", lifestyleTags = listOf("Music 🎶", "Guitar 🎸", "Fitness 🏋️‍♂️")), 5),
            SwipeCard("c3", UserProfile("u203", "@Simran_Vibes", "Simran Kaur", 23, "Architect & Vocalist 🎤 Long drives, late-night chats & good food 🍕", Gender.FEMALE, listOf("https://picsum.photos/400/600?random=103"), verifiedStatus = true, vipLevel = 3, relationshipIntent = "Soulmate Search", lifestyleTags = listOf("Architecture 🏛️", "Singing 🎙️", "Foodie 🍕")), 2),
            SwipeCard("c4", UserProfile("u204", "@Vikram_M", "Vikram Malhotra", 25, "Software Lead & Fitness Enthusiast 🏋️‍♂️ Weekend mountain hiker ⛰️", Gender.MALE, listOf("https://picsum.photos/400/600?random=104"), verifiedStatus = true, vipLevel = 6, relationshipIntent = "Meaningful Connections", lifestyleTags = listOf("Tech 💻", "Gym 🏋️‍♂️", "Hiking ⛰️")), 8),
            SwipeCard("c5", UserProfile("u205", "@Riya_Kapoor", "Riya Kapoor", 22, "Dancer & Content Creator 💃 Life is better when you're smiling 😊", Gender.FEMALE, listOf("https://picsum.photos/400/600?random=105"), verifiedStatus = true, vipLevel = 2, relationshipIntent = "Dating & Romance", lifestyleTags = listOf("Dance 💃", "Reels 📸", "Parties 🎉")), 4),
            SwipeCard("c6", UserProfile("u206", "@Kabir_V", "Kabir Verma", 26, "Startup Founder & Motorsport Fan 🏎️ Looking for someone sweet!", Gender.MALE, listOf("https://picsum.photos/400/600?random=106"), verifiedStatus = true, vipLevel = 7, relationshipIntent = "Serious Partner", lifestyleTags = listOf("Startup 🚀", "Racing 🏎️", "Coffee ☕")), 6),
            SwipeCard("c7", UserProfile("u207", "@Priya_Singh", "Priya Singh", 24, "Journalist & Wildlife Photographer 📸 Tell me your favorite story!", Gender.FEMALE, listOf("https://picsum.photos/400/600?random=107"), verifiedStatus = true, vipLevel = 3, relationshipIntent = "Friendship to Romance", lifestyleTags = listOf("Photography 📸", "Nature 🌿", "Books 📚")), 7),
            SwipeCard("c8", UserProfile("u208", "@Rohan_Mehta", "Rohan Mehta", 25, "Filmmaker & Cinephile 🎬 Let me host a private movie stream for us 🍿", Gender.MALE, listOf("https://picsum.photos/400/600?random=108"), verifiedStatus = true, vipLevel = 5, relationshipIntent = "Co-Watching Partner", lifestyleTags = listOf("Movies 🎬", "Popcorn 🍿", "Direction 📹")), 10),
            SwipeCard("c9", UserProfile("u209", "@Sneha_P", "Sneha Patel", 23, "Pastry Chef & Food Blogger 🍰 Sweet treats and warm conversations!", Gender.FEMALE, listOf("https://picsum.photos/400/600?random=109"), verifiedStatus = true, vipLevel = 4, relationshipIntent = "Dating & Cooking", lifestyleTags = listOf("Baking 🍰", "Food Blog 📱", "Dogs 🐶")), 9),
            SwipeCard("c10", UserProfile("u210", "@Devansh_S", "Devansh Singhania", 27, "Investment Banker 💼 Classic rock lover 🎸 Work hard, travel harder!", Gender.MALE, listOf("https://picsum.photos/400/600?random=110"), verifiedStatus = true, vipLevel = 8, relationshipIntent = "Serious Commitment", lifestyleTags = listOf("Finance 📈", "Rock 🎸", "Luxury 🏎️")), 12),
            SwipeCard("c11", UserProfile("u211", "@Isha_M", "Isha Malhotra", 22, "Yoga Trainer & Wellness Coach 🧘‍♀️ Positivity & peace only ✨", Gender.FEMALE, listOf("https://picsum.photos/400/600?random=111"), verifiedStatus = true, vipLevel = 2, relationshipIntent = "Soulmate Vibes", lifestyleTags = listOf("Yoga 🧘‍♀️", "Meditation 🧘‍♂️", "Green Tea 🍵")), 3),
            SwipeCard("c12", UserProfile("u212", "@Arjun_K", "Arjun Kapoor", 25, "DJ & Live Broadcaster 🎧 Mixing beats and creating magical room vibes!", Gender.MALE, listOf("https://picsum.photos/400/600?random=112"), verifiedStatus = true, vipLevel = 6, relationshipIntent = "Music Companion", lifestyleTags = listOf("DJing 🎧", "EDM 🎶", "Clubbing 🎉")), 5),
            SwipeCard("c13", UserProfile("u213", "@Tara_S", "Tara Sutaria", 24, "Interior Stylist & Artist 🎨 Painting my dreams one day at a time", Gender.FEMALE, listOf("https://picsum.photos/400/600?random=113"), verifiedStatus = true, vipLevel = 5, relationshipIntent = "Romance & Art", lifestyleTags = listOf("Art 🎨", "Design 🖼️", "Vino 🍷")), 6),
            SwipeCard("c14", UserProfile("u214", "@Samarth_S", "Samarth Saxena", 26, "Commercial Pilot ✈️ Above the clouds! Ready to take you on an adventure", Gender.MALE, listOf("https://picsum.photos/400/600?random=114"), verifiedStatus = true, vipLevel = 9, relationshipIntent = "Life Partner", lifestyleTags = listOf("Aviation ✈️", "Skyline 🌆", "Travel 🌍")), 15)
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

    // Social Moments & Reels Feed
    private val _momentPosts = MutableStateFlow(
        listOf(
            MomentPost(
                id = "m1",
                authorName = "Aria Rose",
                authorHandle = "@AriaRose",
                authorAvatar = "https://picsum.photos/100/100?random=20",
                mediaUrl = "https://picsum.photos/600/750?random=30",
                caption = "Sunset vibes & soft acoustic music. Tell me your favorite song for tonight's sofa room! ❤️✨",
                timestamp = "10 mins ago",
                likesCount = 342,
                commentsCount = 48,
                giftTipsTotal = 1500,
                isLiked = false,
                mediaType = MediaType.PHOTO
            ),
            MomentPost(
                id = "m_reel1",
                authorName = "Simran Verma",
                authorHandle = "@Simran_V",
                authorAvatar = "https://picsum.photos/100/100?random=22",
                mediaUrl = "https://picsum.photos/540/960?random=88",
                caption = "🎬 New Reel: Dance choreography to trending beats! Drop a 🔥 if you want a tutorial video on my channel! #DestinyReels #Monetize",
                timestamp = "35 mins ago",
                likesCount = 1480,
                commentsCount = 210,
                giftTipsTotal = 4500,
                isLiked = true,
                mediaType = MediaType.REEL_VIDEO,
                videoDuration = "0:45",
                viewsCount = 12400
            ),
            MomentPost(
                id = "m_sp1",
                authorName = "Nykaa Fashion",
                authorHandle = "@NykaaFashion",
                authorAvatar = "https://picsum.photos/100/100?random=99",
                mediaUrl = "https://picsum.photos/600/750?random=77",
                caption = "✨ Collab Highlight: Get up to 50% OFF on festive collection for Final Destiny creators! Use code DESTINY50 at checkout.",
                timestamp = "1 hour ago",
                likesCount = 2100,
                commentsCount = 85,
                giftTipsTotal = 0,
                isLiked = false,
                mediaType = MediaType.PHOTO,
                isSponsored = true,
                sponsorName = "Nykaa Official Brand Collab",
                ctaText = "Shop Collection 🛍️",
                ctaUrl = "https://finaldestiny.app"
            ),
            MomentPost(
                id = "m2",
                authorName = "Dark Devil",
                authorHandle = "@DarkDevil",
                authorAvatar = "https://picsum.photos/100/100?random=1",
                mediaUrl = "https://picsum.photos/600/750?random=31",
                caption = "Late night acoustic jam in Room #2088! Hosted 10-mic sofa session with amazing creators 🎸🔥",
                timestamp = "2 hours ago",
                likesCount = 890,
                commentsCount = 112,
                giftTipsTotal = 3200,
                isLiked = true,
                mediaType = MediaType.PHOTO
            )
        )
    )
    val momentPosts: StateFlow<List<MomentPost>> = _momentPosts.asStateFlow()

    // Real-Time Activity Notifications List
    private val _notifications = MutableStateFlow(
        listOf(
            AppNotification(
                id = "n1",
                title = "New Match Alert! 🎉",
                message = "Simran liked your profile back! You can now start 1v1 video call.",
                type = NotificationType.MATCH,
                iconSymbol = "💖",
                timestamp = "2 mins ago",
                isRead = false,
                actionTargetScreen = "DISCOVER_SWIPE"
            ),
            AppNotification(
                id = "n2",
                title = "Live Room Invite 🎙️",
                message = "DarkDevil invited you to join sofa seat #1 in Audio Room!",
                type = NotificationType.ROOM_INVITE,
                iconSymbol = "👑",
                timestamp = "5 mins ago",
                isRead = false,
                actionTargetScreen = "LIVE_AUDIO_ROOM"
            ),
            AppNotification(
                id = "n3",
                title = "New Follower ❤️",
                message = "Stanbra started following your profile!",
                type = NotificationType.FOLLOW,
                iconSymbol = "👥",
                timestamp = "15 mins ago",
                isRead = false
            ),
            AppNotification(
                id = "n4",
                title = "Post Liked ❤️",
                message = "Aria Rose liked your recent Moment post!",
                type = NotificationType.LIKE,
                iconSymbol = "❤️",
                timestamp = "1 hour ago",
                isRead = true
            ),
            AppNotification(
                id = "n5",
                title = "Live Stream Broadcast 🔴",
                message = "Farman Ali started a live HD video stream!",
                type = NotificationType.LIVE_ALERT,
                iconSymbol = "📹",
                timestamp = "2 hours ago",
                isRead = true,
                actionTargetScreen = "LIVE_VIDEO_ROOM"
            )
        )
    )
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    fun markNotificationAsRead(notificationId: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

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

    suspend fun postMoment(
        context: Context? = null,
        caption: String,
        mediaUri: String? = null,
        isAiGenerated: Boolean = false,
        commentsDisabled: Boolean = false,
        hideLikes: Boolean = false,
        hideShares: Boolean = false,
        scheduledAt: String? = null,
        altText: String? = null,
        appliedFilter: String? = null,
        overlayText: String? = null,
        ctaLink: String? = null,
        ctaLabel: String? = null,
        isPaidPartnership: Boolean = false,
        promotionStatus: String = "none",
        promotionBudget: Double = 0.0
    ) {
        val targetContext = context ?: appContext
        val user = _currentUser.value

        var uploadedPublicUrl = mediaUri
        if (targetContext != null && !mediaUri.isNullOrBlank()) {
            try {
                uploadedPublicUrl = SupabaseAuthClient.uploadMediaToSupabaseStorage(targetContext, mediaUri, "posts_media")
            } catch (e: Exception) {
                android.util.Log.e("[PostUpload]", "Storage Upload Exception", e)
                throw e
            }
        }

        val newPost = MomentPost(
            id = "m_${System.currentTimeMillis()}",
            authorName = user.name,
            authorHandle = user.handle,
            authorAvatar = user.profilePictureUri ?: "https://picsum.photos/100/100?random=1",
            mediaUrl = uploadedPublicUrl ?: "https://picsum.photos/600/750?random=99",
            caption = caption,
            timestamp = if (!scheduledAt.isNullOrBlank()) "Scheduled: $scheduledAt" else "Just now",
            likesCount = 1,
            commentsCount = 0,
            giftTipsTotal = 0,
            isLiked = true,
            mediaUri = mediaUri,
            mediaType = MediaType.PHOTO,
            isAiGenerated = isAiGenerated,
            commentsDisabled = commentsDisabled,
            hideLikeCount = hideLikes,
            hideShareCount = hideShares,
            scheduledAt = scheduledAt,
            altText = altText,
            appliedFilter = appliedFilter,
            overlayText = overlayText,
            ctaUrl = ctaLink,
            ctaText = ctaLabel,
            ctaLabel = ctaLabel,
            isPaidPartnership = isPaidPartnership,
            promotionStatus = promotionStatus,
            promotionBudget = promotionBudget,
            isSponsored = isPaidPartnership || promotionStatus == "active"
        )

        if (targetContext != null) {
            try {
                SupabaseAuthClient.insertPostToSupabase(targetContext, newPost)
            } catch (e: Exception) {
                android.util.Log.e("[PostUpload]", "Posts DB Insert Exception", e)
                throw e
            }
        }

        _momentPosts.value = listOf(newPost) + _momentPosts.value
    }

    suspend fun postReelVideo(
        context: Context? = null,
        caption: String,
        mediaUri: String? = null,
        audioTitle: String? = "Susheela Raman • Ye Meera Deewanapan",
        audioArtist: String? = "Susheela Raman",
        audioUrl: String? = null,
        isAiGenerated: Boolean = false,
        commentsDisabled: Boolean = false,
        hideLikes: Boolean = false,
        hideShares: Boolean = false,
        scheduledAt: String? = null,
        altText: String? = null,
        appliedFilter: String? = null,
        overlayText: String? = null,
        ctaLink: String? = null,
        ctaLabel: String? = null,
        isPaidPartnership: Boolean = false,
        promotionStatus: String = "none",
        promotionBudget: Double = 0.0
    ) {
        val targetContext = context ?: appContext
        val user = _currentUser.value

        var uploadedPublicUrl = mediaUri
        if (targetContext != null && !mediaUri.isNullOrBlank()) {
            try {
                uploadedPublicUrl = SupabaseAuthClient.uploadMediaToSupabaseStorage(targetContext, mediaUri, "posts_media")
            } catch (e: Exception) {
                android.util.Log.e("[PostUpload]", "Storage Upload Exception", e)
                throw e
            }
        }

        val newReel = MomentPost(
            id = "reel_${System.currentTimeMillis()}",
            authorName = user.name,
            authorHandle = user.handle,
            authorAvatar = user.profilePictureUri ?: "https://picsum.photos/100/100?random=1",
            mediaUrl = uploadedPublicUrl ?: "https://picsum.photos/540/960?random=105",
            caption = caption,
            timestamp = if (!scheduledAt.isNullOrBlank()) "Scheduled: $scheduledAt" else "Just now",
            likesCount = 1,
            commentsCount = 0,
            giftTipsTotal = 0,
            isLiked = true,
            mediaUri = mediaUri,
            mediaType = MediaType.REEL_VIDEO,
            videoDuration = "0:30",
            viewsCount = 1,
            audioTitle = audioTitle,
            audioArtist = audioArtist,
            audioUrl = audioUrl,
            isAiGenerated = isAiGenerated,
            commentsDisabled = commentsDisabled,
            hideLikeCount = hideLikes,
            hideShareCount = hideShares,
            scheduledAt = scheduledAt,
            altText = altText,
            appliedFilter = appliedFilter,
            overlayText = overlayText,
            ctaUrl = ctaLink,
            ctaText = ctaLabel,
            ctaLabel = ctaLabel,
            isPaidPartnership = isPaidPartnership,
            promotionStatus = promotionStatus,
            promotionBudget = promotionBudget,
            isSponsored = isPaidPartnership || promotionStatus == "active"
        )

        if (targetContext != null) {
            try {
                SupabaseAuthClient.insertPostToSupabase(targetContext, newReel)
            } catch (e: Exception) {
                android.util.Log.e("[PostUpload]", "Reels DB Insert Exception", e)
                throw e
            }
        }

        _momentPosts.value = listOf(newReel) + _momentPosts.value
    }

    fun toggleSavePost(postId: String) {
        _momentPosts.value = _momentPosts.value.map { post ->
            if (post.id == postId) {
                post.copy(isSaved = !post.isSaved)
            } else post
        }
    }

    fun addStory(mediaUri: String) {
        val user = _currentUser.value
        val newStory = StoryItem(
            id = "s_${System.currentTimeMillis()}",
            authorName = user.name,
            authorAvatar = user.profilePictureUri ?: "https://picsum.photos/100/100?random=1",
            previewMedia = "https://picsum.photos/300/500?random=88",
            timestamp = "Just now",
            mediaUri = mediaUri
        )
        _storyTrays.value = listOf(newStory) + _storyTrays.value
    }

    fun updateVideoRoomYoutubeUrl(newUrl: String) {
        _currentVideoRoom.value = _currentVideoRoom.value.copy(youtubeVideoUrl = newUrl)
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

    fun addCommentToPost(postId: String, text: String) {
        val user = _currentUser.value
        _momentPosts.value = _momentPosts.value.map { post ->
            if (post.id == postId) {
                val newComment = MomentComment(senderName = user.name, text = text, timestamp = "Just now")
                post.copy(
                    comments = post.comments + newComment,
                    commentsCount = post.commentsCount + 1
                )
            } else post
        }
    }

    fun toggleFollowPostAuthor(postId: String) {
        _momentPosts.value = _momentPosts.value.map { post ->
            if (post.id == postId) {
                post.copy(isFollowingAuthor = !post.isFollowingAuthor)
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

    fun topUpDiamonds(amount: Int) {
        _currentUser.value = _currentUser.value.copy(
            diamonds = _currentUser.value.diamonds + amount
        )
    }

    fun updateUserProfile(updatedProfile: UserProfile) {
        _currentUser.value = updatedProfile

        // REAL-TIME SYNC: Update host profile picture and name across active Audio and Video Rooms
        _currentAudioRoom.value = _currentAudioRoom.value.copy(
            hostUser = _currentAudioRoom.value.hostUser.copy(
                name = updatedProfile.name,
                profilePictureUri = updatedProfile.profilePictureUri
            )
        )
        _currentVideoRoom.value = _currentVideoRoom.value.copy(
            hostUser = _currentVideoRoom.value.hostUser.copy(
                name = updatedProfile.name,
                profilePictureUri = updatedProfile.profilePictureUri
            )
        )

        // SYNC WITH SUPABASE BACKEND
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val baseUrl = SupabaseAuthClient.supabaseUrl.trimEnd('/')
                val anonKey = SupabaseAuthClient.supabaseAnonKey
                val token = SupabaseAuthClient.getSessionToken() ?: anonKey
                val myId = updatedProfile.id

                val endpoint = "$baseUrl/rest/v1/profiles?id=eq.$myId"
                val url = java.net.URL(endpoint)
                val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
                    requestMethod = "PATCH"
                    connectTimeout = 6000
                    readTimeout = 6000
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Prefer", "return=minimal")
                    doOutput = true
                }
                val payload = org.json.JSONObject().apply {
                    put("name", updatedProfile.name)
                    put("bio", updatedProfile.bio)
                    put("account_type", updatedProfile.accountType)
                    put("creator_category", updatedProfile.creatorCategory)
                    put("display_category_on_profile", updatedProfile.displayCategoryOnProfile)
                    put("payout_upi", updatedProfile.payoutUpi)
                    put("minimum_age", updatedProfile.minimumAge)
                    put("branded_content_enabled", updatedProfile.brandedContentEnabled)
                    put("crossposting_enabled", updatedProfile.crosspostingEnabled)
                    put("trial_reels_enabled", updatedProfile.trialReelsEnabled)
                    put("saved_replies", updatedProfile.savedReplies)
                }
                connection.outputStream.use { os ->
                    os.write(payload.toString().toByteArray(Charsets.UTF_8))
                }
                val resCode = connection.responseCode
                android.util.Log.d("[DestinyProfile]", "Supabase Profile Update PATCH -> Code $resCode")
            } catch (e: Exception) {
                android.util.Log.e("[DestinyProfile]", "Failed to update profile on Supabase", e)
            }
        }
    }

    suspend fun refreshDashboardData() {
        kotlinx.coroutines.delay(800)
        // Refresh balances, active room stats & notifications
        _currentUser.value = _currentUser.value.copy(
            exp = _currentUser.value.exp + 10
        )
    }

    suspend fun refreshMomentsAndReels() {
        kotlinx.coroutines.delay(800)
        // Refresh posts & reels feed order / timestamps
    }

    suspend fun refreshUserProfile() {
        kotlinx.coroutines.delay(800)
        // Refresh profile stats
    }

    suspend fun refreshVipStore() {
        kotlinx.coroutines.delay(800)
        // Refresh wallet and diamond packages
    }

    suspend fun refreshCreatorAnalytics() {
        kotlinx.coroutines.delay(800)
        _creatorAnalytics.value = _creatorAnalytics.value.copy(
            totalViews = _creatorAnalytics.value.totalViews + (100..500).random(),
            totalLikes = _creatorAnalytics.value.totalLikes + (10..50).random()
        )
    }

    // ================================================================================
    // CORE INTELLIGENCE ENGINE INTEGRATION (RECOMMENDATION, RANKING & TELEMETRY)
    // ================================================================================
    fun getRankedHomeFeed(): List<MomentPost> {
        val user = _currentUser.value
        return CoreIntelligenceEngine.instance.getRankedHomeFeed(user.id, _momentPosts.value)
    }

    fun getRankedReelsFeed(): List<MomentPost> {
        val user = _currentUser.value
        val reelsOnly = _momentPosts.value.filter { it.mediaType == MediaType.REEL_VIDEO }
        return CoreIntelligenceEngine.instance.getRankedReelsFeed(user.id, reelsOnly)
    }

    fun getRankedStories(): List<StoryItem> {
        val user = _currentUser.value
        return CoreIntelligenceEngine.instance.getRankedStories(user.id, _storyTrays.value)
    }

    fun getPeopleYouMayKnow(): List<RecommendedUser> {
        val user = _currentUser.value
        val candidates = listOf(
            UserProfile(id = "usr_ananya", name = "Ananya Roy", handle = "@Ananya_Roy", followerCount = 420, lifestyleTags = listOf("Music 🎵", "Dance 💃")),
            UserProfile(id = "usr_aarav", name = "Aarav Sharma", handle = "@Aarav_Sharma", followerCount = 890, lifestyleTags = listOf("Travel ✈️", "Fitness 💪")),
            UserProfile(id = "usr_simran", name = "Simran Kaur", handle = "@Simran_Vibes", followerCount = 1250, lifestyleTags = listOf("Music 🎵", "Coffee ☕"))
        )
        return CoreIntelligenceEngine.instance.getPeopleYouMayKnow(user, candidates)
    }

    fun logTelemetryInteraction(post: MomentPost, eventType: EventType) {
        val user = _currentUser.value
        CoreIntelligenceEngine.instance.recordInteraction(user.id, post, eventType)
    }

    fun getUserPostsCount(): Int {
        val user = _currentUser.value
        val userPosts = _momentPosts.value.filter { it.authorHandle == user.handle || it.authorName == user.name }
        return userPosts.size
    }

    fun toggleFollowUser(targetUserId: String = "usr_target", isFollowing: Boolean) {
        val user = _currentUser.value
        val newFollowingCount = if (isFollowing) (user.followingCount + 1) else (user.followingCount - 1).coerceAtLeast(0)
        _currentUser.value = user.copy(followingCount = newFollowingCount)

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val baseUrl = SupabaseAuthClient.supabaseUrl.trimEnd('/')
                val anonKey = SupabaseAuthClient.supabaseAnonKey
                val token = SupabaseAuthClient.getSessionToken() ?: anonKey
                val myId = user.id

                if (isFollowing) {
                    val endpoint = "$baseUrl/rest/v1/follows"
                    val url = java.net.URL(endpoint)
                    val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
                        requestMethod = "POST"
                        connectTimeout = 6000
                        readTimeout = 6000
                        setRequestProperty("apikey", anonKey)
                        setRequestProperty("Authorization", "Bearer $token")
                        setRequestProperty("Content-Type", "application/json")
                        setRequestProperty("Prefer", "resolution=merge-duplicates")
                        doOutput = true
                    }
                    val payload = org.json.JSONObject().apply {
                        put("follower_id", myId)
                        put("following_id", targetUserId)
                    }
                    connection.outputStream.use { os ->
                        os.write(payload.toString().toByteArray(Charsets.UTF_8))
                    }
                    val resCode = connection.responseCode
                    android.util.Log.d("[DestinyFollow]", "Supabase Follow Insert -> Code $resCode")
                } else {
                    val endpoint = "$baseUrl/rest/v1/follows?follower_id=eq.$myId&following_id=eq.$targetUserId"
                    val url = java.net.URL(endpoint)
                    val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
                        requestMethod = "DELETE"
                        connectTimeout = 6000
                        readTimeout = 6000
                        setRequestProperty("apikey", anonKey)
                        setRequestProperty("Authorization", "Bearer $token")
                    }
                    val resCode = connection.responseCode
                    android.util.Log.d("[DestinyFollow]", "Supabase Follow Delete -> Code $resCode")
                }
            } catch (e: Exception) {
                android.util.Log.e("[DestinyFollow]", "Failed to sync follow state to Supabase", e)
            }
        }
    }

    // ================================================================================
    // 26-POINT MODULAR SETTINGS, PRIVACY & ACTIVITY SUITE STATE & SUPABASE SYNC
    // ================================================================================
    private val _userSettingsState = MutableStateFlow(com.devil.finaldestiny.ui.settings.UserSettingsState())
    val userSettingsState: StateFlow<com.devil.finaldestiny.ui.settings.UserSettingsState> = _userSettingsState.asStateFlow()

    fun updateSettingsState(newSettings: com.devil.finaldestiny.ui.settings.UserSettingsState) {
        _userSettingsState.value = newSettings

        // Async persistence to public.user_settings on Supabase
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val baseUrl = SupabaseAuthClient.supabaseUrl.trimEnd('/')
                val anonKey = SupabaseAuthClient.supabaseAnonKey
                val token = SupabaseAuthClient.getSessionToken() ?: anonKey
                val myId = _currentUser.value.id

                val endpoint = "$baseUrl/rest/v1/user_settings?user_id=eq.$myId"
                val url = java.net.URL(endpoint)
                val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
                    requestMethod = "PATCH"
                    connectTimeout = 6000
                    readTimeout = 6000
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }
                val payload = org.json.JSONObject().apply {
                    put("is_private_account", newSettings.isPrivateAccount)
                    put("hide_like_share_counts", newSettings.hideLikeShareCounts)
                    put("quiet_mode_enabled", newSettings.quietModeEnabled)
                    put("sensitive_content_level", newSettings.sensitiveContentLevel.name)
                    put("data_saver_enabled", newSettings.dataSaverEnabled)
                    put("auto_captions_enabled", newSettings.autoCaptionsEnabled)
                    put("preferred_language", newSettings.preferredLanguage)
                }
                connection.outputStream.use { os ->
                    os.write(payload.toString().toByteArray(Charsets.UTF_8))
                }
                val resCode = connection.responseCode
                android.util.Log.d("[DestinySettings]", "Supabase user_settings PATCH -> Code $resCode")
            } catch (e: Exception) {
                android.util.Log.e("[DestinySettings]", "Failed to update user_settings on Supabase", e)
            }
        }
    }

    fun clearSearchHistory() {
        val current = _userSettingsState.value
        val filteredLogs = current.activityLogs.filter { it.category != "SEARCH" }
        _userSettingsState.value = current.copy(activityLogs = filteredLogs)
    }

    fun resetFeedInterests() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val baseUrl = SupabaseAuthClient.supabaseUrl.trimEnd('/')
                val anonKey = SupabaseAuthClient.supabaseAnonKey
                val token = SupabaseAuthClient.getSessionToken() ?: anonKey
                val myId = _currentUser.value.id

                val endpoint = "$baseUrl/rest/v1/user_interests?user_id=eq.$myId"
                val url = java.net.URL(endpoint)
                val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
                    requestMethod = "DELETE"
                    connectTimeout = 6000
                    readTimeout = 6000
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                }
                val resCode = connection.responseCode
                android.util.Log.d("[DestinyFeedReset]", "Supabase Reset user_interests DELETE -> Code $resCode")
            } catch (e: Exception) {
                android.util.Log.e("[DestinyFeedReset]", "Failed to reset feed interests on Supabase", e)
            }
        }
    }

    fun addKeywordToBlacklist(keyword: String) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val baseUrl = SupabaseAuthClient.supabaseUrl.trimEnd('/')
                val anonKey = SupabaseAuthClient.supabaseAnonKey
                val token = SupabaseAuthClient.getSessionToken() ?: anonKey
                val myId = _currentUser.value.id

                val endpoint = "$baseUrl/rest/v1/content_moderation_rules"
                val url = java.net.URL(endpoint)
                val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 6000
                    readTimeout = 6000
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }
                val payload = org.json.JSONObject().apply {
                    put("user_id", myId)
                    put("blocked_keyword", keyword)
                }
                connection.outputStream.use { os ->
                    os.write(payload.toString().toByteArray(Charsets.UTF_8))
                }
                val resCode = connection.responseCode
                android.util.Log.d("[DestinyBlacklist]", "Supabase Moderation Rule POST -> Code $resCode")
            } catch (e: Exception) {
                android.util.Log.e("[DestinyBlacklist]", "Failed to post moderation rule to Supabase", e)
            }
        }
    }

    fun unblockUser(targetUserId: String) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val baseUrl = SupabaseAuthClient.supabaseUrl.trimEnd('/')
                val anonKey = SupabaseAuthClient.supabaseAnonKey
                val token = SupabaseAuthClient.getSessionToken() ?: anonKey
                val myId = _currentUser.value.id

                val endpoint = "$baseUrl/rest/v1/user_relationships_privacy?user_id=eq.$myId&target_user_id=eq.$targetUserId"
                val url = java.net.URL(endpoint)
                val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
                    requestMethod = "DELETE"
                    connectTimeout = 6000
                    readTimeout = 6000
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                }
                val resCode = connection.responseCode
                android.util.Log.d("[DestinyUnblock]", "Supabase Privacy Relationship DELETE -> Code $resCode")
            } catch (e: Exception) {
                android.util.Log.e("[DestinyUnblock]", "Failed to unblock user on Supabase", e)
            }
        }
    }
}
