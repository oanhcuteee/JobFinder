package com.example.jobfinder.UI.Home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.jobfinder.Datas.Model.JobModel
import com.example.jobfinder.Utils.GetData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeViewModel : ViewModel() {

    var userRole: String = ""
    private val database = FirebaseDatabase.getInstance().getReference("Job")
    private val appliedJobDb = FirebaseDatabase.getInstance().getReference("AppliedJob")

    fun fetchJobs() {
        FirebaseDatabase.getInstance().getReference("Job")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (userSnapshot in dataSnapshot.children) {
                        val buserId = userSnapshot.key.toString()
                        val tempList: MutableList<JobModel> = mutableListOf()
                        GetData.getUsernameFromUserId(buserId) { username ->
                            for (jobSnapshot in userSnapshot.children) {
                                val jobModel = jobSnapshot.getValue(JobModel::class.java)
                                jobModel?.let {
                                    it.BUserName = username.toString()
                                    it.status = GetData.setStatus(it.startTime.toString(), it.endTime.toString(), it.empAmount.toString(), it.numOfRecruited.toString())

                                    if(it.status == "closed"){
                                        deleteAppliedJob(it.jobId.toString())
//                                        approvedJobViewModel.deleteJob(it.jobId.toString())
                                    }

                                    tempList.add(it) //Chứa full data toàn bộ các job

                                }
                            }
                            updateStatusToFirebase(buserId,tempList)
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
    }

    fun updateStatusToFirebase(userId :String,jobList: List<JobModel>) {
        val updatesMap = mutableMapOf<String, Any?>()
        for (jobModel in jobList) {
            updatesMap["/${jobModel.jobId}/buserName"] = jobModel.BUserName
            updatesMap["/${jobModel.jobId}/status"] = jobModel.status
        }
        database.child(userId).updateChildren(updatesMap)
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

}