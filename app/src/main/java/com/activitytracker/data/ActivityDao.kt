package com.activitytracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class ActivitySummary(
    val activityType: String,
    val totalDurationSeconds: Long,
    val totalEntries: Int
)

@Dao
interface ActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertActivity(activity: ActivityRecord): Long

    @Query("DELETE FROM activities WHERE id = :activityId")
    fun deleteActivity(activityId: Long): Int

    @Query("SELECT * FROM activities ORDER BY occurredAt DESC, id DESC LIMIT :limit")
    fun getRecentActivities(limit: Int = 100): Flow<List<ActivityRecord>>

    @Query(
        """
        SELECT activityType,
               SUM(durationSeconds) AS totalDurationSeconds,
               COUNT(*) AS totalEntries
        FROM activities
        GROUP BY activityType
        ORDER BY totalDurationSeconds DESC, activityType
        """
    )
    fun getActivitySummary(): Flow<List<ActivitySummary>>
}