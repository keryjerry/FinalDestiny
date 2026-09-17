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
            days < 7 -> "${days}d ago"
            else -> {
                val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.US)
                sdf.format(java.util.Date(epochMillis))
            }
        }
    }

    private fun parseIsoToEpochMs(raw: String): Long? {
        if (raw.isBlank()) return null
        return try {
            java.time.Instant.parse(raw).toEpochMilli()
        } catch (e: Exception) {
            try {
                java.time.OffsetDateTime.parse(raw).toInstant().toEpochMilli()
            } catch (e2: Exception) {
                try {
                    java.time.ZonedDateTime.parse(raw).toInstant().toEpochMilli()
                } catch (e3: Exception) {
                    try {
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                        val clean = raw.split(".").firstOrNull() ?: raw
                        sdf.parse(clean)?.time
                    } catch (e4: Exception) {
                        null
                    }
                }
            }
        }
    }

    /**
     * Resilient Supabase UTC ISO-8601 & Relative Timestamp Formatter.
     */
    fun formatTimeAgo(timestampString: String?): String {
        if (timestampString.isNullOrBlank()) return "Just now"
        val trimmed = timestampString.trim()

        if (trimmed.equals("Just now", ignoreCase = true) ||
            trimmed.endsWith("ago", ignoreCase = true) ||
            trimmed.equals("Yesterday", ignoreCase = true) ||
            trimmed.startsWith("Scheduled:", ignoreCase = true)) {
            return trimmed
        }

        val numericEpoch = trimmed.toLongOrNull()
        val postMillis: Long = if (numericEpoch != null && numericEpoch > 1000000000L) {
            numericEpoch
        } else {
            try {
                // Support API 26+ Instant
                try {
                    java.time.Instant.parse(trimmed).toEpochMilli()
                } catch (eInstant: Exception) {
                    java.time.OffsetDateTime.parse(trimmed).toInstant().toEpochMilli()
                }
            } catch (e: Exception) {
                try {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }
                    val cleanStr = trimmed.replace(" ", "T")
                        .substringBefore(".")
                        .substringBefore("+")
                        .substringBefore("Z")
                    sdf.parse(cleanStr)?.time ?: System.currentTimeMillis()
                } catch (e2: Exception) {
                    System.currentTimeMillis()
                }
            }
        }

        val now = System.currentTimeMillis()
        val diff = now - postMillis

        return when {
            diff < 60_000L -> "Just now"
            diff < 3_600_000L -> "${diff / 60_000L}m ago"
            diff < 86_400_000L -> "${diff / 3_600_000L}h ago"
            diff < 172_800_000L -> "Yesterday"
            diff < 604_800_000L -> "${diff / 86_400_000L}d ago"
            else -> {
                val outSdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
                outSdf.format(java.util.Date(postMillis))
            }
        }
    }

    /**
     * Formats existing timestamp string or falls back to current epoch.
     */
    fun formatTimestamp(rawTimestamp: String?, epochMs: Long = 0L): String {
        if (epochMs > 0L) {
            return getRelativeTimeString(epochMs)
        }
        return formatTimeAgo(rawTimestamp)
    }

    fun formatIsoTimestamp(epochMs: Long = System.currentTimeMillis()): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(java.util.Date(epochMs))
    }
}
