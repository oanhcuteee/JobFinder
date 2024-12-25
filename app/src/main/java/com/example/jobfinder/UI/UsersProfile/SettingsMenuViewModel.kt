package com.example.jobfinder.UI.UsersProfile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobfinder.UI.Login.RoomDB
import com.example.jobfinder.UI.Login.UsersDao
import com.example.jobfinder.UI.Login.UsersDataSavedModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsMenuViewModel (application: Application) : AndroidViewModel(application) {
    private val userDao: UsersDao = RoomDB.getDatabase(application).usersDao()

    fun getUserByEmail(email: String, callback: (UsersDataSavedModel?) -> Unit) {
        viewModelScope.launch {
            val user = userDao.getUserByEmail(email)
            withContext(Dispatchers.Main) {
                callback(user)
            }
        }
    }

    fun updateUser(user: UsersDataSavedModel) {
        viewModelScope.launch {
            userDao.updateUser(user)
        }
    }

    fun insertUser(user: UsersDataSavedModel) {
        viewModelScope.launch {
            userDao.insert(user)
            Log.d("insertUser", "User inserted to Database: ${user.email}")
        }
    }
}