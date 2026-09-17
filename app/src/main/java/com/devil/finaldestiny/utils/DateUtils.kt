package com.devil.finaldestiny.utils

object DateUtils {
    /**
     * Resilient Supabase UTC ISO-8601 Timestamp & Relative Time Formatter.
     * Parses ISO-8601 strings with offsets/fractional seconds, numeric epoch timestamps,
     * or existing relative time tokens ("2m ago", "Just now", "Yesterday").
     */
    fun formatTimeAgo(timestampString: String?): String {
        return TimeUtils.formatTimeAgo(timestampString)
    }
}
