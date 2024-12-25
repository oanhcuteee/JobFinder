package com.example.jobfinder.UI.Statistical

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jobfinder.Datas.Model.NUserWorkHour
import com.example.jobfinder.Utils.GetData
import com.google.firebase.database.FirebaseDatabase

class WorkHoursViewModel:ViewModel() {

    private val _workHourList = MutableLiveData<MutableList<NUserWorkHour>>()
    val workHourList: MutableLiveData<MutableList<NUserWorkHour>> get() = _workHourList

    private val _AmountJobPostList = MutableLiveData<MutableList<NUserWorkHour>>()
    val amountJobPostList: MutableLiveData<MutableList<NUserWorkHour>> get() = _AmountJobPostList

    private val database = FirebaseDatabase.getInstance().getReference("NUserWorkHour")

    private val bDatabase = FirebaseDatabase.getInstance().getReference("BUserAmountJobPost")

    @RequiresApi(Build.VERSION_CODES.O)
    fun pushWorkHourToFirebase(uid:String, workTime:String, date:String){

        val toFbDate = GetData.formatDateMonthYearForFirebase(date)
        val formatedDate = GetData.formatDateMonthYear(date)

        database.child(uid).child(toFbDate).get().addOnSuccessListener {
            if(it.exists()){
                val NUserWorkHour = it.getValue(NUserWorkHour::class.java)
                if(NUserWorkHour!= null) {
                    val newWorkHour = NUserWorkHour.workTime.toString().toFloat() + workTime.toFloat()
                    val update = hashMapOf<String, Any>(
                        "workTime" to newWorkHour.toString()
                    )
                    database.child(uid).child(toFbDate).updateChildren(update)
                }
            }else{
                val NUserWorkHour = NUserWorkHour(formatedDate, workTime)
                database.child(uid).child(toFbDate).setValue(NUserWorkHour)
            }
        }
    }

    fun fetchWorkHour(uid: String){
        database.child(uid).get().addOnSuccessListener {
            if(it.exists()){
                val workHourList: MutableList<NUserWorkHour> = mutableListOf()
                it.children.forEach { workSnapshot->
                    val nUserWorkHour = workSnapshot.getValue(NUserWorkHour::class.java)
                    nUserWorkHour?.let{
                        workHourList.add(nUserWorkHour)
                    }
                }
                _workHourList.value = workHourList
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun pushAmountJobPost(uid:String, workTime:String, date:String){

        val toFbDate = GetData.formatDateMonthYearForFirebase(date)
        val formatedDate = GetData.formatDateMonthYear(date)

        bDatabase.child(uid).child(toFbDate).get().addOnSuccessListener {
            if(it.exists()){
                val BUserAmountJobPost = it.getValue(NUserWorkHour::class.java)
                if(BUserAmountJobPost!= null) {
                    val newWorkHour = BUserAmountJobPost.workTime.toString().toDouble() + workTime.toDouble()
                    val update = hashMapOf<String, Any>(
                        "workTime" to newWorkHour.toString()
                    )
                    bDatabase.child(uid).child(toFbDate).updateChildren(update)
                }
            }else{
                val NUserWorkHour = NUserWorkHour(formatedDate, workTime)
                bDatabase.child(uid).child(toFbDate).setValue(NUserWorkHour)
            }
        }
    }

    fun fetchAmountJobPost(uid: String){
        bDatabase.child(uid).get().addOnSuccessListener {
            if(it.exists()){
                val workHourList: MutableList<NUserWorkHour> = mutableListOf()
                it.children.forEach { workSnapshot->
                    val nUserWorkHour = workSnapshot.getValue(NUserWorkHour::class.java)
                    nUserWorkHour?.let{
                        workHourList.add(nUserWorkHour)
                    }
                }
                _AmountJobPostList.value = workHourList
            }
        }
    }
}