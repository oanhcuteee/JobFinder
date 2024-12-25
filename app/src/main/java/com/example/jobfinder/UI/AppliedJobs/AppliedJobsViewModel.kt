package com.example.jobfinder.UI.AppliedJobs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jobfinder.Datas.Model.AppliedJobModel
import com.example.jobfinder.Utils.GetData
import com.google.firebase.database.FirebaseDatabase

class AppliedJobsViewModel: ViewModel()  {
    private val appliedList: MutableList<AppliedJobModel> = mutableListOf()
    private val _appliedListLiveData = MutableLiveData<List<AppliedJobModel>>()
    private val _isFetchingData = MutableLiveData<Boolean>()
    private val database = FirebaseDatabase.getInstance().getReference("AppliedJob")


    val appliedListLiveData: LiveData<List<AppliedJobModel>> get() = _appliedListLiveData
    val isFetchingData: LiveData<Boolean> get() = _isFetchingData


    init {
        _isFetchingData.value = false
    }

    fun getAppliedList(): List<AppliedJobModel>{
        return appliedList
    }

    fun clearAppliedList(){
        appliedList.clear()
        _appliedListLiveData.value = appliedList
    }

    fun addAppliedToAppliedList(AppliedJobsData: AppliedJobModel) {
        appliedList.add(AppliedJobsData)
        _appliedListLiveData.value = appliedList
    }

    fun sortByNewestApplied(){
        appliedList.sortByDescending { GetData.convertStringToDate(it.appliedDate.toString()) }
        _appliedListLiveData.value = appliedList
    }

    // Xóa job khỏi danh sách khi ấn hủy ứng tuyển
    fun removeAppliedJob(jobId: String) {
        val iterator = appliedList.iterator()
        while (iterator.hasNext()) {
            val job = iterator.next()
            if (job.jobId == jobId) {
                iterator.remove()
                break
            }
        }
        _appliedListLiveData.value = appliedList
    }

    fun cancelAppliedJob(jobId: String, uid:String) {
        database.child(uid).child(jobId).removeValue()
            .addOnSuccessListener {
            }
            .addOnFailureListener {
            }
    }

    fun deleteAppliedJob(jobId:String){
        database.get().addOnSuccessListener {
            for(uid in it.children){
                database.child(uid.key.toString()).child(jobId).removeValue()
            }
        }
    }

    fun fetchAppliedJobs(){
        _isFetchingData.value = true
        clearAppliedList()
        val userID = GetData.getCurrentUserId()
        FirebaseDatabase.getInstance().getReference("AppliedJob").child(userID.toString())
            .get().addOnSuccessListener {datasnapshot ->
                for (AppliedJobSnapshot in datasnapshot.children) {
                    val AppliedJobModel = AppliedJobSnapshot.getValue(AppliedJobModel::class.java)
                    AppliedJobModel?.let {
                        addAppliedToAppliedList(it)
                    }
                }
                sortByNewestApplied()
                _isFetchingData.value = false
            }
    }
}