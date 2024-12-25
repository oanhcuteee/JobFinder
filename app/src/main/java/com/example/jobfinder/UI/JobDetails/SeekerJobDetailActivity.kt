package com.example.jobfinder.UI.JobDetails

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.jobfinder.Datas.Model.ApplicantsModel
import com.example.jobfinder.Datas.Model.AppliedJobModel
import com.example.jobfinder.Datas.Model.JobModel
import com.example.jobfinder.Datas.Model.NotificationsRowModel
import com.example.jobfinder.R
import com.example.jobfinder.UI.Applicants.ApplicantViewModel
import com.example.jobfinder.UI.AppliedJobs.AppliedJobsViewModel
import com.example.jobfinder.UI.FindNewJobs.FindNewJobViewModel
import com.example.jobfinder.UI.UserDetailInfo.BUserDetailInfoActivity
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.Utils.RetriveImg
import com.example.jobfinder.databinding.ActivitySeekerJobDetailBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Currency
import java.util.Locale

class SeekerJobDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeekerJobDetailBinding
    private lateinit var viewModel: FindNewJobViewModel
    private val applicantViewModel: ApplicantViewModel by viewModels()
    private val appliedJobViewModel: AppliedJobsViewModel by viewModels()
    var isBookmarked: Boolean = false
    var isApplied: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeekerJobDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Khởi tạo viewmodel
        viewModel = ViewModelProvider(this).get(FindNewJobViewModel::class.java)

        var uid = ""
        val job = intent.getParcelableExtra<JobModel>("job")
        val job_id = intent.getStringExtra("job_id")
        val buser_id = intent.getStringExtra("buser_id")
        // Nhận trạng thái (btn apply) từ AppliedJobActivity
        isApplied = intent.getBooleanExtra("is_applied", false)
        updateApplyButton()     // cập nhật text cho btn sau khi nhận trạng thái từ AppliedJobActivity


        if (job_id != null && buser_id != null) {
            fetchJobData(job_id, buser_id)
            uid = buser_id

            // Nút apply (logic nhận từ AppliedJob)
            binding.applyBtn.setOnClickListener {
                if (isApplied) {
                    unapplyJob(job_id)
                } else {
                    // Logic apply job nếu cần thiết
                }
            }
        } else if (job != null) {
            uid = job.BUserId.toString()
            // Gán data
            fetchJobData(job.jobId.toString(), job.BUserId.toString())

            // Kiểm tra nếu công việc đã được apply
            checkIfApplied(job.jobId.toString())

            val dialog = Dialog(binding.root.context)
            dialog.setContentView(R.layout.dialog_apply_job_des)

            // Nút apply (logic nhận từ NewJobActivity)
            binding.applyBtn.setOnClickListener {
                if (isApplied) {
                    unapplyJob(job.jobId.toString())
                } else {
                    applyJob(job)
                }
            }
        }

        binding.animationView.visibility = View.VISIBLE
        binding.detailJobScrollView.visibility = View.GONE

        binding.jobDetailBuserName.setOnClickListener {
            val intent = Intent(this, BUserDetailInfoActivity::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
        }


        binding.buserLogo.setOnClickListener {
            val intent = Intent(this, BUserDetailInfoActivity::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

    }


    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun fetchJobData(jobId: String, bUserId: String) {
        FirebaseDatabase.getInstance().getReference("Job").child(bUserId).child(jobId).get()
            .addOnSuccessListener { dataSnapshot ->
                val job = dataSnapshot.getValue(JobModel::class.java)
                job?.let {
                    val recruitedAmount = it.numOfRecruited
                    val emp = "$recruitedAmount/${it.empAmount}"
                    val format = java.text.NumberFormat.getCurrencyInstance()
                    format.currency = Currency.getInstance("VND")
                    val salaryTxt =
                        format.format(job.salaryPerEmp?.toDouble()) + resources.getString(R.string.Ji_unit3)
                    val shift = "${it.startHr} - ${it.endHr}"

                    binding.jobDetailBuserName.text = it.BUserName?.uppercase(Locale.getDefault())
                    binding.jobDetailJobTitle.text = it.jobTitle
                    binding.jobDetailJobType.text = it.jobType
                    binding.jobDetailSalary.text = salaryTxt
                    binding.jobDetailEmpAmount.text = emp
                    binding.jobDetailStartTime.text = it.startTime
                    binding.jobDetailEndTime.text = it.endTime
                    binding.jobDetailWorkShift.text = shift
                    binding.jobDetailAddress.text = it.address
                    binding.jobDetailDes.text = it.jobDes

                    binding.detailJobScrollView.visibility = View.VISIBLE
                    binding.animationView.visibility = View.GONE

                    RetriveImg.retrieveImage(job.BUserId.toString(), binding.buserLogo)

                    binding.detailJobScrollView.visibility = View.VISIBLE
                    binding.animationView.visibility = View.GONE
                }
            }
    }

    private fun checkIfApplied(jobId: String) {
        val userId = GetData.getCurrentUserId()
        userId?.let {
            FirebaseDatabase.getInstance().getReference("AppliedJob")
                .child(it)
                .child(jobId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        isApplied = snapshot.exists()
                        updateApplyButton()
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }

    private fun updateApplyButton() {
        binding.applyBtn.text = if (isApplied) {
            getString(R.string.unapply)
        } else {
            getString(R.string.apply)
        }
    }

    private fun applyJob(job: JobModel) {
        val dialog = Dialog(binding.root.context)
        dialog.setContentView(R.layout.dialog_apply_job_des)

        val cancel = dialog.findViewById<Button>(R.id.cancel_btn)
        val send = dialog.findViewById<Button>(R.id.send)
        val description = dialog.findViewById<TextInputEditText>(R.id.description)
        val notiRef = FirebaseDatabase.getInstance().getReference("Notifications")
            .child(job.BUserId.toString())

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        send.setOnClickListener {
            val userID = GetData.getCurrentUserId()
            if (userID != null) {
                GetData.getUsernameFromUserId(userID.toString()) { username ->
                    if (username != null) {
                        val curTime = GetData.getCurrentDateTime()
                        val desc = description.text.toString()

                        val applicant = ApplicantsModel(userID.toString(), desc, curTime, username)
                        val appliedJob = AppliedJobModel(
                            job.BUserId.toString(),
                            job.jobId.toString(),
                            curTime,
                            job.jobTitle.toString(),
                            job.startHr.toString(),
                            job.endHr.toString(),
                            job.salaryPerEmp.toString(),
                            job.postDate.toString(),
                            job.startTime.toString(),
                            job.endTime.toString()
                        )

                        val notiId = notiRef.push().key.toString()
                        val notification = NotificationsRowModel(
                            notiId, "Admin",
                            "$username ${getString(R.string.applied)} ${job.jobTitle.toString()}.", curTime
                        )

                        FirebaseDatabase.getInstance().getReference("Applicant")
                            .child(job.jobId.toString()).child(userID.toString())
                            .setValue(applicant)
                        FirebaseDatabase.getInstance().getReference("AppliedJob")
                            .child(userID.toString()).child(job.jobId.toString())
                            .setValue(appliedJob)
                        notiRef.child(notiId).setValue(notification)

                        Toast.makeText(
                            binding.root.context,
                            getString(R.string.applied_success),
                            Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()

                        isApplied = true
                        updateApplyButton()
                    } else {
                        println("Không thể lấy được username.")
                    }
                }
            } else {
                println("Không thể lấy được userID.")
            }
        }
        dialog.show()
    }

    private fun unapplyJob(jobId: String) {
        val userID = GetData.getCurrentUserId()
        if (userID != null) {
            appliedJobViewModel.cancelAppliedJob(jobId, userID)
            applicantViewModel.deleteApplicant(jobId, userID)
            isApplied = false
            updateApplyButton()

            // Gửi kết quả về AppliedJobActivity (nếu hủy ứng tuyển từ AppliedJobActivity)
            val resultIntent = Intent()
            resultIntent.putExtra("job_id", jobId)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
            Toast.makeText(this, getString(R.string.unapplied_success), Toast.LENGTH_SHORT).show()
        }
    }
}