package com.example.jobfinder.UI.Statistical

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jobfinder.Datas.Model.IncomeByJobTypeModel
import com.example.jobfinder.Datas.Model.IncomeModel
import com.example.jobfinder.Utils.GetData
import com.google.firebase.database.FirebaseDatabase

class IncomeViewModel:ViewModel() {
    private val _incomeList = MutableLiveData<MutableList<IncomeModel>>()
    val incomeList: MutableLiveData<MutableList<IncomeModel>> get() = _incomeList

    private val _incomeByJobTypeList = MutableLiveData<MutableList<IncomeByJobTypeModel>>()
    val incomeByJobTypeList: MutableLiveData<MutableList<IncomeByJobTypeModel>> get() = _incomeByJobTypeList

    private val _expenseList = MutableLiveData<MutableList<IncomeModel>>()
    val expenseList: MutableLiveData<MutableList<IncomeModel>> get() = _expenseList

    private val _expenseByJobTypeList = MutableLiveData<MutableList<IncomeByJobTypeModel>>()
    val expenseByJobTypeList: MutableLiveData<MutableList<IncomeByJobTypeModel>> get() = _expenseByJobTypeList

    private val database = FirebaseDatabase.getInstance().getReference("NUserIncome")

    private val databaseByJobId = FirebaseDatabase.getInstance().getReference("NUserIncomeByJobId")

    private val bDatabase = FirebaseDatabase.getInstance().getReference("BUserExpense")

    private val bDatabaseByJobId = FirebaseDatabase.getInstance().getReference("BUserExpenseByJobId")

    fun pushIncomeToFirebaseByDate(uid:String, income:String, date:String){

        val toFbDate = GetData.formatDateForFirebase(date)

        database.child(uid).child(toFbDate).get().addOnSuccessListener {
            if(it.exists()){
                val incomeModel = it.getValue(IncomeModel::class.java)
                if(incomeModel!= null) {
                    val newIncome = incomeModel.incomeAmount.toString().toDouble() + income.toDouble()
                    val update = hashMapOf<String, Any>(
                        "incomeAmount" to newIncome.toString()
                    )
                    database.child(uid).child(toFbDate).updateChildren(update)
                }
            }else{
                val incomeModel = IncomeModel(date, income)
                database.child(uid).child(toFbDate).setValue(incomeModel)
            }
        }
    }

    fun pushIncomeToFirebaseJobTypeId(uid:String, income:String, jobTypeId:Int){

        databaseByJobId.child(uid).child(jobTypeId.toString()).get().addOnSuccessListener {
            if(it.exists()){
                val incomeModel = it.getValue(IncomeByJobTypeModel::class.java)
                if(incomeModel!= null) {
                    val newIncome = incomeModel.incomeAmount.toString().toDouble() + income.toDouble()
                    val update = hashMapOf<String, Any>(
                        "incomeAmount" to newIncome.toString()
                    )
                    databaseByJobId.child(uid).child(jobTypeId.toString()).updateChildren(update)
                }
            }else{
                val incomeModel = IncomeByJobTypeModel(jobTypeId.toString(), income)
                databaseByJobId.child(uid).child(jobTypeId.toString()).setValue(incomeModel)
            }
        }
    }
    
    fun fetchIncome(uid: String){
        database.child(uid).get().addOnSuccessListener { 
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

    fun fetchIncomeByJobTypeId(uid: String){
        databaseByJobId.child(uid).get().addOnSuccessListener {
            if(it.exists()){
                val incomeList: MutableList<IncomeByJobTypeModel> = mutableListOf()
                it.children.forEach { incomeSnapshot->
                    val incomeModel = incomeSnapshot.getValue(IncomeByJobTypeModel::class.java)

                    incomeModel?.let{
                        incomeList.add(incomeModel)
                    }
                }
                _incomeByJobTypeList.value = incomeList
            }
        }
    }

    fun pushExpenseToFirebaseByDate(uid:String, expense:String, date:String){

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

    fun pushExpenseToFirebaseJobTypeId(uid:String, expense:String, jobTypeId:Int){

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

    fun fetchExpense(uid: String){
        bDatabase.child(uid).get().addOnSuccessListener {
            if(it.exists()){
                val expenseList: MutableList<IncomeModel> = mutableListOf()
                it.children.forEach { expenseSnapshot->
                    val expenseModel = expenseSnapshot.getValue(IncomeModel::class.java)
                    expenseModel?.let{
                        expenseList.add(expenseModel)
                    }
                }
                _expenseList.value = expenseList
            }
        }
    }

    fun fetchExpenseByJobTypeId(uid: String){
        bDatabaseByJobId.child(uid).get().addOnSuccessListener {
            if(it.exists()){
                val expenseList: MutableList<IncomeByJobTypeModel> = mutableListOf()
                it.children.forEach { expenseSnapshot->
                    val expenseModel = expenseSnapshot.getValue(IncomeByJobTypeModel::class.java)

                    expenseModel?.let{
                        expenseList.add(expenseModel)
                    }
                }
                _expenseByJobTypeList.value = expenseList
            }
        }
    }

}