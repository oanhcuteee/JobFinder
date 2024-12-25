package com.example.jobfinder.UI.WorkingJob

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jobfinder.Datas.Model.JobModel
import com.example.jobfinder.Utils.GetData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class WorkingJobViewModel : ViewModel() {
    private val _postedJobList = MutableLiveData<List<JobModel>>()
    val postedJobList: LiveData<List<JobModel>> get() = _postedJobList

    private val _isLoading = MutableLiveData<Boolean>()

    private val auth = FirebaseAuth.getInstance()
    private val uid = auth.currentUser?.uid
    private val database = FirebaseDatabase.getInstance().getReference("Job").child(uid.toString())
    private val userInfoDb = FirebaseDatabase.getInstance().getReference("UserBasicInfo").child(uid.toString())

    fun fetchPostedJobs() {
        _isLoading.value = true
        database.get().addOnSuccessListener { dataSnapshot ->
            val postedJobList: MutableList<JobModel> = mutableListOf()
            userInfoDb.child("name").get().addOnSuccessListener { nameSnapshot ->
                dataSnapshot.children.forEach { jobSnapshot ->
                    val jobModel = jobSnapshot.getValue(JobModel::class.java)
                    jobModel?.let {
                        if(jobModel.status == "working") {
                            postedJobList.add(it)
                        }
                    }
                }
                // Sắp xếp danh sách công việc theo thời gian đăng
                val sortedPostedJobList = postedJobList.sortedByDescending { GetData.convertStringToDate(it.postDate.toString()) }
                _postedJobList.value = sortedPostedJobList
                // Cập nhật trạng thái vào Firebase
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

}
