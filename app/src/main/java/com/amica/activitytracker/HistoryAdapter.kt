package com.amica.activitytracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.activitytracker.data.ActivityRecord
import com.amica.activitytracker.databinding.ItemHistoryRecordBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private var records: List<ActivityRecord> = emptyList(),
    private val onDeleteClick: (ActivityRecord) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    fun updateRecords(newRecords: List<ActivityRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount(): Int = records.size

    class HistoryViewHolder(
        private val binding: ItemHistoryRecordBinding,
        private val onDeleteClick: (ActivityRecord) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: ActivityRecord) {
            val dateFormat = SimpleDateFormat("M/d/yyyy, h:mm:ss a", Locale.getDefault())
            val startTimeStr = dateFormat.format(Date(record.occurredAt))
            val endTimeMs = record.occurredAt + (record.durationSeconds.coerceAtLeast(0) * 1000L)
            val endTimeStr = dateFormat.format(Date(endTimeMs))

            binding.tvHistoryCategory.text = record.activityType
            binding.tvHistoryDuration.text = TrackedSession.formatDurationStandard(record.durationSeconds)
            binding.tvHistoryStartTime.text = "Start: $startTimeStr"
            binding.tvHistoryEndTime.text = "End:   $endTimeStr"

            binding.btnDeleteHistory.setOnClickListener {
                onDeleteClick(record)
            }
        }
    }
}