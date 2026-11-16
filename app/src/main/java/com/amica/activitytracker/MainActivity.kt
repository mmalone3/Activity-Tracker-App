package com.amica.activitytracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.activitytracker.data.ActivityDatabase
import com.activitytracker.data.ActivityRecord
import com.activitytracker.ui.ActivityHistorySection
import com.amica.activitytracker.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val trackerLogic = ActivityTrackerLogic()
    private val sessionAdapter = SessionAdapter()

    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (trackerLogic.isTracking) {
                binding.tvTimerDisplay.text = trackerLogic.getElapsedTimeFormatted()
                timerHandler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        trackerLogic.loadState(this)

        setupRecyclerView()
        setupCategoryChips()
        setupClickListeners()
        setupComposeHistory()

        updateUi()
    }

    private fun setupComposeHistory() {
        try {
            val database = ActivityDatabase.getInstance(applicationContext)
            val dao = database.activityDao()

            binding.composeHistorySection.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycle))
                setContent {
                    MaterialTheme {
                        val activitiesFlow = remember { dao.getRecentActivities() }
                        val summariesFlow = remember { dao.getActivitySummary() }

                        val activities by activitiesFlow.collectAsState(initial = emptyList(), context = Dispatchers.IO)
                        val summaries by summariesFlow.collectAsState(initial = emptyList(), context = Dispatchers.IO)

                        ActivityHistorySection(
                            activities = activities,
                            summaries = summaries,
                            onDeleteActivity = { record ->
                                lifecycleScope.launch(Dispatchers.IO) {
                                    dao.deleteActivity(record.id)
                                }
                            }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up compose history", e)
        }
    }

    override fun onPause() {
        super.onPause()
        trackerLogic.saveState(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerHandler.removeCallbacks(timerRunnable)
    }

    private fun setupRecyclerView() {
        binding.rvRecentSessions.layoutManager = LinearLayoutManager(this)
        binding.rvRecentSessions.adapter = sessionAdapter
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategories.removeAllViews()
        for (category in trackerLogic.categories) {
            val isCustomCategory = !trackerLogic.defaultCategories.any { it.equals(category, ignoreCase = true) }
            val chip = Chip(this).apply {
                text = category
                isCheckable = true
                isClickable = true
                isChecked = trackerLogic.activeCategory == category
                if (isCustomCategory) {
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        if (trackerLogic.removeCategory(category)) {
                            trackerLogic.saveState(this@MainActivity)
                            setupCategoryChips()
                            updateUi()
                            Toast.makeText(this@MainActivity, "Category '$category' removed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                setOnClickListener {
                    onCategoryClicked(category)
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun onCategoryClicked(category: String) {
        trackerLogic.startTracking(category)
        trackerLogic.saveState(this)
        startTimer()
        updateUi()
    }

    private fun startTimer() {
        timerHandler.removeCallbacks(timerRunnable)
        timerHandler.post(timerRunnable)
    }

    private fun stopTimer() {
        timerHandler.removeCallbacks(timerRunnable)
        binding.tvTimerDisplay.text = getString(R.string.timer_default)
    }

    private fun setupClickListeners() {
        binding.btnAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        binding.btnViewHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        binding.btnStopActivity.setOnClickListener {
            if (trackerLogic.isTracking) {
                val session = trackerLogic.stopTracking()
                trackerLogic.saveState(this)
                if (session != null) {
                    saveSessionToRoom(session)
                }
                stopTimer()
                updateUi()
            }
        }

        binding.btnClearSessions.setOnClickListener {
            if (trackerLogic.recentSessions.isNotEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Clear Recent Sessions")
                    .setMessage("Clear home screen recent sessions? (All records remain saved in Full History Page)")
                    .setPositiveButton("Clear Home") { _, _ ->
                        trackerLogic.clearSessions()
                        trackerLogic.saveState(this)
                        updateUi()
                        Toast.makeText(this, "Home screen cleared. Full history retained.", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show()
            }
        }
    }

    private fun saveSessionToRoom(session: TrackedSession) {
        try {
            val database = ActivityDatabase.getInstance(applicationContext)
            val record = ActivityRecord(
                activityType = session.category,
                durationSeconds = (session.durationMs / 1000).coerceAtLeast(1L),
                occurredAt = session.startTimeMs
            )
            lifecycleScope.launch(Dispatchers.IO) {
                database.activityDao().insertActivity(record)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error saving session to Room", e)
        }
    }

    private fun showAddCategoryDialog() {
        val input = EditText(this).apply {
            hint = getString(R.string.dialog_add_category_hint)
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_add_category_title)
            .setView(input)
            .setPositiveButton(R.string.btn_add) { _, _ ->
                val categoryName = input.text.toString().trim()
                if (categoryName.isNotEmpty()) {
                    if (trackerLogic.addCategory(categoryName)) {
                        trackerLogic.saveState(this)
                        setupCategoryChips()
                        Toast.makeText(this, "Category '$categoryName' added", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun updateUi() {
        val activeCategory = trackerLogic.activeCategory
        if ((trackerLogic.isTracking) && (activeCategory != null)) {
            binding.tvSessionStatus.text = getString(R.string.status_tracking_format, activeCategory)
            binding.btnStopActivity.isEnabled = true
            if (binding.tvTimerDisplay.text == getString(R.string.timer_default)) {
                binding.tvTimerDisplay.text = trackerLogic.getElapsedTimeFormatted()
            }
            startTimer()
        } else {
            binding.tvSessionStatus.text = getString(R.string.status_no_active_session)
            binding.btnStopActivity.isEnabled = false
            stopTimer()
        }

        for (i in 0 until binding.chipGroupCategories.childCount) {
            val chip = binding.chipGroupCategories.getChildAt(i) as? Chip ?: continue
            chip.isChecked = chip.text.toString() == activeCategory
        }

        val sessions = trackerLogic.recentSessions
        if (sessions.isEmpty()) {
            binding.tvEmptySessions.visibility = View.VISIBLE
            binding.rvRecentSessions.visibility = View.GONE
        } else {
            binding.tvEmptySessions.visibility = View.GONE
            binding.rvRecentSessions.visibility = View.VISIBLE
            sessionAdapter.updateSessions(sessions)
        }
    }
}