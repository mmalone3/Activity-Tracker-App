package com.amica.activitytracker

import com.activitytracker.data.ActivityRecord
import com.activitytracker.data.ActivitySummary
import com.activitytracker.ui.formatHistoryDuration
import org.junit.Assert.*
import org.junit.Test

class HistoryPageUnitTest {

    @Test
    fun testDurationFormattingZeroSeconds() {
        val result = formatHistoryDuration(0L)
        assertEquals("0s", result)
    }

    @Test
    fun testDurationFormattingSecondsOnly() {
        val result = formatHistoryDuration(45L)
        assertEquals("45s", result)
    }

    @Test
    fun testDurationFormattingMinutesAndSeconds() {
        // 2 minutes, 5 seconds = 125 seconds
        val result = formatHistoryDuration(125L)
        assertEquals("2m 5s", result)
    }

    @Test
    fun testDurationFormattingHoursMinutesSeconds() {
        // 1 hour, 1 minute, 5 seconds = 3665 seconds
        val result = formatHistoryDuration(3665L)
        assertEquals("1h 1m 5s", result)
    }

    @Test
    fun testDurationFormattingNegativeSecondsHandledSafely() {
        val result = formatHistoryDuration(-100L)
        assertEquals("0s", result)
    }

    @Test
    fun testActivityRecordEntityCreation() {
        val record = ActivityRecord(
            id = 1L,
            activityType = "Work",
            durationSeconds = 3600L,
            occurredAt = 1700000000000L,
            detail = "Coding session",
            target = "Feature implementation",
            mindset = "Focused",
            tags = "Kotlin,Android"
        )

        assertEquals(1L, record.id)
        assertEquals("Work", record.activityType)
        assertEquals(3600L, record.durationSeconds)
        assertEquals(1700000000000L, record.occurredAt)
        assertEquals("Coding session", record.detail)
        assertEquals("Feature implementation", record.target)
        assertEquals("Focused", record.mindset)
        assertEquals("Kotlin,Android", record.tags)
    }

    @Test
    fun testActivitySummaryDataHolder() {
        val summary = ActivitySummary(
            activityType = "Play",
            totalDurationSeconds = 7200L,
            totalEntries = 4
        )

        assertEquals("Play", summary.activityType)
        assertEquals(7200L, summary.totalDurationSeconds)
        assertEquals(4, summary.totalEntries)
    }

    @Test
    fun testTrackedSessionFormatDurationStandardHelper() {
        val formatted = TrackedSession.formatDurationStandard(3723L)
        assertEquals("1h 2m 3s", formatted)
    }
}