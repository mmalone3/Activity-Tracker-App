package com.amica.activitytracker

import com.activitytracker.ui.formatHistoryDuration
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ActivityTrackerUnitTest {

    private lateinit var tracker: ActivityTrackerLogic

    @Before
    fun setUp() {
        tracker = ActivityTrackerLogic()
    }

    @Test
    fun testDefaultCategoriesExist() {
        val categories = tracker.categories
        assertEquals(3, categories.size)
        assertTrue(categories.contains("Work"))
        assertTrue(categories.contains("Play"))
        assertTrue(categories.contains("Sleep"))
    }

    @Test
    fun testAddCustomCategory() {
        val added = tracker.addCategory("  Exercise  ")
        assertTrue(added)
        assertTrue(tracker.categories.contains("Exercise"))
    }

    @Test
    fun testRemoveCustomCategory() {
        tracker.addCategory("Exercise")
        assertTrue(tracker.categories.contains("Exercise"))

        val removed = tracker.removeCategory("Exercise")
        assertTrue(removed)
        assertFalse(tracker.categories.contains("Exercise"))
    }

    @Test
    fun testCannotRemoveDefaultCategory() {
        val removed = tracker.removeCategory("Work")
        assertFalse(removed)
        assertTrue(tracker.categories.contains("Work"))
    }

    @Test
    fun testAddEmptyOrBlankCategoryFails() {
        assertFalse(tracker.addCategory(""))
        assertFalse(tracker.addCategory("   "))
    }

    @Test
    fun testDuplicateCategoryNotAddedCaseInsensitive() {
        assertFalse(tracker.addCategory("work"))
        assertFalse(tracker.addCategory("WORK"))
        assertFalse(tracker.addCategory("Play"))
    }

    @Test
    fun testStartTrackingCategory() {
        val startTime = 1000000L
        val started = tracker.startTracking("Work", startTime)

        assertTrue(started)
        assertTrue(tracker.isTracking)
        assertEquals("Work", tracker.activeCategory)
        assertEquals(1000000L, tracker.sessionStartTimeMs)
    }

    @Test
    fun testSwitchingCategoryAutoStopsPreviousCategory() {
        val start1 = 1000L
        val start2 = 5000L

        tracker.startTracking("Work", start1)
        assertEquals("Work", tracker.activeCategory)

        // Switch directly to "Play"
        tracker.startTracking("Play", start2)

        // Previous Work session logged
        assertEquals(1, tracker.recentSessions.size)
        assertEquals("Work", tracker.recentSessions[0].category)
        assertEquals(4000L, tracker.recentSessions[0].durationMs)

        // New Play session active
        assertEquals("Play", tracker.activeCategory)
        assertTrue(tracker.isTracking)
    }

    @Test
    fun testElapsedTimeCalculationAndFormatting() {
        val startTime = 1000L
        tracker.startTracking("Play", startTime)

        assertEquals(0L, tracker.getElapsedTimeMs(1000L))
        assertEquals("0s", tracker.getElapsedTimeFormatted(1000L))

        assertEquals(5000L, tracker.getElapsedTimeMs(6000L))
        assertEquals("5s", tracker.getElapsedTimeFormatted(6000L))

        assertEquals("1h 1m 5s", TrackedSession.formatDuration(3665000L))
    }

    @Test
    fun testFormatHistoryDurationStandard() {
        assertEquals("0s", formatHistoryDuration(0L))
        assertEquals("15s", formatHistoryDuration(15L))
        assertEquals("1m 5s", formatHistoryDuration(65L))
        assertEquals("1h 2m 3s", formatHistoryDuration(3723L))
    }

    @Test
    fun testStopTrackingWhenNotTrackingReturnsNull() {
        val session = tracker.stopTracking(5000L)
        assertNull(session)
        assertTrue(tracker.recentSessions.isEmpty())
    }

    @Test
    fun testStopTrackingLogsSessionInDescendingOrder() {
        tracker.startTracking("Work", 1000L)
        tracker.stopTracking(3000L)

        tracker.startTracking("Sleep", 5000L)
        tracker.stopTracking(10000L)

        assertEquals(2, tracker.recentSessions.size)
        assertEquals("Sleep", tracker.recentSessions[0].category)
        assertEquals(5000L, tracker.recentSessions[0].durationMs)

        assertEquals("Work", tracker.recentSessions[1].category)
        assertEquals(2000L, tracker.recentSessions[1].durationMs)
    }

    @Test
    fun testFormattedStringOutput() {
        val session = TrackedSession(
            category = "Work",
            startTimeMs = 1700000000000L,
            endTimeMs = 1700003600000L
        )
        val formatted = session.formatFormattedString()
        assertTrue(formatted.contains("(1h 0m 0s)"))
    }

    @Test
    fun testClearSessions() {
        tracker.startTracking("Work", 1000L)
        tracker.stopTracking(2000L)
        assertEquals(1, tracker.recentSessions.size)

        tracker.clearSessions()
        assertTrue(tracker.recentSessions.isEmpty())
    }
}