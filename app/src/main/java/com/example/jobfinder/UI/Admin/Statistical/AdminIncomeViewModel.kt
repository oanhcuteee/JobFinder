package com.example.jobfinder.UI.Admin.Statistical

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jobfinder.Datas.Model.IncomeModel
import com.example.jobfinder.Utils.GetData
import com.google.firebase.database.FirebaseDatabase

class AdminIncomeViewModel: ViewModel() {

    private val _incomeList = MutableLiveData<MutableList<IncomeModel>>()
    val incomeList: MutableLiveData<MutableList<IncomeModel>> get() = _incomeList

    private val database = FirebaseDatabase.getInstance().getReference("AdminRef").child("AppIncome")

    fun pushIncomeToFirebaseByDate( income:String, date:String){

        val toFbDate = GetData.formatDateForFirebase(date)

        database.child(toFbDate).get().addOnSuccessListener {
            if(it.exists()){
                val incomeModel = it.getValue(IncomeModel::class.java)
                if(incomeModel!= null) {
                    val newIncome = incomeModel.incomeAmount.toString().toDouble() + income.toDouble()
                    val update = hashMapOf<String, Any>(
                        "incomeAmount" to newIncome.toString()
                    )
                    database.child(toFbDate).updateChildren(update)
                }
            }else{
                val incomeModel = IncomeModel(date, income)
                database.child(toFbDate).setValue(incomeModel)
            }
        }
    }

    fun fetchIncome(){
        database.get().addOnSuccessListener {
            if(it.exists()){
                val incomeList: MutableList<IncomeModel> = mutableListOf()
                it.children.forEach { incomeSnapshot->
                    val incomeModel = incomeSnapshot.getValue(IncomeModel::class.java)
                    incomeModel?.let{
                        incomeList.add(incomeModel)
                    }
                }
                _incomeList.value = incomeList
            }
        }
    }
}