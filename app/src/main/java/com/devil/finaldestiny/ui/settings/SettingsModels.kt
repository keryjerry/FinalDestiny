package com.devil.finaldestiny.ui.settings

enum class SensitiveContentLevel { LOW, MEDIUM, HIGH }

data class SavedCollectionFolder(
    val id: String,
    val name: String,
    val category: String, // "POSTS", "REELS", "AUDIO"
    val itemCount: Int,
    val thumbnailUrls: List<String>
)

data class ArchiveVaultItem(
    val id: String,
    val title: String,
    val mediaUrl: String,
    val type: String, // "STORY", "LIVE"
    val dateLabel: String,
    val daysRemaining: Int = 30
)

data class ActivityLogEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val timestamp: String,
    val category: String // "WATCH", "SEARCH", "LIKE", "COMMENT", "VISIT"
)

data class CloseFriendsList(
    val id: String,
    val name: String,
    val memberCount: Int,
    val memberNames: List<String>
)

data class BlockedMutedUser(
    val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String?,
    val isBlocked: Boolean,
    val isMuted: Boolean,
    val category: String // "POSTS", "STORIES", "REELS", "MESSAGES"
)

data class LoginSessionItem(
    val id: String,
    val deviceName: String,
    val location: String,
    val ipAddress: String,
    val lastActive: String,
    val isCurrentDevice: Boolean = false
)

data class UserSettingsState(
    // 1. Accounts Center & Security
    val is2faEnabled: Boolean = false,
    val email: String = "user@finaldestiny.app",
    val phoneNumber: String = "+91 9876543210",
    val birthday: String = "15 August 2000",
    val walletBalanceInr: Double = 1250.0,

    // 2. Saved & Activity
    val savedCollections: List<SavedCollectionFolder> = listOf(
        SavedCollectionFolder("sc1", "Travel & Mountains", "POSTS", 12, listOf("https://picsum.photos/300/300?random=501", "https://picsum.photos/300/300?random=502", "https://picsum.photos/300/300?random=503")),
        SavedCollectionFolder("sc2", "Trending Reels", "REELS", 8, listOf("https://picsum.photos/300/300?random=504", "https://picsum.photos/300/300?random=505")),
        SavedCollectionFolder("sc3", "Acoustic Audio Tracks", "AUDIO", 5, listOf("https://picsum.photos/300/300?random=506"))
    ),
    val archiveItems: List<ArchiveVaultItem> = listOf(
        ArchiveVaultItem("av1", "Darjeeling Sunset Story", "https://picsum.photos/300/500?random=601", "STORY", "12 Aug 2026", 24),
        ArchiveVaultItem("av2", "10-Mic Red Velvet Live Stream", "https://picsum.photos/300/500?random=602", "LIVE", "01 Sep 2026", 28)
    ),
    val activityLogs: List<ActivityLogEntry> = listOf(
        ActivityLogEntry("al1", "Watched Reel by @Simran_V", "Dance choreography to trending beats", "10 mins ago", "WATCH"),
        ActivityLogEntry("al2", "Searched for 'Darjeeling travel'", "Recent search query", "25 mins ago", "SEARCH"),
        ActivityLogEntry("al3", "Liked post by @AriaRose", "Sunset vibes & soft acoustic music", "1 hour ago", "LIKE")
    ),

    // 3. Notifications, Focus & Usage
    val pushLikesEnabled: Boolean = true,
    val pushMentionsEnabled: Boolean = true,
    val pushDirectMessagesEnabled: Boolean = true,
    val pushCallsEnabled: Boolean = true,
    val pushSecurityAlertsEnabled: Boolean = true,
    val quietModeEnabled: Boolean = false,
    val quietModeStartTime: String = "22:00",
    val quietModeEndTime: String = "07:00",
    val breakReminderMinutes: Int = 30, // 0 (Off), 15, 30, 45
    val dailyScreenTimeMinutes: Int = 42,

    // 4. Privacy & Social Controls
    val isPrivateAccount: Boolean = false,
    val hideLikeShareCounts: Boolean = false,
    val hideFollowerFollowingList: Boolean = false,
    val closeFriendsLists: List<CloseFriendsList> = listOf(
        CloseFriendsList("cf1", "Best Travel Buddies", 4, listOf("@Anurag_Ray", "@Sahil_Khan", "@Sagarika_S", "@DarkDevil")),
        CloseFriendsList("cf2", "VIP Creators", 2, listOf("@AriaRose", "@SRK_King"))
    ),
    val tagApprovalRequired: Boolean = true,
    val offensiveCommentFilterEnabled: Boolean = true,
    val keywordBlacklist: List<String> = listOf("spam", "scam", "hate"),
    val blockedMutedUsers: List<BlockedMutedUser> = listOf(
        BlockedMutedUser("bm1", "Spam User 99", "@spam_user99", null, isBlocked = true, isMuted = false, category = "MESSAGES"),
        BlockedMutedUser("bm2", "Noisy Bot", "@noisy_bot", null, isBlocked = false, isMuted = true, category = "STORIES")
    ),
    val messageRequestGate: String = "Everyone", // "Everyone", "Followers Only", "No One"
    val readReceiptsEnabled: Boolean = true,
    val storyReplyRestriction: String = "Everyone",

    // 5. Safety & Content Controls
    val sensitiveContentLevel: SensitiveContentLevel = SensitiveContentLevel.MEDIUM,
    val nudityProtectionEnabled: Boolean = true,
    val teenSupervisionLinked: Boolean = false,

    // 6. Accessibility & Media Utilities
    val autoCaptionsEnabled: Boolean = true,
    val hdrPlaybackEnabled: Boolean = true,
    val reduceMotionEnabled: Boolean = false,
    val highContrastEnabled: Boolean = false,
    val fontScale: Float = 1.0f,
    val dataSaverEnabled: Boolean = false,
    val highestQualityUploadEnabled: Boolean = true,
    val preferredLanguage: String = "English",
    val autoTranslationEnabled: Boolean = true
)
