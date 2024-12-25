package com.example.jobfinder.UI.WorkingJob

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobfinder.Datas.Model.JobModel
import com.example.jobfinder.R
import com.example.jobfinder.UI.JobDetails.RecruiterJobDetailActivity
import com.example.jobfinder.UI.JobEmpList.JobEmpListActivity
import com.example.jobfinder.UI.PostedJob.PostedJobAdapter
import com.example.jobfinder.UI.PostedJob.PostedJobViewModel
import com.example.jobfinder.UI.Wallet.WalletFragment
import com.example.jobfinder.Utils.FragmentHelper
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.databinding.ActivityWorkingJobBinding

class WorkingJobActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityWorkingJobBinding
    private val viewModel: WorkingJobViewModel by viewModels()
    private lateinit var adapter: WorkingJobAdapter
    private var isActivityOpened = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkingJobBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        adapter = WorkingJobAdapter(this, listOf())
        binding.recyclerWorkingJob.adapter = adapter
        binding.recyclerWorkingJob.layoutManager = LinearLayoutManager(this)

        adapter.setOnItemClickListener(object : WorkingJobAdapter.OnItemClickListener {
            override fun onItemClick(job: JobModel) {
                if (!isActivityOpened) {
                    // Mở activity chỉ khi activity chưa được mở
                    val intent = Intent(this@WorkingJobActivity, JobEmpListActivity::class.java)
                    intent.putExtra("job", job)
                    startActivityForResult(intent, 1004)
                    // Đặt biến kiểm tra là đã mở
                    isActivityOpened = true
                }
            }
        })

        viewModel.postedJobList.observe(this    ) { updatedList ->
            adapter.updateData(updatedList)
            checkEmptyAdapter(updatedList)
        }

        // Khi Activity được tạo, gọi phương thức để tải danh sách công việc đã đăng
        viewModel.fetchPostedJobs()

        binding.animationView.visibility = View.VISIBLE
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1004 && resultCode == Activity.RESULT_OK) {
            isActivityOpened = false
            viewModel.fetchPostedJobs()
        }
    }

    private fun checkEmptyAdapter(list: List<JobModel>) {
        if (list.isEmpty()) {
            binding.noJob.visibility = View.VISIBLE
            binding.animationView.visibility = View.GONE
        } else {
            binding.noJob.visibility = View.GONE
            binding.animationView.visibility = View.GONE
        }
    }
}