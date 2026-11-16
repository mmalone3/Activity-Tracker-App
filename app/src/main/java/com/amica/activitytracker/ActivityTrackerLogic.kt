package com.amica.activitytracker

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

class ActivityTrackerLogic {

    val defaultCategories = listOf("Work", "Play", "Sleep")
    private val _categories = mutableListOf<String>().apply { addAll(defaultCategories) }
    val categories: List<String> get() = _categories

    private val _recentSessions = mutableListOf<TrackedSession>()
    val recentSessions: List<TrackedSession> get() = _recentSessions

    var activeCategory: String? = null
        private set

    var sessionStartTimeMs: Long = 0L
        private set

    val isTracking: Boolean get() = activeCategory != null

    fun addCategory(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return false
        if (_categories.any { it.equals(trimmed, ignoreCase = true) }) return false
        _categories.add(trimmed)
        return true
    }

    fun removeCategory(name: String): Boolean {
        val trimmed = name.trim()
        if (defaultCategories.any { it.equals(trimmed, ignoreCase = true) }) return false
        val index = _categories.indexOfFirst { it.equals(trimmed, ignoreCase = true) }
        if (index != -1) {
            val removedCat = _categories.removeAt(index)
            if (activeCategory == removedCat) {
                activeCategory = null
            }
            return true
        }
        return false
    }

    fun startTracking(category: String, nowMs: Long = System.currentTimeMillis()): Boolean {
        if (isTracking) {
            stopTracking(nowMs)
        }
        activeCategory = category
        sessionStartTimeMs = nowMs
        return true
    }

    fun stopTracking(nowMs: Long = System.currentTimeMillis()): TrackedSession? {
        val category = activeCategory ?: return null
        val startTime = sessionStartTimeMs
        val session = TrackedSession(
            category = category,
            startTimeMs = startTime,
            endTimeMs = nowMs
        )
        _recentSessions.add(0, session)
        activeCategory = null
        sessionStartTimeMs = 0L
        return session
    }

    fun clearSessions() {
        _recentSessions.clear()
    }

    fun getElapsedTimeMs(nowMs: Long = System.currentTimeMillis()): Long {
        if (!isTracking) return 0L
        return (nowMs - sessionStartTimeMs).coerceAtLeast(0L)
    }

    fun getElapsedTimeFormatted(nowMs: Long = System.currentTimeMillis()): String {
        return TrackedSession.formatDuration(getElapsedTimeMs(nowMs))
    }

    fun saveState(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Save categories
        val categoriesArray = JSONArray(_categories)

        // Save sessions
        val sessionsArray = JSONArray()
        for (session in _recentSessions) {
            val obj = JSONObject().apply {
                put("id", session.id)
                put("category", session.category)
                put("startTimeMs", session.startTimeMs)
                put("endTimeMs", session.endTimeMs)
            }
            sessionsArray.put(obj)
        }

        prefs.edit {
            putString(KEY_CATEGORIES, categoriesArray.toString())
            putString(KEY_SESSIONS, sessionsArray.toString())
        }
    }

    fun loadState(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Load categories
        val categoriesJson = prefs.getString(KEY_CATEGORIES, null)
        if (!categoriesJson.isNullOrEmpty()) {
            try {
                val array = JSONArray(categoriesJson)
                _categories.clear()
                for (i in 0 until array.length()) {
                    _categories.add(array.getString(i))
                }
            } catch (_: Exception) {}
        }

        // Ensure default categories are present
        for (cat in defaultCategories) {
            if (!_categories.contains(cat)) {
                _categories.add(cat)
            }
        }

        // Load sessions
        val sessionsJson = prefs.getString(KEY_SESSIONS, null)
        if (!sessionsJson.isNullOrEmpty()) {
            try {
                val array = JSONArray(sessionsJson)
                _recentSessions.clear()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val session = TrackedSession(
                        id = obj.optString("id"),
                        category = obj.getString("category"),
                        startTimeMs = obj.getLong("startTimeMs"),
                        endTimeMs = obj.getLong("endTimeMs")
                    )
                    _recentSessions.add(session)
                }
            } catch (_: Exception) {}
        }
    }

    companion object {
        private const val PREFS_NAME = "activity_tracker_prefs"
        private const val KEY_CATEGORIES = "categories"
        private const val KEY_SESSIONS = "sessions"
    }
}