package com.example.jobfinder.UI.UserDetailInfo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobfinder.Datas.Model.ApplicantsModel
import com.example.jobfinder.Datas.Model.AppliedJobModel
import com.example.jobfinder.Datas.Model.JobModel
import com.example.jobfinder.Datas.Model.NotificationsRowModel
import com.example.jobfinder.Datas.Model.SalaryModel
import com.example.jobfinder.R
import com.example.jobfinder.UI.Applicants.ApplicantAdapter
import com.example.jobfinder.UI.Applicants.ApplicantViewModel
import com.example.jobfinder.UI.UsersProfile.ProfileViewModel
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.Utils.RetriveImg
import com.example.jobfinder.databinding.ActivityNuserDetailInfoBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.lifecycle.Observer
import com.example.jobfinder.Datas.Model.JobHistoryModel
import com.example.jobfinder.Datas.Model.JobHistoryParentModel
import com.example.jobfinder.UI.JobHistory.JobHistoryViewModel


class NUserDetailInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNuserDetailInfoBinding

    lateinit var viewModel: ProfileViewModel
    private val applicantViewModel: ApplicantViewModel by viewModels()
    private val rvViewModel: JobHistoryViewModel by viewModels()

    private lateinit var adapter: ApplicantAdapter
    private lateinit var rvAdapter: WNuserReviewedAdapter


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuserDetailInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = FirebaseDatabase.getInstance().reference
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        val applicant = intent.getParcelableExtra<ApplicantsModel>("nuser_applicant")
        val job = intent.getParcelableExtra<JobModel>("job")

        binding.animationView.visibility = View.VISIBLE

        if (applicant != null && job != null) {
            val userId = applicant.userId.toString()
            setupUserInformation(database, userId)
            setupApplicantDescription(applicant)
            setupButtons(job, applicant, userId)
            // adapter infor của user
            adapter = ApplicantAdapter(mutableListOf(), job, binding.root.context, applicantViewModel)


            rvViewModel.fetchNUserReview(userId)
            // Quan sát data adapter từ ViewModel
            rvViewModel.JobHistoryList.observe(this, Observer { reviews ->
                rvAdapter.updateData(reviews)
                checkEmptyAdapter(reviews)
            })

            // adapter reviews của nuser
            rvAdapter = WNuserReviewedAdapter(listOf())
            binding.historyRVrecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.historyRVrecyclerView.adapter = rvAdapter


        }

    }

    override fun onResume() {
        super.onResume()
        RetriveImg.retrieveImage(viewModel.userid, binding.profileImage)
    }

    private fun setupUserInformation(database: DatabaseReference, userId: String) {
        database.child("UserBasicInfo").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.child("name").getValue(String::class.java)
                userName?.let {
                    viewModel.name = it
                    binding.editProfileName.setText(viewModel.name)
                }
                val email = snapshot.child("email").getValue(String::class.java)
                email?.let {
                    viewModel.email = it
                    binding.editProfileEmail.setText(viewModel.email)
                }
                val phone = snapshot.child("phone_num").getValue(String::class.java)
                phone?.let {
                    viewModel.phone = it
                    binding.editProfilePhonenum.setText(viewModel.phone)
                }
                val address = snapshot.child("address").getValue(String::class.java)
                address?.let {
                    viewModel.address = it
                    binding.editProfileAddress.setText(viewModel.address)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SeekerEditProfileFragment", "Database error: ${error.message}")
            }
        })

        database.child("NUserInfo").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val age = snapshot.child("age").getValue(String::class.java)
                age?.let {
                    viewModel.age = it
                    if(it ==""){
                        binding.editProfileAge.setText(R.string.blank_age)
                    }else {
                        binding.editProfileAge.setText(viewModel.age)
                    }
                }
                val gender = snapshot.child("gender").getValue(String::class.java)
                gender?.let {
                    viewModel.gender = it
                    if(it ==""){
                        binding.editProfileGender.setText(R.string.error_invalid_Gender)
                    }else {
                        binding.editProfileGender.setText(viewModel.gender)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SeekerEditProfileFragment", "Database error: ${error.message}")
            }
        })

        viewModel.userid = userId
    }


    private fun setupApplicantDescription(applicant: ApplicantsModel) {
        if(applicant.applicantDes == ""){
            binding.applicantDescription.setText(R.string.no_job_des2)
        }else {
            binding.applicantDescription.setText(applicant.applicantDes.toString())
        }
        binding.animationView.visibility = View.GONE
    }


    private fun setupButtons(job: JobModel, applicant: ApplicantsModel, userId: String) {
        binding.approveBtn.setOnClickListener {
            approveApplicant(job, applicant, userId)
        }

        binding.rejectBtn.setOnClickListener {
            rejectApplicant(job, applicant, userId)
        }

        binding.backButton.setOnClickListener {
            sendResultAndFinish(job, "false")
        }
    }


    private fun approveApplicant(job: JobModel, applicant: ApplicantsModel, userId: String) {
        val jobRef = FirebaseDatabase.getInstance().getReference("Job").child(job.BUserId.toString()).child(job.jobId.toString())
        val curTime = GetData.getCurrentDateTime()
        val notiRef = FirebaseDatabase.getInstance().getReference("Notifications").child(userId)
        val appliedJobRef = FirebaseDatabase.getInstance().getReference("AppliedJob").child(userId).child(job.jobId.toString())

        jobRef.get().addOnSuccessListener { data ->
            val recruitedAmount = data.child("numOfRecruited").getValue(String::class.java)?.toInt() ?: 0
            val empAmount = data.child("empAmount").getValue(String::class.java)?.toInt() ?: 0

            if (recruitedAmount < empAmount) {
                applicantViewModel.deleteApplicant(job.jobId.toString(), userId)
                jobRef.child("numOfRecruited").setValue((recruitedAmount + 1).toString())
                appliedJobRef.removeValue()

                val empInJob = ApplicantsModel(applicant.userId, applicant.applicantDes, curTime, applicant.userName)
                FirebaseDatabase.getInstance().getReference("EmpInJob").child(job.jobId.toString()).child(userId).setValue(empInJob)

                val approvedJob = AppliedJobModel(job.BUserId.toString(), job.jobId.toString(), curTime, job.jobTitle.toString(),
                    job.startHr.toString(), job.endHr.toString(), job.salaryPerEmp.toString(), job.postDate.toString(),
                    job.startTime.toString(), job.endTime.toString())
                FirebaseDatabase.getInstance().getReference("ApprovedJob").child(userId).child(job.jobId.toString()).setValue(approvedJob)

                val totalWorkDay = GetData.countDaysBetweenDates(job.startTime.toString(), job.endTime.toString())
                val salary = SalaryModel(totalWorkDay, 0, 0f)
                FirebaseDatabase.getInstance().getReference("Salary").child(job.jobId.toString()).child(userId).setValue(salary)

                val notiId = notiRef.push().key.toString()
                val notification = NotificationsRowModel(notiId, job.BUserName.toString(), "${getString(R.string.approve_from)} ${job.jobTitle}", curTime)
                notiRef.child(notiId).setValue(notification)

                Toast.makeText(binding.root.context, getString(R.string.approve_success), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(binding.root.context, getString(R.string.enough_Emp), Toast.LENGTH_SHORT).show()
                clearAllApplicants(job)
            }
            sendResultAndFinish(job, "true")
        }
    }


    private fun rejectApplicant(job: JobModel, applicant: ApplicantsModel, userId: String) {
        val curTime = GetData.getCurrentDateTime()
        val notiRef = FirebaseDatabase.getInstance().getReference("Notifications").child(userId)
        val appliedJobRef = FirebaseDatabase.getInstance().getReference("AppliedJob").child(userId).child(job.jobId.toString())

        applicantViewModel.deleteApplicant(job.jobId.toString(), userId)

        val notiId = notiRef.push().key.toString()
        val notification = NotificationsRowModel(notiId, job.BUserName.toString(), "${getString(R.string.reject_from)} ${job.jobTitle}", curTime)
        notiRef.child(notiId).setValue(notification)

        appliedJobRef.removeValue()

        Toast.makeText(binding.root.context, getString(R.string.reject_success), Toast.LENGTH_SHORT).show()
        sendResultAndFinish(job, "true")
    }


    private fun clearAllApplicants(job: JobModel) {
        val curTime = GetData.getCurrentDateTime()
        val applicantViewModel = ApplicantViewModel()

        FirebaseDatabase.getInstance().getReference("AppliedJob").get().addOnSuccessListener {
            for (uid in it.children) {
                applicantViewModel.deleteApplicant(job.jobId.toString(), uid.key.toString())
                FirebaseDatabase.getInstance().getReference("AppliedJob").child(uid.key.toString()).child(job.jobId.toString()).removeValue()

                val notiId = FirebaseDatabase.getInstance().getReference("Notifications").child(uid.key.toString()).push().key.toString()
                val notification = NotificationsRowModel(notiId, job.BUserName.toString(), "${getString(R.string.reject_from)} ${job.jobTitle}", curTime)
                FirebaseDatabase.getInstance().getReference("Notifications").child(uid.key.toString()).child(notiId).setValue(notification)
            }
        }
        FirebaseDatabase.getInstance().getReference("Applicant").child(job.jobId.toString()).removeValue()
    }


    private fun sendResultAndFinish(job:JobModel, change:String) {
        val resultIntent = Intent()
        resultIntent.putExtra("jobId", job.jobId.toString())
        resultIntent.putExtra("change", change)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun checkEmptyAdapter(list: MutableList<JobHistoryModel>) {
        if (list.isEmpty()) {
            binding.noDataReview.visibility = View.VISIBLE
            binding.animationView.visibility = View.GONE
        } else {
            binding.noDataReview.visibility = View.GONE
            binding.animationView.visibility = View.GONE
        }
    }

}