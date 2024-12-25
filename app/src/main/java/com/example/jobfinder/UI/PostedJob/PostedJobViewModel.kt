package com.example.jobfinder.UI.PostedJob

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jobfinder.Datas.Model.IncomeByJobTypeModel
import com.example.jobfinder.Datas.Model.IncomeModel
import com.example.jobfinder.Datas.Model.JobModel
import com.example.jobfinder.Datas.Model.NotificationsRowModel
import com.example.jobfinder.R
import com.example.jobfinder.Utils.GetData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.Currency

class PostedJobViewModel: ViewModel() {
    private val _postedJobList = MutableLiveData<List<JobModel>>()
    val postedJobList: LiveData<List<JobModel>> get() = _postedJobList

    private val _isLoading = MutableLiveData<Boolean>()

    private val auth = FirebaseAuth.getInstance()
    private val uid = auth.currentUser?.uid
    private val database = FirebaseDatabase.getInstance().getReference("Job").child(uid.toString())
    private val userInfoDb = FirebaseDatabase.getInstance().getReference("UserBasicInfo").child(uid.toString())
    private val appliedJobDb = FirebaseDatabase.getInstance().getReference("AppliedJob")
    private val approvedJobDb = FirebaseDatabase.getInstance().getReference("ApprovedJob")
    private val walletAmountRef = FirebaseDatabase.getInstance().getReference("WalletAmount")
    private val notiRef = FirebaseDatabase.getInstance().getReference("Notifications")
    private val bDatabase = FirebaseDatabase.getInstance().getReference("BUserExpense")
    private val bDatabaseByJobId = FirebaseDatabase.getInstance().getReference("BUserExpenseByJobId")

    fun fetchPostedJobs(context: Context) {

        val today =GetData.getCurrentDateTime()
        val todayDate= GetData.getDateFromString(today)

        val format = NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance("VND")

        _isLoading.value = true
        database.get().addOnSuccessListener { dataSnapshot ->
            val postedJobList: MutableList<JobModel> = mutableListOf()
            userInfoDb.child("name").get().addOnSuccessListener { nameSnapshot ->
                val userName = nameSnapshot.getValue(String::class.java).toString()
                dataSnapshot.children.forEach { jobSnapshot ->
                    val jobModel = jobSnapshot.getValue(JobModel::class.java)
                    jobModel?.let {
                        it.BUserName = userName
                        it.status = GetData.setStatus(it.startTime.toString(), it.endTime.toString(), it.empAmount.toString(), it.numOfRecruited.toString())
                        if(it.status == "closed"){
                            deleteAppliedJob(it.jobId.toString())

                        }
                        // khi doanh nghiệp fetch job thì việc đã đóng 7 ngày sau endTime thì sẽ xóa việc và hoàn số tiền còn lại từ việc về cho doanh nghiệp
                        if(GetData.countDaysBetweenDates(it.endTime.toString(), todayDate) >=7 && it.status == "closed"){
                            val refundSalary = -it.totalSalary.toString().toDouble()
                            val postDate = GetData.getDateFromString(it.postDate.toString())
                            val jobTypeId = GetData.getIntFromJobType(it.jobType.toString())
                            // hoàn tiền
                            addWalletAmount(it.BUserId.toString(), it.totalSalary.toString().toFloat())
                            pushExpenseToFirebaseByDate(it.BUserId.toString(), refundSalary.toString(), postDate)
                            pushExpenseToFirebaseJobTypeId(it.BUserId.toString(), refundSalary.toString(), jobTypeId)

                            // thông báo
                            val bUserNotiDetail = "${context.getText(R.string.refund)} ${format.format(it.totalSalary.toString().toDouble())} " +
                                    "${context.getText(R.string.from_job_text)} ${jobModel.jobTitle}."

                            val noti_id = notiRef.child(jobModel.BUserId.toString()).push().key
                            val noti = NotificationsRowModel(noti_id, "Admin",bUserNotiDetail ,today)
                            notiRef.child(jobModel.BUserId.toString()).child(noti.notiId.toString()).setValue(noti)

                            // xóa việc thông qua của ứng viên- này để tránh trường hợp nhân viên không đi làm và không xác nhận kết thúc
                            deleteApprovedJob(it.jobId.toString())

                            // xóa job
                            deleteJob(it.jobId.toString())
                        }else {
                            postedJobList.add(it)
                        }
                    }
                }
                // Sắp xếp danh sách công việc theo thời gian đăng
                val sortedPostedJobList = postedJobList.sortedByDescending { GetData.convertStringToDate(it.postDate.toString()) }
                _postedJobList.value = sortedPostedJobList
                // Cập nhật trạng thái vào Firebase
                updateStatusToFirebase(sortedPostedJobList)
                _isLoading.value = false
            }.addOnFailureListener {
                _isLoading.value = false
                // Xử lý lỗi
            }
        }.addOnFailureListener {
            _isLoading.value = false
            // Xử lý lỗi
        }
    }

    fun deleteJob(jobId: String) {
        database.child(jobId).removeValue()
            .addOnSuccessListener {
            }
            .addOnFailureListener {
            }
    }

    private fun updateStatusToFirebase(jobList: List<JobModel>) {
        val updatesMap = mutableMapOf<String, Any?>()
        for (jobModel in jobList) {
            updatesMap["/${jobModel.jobId}/buserName"] = jobModel.BUserName
            updatesMap["/${jobModel.jobId}/status"] = jobModel.status
        }
        database.updateChildren(updatesMap)
            .addOnSuccessListener {
                // Tất cả các trạng thái đã được cập nhật thành công
            }
            .addOnFailureListener {
                // Xử lý lỗi
            }
    }

    fun deleteAppliedJob(jobId:String){
        appliedJobDb.get().addOnSuccessListener {
            for(uid in it.children){
                appliedJobDb.child(uid.key.toString()).child(jobId).removeValue()
            }
        }
    }

    fun deleteApprovedJob(jobId:String){
        approvedJobDb.get().addOnSuccessListener {
            for(uid in it.children){
                appliedJobDb.child(uid.key.toString()).child(jobId).removeValue()
            }
        }
    }

    fun addWalletAmount(uid: String, amount: Float) {
        walletAmountRef.child(uid).get().addOnSuccessListener { walletAmountSnapShot ->
            if (walletAmountSnapShot.exists()) {
                val walletAmount = walletAmountSnapShot.child("amount").getValue(String::class.java)

                if (walletAmount != null) {
                    try {
                        val currentWalletAmount = walletAmount.toFloat()
                        val newWalletAmount = currentWalletAmount + amount

                        val update = hashMapOf<String, Any>(
                            "amount" to newWalletAmount.toString()
                        )

                        walletAmountRef.child(uid).updateChildren(update).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("addWalletAmount", "Wallet amount updated successfully for user $uid")
                            } else {
                                Log.e("addWalletAmount", "Failed to update wallet amount for user $uid")
                            }
                        }.addOnFailureListener { exception ->
                            Log.e("addWalletAmount", "Failed to update wallet amount", exception)
                        }
                    } catch (e: NumberFormatException) {
                        Log.e("addWalletAmount", "Invalid wallet amount: $walletAmount", e)
                    }
                } else {
                    Log.e("addWalletAmount", "Wallet amount is null for user $uid")
                }
            } else {
                Log.e("addWalletAmount", "Wallet snapshot does not exist for user $uid")
            }
        }.addOnFailureListener { exception ->
            Log.e("addWalletAmount", "Failed to get wallet snapshot", exception)
        }
    }

    private fun pushExpenseToFirebaseByDate(uid:String, expense:String, date:String){

        val toFbDate = GetData.formatDateForFirebase(date)

        bDatabase.child(uid).child(toFbDate).get().addOnSuccessListener {
            if(it.exists()){
                val expenseModel = it.getValue(IncomeModel::class.java)
                if(expenseModel!= null) {
                    val newExpense = expenseModel.incomeAmount.toString().toDouble() + expense.toDouble()
                    val update = hashMapOf<String, Any>(
                        "incomeAmount" to newExpense.toString()
                    )
                    bDatabase.child(uid).child(toFbDate).updateChildren(update)
                }
            }else{
                val incomeModel = IncomeModel(date, expense)
                bDatabase.child(uid).child(toFbDate).setValue(incomeModel)
            }
        }
    }

    private fun pushExpenseToFirebaseJobTypeId(uid:String, expense:String, jobTypeId:Int){

        bDatabaseByJobId.child(uid).child(jobTypeId.toString()).get().addOnSuccessListener {
            if(it.exists()){
                val expenseModel = it.getValue(IncomeByJobTypeModel::class.java)
                if(expenseModel!= null) {
                    val newExpense = expenseModel.incomeAmount.toString().toDouble() + expense.toDouble()
                    if(newExpense == 0.0){
                        bDatabaseByJobId.child(uid).child(jobTypeId.toString()).removeValue()
                    }else {
                        val update = hashMapOf<String, Any>(
                            "incomeAmount" to newExpense.toString())
                        bDatabaseByJobId.child(uid).child(jobTypeId.toString())
                            .updateChildren(update)
                    }
                }
            }else{
                val incomeModel = IncomeByJobTypeModel(jobTypeId.toString(), expense)
                bDatabaseByJobId.child(uid).child(jobTypeId.toString()).setValue(incomeModel)
            }
        }
    }

}
