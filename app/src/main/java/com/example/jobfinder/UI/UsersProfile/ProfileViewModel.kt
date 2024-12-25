package com.example.jobfinder.UI.UsersProfile

import android.net.Uri
import androidx.lifecycle.ViewModel

class ProfileViewModel:ViewModel() {
    var imageUri: Uri? = null
    var userid : String = ""
    var name : String = ""
    var email : String = ""
    var age : String = ""
    var address : String = ""
    var phone : String = ""
    var des : String = ""
    var busType : String = ""
    var busSec : String = ""
    var tax : String = ""
    var gender : String = ""

    fun updateImageUri(uri: Uri) {
        imageUri = uri
    }
}