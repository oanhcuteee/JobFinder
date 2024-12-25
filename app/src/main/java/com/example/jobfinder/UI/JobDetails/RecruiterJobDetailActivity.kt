package com.example.jobfinder.UI.JobDetails

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.jobfinder.Datas.Model.JobModel
import com.example.jobfinder.Datas.Model.NotificationsRowModel
import com.example.jobfinder.R
import com.example.jobfinder.UI.Applicants.ActivityApplicantsList
import com.example.jobfinder.UI.Applicants.ApplicantViewModel
import com.example.jobfinder.UI.AppliedJobs.AppliedJobsViewModel
import com.example.jobfinder.UI.CheckIn.CheckInViewModel
import com.example.jobfinder.UI.JobEmpList.JobEmpListViewModel
import com.example.jobfinder.UI.PostedJob.PostedJobViewModel
import com.example.jobfinder.UI.Statistical.IncomeViewModel
import com.example.jobfinder.UI.Statistical.WorkHoursViewModel
import com.example.jobfinder.UI.UserDetailInfo.BUserDetailInfoActivity
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.Utils.RetriveImg
import com.example.jobfinder.databinding.ActivityRecruiterJobDetailBinding
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.Currency

class RecruiterJobDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecruiterJobDetailBinding
    private val viewModel: PostedJobViewModel by viewModels()
    private val applicantViewModel: ApplicantViewModel by viewModels()
    private val appliedJobViewModel: AppliedJobsViewModel by viewModels()
    private val empInJobViewModel: JobEmpListViewModel by viewModels()
    private val approvedJobViewModel: CheckInViewModel by viewModels()
    private val incomeViewModel: IncomeViewModel by viewModels()
    private val workHourViewModel: WorkHoursViewModel by viewModels()

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecruiterJobDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val job = intent.getParcelableExtra<JobModel>("job")

        binding.animationView.visibility = View.VISIBLE
        binding.detailJobScrollView.visibility = View.GONE

        if (job != null) {
            if(job.status.toString()== "closed"){
                binding.detailJobBtnHolder.visibility = View.GONE
            }
            if(job.status.toString()== "working"){
                binding.detailJobBtnHolder.visibility = View.GONE
            }

            fetchJobData(job.jobId.toString(), job.BUserId.toString())

            binding.deleteBtn.setOnClickListener {
                if (job.status.toString() == "recruiting") {
                    amountWorking(job)
                }
                viewModel.deleteJob(job.jobId.toString())
                applicantViewModel.deleteJobApplicant(job.jobId.toString())
                appliedJobViewModel.deleteAppliedJob(job.jobId.toString())
                empInJobViewModel.deleteJobEmpInJob(job.jobId.toString())
                approvedJobViewModel.deleteJob(job.jobId.toString())
                FirebaseDatabase.getInstance().getReference("CheckInFromBUser").child(job.jobId.toString()).removeValue()
                FirebaseDatabase.getInstance().getReference("NUserCheckIn").child(job.jobId.toString()).removeValue()


                // bao giờ làm viewModel cho savedJob thì sẽ thêm hàm xóa ở đây

                Toast.makeText(this@RecruiterJobDetailActivity, getString(R.string.deleted_job), Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }

            binding.appliedListBtn.setOnClickListener {
                val intent = Intent(this, ActivityApplicantsList::class.java)
                intent.putExtra("job", job)
                startActivityForResult(intent, 1000)
            }

            binding.buserLogo.setOnClickListener{
                val intent = Intent(this, BUserDetailInfoActivity::class.java)
                intent.putExtra("uid", job.BUserId.toString())
                startActivity(intent)
            }
        }

        binding.backButton.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

    }

    private fun amountWorking(job: JobModel){
        val walletAmountRef =
            FirebaseDatabase.getInstance().getReference("WalletAmount")
                .child(job.BUserId.toString()).child("amount")
        walletAmountRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val walletAmountString = snapshot.getValue(String::class.java)
                    val walletAmount = walletAmountString?.toFloatOrNull() ?: 0f
                    val newWalletAmount = walletAmount + job.totalSalary.toString().toFloat()
                    //refund to wallet
                    walletAmountRef.setValue(newWalletAmount.toString())

                    //khi xóa việc trạng thái đang tuyển thì phải trừ đi chi tiêu, trừ đi số việc đã đăng
                    val refundExpense = -job.totalSalary?.toDouble()!!
                    val jobTypeId = GetData.getIntFromJobType(job.jobType.toString())
                    val expenseDate = GetData.getDateFromString(job.postDate.toString())

                    incomeViewModel.pushExpenseToFirebaseByDate(job.BUserId.toString(), refundExpense.toString(), expenseDate)

                    incomeViewModel.pushExpenseToFirebaseJobTypeId(job.BUserId.toString(), refundExpense.toString(), jobTypeId)

                    workHourViewModel.pushAmountJobPost(job.BUserId.toString(), "-1", expenseDate)

                    // notification
                    val date = GetData.getCurrentDateTime()
                    val notiId = FirebaseDatabase.getInstance()
                        .getReference("Notifications")
                        .child(job.BUserId.toString()).push().key.toString()

                    // format tiền
                    val format = NumberFormat.getCurrencyInstance()
                    format.currency = Currency.getInstance("VND")

                    val notificationsRowModel = NotificationsRowModel(
                        notiId,
                        "Admin",
                        "${getString(R.string.refund)}.\n" +
                                "${getString(R.string.from_job)} ${job.jobTitle}.\n" +
                                "+${format.format(job.totalSalary?.toDouble())} ${getString(R.string.to_wallet)}",
                        date
                    )
                    FirebaseDatabase.getInstance()
                        .getReference("Notifications")
                        .child(job.BUserId.toString())
                        .child(notiId)
                        .setValue(notificationsRowModel)
                } else {
                    // Handle case when wallet data does not exist
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            // Lấy jobId và bUserId từ Intent nếu cần
            val jobId = data?.getStringExtra("jobId")
            val bUserId = data?.getStringExtra("bUserId")
            jobId?.let { fetchJobData(it, bUserId ?: "") }
        }
    }

    private fun fetchJobData(jobId: String, bUserId: String) {
        FirebaseDatabase.getInstance().getReference("Job").child(bUserId).child(jobId).get()
            .addOnSuccessListener { dataSnapshot ->
                val job = dataSnapshot.getValue(JobModel::class.java)
                job?.let {
                    val recruitedAmount = it.numOfRecruited
                    val emp = "$recruitedAmount/${it.empAmount}"
                    val format = NumberFormat.getCurrencyInstance()
                    format.currency = Currency.getInstance("VND")
                    val salaryTxt =
                        format.format(job.salaryPerEmp?.toDouble()) + resources.getString(R.string.Ji_unit3)
                    val shift = "${it.startHr} - ${it.endHr}"

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


}
