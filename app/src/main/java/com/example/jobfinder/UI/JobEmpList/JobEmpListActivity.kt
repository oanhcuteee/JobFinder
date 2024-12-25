package com.example.jobfinder.UI.JobEmpList

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobfinder.Datas.Model.ApplicantsModel
import com.example.jobfinder.Datas.Model.JobModel
import com.example.jobfinder.databinding.ActivityJobEmpListBinding

class JobEmpListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJobEmpListBinding
    private val REQUEST_CODE = 1002
    private var isActivityOpened = false
    private val viewModel: JobEmpListViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobEmpListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.animationView.visibility = View.VISIBLE

        val job = intent.getParcelableExtra<JobModel>("job")

        if(job!= null) {

            // Tạo adapter và gán vào RecyclerView
            val adapter = JobEmpListAdapter(mutableListOf(), binding.root.context, job.jobId.toString())
            binding.recyclerEmpInJobList.adapter = adapter
            binding.recyclerEmpInJobList.layoutManager = LinearLayoutManager(this)

            viewModel.EmployeeList.observe(this) { updatedList ->
                adapter.updateData(updatedList)
                checkEmptyAdapter(updatedList)
            }

            viewModel.fetchEmployee(job.jobId.toString())

            binding.backButton.setOnClickListener {
                sendResultAndFinish(job)
            }

            binding.jobEmpListSwipe.setOnRefreshListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.fetchEmployee(job.jobId.toString())
                    binding.jobEmpListSwipe.isRefreshing = false
                }, 1000)
            }

        }

    }

    private fun sendResultAndFinish(job: JobModel) {
        val resultIntent = Intent()
        resultIntent.putExtra("jobId", job.jobId.toString())
        resultIntent.putExtra("bUserId", job.BUserId.toString())
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val job = intent.getParcelableExtra<JobModel>("job")
        if (job != null) {
            sendResultAndFinish(job)
        } else {
            super.onBackPressed()
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            isActivityOpened = false
        }
    }

    private fun checkEmptyAdapter(list: MutableList<ApplicantsModel>) {
        if (list.isEmpty()) {
            binding.noEmpInJob.visibility = View.VISIBLE
            binding.animationView.visibility = View.GONE
        } else {
            binding.noEmpInJob.visibility = View.GONE
            binding.animationView.visibility = View.GONE
        }
    }
}