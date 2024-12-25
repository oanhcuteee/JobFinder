package com.example.jobfinder.UI.Applicants

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jobfinder.Datas.Model.ApplicantsModel
import com.example.jobfinder.Utils.GetData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ApplicantViewModel: ViewModel() {
    private val _applicantList = MutableLiveData<MutableList<ApplicantsModel>>()
    val applicantList: MutableLiveData<MutableList<ApplicantsModel>> get() = _applicantList

    private val _isLoading = MutableLiveData<Boolean>()

    private val database = FirebaseDatabase.getInstance().getReference("Applicant")

    fun fetchApplicant(jobId: String) {
        _isLoading.value = true
        database.child(jobId).get().addOnSuccessListener { dataSnapshot ->
            val applicantList: MutableList<ApplicantsModel> = mutableListOf()
            dataSnapshot.children.forEach { applicantSnapshot ->
                val applicantModel = applicantSnapshot.getValue(ApplicantsModel::class.java)
                applicantModel?.let {
                    applicantList.add(it)
                }
            }
            // Sort the list of applicants by application date
            val sortedApplicantList = applicantList.sortedByDescending { GetData.convertStringToDate(it.appliedDate.toString()) }
            val mutableSortedApplicantList = sortedApplicantList.toMutableList()
            _applicantList.value = mutableSortedApplicantList
        }.addOnFailureListener {
            _isLoading.value = false
            // Handle failure
        }
    }


    fun deleteApplicant(jobId:String ,userId: String) {
        database.child(jobId).child(userId).removeValue()
            .addOnSuccessListener {
                updateApplicantListAfterDeletion(userId)
            }
            .addOnFailureListener {
            }
    }

    fun deleteJobApplicant(jobId :String){
        database.child(jobId).removeValue()
    }

    private fun updateApplicantListAfterDeletion(userId: String) {
        val currentList = _applicantList.value
        currentList?.let { list ->
            // Tìm và xóa ứng viên có userId tương ứng khỏi danh sách
            val updatedList = list.filter { it.userId != userId }.toMutableList()
            _applicantList.value = updatedList
        }
    }
}