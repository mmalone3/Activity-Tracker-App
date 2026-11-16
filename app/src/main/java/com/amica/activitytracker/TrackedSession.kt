package com.amica.activitytracker

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class TrackedSession(
    val id: String = UUID.randomUUID().toString(),
    val category: String,
    val startTimeMs: Long,
    val endTimeMs: Long
) {
    val durationMs: Long get() = endTimeMs - startTimeMs

    fun formatFormattedString(): String {
        val dateFormat = SimpleDateFormat("M/d/yyyy, h:mm:ss a", Locale.getDefault())
        val startFormatted = dateFormat.format(Date(startTimeMs))
        val endFormatted = dateFormat.format(Date(endTimeMs))
        val durationFormatted = formatDurationStandard(durationMs / 1000)
        return "$startFormatted -> $endFormatted ($durationFormatted)"
    }

    companion object {
        fun formatDuration(durationMs: Long): String {
            return formatDurationStandard(durationMs / 1000)
        }

        fun formatDurationStandard(durationSeconds: Long): String {
            val safeSeconds = durationSeconds.coerceAtLeast(0)
            val hours = safeSeconds / 3600
            val minutes = (safeSeconds % 3600) / 60
            val seconds = safeSeconds % 60

            return when {
                hours > 0 -> String.format(Locale.US, "%dh %dm %ds", hours, minutes, seconds)
                minutes > 0 -> String.format(Locale.US, "%dm %ds", minutes, seconds)
                else -> String.format(Locale.US, "%ds", seconds)
            }
        }
    }
}