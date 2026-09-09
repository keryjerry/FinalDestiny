package com.devil.finaldestiny.engine.intelligence

import com.devil.finaldestiny.model.MomentPost
import com.devil.finaldestiny.model.StoryItem
import com.devil.finaldestiny.model.TelemetryEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class CacheSyncWorker private constructor() {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val cachedHomeFeeds = ConcurrentHashMap<String, List<MomentPost>>()
    private val cachedReels = ConcurrentHashMap<String, List<MomentPost>>()
    private val cachedStories = ConcurrentHashMap<String, List<StoryItem>>()

    fun getCachedHomeFeed(userId: String): List<MomentPost>? {
        return cachedHomeFeeds[userId]
    }

    fun updateCachedHomeFeed(userId: String, posts: List<MomentPost>) {
        cachedHomeFeeds[userId] = posts
    }

    fun getCachedReels(userId: String): List<MomentPost>? {
        return cachedReels[userId]
    }

    fun updateCachedReels(userId: String, reels: List<MomentPost>) {
        cachedReels[userId] = reels
    }

    fun getCachedStories(userId: String): List<StoryItem>? {
        return cachedStories[userId]
    }

    fun updateCachedStories(userId: String, stories: List<StoryItem>) {
        cachedStories[userId] = stories
    }

    fun syncTelemetryToSupabaseAsync(event: TelemetryEvent) {
        scope.launch {
            try {
                // In production, posts telemetry payloads to Supabase /user_event_telemetry table via HTTP REST / RPC
                // Non-blocking background execution guarantees zero UI latency.
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun invalidateUserCache(userId: String) {
        cachedHomeFeeds.remove(userId)
        cachedReels.remove(userId)
        cachedStories.remove(userId)
    }

    companion object {
        val instance: CacheSyncWorker by lazy { CacheSyncWorker() }
    }
}
