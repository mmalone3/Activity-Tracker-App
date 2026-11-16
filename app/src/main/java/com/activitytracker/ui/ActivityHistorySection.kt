package com.activitytracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.activitytracker.data.ActivityRecord
import com.activitytracker.data.ActivitySummary
import com.amica.activitytracker.TrackedSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val historyBorder = Color(0xFFD1E2DD)
private val historyHeaderBackground = Color(0xFFF2F8F6)
private val historyHeadingColor = Color(0xFF315C56)
private val historyTextColor = Color(0xFF263D39)

private data class HistoryColumn(
    val title: String,
    val width: Dp
)

private val historyColumns = listOf(
    HistoryColumn("Category", 110.dp),
    HistoryColumn("Duration", 120.dp),
    HistoryColumn("Start", 140.dp),
    HistoryColumn("End", 140.dp),
    HistoryColumn("Action", 80.dp)
)

@Composable
fun ActivityHistorySection(
    activities: List<ActivityRecord>,
    summaries: List<ActivitySummary> = emptyList(),
    onDeleteActivity: (ActivityRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, historyBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "History Tracker",
            color = historyHeadingColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (summaries.isNotEmpty()) {
            Text(
                text = "Category Summaries",
                color = historyHeadingColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            summaries.forEach { summary ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF2F8F6))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = summary.activityType,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F4C81),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${summary.totalEntries} sessions",
                        fontSize = 12.sp,
                        color = Color(0xFF5F6D7A),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = formatHistoryDuration(summary.totalDurationSeconds),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "Detailed Logs",
            color = historyHeadingColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, historyBorder, RoundedCornerShape(8.dp))
                .horizontalScroll(rememberScrollState())
        ) {
            Row(modifier = Modifier.background(historyHeaderBackground)) {
                historyColumns.forEach { column ->
                    Text(
                        text = column.title,
                        modifier = Modifier
                            .width(column.width)
                            .padding(horizontal = 8.dp, vertical = 9.dp),
                        color = historyHeadingColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            if (activities.isEmpty()) {
                HistoryRow(
                    values = listOf("No history yet.", "", "", "", "")
                )
            } else {
                activities.forEach { activity ->
                    val endTime = activity.occurredAt +
                        activity.durationSeconds.coerceAtLeast(0) * 1_000L
                    HistoryRow(
                        values = listOf(
                            activity.activityType,
                            formatHistoryDuration(activity.durationSeconds),
                            formatHistoryDateTime(activity.occurredAt),
                            formatHistoryDateTime(endTime),
                            ""
                        ),
                        onDelete = { onDeleteActivity(activity) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(
    values: List<String>,
    onDelete: (() -> Unit)? = null
) {
    Row {
        historyColumns.zip(values).forEachIndexed { index, (column, value) ->
            if (index == historyColumns.lastIndex && onDelete != null) {
                TextButton(onClick = onDelete) {
                    Text(
                        text = "Delete",
                        color = Color(0xFFB3261E),
                        fontSize = 12.sp
                    )
                }
            } else {
                Text(
                    text = value,
                    modifier = Modifier
                        .width(column.width)
                        .padding(horizontal = 8.dp, vertical = 9.dp),
                    color = historyTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun formatHistoryDateTime(timestamp: Long): String =
    SimpleDateFormat("M/d/yyyy,\nh:mm:ss a", Locale.getDefault()).format(Date(timestamp))

fun formatHistoryDuration(durationSeconds: Long): String {
    return TrackedSession.formatDurationStandard(durationSeconds)
}