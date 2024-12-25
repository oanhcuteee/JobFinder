package com.example.jobfinder.UI.JobHistory

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobfinder.Datas.Model.JobHistoryParentModel
import com.example.jobfinder.databinding.ActivityBuserJobHistoryBinding

class BUserJobHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBuserJobHistoryBinding
    private lateinit var adapter: BUserJobHistoryParentAdapter
    private val viewModel: JobHistoryViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuserJobHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        binding.animationView.visibility = View.VISIBLE

        // Tạo adapter và gán vào RecyclerView
        adapter = BUserJobHistoryParentAdapter(mutableListOf())
        binding.recyclerJobHistoryList.adapter = adapter
        binding.recyclerJobHistoryList.layoutManager = LinearLayoutManager(binding.root.context)

        viewModel.JobIdList.observe(this) { updatedList ->
            adapter.updateData(updatedList)
            checkEmptyAdapter(updatedList)
        }

        viewModel.fetchBUserJobHistoryId()

        binding.buserJobHistorySwipe.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.fetchBUserJobHistoryId()
                binding.buserJobHistorySwipe.isRefreshing = false
            }, 1000)
        }

    }

    private fun checkEmptyAdapter(list: MutableList<JobHistoryParentModel>) {
        if (list.isEmpty()) {
            binding.noJobHistory.visibility = View.VISIBLE
            binding.animationView.visibility = View.GONE
        } else {
            binding.noJobHistory.visibility = View.GONE
            binding.animationView.visibility = View.GONE
        }
    }
}