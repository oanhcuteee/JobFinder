package com.example.jobfinder.UI.Notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jobfinder.Datas.Model.NotificationsRowModel
import com.example.jobfinder.Utils.GetData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class NotificationViewModel: ViewModel() {
    private val _notificationList = MutableLiveData<List<NotificationsRowModel>>()
    private val notiRef = FirebaseDatabase.getInstance().getReference("Notifications")
    val notificationList: LiveData<List<NotificationsRowModel>>
        get() = _notificationList

    fun fetchNotificationsFromFirebase() {
        val uid = GetData.getCurrentUserId()
        if(uid!= null) {
            FirebaseDatabase.getInstance()
                .getReference("Notifications")
                .child(uid.toString())
                .get()
                .addOnSuccessListener { dataSnapshot ->
                    val notificationList = mutableListOf<NotificationsRowModel>()
                    for (notiSnapshot in dataSnapshot.children) {
                        val notiId = notiSnapshot.child("notiId").getValue(String::class.java)
                        val from = notiSnapshot.child("from").getValue(String::class.java)
                        val detail = notiSnapshot.child("detail").getValue(String::class.java)
                        val date = notiSnapshot.child("date").getValue(String::class.java)

                        val notification = NotificationsRowModel(notiId, from, detail, date)
                        notificationList.add(notification)
                    }
                    notificationList.sortByDescending { GetData.convertStringToDate(it.date.toString()) }

                    // Cập nhật danh sách thông báo trong ViewModel
                    updateNotificationList(notificationList)
                }
        }
    }

    // Hàm cập nhật danh sách thông báo
    fun updateNotificationList(list: List<NotificationsRowModel>) {
        _notificationList.value = list
    }

    fun deleteSingleNoti(noti_id:String){
        val uid = GetData.getCurrentUserId()
        if(uid!= null){
            notiRef.child(uid).child(noti_id).removeValue()
        }
    }

    fun deleteAllNoti(){
        val uid = GetData.getCurrentUserId()
        if(uid!= null){
            notiRef.child(uid).removeValue()
        }
    }

    fun deleteNotiForUser(uid:String, noti_id: String){
        notiRef.child(uid).child(noti_id).removeValue()
    }

    fun addNotiForCurrUser(from:String, detai:String, date:String){
        val uid = GetData.getCurrentUserId()
        if(uid!= null){
            val noti_id = notiRef.child(uid).push().key
            val noti = NotificationsRowModel(noti_id,from, detai, date)
            notiRef.child(uid).child(noti.notiId.toString()).setValue(noti)
        }
    }

    fun addNotificationForUser(uid: String, from:String, detai:String, date:String){
        val noti_id = notiRef.child(uid).push().key
        val noti = NotificationsRowModel(noti_id,from, detai, date)
        notiRef.child(uid).child(noti.notiId.toString()).setValue(noti)
    }
}