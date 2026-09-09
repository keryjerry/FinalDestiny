package com.devil.finaldestiny.engine.intelligence

import com.devil.finaldestiny.model.EventType
import com.devil.finaldestiny.model.MomentPost
import com.devil.finaldestiny.model.TelemetryEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

enum class ScrollState { RAPID_SKIMMING, SLOW_ENGAGED, IDLE }

class TelemetryTracker private constructor() {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val _eventFlow = MutableSharedFlow<TelemetryEvent>(extraBufferCapacity = 500)
    val eventFlow: SharedFlow<TelemetryEvent> = _eventFlow.asSharedFlow()

    private val activePostDwellStart = ConcurrentHashMap<String, Long>()
    private val activePostWatchStart = ConcurrentHashMap<String, Long>()
    private val userSearchHistory = ConcurrentHashMap<String, MutableList<String>>()

    fun trackExplicitInteraction(
        userId: String,
        postId: String,
        eventType: EventType
    ) {
        val event = TelemetryEvent(
            userId = userId,
            postId = postId,
            eventType = eventType
        )
        emitTelemetryEvent(event)
    }

    fun onPostEnterViewport(userId: String, postId: String) {
        activePostDwellStart[postId] = System.currentTimeMillis()
        activePostWatchStart[postId] = System.currentTimeMillis()
    }

    fun onPostExitViewport(
        userId: String,
        post: MomentPost,
        totalDurationMs: Long = 30000L,
        scrollVelocityPxPerSec: Float = 0.0f
    ) {
        val enterTime = activePostDwellStart.remove(post.id) ?: return
        val dwellTimeMs = System.currentTimeMillis() - enterTime

        val watchStart = activePostWatchStart.remove(post.id) ?: enterTime
        val watchTimeMs = System.currentTimeMillis() - watchStart
        val completionRate = (watchTimeMs.toFloat() / totalDurationMs.coerceAtLeast(1000L)).coerceIn(0.0f, 1.0f)
        val dropOffTimestampSec = watchTimeMs / 1000.0f

        val eventType = when {
            dwellTimeMs < 2000L || scrollVelocityPxPerSec > 2500.0f -> EventType.FAST_SKIP
            completionRate >= 0.98f -> EventType.WATCH_COMPLETE
            dwellTimeMs > 5000L -> EventType.DWELL
            else -> EventType.DWELL
        }

        val event = TelemetryEvent(
            userId = userId,
            postId = post.id,
            eventType = eventType,
            dwellTimeMs = dwellTimeMs,
            scrollSpeed = scrollVelocityPxPerSec,
            completionRate = completionRate,
            dropOffTimestamp = dropOffTimestampSec
        )
        emitTelemetryEvent(event)
    }

    fun trackSearchQuery(userId: String, query: String) {
        if (query.isBlank()) return
        val history = userSearchHistory.getOrPut(userId) { mutableListOf() }
        synchronized(history) {
            history.add(query)
            if (history.size > 50) history.removeAt(0)
        }

        val event = TelemetryEvent(
            userId = userId,
            postId = "search_$query",
            eventType = EventType.SEARCH_QUERY
        )
        emitTelemetryEvent(event)
    }

    fun classifyScrollState(velocityPxPerSec: Float): ScrollState {
        return when {
            velocityPxPerSec > 2000.0f -> ScrollState.RAPID_SKIMMING
            velocityPxPerSec > 50.0f -> ScrollState.SLOW_ENGAGED
            else -> ScrollState.IDLE
        }
    }

    private fun emitTelemetryEvent(event: TelemetryEvent) {
        scope.launch {
            _eventFlow.emit(event)
        }
    }

    companion object {
        val instance: TelemetryTracker by lazy { TelemetryTracker() }
    }
}
