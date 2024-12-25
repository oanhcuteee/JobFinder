package com.example.jobfinder.UI.Applicants

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
import com.example.jobfinder.UI.UserDetailInfo.NUserDetailInfoActivity
import com.example.jobfinder.databinding.ActivityApplicantsListBinding

class ActivityApplicantsList : AppCompatActivity() {
    private lateinit var binding: ActivityApplicantsListBinding
    private val REQUEST_CODE = 1002
    private var isActivityOpened = false
    private val viewModel: ApplicantViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicantsListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.animationView.visibility = View.VISIBLE

        val job = intent.getParcelableExtra<JobModel>("job")

        if(job!= null) {

            // Tạo adapter và gán vào RecyclerView
            val adapter = ApplicantAdapter(mutableListOf(), job, binding.root.context, viewModel)
            binding.recyclerApplicantList.adapter = adapter
            binding.recyclerApplicantList.layoutManager = LinearLayoutManager(this)
            binding.animationView.visibility = View.GONE

            adapter.setOnItemClickListener(object : ApplicantAdapter.OnItemClickListener {
                override fun onItemClick(applicant: ApplicantsModel) {
                    if (!isActivityOpened) {
                        val intent = Intent(this@ActivityApplicantsList, NUserDetailInfoActivity::class.java)
                        intent.putExtra("nuser_applicant", applicant)
                        intent.putExtra("job",job)
                        startActivityForResult(intent, REQUEST_CODE)
                        isActivityOpened = true
                    }
                }
            })

            viewModel.applicantList.observe(this) { updatedList ->
                adapter.updateData(updatedList)
                checkEmptyAdapter(updatedList)
            }

            viewModel.fetchApplicant(job.jobId.toString())

            binding.backButton.setOnClickListener {
                sendResultAndFinish(job)
            }

            binding.applicantSwipe.setOnRefreshListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.fetchApplicant(job.jobId.toString())
                    binding.applicantSwipe.isRefreshing = false
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
            val jobId = data?.getStringExtra("jobId")
            val change =data?.getStringExtra("change")
            if(change =="true") {
                viewModel.fetchApplicant(jobId.toString())
            }
        }
    }

    private fun checkEmptyAdapter(list: MutableList<ApplicantsModel>) {
        if (list.isEmpty()) {
            binding.noApplicant.visibility = View.VISIBLE
            binding.animationView.visibility = View.GONE
        } else {
            binding.noApplicant.visibility = View.GONE
            binding.animationView.visibility = View.GONE
        }
    }
}