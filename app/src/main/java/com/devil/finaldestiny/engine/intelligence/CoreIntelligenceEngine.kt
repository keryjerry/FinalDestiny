package com.devil.finaldestiny.engine.intelligence

import com.devil.finaldestiny.model.EventType
import com.devil.finaldestiny.model.MomentPost
import com.devil.finaldestiny.model.RecommendedUser
import com.devil.finaldestiny.model.StoryItem
import com.devil.finaldestiny.model.TelemetryEvent
import com.devil.finaldestiny.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CoreIntelligenceEngine private constructor() {

    private val scope = CoroutineScope(Dispatchers.Default)

    val telemetryTracker = TelemetryTracker.instance
    val interestManager = UserInterestManager.instance
    val rankingEngine = FeedRankingEngine.instance
    val socialGraphEngine = SocialGraphEngine.instance
    val safetyEngine = SafetyAntiAbuseEngine.instance
    val cacheSyncWorker = CacheSyncWorker.instance

    init {
        // Observe telemetry event stream asynchronously for real-time vector updates (<500ms)
        scope.launch {
            telemetryTracker.eventFlow.collectLatest { event ->
                if (!safetyEngine.isSpamBotVelocity(event)) {
                    // Update user interest profile asynchronously
                    interestManager.processTelemetryEvent(event, null)
                    cacheSyncWorker.syncTelemetryToSupabaseAsync(event)
                }
            }
        }
    }

    fun getRankedHomeFeed(userId: String, rawPosts: List<MomentPost>): List<MomentPost> {
        val cached = cacheSyncWorker.getCachedHomeFeed(userId)
        if (cached != null && cached.isNotEmpty()) {
            return cached
        }

        val safePosts = safetyEngine.filterSafePosts(rawPosts)
        val userVector = interestManager.getUserInterestVector(userId)
        val isNewUser = interestManager.isNewUser(userId)

        val ranked = rankingEngine.rankHomeFeed(userVector, safePosts, isNewUser)
        cacheSyncWorker.updateCachedHomeFeed(userId, ranked)
        return ranked
    }

    fun getRankedReelsFeed(userId: String, rawReels: List<MomentPost>): List<MomentPost> {
        val cached = cacheSyncWorker.getCachedReels(userId)
        if (cached != null && cached.isNotEmpty()) {
            return cached
        }

        val safeReels = safetyEngine.filterSafePosts(rawReels)
        val userVector = interestManager.getUserInterestVector(userId)

        val ranked = rankingEngine.rankReelsFeed(userVector, safeReels)
        cacheSyncWorker.updateCachedReels(userId, ranked)
        return ranked
    }

    fun getRankedStories(userId: String, rawStories: List<StoryItem>): List<StoryItem> {
        val cached = cacheSyncWorker.getCachedStories(userId)
        if (cached != null && cached.isNotEmpty()) {
            return cached
        }

        val userVector = interestManager.getUserInterestVector(userId)
        val ranked = rankingEngine.rankStories(userVector, rawStories)
        cacheSyncWorker.updateCachedStories(userId, ranked)
        return ranked
    }

    fun getPeopleYouMayKnow(currentUser: UserProfile, candidates: List<UserProfile>): List<RecommendedUser> {
        val userVector = interestManager.getUserInterestVector(currentUser.id)
        return socialGraphEngine.getPeopleYouMayKnow(currentUser, userVector, candidates)
    }

    fun recordInteraction(userId: String, post: MomentPost, eventType: EventType) {
        if (eventType == EventType.NOT_INTERESTED) {
            cacheSyncWorker.invalidateUserCache(userId)
        }
        val event = TelemetryEvent(userId = userId, postId = post.id, eventType = eventType)
        telemetryTracker.trackExplicitInteraction(userId, post.id, eventType)
        interestManager.processTelemetryEvent(event, post)
    }

    companion object {
        val instance: CoreIntelligenceEngine by lazy { CoreIntelligenceEngine() }
    }
}
