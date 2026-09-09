package com.devil.finaldestiny.utils

import java.util.concurrent.TimeUnit

object TimeUtils {
    /**
     * DYNAMIC REAL-TIME RELATIVE TIMING CALCULATOR
     * Computes exact human-readable relative time against system time:
     * - "Just now" (under 60 seconds)
     * - "2m ago", "15m ago"
     * - "1h ago", "5h ago"
     * - "Yesterday"
     * - "3d ago", "12d ago"
     * - "2mo ago", "1y ago"
     */
    fun getRelativeTimeString(epochMillis: Long): String {
        if (epochMillis <= 0) return "Just now"
        val now = System.currentTimeMillis()
        val diffMs = now - epochMillis

        if (diffMs < 0) return "Just now"

        val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
        val days = TimeUnit.MILLISECONDS.toDays(diffMs)

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days == 1L -> "Yesterday"
            days < 30 -> "${days}d ago"
            days < 365 -> "${days / 30}mo ago"
            else -> "${days / 365}y ago"
        }
    }

    /**
     * Formats existing timestamp string or falls back to current epoch.
     */
    fun formatTimestamp(rawTimestamp: String?, epochMs: Long = 0L): String {
        if (epochMs > 0L) {
            return getRelativeTimeString(epochMs)
        }
        val clean = rawTimestamp?.trim().orEmpty()
        if (clean.isEmpty() || clean.equals("Just now", ignoreCase = true)) return "Just now"

        // If numeric timestamp string
        val parsedEpoch = clean.toLongOrNull()
        if (parsedEpoch != null && parsedEpoch > 1000000000L) {
            return getRelativeTimeString(parsedEpoch)
        }

        // If string like "2h ago", parse relative token dynamically
        if (clean.endsWith("ago", ignoreCase = true)) {
            val token = clean.split(" ").firstOrNull().orEmpty()
            val unit = token.takeLast(1).lowercase()
            val amount = token.dropLast(1).toLongOrNull()
            if (amount != null) {
                val now = System.currentTimeMillis()
                val calculatedMs = when (unit) {
                    "s" -> now - TimeUnit.SECONDS.toMillis(amount)
                    "m" -> now - TimeUnit.MINUTES.toMillis(amount)
                    "h" -> now - TimeUnit.HOURS.toMillis(amount)
                    "d" -> now - TimeUnit.DAYS.toMillis(amount)
                    else -> now
                }
                return getRelativeTimeString(calculatedMs)
            }
        }

        return clean
    }
}
