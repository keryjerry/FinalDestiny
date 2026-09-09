package com.devil.finaldestiny.engine.intelligence

import com.devil.finaldestiny.model.MomentPost
import com.devil.finaldestiny.model.StoryItem
import com.devil.finaldestiny.model.UserInterestVector
import kotlin.math.ln

class FeedRankingEngine private constructor() {

    // Weight parameters for Home Feed Scoring Formula
    private val w1Interest = 3.5f
    private val w2Like = 1.5f
    private val w3Comment = 1.5f
    private val w4Share = 2.0f
    private val w5WatchComplete = 2.5f
    private val w6CreatorAffinity = 2.0f
    private val w7Freshness = 1.8f

    fun rankHomeFeed(
        userVector: UserInterestVector,
        posts: List<MomentPost>,
        isNewUser: Boolean = false
    ): List<MomentPost> {
        val scoredPosts = posts.map { post ->
            val score = computeHomeFeedScore(userVector, post, isNewUser)
            Pair(post, score)
        }.sortedByDescending { it.second }

        val rankedList = scoredPosts.map { it.first }
        return enforceDiversityConstraints(rankedList)
    }

    fun rankReelsFeed(
        userVector: UserInterestVector,
        reels: List<MomentPost>
    ): List<MomentPost> {
        val scoredReels = reels.map { reel ->
            val affinityScore = computeReelAffinityScore(userVector, reel)
            val viralScore = reel.viralVelocity * 2.0f
            val serendipityScore = (reel.id.hashCode() % 100) / 100.0f

            // 75% Hyper-personalized Affinity + 15% Viral/Trending + 10% Serendipity/Exploration
            val finalReelScore = (0.75f * affinityScore) + (0.15f * viralScore) + (0.10f * serendipityScore)
            Pair(reel, finalReelScore)
        }.sortedByDescending { it.second }

        return enforceDiversityConstraints(scoredReels.map { it.first })
    }

    fun rankStories(
        userVector: UserInterestVector,
        stories: List<StoryItem>,
        nowMs: Long = System.currentTimeMillis()
    ): List<StoryItem> {
        val active24hStories = stories.filter { story ->
            (nowMs - story.createdAtEpochMs) <= 24 * 60 * 60 * 1000L
        }

        return active24hStories.map { story ->
            val intimacyScore = userVector.creatorAffinities.getOrDefault(story.authorId, 0.5f)
            val freshnessScore = 1.0f - ((nowMs - story.createdAtEpochMs).toFloat() / (24 * 60 * 60 * 1000L))
            val finalStoryScore = (0.7f * intimacyScore) + (0.3f * freshnessScore)
            Pair(story, finalStoryScore)
        }.sortedByDescending { it.second }.map { it.first }
    }

    fun rerankSearchResults(
        userVector: UserInterestVector,
        matchedPosts: List<MomentPost>,
        query: String
    ): List<MomentPost> {
        val lowerQuery = query.lowercase().trim()

        return matchedPosts.map { post ->
            val lexicalMatch = if (post.caption.lowercase().contains(lowerQuery) ||
                post.hashtags.any { it.lowercase().contains(lowerQuery) } ||
                post.keywords.any { it.lowercase().contains(lowerQuery) }
            ) 3.0f else 1.0f

            val categoryAffinity = userVector.categoryAffinities.getOrDefault(post.primaryCategory, 0.5f)
            val creatorAffinity = userVector.creatorAffinities.getOrDefault(post.authorHandle, 0.5f)
            val authorityScore = (post.likesCount * 0.001f) + (post.sharesCount * 0.005f)

            val rerankScore = (lexicalMatch * 4.0f) + (categoryAffinity * 2.0f) + (creatorAffinity * 1.5f) + (authorityScore * 1.0f)
            Pair(post, rerankScore)
        }.sortedByDescending { it.second }.map { it.first }
    }

    private fun computeHomeFeedScore(
        userVector: UserInterestVector,
        post: MomentPost,
        isNewUser: Boolean
    ): Float {
        val interestMatch = userVector.categoryAffinities.getOrDefault(post.primaryCategory, 0.5f)
        val pLike = (post.likesCount.toFloat() / (post.viewsCount.coerceAtLeast(1))).coerceIn(0.0f, 1.0f)
        val pComment = (post.commentsCount.toFloat() / (post.viewsCount.coerceAtLeast(1))).coerceIn(0.0f, 1.0f)
        val pShare = (post.sharesCount.toFloat() / (post.viewsCount.coerceAtLeast(1))).coerceIn(0.0f, 1.0f)
        val pWatchComplete = post.qualityScore.coerceIn(0.0f, 1.0f)
        val creatorAffinity = userVector.creatorAffinities.getOrDefault(post.authorHandle, 0.5f)

        val hoursSinceCreation = ((System.currentTimeMillis() - post.createdAtEpochMs) / (1000.0f * 3600)).coerceAtLeast(0.0f)
        val freshnessScore = (1.0f / ln(2.0f + hoursSinceCreation)).toFloat()

        var rawScore = (w1Interest * interestMatch) +
                (w2Like * pLike) +
                (w3Comment * pComment) +
                (w4Share * pShare) +
                (w5WatchComplete * pWatchComplete) +
                (w6CreatorAffinity * creatorAffinity) +
                (w7Freshness * freshnessScore)

        if (isNewUser) {
            // Cold start: 70% trending velocity + 30% exploration
            val viralVelocityScore = post.viralVelocity
            rawScore = (0.7f * viralVelocityScore) + (0.3f * rawScore)
        }

        return rawScore
    }

    private fun computeReelAffinityScore(
        userVector: UserInterestVector,
        reel: MomentPost
    ): Float {
        val catAffinity = userVector.categoryAffinities.getOrDefault(reel.primaryCategory, 0.5f)
        val audioAffinity = reel.audioTrackId?.let { userVector.audioAffinities[it] } ?: 0.5f
        val tagAffinity = reel.hashtags.map { userVector.tagAffinities.getOrDefault(it, 0.5f) }.maxOrNull() ?: 0.5f
        val creatorAffinity = userVector.creatorAffinities.getOrDefault(reel.authorHandle, 0.5f)

        return (catAffinity * 0.35f) + (audioAffinity * 0.25f) + (tagAffinity * 0.20f) + (creatorAffinity * 0.20f)
    }

    private fun enforceDiversityConstraints(posts: List<MomentPost>): List<MomentPost> {
        if (posts.size <= 2) return posts

        val result = mutableListOf<MomentPost>()
        val remaining = posts.toMutableList()

        while (remaining.isNotEmpty()) {
            var selectedIndex = -1

            for (i in remaining.indices) {
                val candidate = remaining[i]
                if (result.size >= 2) {
                    val last1 = result[result.size - 1]
                    val last2 = result[result.size - 2]

                    val sameCreatorConstraint = candidate.authorHandle == last1.authorHandle && candidate.authorHandle == last2.authorHandle
                    val sameCategoryConstraint = candidate.primaryCategory == last1.primaryCategory && candidate.primaryCategory == last2.primaryCategory

                    if (!sameCreatorConstraint && !sameCategoryConstraint) {
                        selectedIndex = i
                        break
                    }
                } else {
                    selectedIndex = 0
                    break
                }
            }

            if (selectedIndex == -1) selectedIndex = 0

            result.add(remaining.removeAt(selectedIndex))
        }

        return result
    }

    companion object {
        val instance: FeedRankingEngine by lazy { FeedRankingEngine() }
    }
}
