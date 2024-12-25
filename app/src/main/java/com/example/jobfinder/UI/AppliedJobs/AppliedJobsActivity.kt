package com.example.jobfinder.UI.AppliedJobs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobfinder.Datas.Model.AppliedJobModel
import com.example.jobfinder.UI.JobDetails.SeekerJobDetailActivity
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.databinding.ActivityAppliedJobsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AppliedJobsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppliedJobsBinding
    private val viewModel: AppliedJobsViewModel by viewModels()
    private lateinit var adapter: AppliedJobsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppliedJobsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // nút back về home trên màn hình
        binding.backButton.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        if (viewModel.getAppliedList().isEmpty()){viewModel.fetchAppliedJobs()}

        adapter = AppliedJobsAdapter(viewModel.getAppliedList(),binding.noDataImage)
        binding.appliedJobRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.appliedJobRecyclerView.adapter = adapter

        viewModel.appliedListLiveData.observe(this) { newItem ->
            newItem?.let {
                adapter.updateData(newItem) // Cập nhật adapter khi có dữ liệu mới từ ViewModel
                if (newItem.isEmpty()) {
                    adapter.showNoDataFoundImg()
                } else {
                    adapter.hideNoDataFoundImg()
                }
            }
        }

        viewModel.isFetchingData.observe(this) { isFetching ->
            if (isFetching) {
                binding.animationView.visibility = View.VISIBLE
                adapter.hideNoDataFoundImg()
            } else {
                binding.animationView.visibility = View.GONE
                if (viewModel.getAppliedList().isEmpty()) {
                    adapter.showNoDataFoundImg()
                } else {
                    adapter.hideNoDataFoundImg()
                }
            }
        }

        // Ẩn animationView ban đầu
        binding.animationView.visibility = View.GONE


//      click vào item applied job
        adapter.setOnItemClickListener(object : AppliedJobsAdapter.onItemClickListener {
            override fun onItemClicked(AppliedJob: AppliedJobModel) {
                val intent = Intent(this@AppliedJobsActivity, SeekerJobDetailActivity::class.java)
                intent.putExtra("job_id", AppliedJob.jobId)
                intent.putExtra("buser_id", AppliedJob.buserId)
                intent.putExtra("is_applied", true)
                jobDetailActivityResultLauncher.launch(intent)
            }
        })
    }


    // Biến để đăng ký cho kết quả activity (để xử lý kết quả trả về từ SeekerJobDetailActivity khi ấn hủy ứng tuyển)
    private val jobDetailActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val jobId = result.data?.getStringExtra("job_id")
            if (jobId != null) {
                // Loại bỏ công việc khỏi danh sách đã ứng tuyển
                viewModel.removeAppliedJob(jobId)
            }
        }
    }



}