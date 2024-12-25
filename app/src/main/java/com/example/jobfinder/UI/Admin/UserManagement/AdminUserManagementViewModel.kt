package com.example.jobfinder.UI.Admin.UserManagement

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jobfinder.Datas.Model.AdminModel.BasicInfoAndRole
import com.example.jobfinder.Datas.Model.UserBasicInfoModel
import com.google.firebase.database.FirebaseDatabase

class AdminUserManagementViewModel:ViewModel() {

    private val _userList = MutableLiveData<MutableList<BasicInfoAndRole>>()
    val userList: MutableLiveData<MutableList<BasicInfoAndRole>> get() = _userList

    private val database = FirebaseDatabase.getInstance().getReference("UserBasicInfo")
    private val roleDatabase = FirebaseDatabase.getInstance().getReference("UserRole")

    fun fetchUserList(){
        database.get().addOnSuccessListener { userInfoSnapshot ->
            if (userInfoSnapshot.exists()) {
                val userList: MutableList<BasicInfoAndRole> = mutableListOf()
                var pendingTasks = userInfoSnapshot.childrenCount.toInt() // Số lượng user cần xử lý
                userInfoSnapshot.children.forEach { userInfo ->
                    FirebaseDatabase.getInstance().getReference("UserRole")
                        .child(userInfo.key.toString()).get().addOnSuccessListener { roleSnapshot ->
                            if (roleSnapshot.exists()) {
                                val role = roleSnapshot.child("role").getValue(String::class.java)
                                val accStatus = roleSnapshot.child("accountStatus").getValue(String::class.java)
                                val userInfoModel = userInfo.getValue(UserBasicInfoModel::class.java)
                                userInfoModel?.let {
                                    val infoAndRole = BasicInfoAndRole(it, role.toString(), accStatus.toString())
                                    userList.add(infoAndRole)
                                }
                            }
                            pendingTasks--
                            if (pendingTasks == 0) {
                                _userList.value = userList
                            }
                        }.addOnFailureListener {
                            pendingTasks--
                            if (pendingTasks == 0) {
                                _userList.value = userList
                            }
                        }
                }
            } else {
                _userList.value = mutableListOf()
                Log.d("FetchUserList", "No user info found.")
            }
        }.addOnFailureListener {
            Log.e("FetchUserList", "Failed to fetch user info", it)
        }
    }

    fun updateAccountStatus(uid:String, disabled:Boolean){

        if(disabled){
            val update = hashMapOf<String, Any>(
                "accountStatus" to "active"
            )
            roleDatabase.child(uid).updateChildren(update)
        }else{
            val update = hashMapOf<String, Any>(
                "accountStatus" to "disable"
            )
            roleDatabase.child(uid).updateChildren(update)
        }


    }

}