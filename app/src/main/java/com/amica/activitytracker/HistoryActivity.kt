package com.amica.activitytracker

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.activitytracker.data.ActivityDatabase
import com.amica.activitytracker.databinding.ActivityHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var summaryAdapter: CategorySummaryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityHistoryBinding.inflate(layoutInflater)
            setContentView(binding.root)

            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "History Page"

            binding.btnBackToTracker.setOnClickListener {
                finish()
            }

            summaryAdapter = CategorySummaryAdapter()
            binding.rvSummaryRecords.layoutManager = LinearLayoutManager(this)
            binding.rvSummaryRecords.adapter = summaryAdapter

            historyAdapter = HistoryAdapter { record ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val database = ActivityDatabase.getInstance(this@HistoryActivity)
                        database.activityDao().deleteActivity(record.id)
                    } catch (e: Exception) {
                        Log.e("HistoryActivity", "Error deleting activity", e)
                    }
                }
            }

            binding.rvHistoryRecords.layoutManager = LinearLayoutManager(this)
            binding.rvHistoryRecords.adapter = historyAdapter

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val database = ActivityDatabase.getInstance(this@HistoryActivity)
                    val dao = database.activityDao()

                    launch {
                        dao.getActivitySummary().collect { summaries ->
                            withContext(Dispatchers.Main) {
                                if (summaries.isEmpty()) {
                                    binding.tvHeaderCategoryTotals.visibility = View.GONE
                                    binding.rvSummaryRecords.visibility = View.GONE
                                } else {
                                    binding.tvHeaderCategoryTotals.visibility = View.VISIBLE
                                    binding.rvSummaryRecords.visibility = View.VISIBLE
                                    summaryAdapter.updateSummaries(summaries)
                                }
                            }
                        }
                    }

                    launch {
                        dao.getRecentActivities().collect { records ->
                            withContext(Dispatchers.Main) {
                                if (records.isEmpty()) {
                                    binding.tvEmptyHistory.visibility = View.VISIBLE
                                    binding.rvHistoryRecords.visibility = View.GONE
                                } else {
                                    binding.tvEmptyHistory.visibility = View.GONE
                                    binding.rvHistoryRecords.visibility = View.VISIBLE
                                    historyAdapter.updateRecords(records)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HistoryActivity", "Error observing activities", e)
                }
            }
        } catch (e: Exception) {
            Log.e("HistoryActivity", "Error in onCreate", e)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}