package com.example.jobfinder.UI.JobEmpList

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jobfinder.Datas.Model.ApplicantsModel
import com.example.jobfinder.Utils.GetData
import com.google.firebase.database.FirebaseDatabase

class JobEmpListViewModel: ViewModel() {
    private val _EmployeeList = MutableLiveData<MutableList<ApplicantsModel>>()
    val EmployeeList: MutableLiveData<MutableList<ApplicantsModel>> get() = _EmployeeList

    private val _isLoading = MutableLiveData<Boolean>()

    private val database = FirebaseDatabase.getInstance().getReference("EmpInJob")

    fun fetchEmployee(jobId: String) {
        _isLoading.value = true
        database.child(jobId).get().addOnSuccessListener { dataSnapshot ->
            val EmployeeList: MutableList<ApplicantsModel> = mutableListOf()
            dataSnapshot.children.forEach { EmployeeSnapshot ->
                val EmployeeModel = EmployeeSnapshot.getValue(ApplicantsModel::class.java)
                EmployeeModel?.let {
                    EmployeeList.add(it)
                }
            }
            // Sort the list of Employees by application date
            val sortedEmployeeList = EmployeeList.sortedByDescending { GetData.convertStringToDate(it.appliedDate.toString()) }
            val mutableSortedEmployeeList = sortedEmployeeList.toMutableList()
            _EmployeeList.value = mutableSortedEmployeeList
        }.addOnFailureListener {
            _isLoading.value = false
            // Handle failure
        }
    }


    fun deleteEmployee(jobId:String ,userId: String) {
        database.child(jobId).child(userId).removeValue()
            .addOnSuccessListener {
                updateEmployeeListAfterDeletion(userId)
            }
            .addOnFailureListener {
            }
    }

    fun deleteJobEmpInJob(jobId :String){
        database.child(jobId).removeValue()
    }

    private fun updateEmployeeListAfterDeletion(userId: String) {
        val currentList = _EmployeeList.value
        currentList?.let { list ->
            // Tìm và xóa ứng viên có userId tương ứng khỏi danh sách
            val updatedList = list.filter { it.userId != userId }.toMutableList()
            _EmployeeList.value = updatedList
        }
    }
}