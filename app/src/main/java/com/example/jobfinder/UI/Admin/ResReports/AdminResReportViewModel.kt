package com.example.jobfinder.UI.Admin.ResReports

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jobfinder.Datas.Model.SupportUser
import com.google.firebase.database.FirebaseDatabase

class AdminResReportViewModel: ViewModel() {

    private val _ReportList = MutableLiveData<MutableList<SupportUser>>()
    val reportList: MutableLiveData<MutableList<SupportUser>> get() = _ReportList

    private val _FeedbackList = MutableLiveData<MutableList<SupportUser>>()
    val feedbackList: MutableLiveData<MutableList<SupportUser>> get() = _FeedbackList

    private val _TechList = MutableLiveData<MutableList<SupportUser>>()
    val techList: MutableLiveData<MutableList<SupportUser>> get() = _TechList

    private val database = FirebaseDatabase.getInstance().getReference("AdminRef").child("Report")

    fun fetchReport(){
        database.get().addOnSuccessListener {
            if(it.exists()){
                val reportList: MutableList<SupportUser> = mutableListOf()
                val techList: MutableList<SupportUser> = mutableListOf()
                val feedbackList: MutableList<SupportUser> = mutableListOf()
                it.children.forEach { reportSnapshot->
                    val reportModel = reportSnapshot.getValue(SupportUser::class.java)
                    reportModel?.let{
                        if (reportModel.supportName == "report"){ reportList.add(reportModel)}
                        if (reportModel.supportName == "feedback"){ feedbackList.add(reportModel)}
                        if (reportModel.supportName == "technical"){ techList.add(reportModel)}
                    }
                }
                _FeedbackList.value = feedbackList
                _ReportList.value = reportList
                _TechList.value = techList
            }else{
                _FeedbackList.value = mutableListOf()
                _ReportList.value = mutableListOf()
                _TechList.value = mutableListOf()
            }
        }
    }

    fun deleteReport(reportId:String) {
        database.child(reportId).removeValue()
            .addOnSuccessListener {
                updateReportListAfterDeletion(reportId)
            }
            .addOnFailureListener {
            }
    }

    private fun updateReportListAfterDeletion(reportId: String) {
        val reportList = _ReportList.value
        val techList = _TechList.value
        val feedback = _FeedbackList.value

        reportList?.let { list ->
            val updatedList = list.filter { it.supportId != reportId }.toMutableList()
            _ReportList.value = updatedList
        }

        techList?.let { list ->
            val updatedList = list.filter { it.supportId != reportId }.toMutableList()
            _TechList.value = updatedList
        }

        feedback?.let { list ->
            val updatedList = list.filter { it.supportId != reportId }.toMutableList()
            _FeedbackList.value = updatedList
        }
    }

}