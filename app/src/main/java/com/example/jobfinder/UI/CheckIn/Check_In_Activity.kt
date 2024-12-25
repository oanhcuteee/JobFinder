package com.example.jobfinder.UI.CheckIn

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobfinder.Datas.Model.AppliedJobModel
import com.example.jobfinder.UI.Home.HomeViewModel
import com.example.jobfinder.UI.Statistical.IncomeViewModel
import com.example.jobfinder.UI.Statistical.WorkHoursViewModel
import com.example.jobfinder.databinding.ActivityCheckInBinding

class Check_In_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckInBinding
    private val viewModel: CheckInViewModel by viewModels()
    private val incomeViewModel: IncomeViewModel by viewModels()
    private val workHourViewModel: WorkHoursViewModel by viewModels()
    private val homeViewModel:HomeViewModel by viewModels()
    private lateinit var adapter: CheckInAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        homeViewModel.fetchJobs()

        binding.animationView.visibility = View.VISIBLE

        // Tạo adapter và gán vào RecyclerView
        adapter = CheckInAdapter(mutableListOf(), binding.root.context, incomeViewModel, workHourViewModel)
        binding.recyclerCheckIn.adapter = adapter
        binding.recyclerCheckIn.layoutManager = LinearLayoutManager(this)

        viewModel.ApprovedJobList.observe(this) { updatedList ->
            adapter.updateData(updatedList)
            checkEmptyAdapter(updatedList)
        }

        viewModel.fetchApprovedJobForCheckIn()

        binding.backButton.setOnClickListener {
            sendResultAndFinish()
        }

        binding.checkInSwipe.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.fetchApprovedJobForCheckIn()
                binding.checkInSwipe.isRefreshing = false
            }, 1000)
        }


    }

    private fun sendResultAndFinish() {
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }


    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        sendResultAndFinish()
    }

    private fun checkEmptyAdapter(list: MutableList<AppliedJobModel>) {
        if (list.isEmpty()) {
            binding.noJob.visibility = View.VISIBLE
            binding.animationView.visibility = View.GONE
        } else {
            binding.noJob.visibility = View.GONE
            binding.animationView.visibility = View.GONE
        }
    }
}