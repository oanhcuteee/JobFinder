package com.example.jobfinder.UI.Admin.Statistical

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jobfinder.Datas.Model.AdminModel.RegisteredUserModel
import com.example.jobfinder.Datas.Model.AdminModel.UserLists
import com.example.jobfinder.Utils.GetData
import com.google.firebase.database.FirebaseDatabase

class AdminUserCountViewModel:ViewModel() {

    private val _registeredBUserList = MutableLiveData<MutableList<RegisteredUserModel>>()
    private val _registeredNUserList = MutableLiveData<MutableList<RegisteredUserModel>>()

    private val _registeredUserLists = MediatorLiveData<UserLists>().apply {
        addSource(_registeredBUserList) { updateLists() }
        addSource(_registeredNUserList) { updateLists() }
    }

    val registeredUserLists: LiveData<UserLists> get() = _registeredUserLists

    private fun updateLists() {
        val bList = _registeredBUserList.value ?: mutableListOf()
        val nList = _registeredNUserList.value ?: mutableListOf()
        _registeredUserLists.value = UserLists(bList, nList)
    }


    private val database = FirebaseDatabase.getInstance().getReference("AdminRef").child("RegisteredUser")


    fun pushRegisteredUserToFirebaseByDate(userType:String, number:String, date:String){

        val toFbDate = GetData.formatDateForFirebase(date)

        database.child(userType).child(toFbDate).get().addOnSuccessListener {
            if(it.exists()){
                val RegisteredUserModel = it.getValue(RegisteredUserModel::class.java)
                if(RegisteredUserModel!= null) {
                    val newIncome = RegisteredUserModel.amount.toString().toDouble() + number.toDouble()
                    val update = hashMapOf<String, Any>(
                        "amount" to newIncome.toString()
                    )
                    database.child(userType).child(toFbDate).updateChildren(update)
                }
            }else{
                val RegisteredUserModel = RegisteredUserModel(userType, date, number)
                database.child(userType).child(toFbDate).setValue(RegisteredUserModel)
            }
        }
    }

    fun fetchRegisteredBUser(){
        database.child("BUser").get().addOnSuccessListener {
            if(it.exists()){
                val registeredList: MutableList<RegisteredUserModel> = mutableListOf()
                it.children.forEach { registeredSnapshot->
                    val registeredModel = registeredSnapshot.getValue(RegisteredUserModel::class.java)
                    registeredModel?.let{
                        registeredList.add(registeredModel)
                    }
                }
                _registeredBUserList.value = registeredList
            }
        }
    }

    fun fetchRegisteredNUser(){
        database.child("NUser").get().addOnSuccessListener {
            if(it.exists()){
                val registeredList: MutableList<RegisteredUserModel> = mutableListOf()
                it.children.forEach { registeredSnapshot->
                    val registeredModel = registeredSnapshot.getValue(RegisteredUserModel::class.java)
                    registeredModel?.let{
                        registeredList.add(registeredModel)
                    }
                }
                _registeredNUserList.value = registeredList
            }
        }
    }
}