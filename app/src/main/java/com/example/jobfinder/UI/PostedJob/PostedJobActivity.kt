package com.example.jobfinder.UI.PostedJob

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobfinder.Datas.Model.JobModel
import com.example.jobfinder.UI.JobDetails.RecruiterJobDetailActivity
import com.example.jobfinder.databinding.ActivityPostedJobBinding


class PostedJobActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostedJobBinding
    private val viewModel: PostedJobViewModel by viewModels()
    private lateinit var adapter: PostedJobAdapter
    private val REQUEST_CODE_DELETE_JOB = 1002
    private var isActivityOpened = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostedJobBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        adapter = PostedJobAdapter(this, listOf())
        binding.recyclerPostedJob.adapter = adapter
        binding.recyclerPostedJob.layoutManager = LinearLayoutManager(this)

        binding.swipeLayout.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.fetchPostedJobs(binding.root.context)
                binding.swipeLayout.isRefreshing = false
            }, 1000)
        }

        adapter.setOnItemClickListener(object : PostedJobAdapter.OnItemClickListener {
            override fun onItemClick(job: JobModel) {
                if (!isActivityOpened) {
                    // Mở activity chỉ khi activity chưa được mở
                    val intent = Intent(this@PostedJobActivity, RecruiterJobDetailActivity::class.java)
                    intent.putExtra("job", job)
                    startActivityForResult(intent, REQUEST_CODE_DELETE_JOB)
                    // Đặt biến kiểm tra là đã mở
                    isActivityOpened = true
                }
            }
        })

        // Gắn kết LiveData trong viewModel với giao diện
        viewModel.postedJobList.observe(this    ) { updatedList ->
            adapter.updateData(updatedList)
            checkEmptyAdapter(updatedList)
        }

        // Khi Activity được tạo, gọi phương thức để tải danh sách công việc đã đăng
        viewModel.fetchPostedJobs(binding.root.context)

        binding.animationView.visibility = View.VISIBLE
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_DELETE_JOB && resultCode == Activity.RESULT_OK) {
            isActivityOpened = false
            viewModel.fetchPostedJobs(binding.root.context)
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