package com.amica.activitytracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amica.activitytracker.databinding.ItemSessionBinding

class SessionAdapter(
    private var sessions: List<TrackedSession> = emptyList()
) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    fun updateSessions(newSessions: List<TrackedSession>) {
        sessions = newSessions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount(): Int = sessions.size

    class SessionViewHolder(
        private val binding: ItemSessionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(session: TrackedSession) {
            binding.tvSessionCategory.text = session.category
            binding.tvSessionTime.text = session.formatFormattedString()
        }
    }
}