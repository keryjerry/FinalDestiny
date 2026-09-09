package com.devil.finaldestiny.engine.intelligence

import com.devil.finaldestiny.model.AppNotification
import com.devil.finaldestiny.model.RecommendedUser
import com.devil.finaldestiny.model.UserProfile
import com.devil.finaldestiny.model.UserInterestVector
import java.util.concurrent.ConcurrentHashMap

enum class NotificationDeliveryPriority { IMMEDIATE_PUSH, BATCHED_DIGEST, SUPPRESSED }

class SocialGraphEngine private constructor() {

    private val userLastActiveMap = ConcurrentHashMap<String, Long>()
    private val userSessionStartMap = ConcurrentHashMap<String, Long>()

    fun recordSessionStart(userId: String) {
        val now = System.currentTimeMillis()
        userSessionStartMap[userId] = now
        userLastActiveMap[userId] = now
    }

    fun recordSessionEnd(userId: String) {
        val now = System.currentTimeMillis()
        userLastActiveMap[userId] = now
        userSessionStartMap.remove(userId)
    }

    fun getPeopleYouMayKnow(
        currentUser: UserProfile,
        userVector: UserInterestVector,
        candidateProfiles: List<UserProfile>
    ): List<RecommendedUser> {
        return candidateProfiles.filter { candidate -> candidate.id != currentUser.id }
            .map { candidate ->
                val sharedInterests = candidate.lifestyleTags.filter { tag ->
                    currentUser.lifestyleTags.contains(tag) || userVector.tagAffinities.containsKey(tag)
                }

                val mutualFollowers = ((candidate.followerCount + currentUser.followerCount) % 7) + 1
                val intimacyScore = userVector.creatorAffinities.getOrDefault(candidate.handle, 0.4f)

                val matchScore = (sharedInterests.size * 2.0f) + (mutualFollowers * 1.5f) + (intimacyScore * 3.0f)

                val reason = when {
                    mutualFollowers > 3 && sharedInterests.isNotEmpty() -> "$mutualFollowers mutual friends & shared love for ${sharedInterests.first()}"
                    sharedInterests.isNotEmpty() -> "Shared interest in ${sharedInterests.take(2).joinToString(", ")}"
                    else -> "Popular creator in your region"
                }

                Pair(
                    RecommendedUser(
                        userProfile = candidate,
                        mutualFollowersCount = mutualFollowers,
                        sharedInterests = sharedInterests,
                        intimacyScore = intimacyScore,
                        matchReason = reason
                    ),
                    matchScore
                )
            }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    fun classifyNotificationPriority(
        notification: AppNotification,
        userVector: UserInterestVector
    ): NotificationDeliveryPriority {
        val isDirectMessage = notification.title.contains("DM", ignoreCase = true) ||
                notification.message.contains("sent you a message", ignoreCase = true)
        val isVIP = notification.title.contains("VIP", ignoreCase = true)

        if (isDirectMessage || isVIP) {
            return NotificationDeliveryPriority.IMMEDIATE_PUSH
        }

        val creatorAffinity = userVector.creatorAffinities.entries.find { (creator, _) ->
            notification.message.contains(creator, ignoreCase = true)
        }?.value ?: 0.3f

        return when {
            creatorAffinity > 0.7f -> NotificationDeliveryPriority.IMMEDIATE_PUSH
            creatorAffinity >= 0.3f -> NotificationDeliveryPriority.BATCHED_DIGEST
            else -> NotificationDeliveryPriority.SUPPRESSED
        }
    }

    fun checkDormantReEngagementTrigger(userId: String): Boolean {
        val lastActive = userLastActiveMap[userId] ?: return false
        val hoursDormant = (System.currentTimeMillis() - lastActive) / (1000.0f * 3600)
        return hoursDormant >= 48.0f
    }

    companion object {
        val instance: SocialGraphEngine by lazy { SocialGraphEngine() }
    }
}
