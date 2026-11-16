package com.amica.activitytracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.activitytracker.data.ActivitySummary
import com.amica.activitytracker.databinding.ItemCategorySummaryBinding

class CategorySummaryAdapter(
    private var summaries: List<ActivitySummary> = emptyList()
) : RecyclerView.Adapter<CategorySummaryAdapter.SummaryViewHolder>() {

    fun updateSummaries(newSummaries: List<ActivitySummary>) {
        summaries = newSummaries
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val binding = ItemCategorySummaryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SummaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        holder.bind(summaries[position])
    }

    override fun getItemCount(): Int = summaries.size

    class SummaryViewHolder(
        private val binding: ItemCategorySummaryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(summary: ActivitySummary) {
            binding.tvSummaryCategory.text = summary.activityType
            val sessionLabel = if (summary.totalEntries == 1) "1 session" else "${summary.totalEntries} sessions"
            binding.tvSummarySessions.text = sessionLabel
            binding.tvSummaryTotalTime.text = "Total: ${TrackedSession.formatDurationStandard(summary.totalDurationSeconds)}"
        }
    }
}