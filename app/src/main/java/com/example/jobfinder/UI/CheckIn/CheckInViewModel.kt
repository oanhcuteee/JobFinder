package com.example.jobfinder.UI.CheckIn

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jobfinder.Datas.Model.AppliedJobModel
import com.example.jobfinder.Utils.CheckTime
import com.example.jobfinder.Utils.GetData
import com.google.firebase.database.FirebaseDatabase

class CheckInViewModel: ViewModel() {
    private val _ApprovedJobList = MutableLiveData<MutableList<AppliedJobModel>>()
    val ApprovedJobList: MutableLiveData<MutableList<AppliedJobModel>> get() = _ApprovedJobList

    private val _isLoading = MutableLiveData<Boolean>()

    private val database = FirebaseDatabase.getInstance().getReference("ApprovedJob")
    private val bUserCheckInDb = FirebaseDatabase.getInstance().getReference("CheckInFromBUser")
    private val nUserCheckInDb = FirebaseDatabase.getInstance().getReference("NUserCheckIn")

    fun fetchApprovedJob() {
        _isLoading.value = true
        val uid = GetData.getCurrentUserId()
        database.child(uid.toString()).get().addOnSuccessListener { dataSnapshot ->
            val ApprovedJobList: MutableList<AppliedJobModel> = mutableListOf()
            dataSnapshot.children.forEach { ApprovedJobSnapshot ->
                val ApprovedJobModel = ApprovedJobSnapshot.getValue(AppliedJobModel::class.java)
                ApprovedJobModel?.let {
                    ApprovedJobList.add(it)
                }
            }
            // Sort the list of ApprovedJobs by application date
            val sortedApprovedJobList = ApprovedJobList.sortedByDescending { GetData.convertStringToDate(it.appliedDate.toString()) }
            val mutableSortedApprovedJobList = sortedApprovedJobList.toMutableList()
            _ApprovedJobList.value = mutableSortedApprovedJobList
        }.addOnFailureListener {
            _isLoading.value = false
            // Handle failure
        }
    }

    fun fetchApprovedJobForCheckIn() {
        val today = GetData.getCurrentDateTime()
        val todayDate = GetData.getDateFromString(today)
        _isLoading.value = true
        val uid = GetData.getCurrentUserId()
        database.child(uid.toString()).get().addOnSuccessListener { dataSnapshot ->
            val ApprovedJobList: MutableList<AppliedJobModel> = mutableListOf()
            dataSnapshot.children.forEach { ApprovedJobSnapshot ->
                val ApprovedJobModel = ApprovedJobSnapshot.getValue(AppliedJobModel::class.java)
                ApprovedJobModel?.let {
                    if(CheckTime.isDateAfter(todayDate, ApprovedJobModel.startTime.toString())) {
                        ApprovedJobList.add(it)
                    }
                }
            }
            // Sort the list of ApprovedJobs by application date
            val sortedApprovedJobList = ApprovedJobList.sortedByDescending { GetData.convertStringToDate(it.appliedDate.toString()) }
            val mutableSortedApprovedJobList = sortedApprovedJobList.toMutableList()
            _ApprovedJobList.value = mutableSortedApprovedJobList
        }.addOnFailureListener {
            _isLoading.value = false
            // Handle failure
        }
    }

    fun deleteJob(job_id:String){
        database.get().addOnSuccessListener {
            for(uid in it.children){
                database.child(uid.key.toString()).child(job_id).removeValue()
            }
        }
    }

    fun removeApprovedJob(jobId: String, uid:String) {
        database.child(uid).child(jobId).removeValue()
            .addOnSuccessListener {
            }
            .addOnFailureListener {
            }
    }

    fun deleteCheckIn(jobId:String){
        bUserCheckInDb.child(jobId).removeValue()

        nUserCheckInDb.child(jobId).removeValue()
    }

}