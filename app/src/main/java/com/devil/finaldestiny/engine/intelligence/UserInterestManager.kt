package com.devil.finaldestiny.engine.intelligence

import com.devil.finaldestiny.model.EventType
import com.devil.finaldestiny.model.MomentPost
import com.devil.finaldestiny.model.TelemetryEvent
import com.devil.finaldestiny.model.UserInterestVector
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow

class UserInterestManager private constructor() {

    private val userVectorStore = ConcurrentHashMap<String, UserInterestVector>()
    private val userInteractionCounts = ConcurrentHashMap<String, Int>()

    fun getUserInterestVector(userId: String): UserInterestVector {
        val existing = userVectorStore[userId]
        if (existing != null) {
            return apply7DayHalfLifeDecay(existing)
        }
        val defaultVector = createColdStartVector(userId)
        userVectorStore[userId] = defaultVector
        return defaultVector
    }

    fun processTelemetryEvent(event: TelemetryEvent, post: MomentPost?) {
        val currentVector = getUserInterestVector(event.userId)
        val scoreDelta = calculateScoreDelta(event)

        val count = userInteractionCounts.compute(event.userId) { _, current -> (current ?: 0) + 1 } ?: 1

        if (post == null) return

        val categoryMap = currentVector.categoryAffinities.toMutableMap()
        val tagMap = currentVector.tagAffinities.toMutableMap()
        val audioMap = currentVector.audioAffinities.toMutableMap()
        val creatorMap = currentVector.creatorAffinities.toMutableMap()

        // 1. Primary Category Score Update
        val oldCatScore = categoryMap.getOrDefault(post.primaryCategory, 0.5f)
        categoryMap[post.primaryCategory] = (oldCatScore + scoreDelta * 0.1f).coerceIn(0.0f, 1.0f)

        // 2. Hashtags / Tags Score Update
        post.hashtags.forEach { tag ->
            val oldTagScore = tagMap.getOrDefault(tag, 0.5f)
            tagMap[tag] = (oldTagScore + scoreDelta * 0.08f).coerceIn(0.0f, 1.0f)
        }

        // 3. Audio Track Score Update
        post.audioTrackId?.let { audioId ->
            val oldAudioScore = audioMap.getOrDefault(audioId, 0.5f)
            audioMap[audioId] = (oldAudioScore + scoreDelta * 0.08f).coerceIn(0.0f, 1.0f)
        }

        // 4. Creator Score Update
        val oldCreatorScore = creatorMap.getOrDefault(post.authorHandle, 0.5f)
        creatorMap[post.authorHandle] = (oldCreatorScore + scoreDelta * 0.12f).coerceIn(0.0f, 1.0f)

        val updatedVector = UserInterestVector(
            userId = event.userId,
            categoryAffinities = categoryMap,
            tagAffinities = tagMap,
            audioAffinities = audioMap,
            creatorAffinities = creatorMap,
            lastUpdatedMs = System.currentTimeMillis()
        )

        userVectorStore[event.userId] = updatedVector
    }

    fun isNewUser(userId: String): Boolean {
        return (userInteractionCounts[userId] ?: 0) < 20
    }

    private fun calculateScoreDelta(event: TelemetryEvent): Float {
        return when (event.eventType) {
            EventType.SAVE_BOOKMARK -> +5.0f
            EventType.SHARE -> +4.0f
            EventType.WATCH_COMPLETE -> +3.5f
            EventType.REWATCH_LOOP -> +3.0f
            EventType.DWELL -> if (event.dwellTimeMs > 5000L) +2.0f else +0.5f
            EventType.LIKE, EventType.COMMENT -> +2.0f
            EventType.PROFILE_VISIT, EventType.STORY_VIEW, EventType.STORY_REPLY, EventType.FOLLOW -> +2.5f
            EventType.FAST_SKIP -> -3.0f
            EventType.NOT_INTERESTED -> -10.0f
            EventType.SEARCH_QUERY -> +1.5f
        }
    }

    private fun apply7DayHalfLifeDecay(vector: UserInterestVector): UserInterestVector {
        val now = System.currentTimeMillis()
        val daysElapsed = (now - vector.lastUpdatedMs) / (1000.0f * 60 * 60 * 24)

        if (daysElapsed < 1.0f) return vector

        val decayFactor = (0.5).pow(daysElapsed / 7.0).toFloat()

        val decayedCategories = vector.categoryAffinities.mapValues { (_, score) -> (score * decayFactor).coerceIn(0.05f, 1.0f) }
        val decayedTags = vector.tagAffinities.mapValues { (_, score) -> (score * decayFactor).coerceIn(0.05f, 1.0f) }
        val decayedAudio = vector.audioAffinities.mapValues { (_, score) -> (score * decayFactor).coerceIn(0.05f, 1.0f) }
        val decayedCreators = vector.creatorAffinities.mapValues { (_, score) -> (score * decayFactor).coerceIn(0.05f, 1.0f) }

        return vector.copy(
            categoryAffinities = decayedCategories,
            tagAffinities = decayedTags,
            audioAffinities = decayedAudio,
            creatorAffinities = decayedCreators,
            lastUpdatedMs = now
        )
    }

    private fun createColdStartVector(userId: String): UserInterestVector {
        val baselineCategories = mapOf(
            "Entertainment" to 0.7f,
            "Music" to 0.7f,
            "Dance" to 0.6f,
            "Lifestyle" to 0.5f,
            "Comedy" to 0.5f
        )
        return UserInterestVector(
            userId = userId,
            categoryAffinities = baselineCategories,
            lastUpdatedMs = System.currentTimeMillis()
        )
    }

    companion object {
        val instance: UserInterestManager by lazy { UserInterestManager() }
    }
}
