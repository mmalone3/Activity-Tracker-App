package com.activitytracker.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "activities", indices = [Index("occurredAt")])
data class ActivityRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityType: String,
    val durationSeconds: Long,
    val distanceKm: Double? = null,
    val steps: Int? = null,
    val notes: String? = null,
    val occurredAt: Long = System.currentTimeMillis(),
    val detail: String? = null,
    val target: String? = null,
    val mindset: String? = null,
    val tags: String? = null
)