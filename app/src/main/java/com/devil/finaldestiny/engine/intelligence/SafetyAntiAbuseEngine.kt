package com.devil.finaldestiny.engine.intelligence

import com.devil.finaldestiny.model.EventType
import com.devil.finaldestiny.model.MomentPost
import com.devil.finaldestiny.model.TelemetryEvent
import java.util.concurrent.ConcurrentHashMap

enum class ModerationStatus { APPROVED, FLAGGED_FOR_REVIEW, REJECTED_PROFANITY, DUPLICATE_SPAM }

class SafetyAntiAbuseEngine private constructor() {

    private val profanityBlacklist = listOf(
        "hate", "abusive", "scam", "phishing", "vulgar_word_1", "vulgar_word_2"
    )

    private val userInteractionTimestamps = ConcurrentHashMap<String, MutableList<Long>>()
    private val knownMediaHashes = ConcurrentHashMap<String, String>()

    fun moderateContent(text: String, mediaUri: String? = null): ModerationStatus {
        val lowerText = text.lowercase()
        if (profanityBlacklist.any { lowerText.contains(it) }) {
            return ModerationStatus.REJECTED_PROFANITY
        }

        mediaUri?.let { uri ->
            val hash = generatePerceptualHash(uri)
            if (knownMediaHashes.containsKey(hash)) {
                return ModerationStatus.DUPLICATE_SPAM
            }
            knownMediaHashes[hash] = uri
        }

        return ModerationStatus.APPROVED
    }

    fun isSpamBotVelocity(event: TelemetryEvent): Boolean {
        if (event.eventType != EventType.LIKE && event.eventType != EventType.COMMENT && event.eventType != EventType.FOLLOW) {
            return false
        }

        val timestamps = userInteractionTimestamps.getOrPut(event.userId) { mutableListOf() }
        val now = System.currentTimeMillis()

        synchronized(timestamps) {
            timestamps.add(now)
            // Remove interactions older than 1 minute (60,000 ms)
            timestamps.removeAll { now - it > 60000L }

            // Rate-limit threshold: > 30 interactions per minute
            return timestamps.size > 30
        }
    }

    fun filterSafePosts(posts: List<MomentPost>): List<MomentPost> {
        return posts.filter { post ->
            moderateContent(post.caption, post.mediaUrl) == ModerationStatus.APPROVED
        }
    }

    private fun generatePerceptualHash(input: String): String {
        val hashCode = input.hashCode().toLong()
        return java.lang.Long.toHexString(hashCode)
    }

    companion object {
        val instance: SafetyAntiAbuseEngine by lazy { SafetyAntiAbuseEngine() }
    }
}
